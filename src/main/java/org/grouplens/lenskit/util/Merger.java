package org.grouplens.lenskit.util;

import java.io.*;

/**
 * Created by diogonicolau on 12-10-2016.
 */
public class Merger {

    private int numberOfProcessors;
    private String fileName;

    public Merger(int numberOfProcessors, String fileName){
        this.numberOfProcessors = numberOfProcessors;
        this.fileName = fileName;
    }

    public void merge() throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        // Merge thread files
        for (int j = 0; j < numberOfProcessors; j++) {
            File file = new File("etc/test_output"+j+".txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line+"\n");
            }
            br.close();
            file.delete();
        }
        bw.close();
    }
}
