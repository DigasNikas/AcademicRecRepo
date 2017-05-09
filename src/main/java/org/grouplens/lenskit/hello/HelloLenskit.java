/*
 * Copyright 2011 University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.grouplens.lenskit.hello;

import com.google.common.base.Throwables;
import org.grouplens.lenskit.util.ConfigReader;
import org.grouplens.lenskit.util.HeapMemoryPrinter;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.file.StaticDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demonstration app for LensKit. This application builds an item-item CF model
 * from a CSV file, then generates recommendations for a user.
 *
 */
public class HelloLenskit implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(HelloLenskit.class);
    private String mode;
    private Path dataFile;
    private HeapMemoryPrinter printer;
    private ConfigReader config_reader;

    /** used by logging to file code
     * global and accessibel by other files
     */

    public static void main(String[] args) {
        if (args.length == 0)
            System.err.println("Proper Usage is: lenskit-hello [Train or Query] [Config File]");
        if(args[0].equals("Train") || args[0].equals("Query")) {
            HelloLenskit hello = new HelloLenskit(args[0], args[1]);
            try {
                hello.run();
            } catch (RuntimeException e) {
                System.err.println(e.toString());
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        else System.err.println("Please select either Train or Query");
    }

    public HelloLenskit(String ModeInput, String ConfigInput) {
        this.mode = ModeInput;
        logger.info("reading config file");
        this.config_reader = new ConfigReader(ConfigInput);
        config_reader.readConfigFile();
        this.dataFile = Paths.get(config_reader.getDataFile());
        this.printer = new HeapMemoryPrinter(config_reader.getLogFile());
    }

    public void run() {
        printer.print(1);
        // We first need to configure the data access.
        // We will load data from a static data source; you could implement your own DAO
        // on top of a database of some kind
        DataAccessObject dao;
        try {
            StaticDataSource data = StaticDataSource.load(dataFile);
            // get the data from the DAO
            dao = data.get();
        } catch (IOException e) {
            logger.error("cannot load data", e);
            throw Throwables.propagate(e);
        }

        // If we select Train we load the configuration file
        // and train the model, followed by writing this one into disk.
        // If we select Test we load the previously trained model together
        // with a test input file, this file has items for querys and it's
        // results will be written in the test ouput file.

        if (mode.equals("Train")) {
            TrainRecommender train = new TrainRecommender(config_reader,dao,logger,printer);
            train.train();
        }
        else if (mode.equals("Query")){
            QueryRecommender query = new QueryRecommender(config_reader,dao,logger,printer);
            query.query();
        }
    }
}
