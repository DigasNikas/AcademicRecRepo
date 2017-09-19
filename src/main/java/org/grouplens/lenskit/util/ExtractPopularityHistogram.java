package org.grouplens.lenskit.util;

import java.io.*;
import java.util.*;

/**
 * Created by diogo on 02-08-2017.
 */
public class ExtractPopularityHistogram {

    public static void main(String args[]) throws IOException{
        String in_path = args[0];
        String out_path = args[1];
        HashMap<String, Integer> map = getPopByItem(in_path);
        HashMap<Integer, Integer> histogram = getHistogram(map);
        writeCSV(histogram, out_path);
    }

    public static HashMap<String, Integer> getPopByItem(String in_path) throws IOException {
        HashMap<String, Integer> map= new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(in_path));
        String line;

        while ((line = br.readLine()) != null) {
            try {
                String[] values = line.split(",");
                if (map.containsKey(values[1])) {
                    map.replace(values[1], map.get(values[1]) + 1);
                } else
                    map.put(values[1], 1);
            }catch (Exception e){
                continue;
            }
        }
        br.close();
        return map;
    }

    public static HashMap<Integer, Integer> getHistogram(HashMap<String, Integer> map){
        HashMap<Integer, Integer> histogram = new HashMap<>();
        Collection<Integer> values = map.values();
        for(int value: values){
            if(histogram.containsKey(value))
                histogram.replace(value, histogram.get(value)+1);
            else
                histogram.put(value, 1);
        }
        return histogram;
    }

    public static void writeCSV(HashMap<Integer, ?> map, String out_path) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(out_path));
        Set<Integer> key_set = map.keySet();
        for(Integer key: key_set){
            bw.write(key + "," + map.get(key)+ "\n");
        }
        bw.close();
    }

}
