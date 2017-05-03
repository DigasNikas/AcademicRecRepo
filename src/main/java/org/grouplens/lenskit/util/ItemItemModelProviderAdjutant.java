package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.util.ScoredIdAccumulator;
import org.lenskit.util.TopNScoredIdAccumulator;
import org.lenskit.util.UnlimitedScoredIdAccumulator;

import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by diogonicolau on 03-05-2017.
 *
 * Containts methods to build objects from serialized
 *
 */
public class ItemItemModelProviderAdjutant {

    private static Long2ObjectMap<ScoredIdAccumulator> makeAccumulators(LongSet items,int modelSize) {
        Long2ObjectMap<ScoredIdAccumulator> rows = new Long2ObjectOpenHashMap<>(items.size());
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            ScoredIdAccumulator accum;
            if (modelSize == 0) {
                accum = new UnlimitedScoredIdAccumulator();
            } else {
                accum = new TopNScoredIdAccumulator(modelSize);
            }
            rows.put(item, accum);
        }
        return rows;
    }

    public static Long2ObjectMap<Long2DoubleMap> finishRows(Long2ObjectMap<Long2DoubleMap> results) {
        File toRead = null;
        try {
            toRead = new File("etc/rows.tmp");
            FileInputStream fis = new FileInputStream(toRead);

            ObjectInputStream input = new ObjectInputStream(fis);

            while (true) {
                Object obj = input.readObject();
                Long2ObjectMap.Entry<ScoredIdAccumulator> e = (Long2ObjectMap.Entry<ScoredIdAccumulator>) obj;
                results.put(e.getLongKey(), e.getValue().finishMap());
            }
        } catch (Exception e) {/* DO NOTHING JON SNOW */}
        toRead.delete();
        return results;
    }

    public static void rowsWriter(Long2ObjectMap<ScoredIdAccumulator> rows) {
        try {
            File fileTwo = new File("etc/rows.tmp");
            FileOutputStream fos = new FileOutputStream(fileTwo);
            ObjectOutputStream pos = new ObjectOutputStream(fos);
            for (Long2ObjectMap.Entry<ScoredIdAccumulator> e : rows.long2ObjectEntrySet()) {
                pos.writeObject(e);
            }
            pos.writeObject(null);
            pos.close();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static Long2ObjectMap<ScoredIdAccumulator> buildRows(LongSortedSet allItems, int i, int modelSize) {
        Long2ObjectMap<ScoredIdAccumulator> rows = makeAccumulators(allItems, modelSize);
        for (int k = 0; k < i; k++) {
            try {
                File toRead = new File("etc/similarities" + k + ".tmp");
                FileInputStream fis = new FileInputStream(toRead);

                Scanner sc = new Scanner(fis);

                String currentLine;
                while (sc.hasNextLine()) {
                    currentLine = sc.nextLine();
                    StringTokenizer st = new StringTokenizer(currentLine, ",", false);
                    rows.get(Long.valueOf(st.nextToken())).put(Long.valueOf(st.nextToken()), Double.valueOf(st.nextToken()));
                }

                fis.close();
                toRead.delete();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        return rows;
    }

}
