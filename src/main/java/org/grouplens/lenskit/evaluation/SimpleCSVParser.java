//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.grouplens.lenskit.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.Parser;
import net.recommenders.rival.core.TemporalDataModel;
import net.recommenders.rival.core.TemporalDataModelIF;

public class SimpleCSVParser implements Parser<Long, Long> {
    public static final int USER_TOK = 0;
    public static final int ITEM_TOK = 1;
    public static final int RATING_TOK = 2;
    public static final int TIME_TOK = 3;

    public SimpleCSVParser() {
    }

    public DataModelIF<Long, Long> parseData(File f) throws IOException {
        return this.parseData(f, ",", false);
    }

    public TemporalDataModelIF<Long, Long> parseTemporalData(File f) throws IOException {
        return this.parseData(f, ",", true);
    }

    public TemporalDataModelIF<Long, Long> parseData(File f, String token, boolean isTemporal) throws IOException {
        TemporalDataModel dataset = new TemporalDataModel();
        BufferedReader br = getBufferedReader(f);
        String line = br.readLine();
        if(line != null && !line.matches(".*[a-zA-Z].*")) {
            this.parseLine(line, dataset, token, isTemporal);
        }

        while((line = br.readLine()) != null) {
            this.parseLine(line, dataset, token, isTemporal);
        }

        br.close();
        return dataset;
    }

    public static BufferedReader getBufferedReader(File f) throws IOException {
        BufferedReader br = null;
        if(f != null && f.isFile()) {
            if(!f.getName().endsWith(".gz") && !f.getName().endsWith(".zip") && !f.getName().endsWith(".tgz")) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            } else {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), "UTF-8"));
            }

            return br;
        } else {
            return br;
        }
    }

    private void parseLine(String line, TemporalDataModelIF<Long, Long> dataset, String token, boolean isTemporal) {
        if(line != null) {
            String[] toks = line.split(token);
            long userId = Long.parseLong(toks[0]);
            long itemId = Long.parseLong(toks[1]);
            double preference = Double.parseDouble(toks[2]);
            long timestamp = -1L;
            if(isTemporal && toks.length > 3) {
                timestamp = Long.parseLong(toks[3]);
            }

            dataset.addPreference(Long.valueOf(userId), Long.valueOf(itemId), Double.valueOf(preference));
            if(timestamp != -1L) {
                dataset.addTimestamp(Long.valueOf(userId), Long.valueOf(itemId), Long.valueOf(timestamp));
            }

        }
    }
}
