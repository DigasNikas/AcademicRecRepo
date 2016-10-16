package org.grouplens.lenskit.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by diogo on 16-10-2016.
 */
public class HeapMemoryPrinter {

    private String file;

    public HeapMemoryPrinter(String file){
        this.file = file;
    }

    public void print(int i){
        // =============== heap memory test ===============================
        int mb = 1024*1024;
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        FileWriter writer;
        BufferedWriter bufferedWriter;
        System.out.println("=============================================================");
        if (i == 1)
            System.out.println("##### Heap utilization statistics [MB] - run() started #####");
        if (i == 2)
            System.out.println("##### Heap utilization statistics [MB] - Train Engine Completed #####");
        if (i == 3)
            System.out.println("##### Heap utilization statistics [MB] - Test Completed #####");
        System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
        System.out.println("Free Memory:" + runtime.freeMemory() / mb);
        System.out.println("Total Available Memory:" + runtime.totalMemory() / mb);
        System.out.println("Max Available Memory:" + runtime.maxMemory() / mb);
        System.out.println("=============================================================");
        // ================================================================
        // =============== log to file ====================================
        try {
            writer = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write("=============================================================");
            bufferedWriter.newLine();
            if (i == 1)
                bufferedWriter.write("##### Heap utilization statistics [MB] - run() started #####");
            if (i == 2)
                bufferedWriter.write("##### Heap utilization statistics [MB] - Train Engine Completed #####");
            if (i == 3)
                bufferedWriter.write("##### Heap utilization statistics [MB] - Test Completed #####");
            bufferedWriter.newLine();
            bufferedWriter.write("Time: " + dateFormat.format(date));
            bufferedWriter.newLine();
            bufferedWriter.write("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
            bufferedWriter.newLine();
            bufferedWriter.write("Free Memory:" + runtime.freeMemory() / mb);
            bufferedWriter.newLine();
            bufferedWriter.write("Total Available Memory:" + runtime.totalMemory() / mb);
            bufferedWriter.newLine();
            bufferedWriter.write("Max Available Memory:" + runtime.maxMemory() / mb);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            if (i == 2 || i == 3)
                bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
