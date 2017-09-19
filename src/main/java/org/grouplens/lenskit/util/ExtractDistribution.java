package org.grouplens.lenskit.util;

import java.io.*;
import java.util.*;

import static org.grouplens.lenskit.util.ExtractPopularityHistogram.getHistogram;
import static org.grouplens.lenskit.util.ExtractPopularityHistogram.getPopByItem;
import static org.grouplens.lenskit.util.ExtractPopularityHistogram.writeCSV;

/**
 * Created by diogo on 05-08-2017.
 */
public class ExtractDistribution {

    public static void main(String args[]) throws IOException {
        String in_path = args[0];
        String out_path = args[1];
        HashMap<String, Integer> map = getPopByItem(in_path);
        HashMap<Integer, Integer> histogram = getHistogram(map);
        HashMap<Integer, Double> cumulative = getCumulutive(histogram);
        writeCSV(cumulative, out_path);
    }

    private static HashMap<Integer, Double> getCumulutive(HashMap<Integer, Integer> histogram){
        HashMap<Integer, Double> cumulutive = new HashMap<>();
        Set<Integer> keys_set = histogram.keySet();
        Collection<Integer> sorted = asSortedList(keys_set);
        Iterator<Integer> iter = sorted.iterator();
        double total = 1682;
        int sum = 0;
        while(iter.hasNext()){
            int key = iter.next();
            int val = histogram.get(key);
            sum += val;
            cumulutive.put(key, (sum/total) * 100);
        }
        return cumulutive;
    }

    private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list, Collections.reverseOrder());
        return list;
    }

}
