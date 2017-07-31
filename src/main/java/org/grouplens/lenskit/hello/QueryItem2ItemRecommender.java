package org.grouplens.lenskit.hello;

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.util.ConfigReader;
import org.grouplens.lenskit.util.HeapMemoryPrinter;
import org.grouplens.lenskit.util.Merger;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.LenskitRecommenderEngineLoader;
import org.lenskit.api.ItemBasedItemRecommender;
import org.lenskit.api.ItemRecommender;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by diogo on 16-10-2016.
 */
public class QueryItem2ItemRecommender {

    private ConfigReader config_reader;
    private DataAccessObject dao;
    private Logger logger;
    private HeapMemoryPrinter printer;

    public QueryItem2ItemRecommender(ConfigReader config_reader,DataAccessObject dao, Logger logger, HeapMemoryPrinter printer){
        this.config_reader = config_reader;
        this.dao = dao;
        this.logger = logger;
        this.printer = printer;
    }

    public void query(){
        //List<String> users = converter();
        LongSet items_set = dao.getEntityIds(CommonTypes.ITEM);
        Long[] items_array = items_set.toArray(new Long[items_set.size()]);
        List<Long> items = Arrays.asList(items_array);
        File modelFile = new File(config_reader.getModelFile());
        Stopwatch timerX;
        LenskitRecommenderEngineLoader loader;
        Object input;
        LenskitRecommenderEngine engine;
        try {
            timerX = Stopwatch.createStarted();
            loader = LenskitRecommenderEngine.newLoader();
            input = new FileInputStream(modelFile);
            engine = loader.load((InputStream) input);
            logger.info("loading recommender from {}", modelFile);
            timerX.stop();
            logger.info("loaded recommender engine in {}", timerX);
            // Finally, get the recommender and use it.
            try (LenskitRecommender rec = engine.createRecommender(dao)) {
                logger.info("obtained recommender from engine");
                // we want to recommend items
                ItemBasedItemRecommender irec = rec.getItemBasedItemRecommender();
                assert irec != null; // not null because we configured one
                if (irec == null) {
                    logger.error("recommender has no global recommender");
                    throw new UnsupportedOperationException("no global recommender");
                }

                Stopwatch thread_timer = Stopwatch.createStarted();

                int n_threads = Runtime.getRuntime().availableProcessors();
                Thread Pool[] = new Thread[n_threads];
                int items_by_thread = items.size()/n_threads;

                logger.info("Building {} Threads", n_threads);
                for(int i = 0; i < n_threads; i++ ){
                    int thread_items = items_by_thread * i;
                    if(i == n_threads-1){
                        items_by_thread = items.size() - ((n_threads - 1) * items_by_thread) - 1;
                        Pool[i] = new Thread(new QueryItem2ItemThread(thread_items, items_by_thread,
                                config_reader.getAmountRecs(), items, irec, dao, i));
                    }
                    else {
                        Pool[i] = new Thread(new QueryItem2ItemThread(thread_items, items_by_thread,
                                config_reader.getAmountRecs(), items, irec, dao, i));
                    }
                    Pool[i].start();
                }
                logger.info("Threads Running");
                for (int j = 0; j < n_threads; j++) {
                    Pool[j].join();
                    Pool[j] = null;
                }
                thread_timer.stop();
                logger.info("recommended in {}", thread_timer);
                printer.print(3);
                Merger merger = new Merger(n_threads,config_reader.getTestOutputFile());
                merger.merge_output();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
