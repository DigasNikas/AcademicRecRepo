package org.grouplens.lenskit.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by diogo on 16-10-2016.
 */
public class ConfigReader {

    private List<String> config_file = new ArrayList<>();
    private String ConfigInput;

    // Class to read config file
    // This file must have:
    //  1 - Trained Model file (either for input and output);
    //  2 - Configuration file for training;
    //  3 - Data file (.yml required);
    //  4 - Test input file;
    //  5 - Test output file;
    //  6 - Log File;
    //  7 - Number of recommendations needed per item;
    //  8 - Number of Threads for testing;

    public ConfigReader(String config){
        this.ConfigInput = config;
    }

    public List<String> readConfigFile(){
        String line = "";
        FileReader aptoide_config = null;
        try {
            aptoide_config = new FileReader(this.ConfigInput);
        } catch (IOException e) {
            System.err.println("Please insert a valid Aptoide Config File");
            e.printStackTrace();
            System.exit(1);
        }
        try (BufferedReader br = new BufferedReader(aptoide_config)) {
            while ((line = br.readLine()) != null) {
                this.config_file.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return this.config_file;
    }

    public String getModelFile(){
        return this.config_file.get(0);
    }

    public String getConfigFile(){
        return this.config_file.get(1);
    }

    public String getDataFile(){
        return this.config_file.get(2);
    }

    public String getTestInputFile(){
        return this.config_file.get(3);
    }

    public String getTestOutputFile() { return this.config_file.get(4); }

    public String getLogFile(){
        return this.config_file.get(5);
    }

    public int getAmountRecs(){
        return Integer.valueOf(this.config_file.get(6));
    }

}
