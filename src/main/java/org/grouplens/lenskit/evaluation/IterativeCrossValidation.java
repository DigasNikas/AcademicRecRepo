/*
 * Copyright 2015 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.grouplens.lenskit.evaluation;

import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.DataModelUtils;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.evaluation.metric.ranking.NDCG;
import net.recommenders.rival.evaluation.metric.ranking.Precision;
import net.recommenders.rival.evaluation.strategy.EvaluationStrategy;
import net.recommenders.rival.recommend.frameworks.RecommenderIO;
import net.recommenders.rival.split.parser.MovielensParser;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.recommenders.rival.core.DataModelFactory;
import net.recommenders.rival.evaluation.metric.error.RMSE;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.hello.TrainRecommender;
import org.grouplens.lenskit.util.ConfigReader;
import org.grouplens.lenskit.util.HeapMemoryPrinter;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.LenskitRecommenderEngineLoader;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.lenskit.data.entities.CommonTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RiVal Movielens100k Mahout Example, using 5-fold  iterative cross-validation.
 *
 * 		Note: Adapted from CrossValidatedMahoutKNNRecommenderEvaluator the main difference is the usage of
 * 			IterativeCrossValidationSplitter instead of CrossValidationSplitter
 *
 * 		Using IterativeCrossValidationSplitter reduces the memory required to generate data folds.
 *
 * @author <a href="https://github.com/afcarvalho1991">André Carvalho</a>
 */
public final class IterativeCrossValidation
{

    /**
     * Default number of folds.
     */
    public static final int N_FOLDS = 5;
    /**
     * Default cutoff for evaluation metrics.
     */
    public static final int AT = 10;
    /**
     * Default relevance threshold.
     */
    public static final double REL_TH = 3.0;
    /**
     * Default seed.
     */
    public static final long SEED = 2048L;

    private static final Logger logger = LoggerFactory.getLogger(IterativeCrossValidation.class);

    /**
     * Utility classes should not have a public or default constructor.
     */
    private IterativeCrossValidation() {
    }

    /**
     * Main method. Parameter is not used.
     *
     * @param args the arguments (not used)
     */
    public static void main(final String[] args) {
        String modelPath = "data/ml-100k/model/";
        String recPath = "data/ml-100k/recommendations/";
        String dataFile = "data/myData/u.data";
        int nFolds = N_FOLDS;
        logger.info("reading config file");
        ConfigReader config_reader = new ConfigReader(args[0]);
        config_reader.readConfigFile();
        prepareSplits(nFolds, dataFile, modelPath);
        recommend(nFolds, modelPath, recPath, config_reader);
        // the strategy files are (currently) being ignored
        prepareStrategy(nFolds, modelPath, recPath, modelPath);
        evaluate(nFolds, modelPath, recPath);
    }

    /**
     * Downloads a dataset and stores the splits generated from it.
     *
     * @param nFolds number of folds
     * @param inFile file to be used once the dataset has been downloaded
     * @param outPath path where the splits will be stored
     */
    public static void prepareSplits(final int nFolds, final String inFile, final String outPath) {

        boolean perUser = true;
        long seed = SEED;
        Parser<Long, Long> parser = new MovielensParser();

        DataModelIF<Long, Long> data = null;
        try {
            data = parser.parseData(new File(inFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new IterativeCrossValidationCSVSplitter<Long,Long>(nFolds, perUser, seed, outPath).split(data);
        File dir = new File(outPath);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.println("Directory " + dir + " could not be created");
            }
        }

    }

    /**
     * Recommends using an UB algorithm.
     *
     * @param nFolds number of folds
     * @param inPath path where training and test models have been stored
     * @param outPath path where recommendation files will be stored
     */
    public static void recommend(final int nFolds, final String inPath, final String outPath, final ConfigReader config_reader) {
        for (int i = 0; i < nFolds; i++) {
            DataAccessObject dao_train;
            DataAccessObject dao_test;
            try {
                StaticDataSource train_data = StaticDataSource.load(Paths.get(inPath + "train_" + i + ".yml"));
                StaticDataSource test_data = StaticDataSource.load(Paths.get(inPath + "test_" + i + ".yml"));
                // get the data from the DAO
                dao_train = train_data.get();
                dao_test = test_data.get();
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }

            TrainRecommender recommender = new TrainRecommender(config_reader,dao_train,logger,
                    new HeapMemoryPrinter(config_reader.getLogFile()));
            recommender.train();

            String fileName = "recs_" + i + ".csv";

            LongSet users = dao_test.getEntityIds(CommonTypes.USER);

            try {
                LongIterator users_iterator = users.iterator();
                boolean createFile = true;
                File modelFile = new File(config_reader.getModelFile());
                logger.info("loading recommender from {}", modelFile);
                Object input = new FileInputStream(modelFile);

                LenskitRecommenderEngineLoader loader = LenskitRecommenderEngine.newLoader();
                LenskitRecommenderEngine engine = loader.load((InputStream) input);
                LenskitRecommender rec = engine.createRecommender(dao_test);
                ItemRecommender irec = rec.getItemRecommender();

                while (users_iterator.hasNext()) {
                    long u = users_iterator.nextLong();
                    assert recommender != null;

                    int size = dao_train.query(CommonTypes.ITEM).get().size();
                    ResultList recs = irec.recommendWithDetails(u, size, null, null);
                    List<RecommendedItem> items =  new ArrayList<>();
                    for (Result item : recs) {
                        Long item_id = item.getId();
                        Float item_score = (float) item.getScore();
                        RecommendedItem to_add = new GenericRecommendedItem(item_id, item_score);
                        items.add(to_add);
                    }
                    RecommenderCSVIO.writeData(u, items, outPath, fileName, !createFile, null);
                    createFile = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Prepares the strategies to be evaluated with the recommenders already
     * generated.
     *
     * @param nFolds number of folds
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     * @param outPath path where the filtered recommendations will be stored
     */
    @SuppressWarnings("unchecked")
    public static void prepareStrategy(final int nFolds, final String splitPath, final String recPath, final String outPath) {
        for (int i = 0; i < nFolds; i++) {
            File trainingFile = new File(splitPath + "train_" + i + ".csv");
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModelIF<Long, Long> trainingModel;
            DataModelIF<Long, Long> testModel;
            DataModelIF<Long, Long> recModel;
            try {
                trainingModel = new SimpleCSVParser().parseData(trainingFile);
                testModel = new SimpleCSVParser().parseData(testFile);
                recModel = new SimpleCSVParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Double threshold = REL_TH;
            String strategyClassName = "net.recommenders.rival.evaluation.strategy.UserTest";
            EvaluationStrategy<Long, Long> strategy = null;
            try {
                strategy = (EvaluationStrategy<Long, Long>) (Class.forName(strategyClassName)).getConstructor(DataModelIF.class, DataModelIF.class, double.class).
                        newInstance(trainingModel, testModel, threshold);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
                e.printStackTrace();
            }

            DataModelIF<Long, Long> modelToEval = DataModelFactory.getDefaultModel();
            for (Long user : recModel.getUsers()) {
                assert strategy != null;
                for (Long item : strategy.getCandidateItemsToRank(user)) {
                    if (recModel.getUserItemPreferences().get(user).containsKey(item)) {
                        modelToEval.addPreference(user, item, recModel.getUserItemPreferences().get(user).get(item));
                    }
                }
            }
            try {
                DataModelUtils.saveDataModel(modelToEval, outPath + "strategymodel_" + i + ".csv", true, "\t");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Evaluates the recommendations generated in previous steps.
     *
     * @param nFolds number of folds
     * @param splitPath path where splits have been stored
     * @param recPath path where recommendation files have been stored
     */
    public static void evaluate(final int nFolds, final String splitPath, final String recPath) {
        double ndcgRes = 0.0;
        double precisionRes = 0.0;
        double rmseRes = 0.0;
        for (int i = 0; i < nFolds; i++) {
            File testFile = new File(splitPath + "test_" + i + ".csv");
            File recFile = new File(recPath + "recs_" + i + ".csv");
            DataModelIF<Long, Long> testModel = null;
            DataModelIF<Long, Long> recModel = null;
            try {
                testModel = new SimpleCSVParser().parseData(testFile);
                recModel = new SimpleCSVParser().parseData(recFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NDCG<Long, Long> ndcg = new NDCG<>(recModel, testModel, new int[]{AT});
            ndcg.compute();
            ndcgRes += ndcg.getValueAt(AT);

            RMSE<Long, Long> rmse = new RMSE<>(recModel, testModel);
            rmse.compute();
            rmseRes += rmse.getValue();

            Precision<Long, Long> precision = new Precision<>(recModel, testModel, REL_TH, new int[]{AT});
            precision.compute();
            precisionRes += precision.getValueAt(AT);
        }
        System.out.println("NDCG@" + AT + ": " + ndcgRes / nFolds);
        System.out.println("RMSE: " + rmseRes / nFolds);
        System.out.println("P@" + AT + ": " + precisionRes / nFolds);

    }
}
