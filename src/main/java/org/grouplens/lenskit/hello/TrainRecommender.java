package org.grouplens.lenskit.hello;

import com.google.common.base.Stopwatch;
import org.grouplens.lenskit.util.ConfigReader;
import org.grouplens.lenskit.util.HeapMemoryPrinter;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.config.ConfigHelpers;
import org.lenskit.data.dao.DataAccessObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by diogo on 16-10-2016.
 */
public class TrainRecommender {

    private ConfigReader config_reader;
    private DataAccessObject dao;
    private Logger logger;
    private HeapMemoryPrinter printer;

    public TrainRecommender(ConfigReader config_reader, DataAccessObject dao, Logger logger, HeapMemoryPrinter printer){
        this.config_reader = config_reader;
        this.dao = dao;
        this.logger = logger;
        this.printer = printer;
    }

    public void train(){
        // Next: load the LensKit algorithm configuration
        LenskitConfiguration config;
        try {
            config = ConfigHelpers.load(new File(config_reader.getConfigFile()));
        } catch (IOException e) {
            throw new RuntimeException("could not load configuration", e);
        }

        // There are more parameters, roles, and components that can be set. See the
        // JavaDoc for each recommender algorithm for more information.

        // Now that we have a configuration, build a recommender engine from the configuration
        // and data source. This will compute the similarity matrix and return a recommender
        // engine that uses it.

        Stopwatch timer1 = Stopwatch.createStarted();
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config, dao);
        timer1.stop();
        logger.info("built recommender engine in {}", timer1);
        File output = new File(config_reader.getModelFile());
        CompressionMode comp = CompressionMode.autodetect(output);
        logger.info("writing model to {}", output);
        try (OutputStream raw = new FileOutputStream(output); OutputStream stream = comp.wrapOutput(raw)) {
            Stopwatch timer_writer = Stopwatch.createStarted();
            engine.write(stream);
            timer_writer.stop();
            logger.info("wrote model in {}", timer_writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        printer.print(2);
    }

}
