//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.grouplens.lenskit.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import net.recommenders.rival.core.TemporalDataModelIF;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.grouplens.lenskit.scored.ScoredId;

public final class RecommenderCSVIO {
    private RecommenderCSVIO() {
    }

    public static <T> void writeData(long user, List<T> recommendations, String path, String fileName, boolean append, TemporalDataModelIF<Long, Long> model) {
        BufferedWriter out = null;

        try {
            try {
                File e = null;
                if(path != null) {
                    e = new File(path);
                    if(!e.isDirectory() && !e.mkdir() && fileName != null) {
                        System.out.println("Directory " + path + " could not be created");
                        return;
                    }
                }

                if(path != null && fileName != null) {
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + "/" + fileName, append), "UTF-8"));
                }

                Iterator e1 = recommendations.iterator();

                while(e1.hasNext()) {
                    Object ri = e1.next();
                    if(ri instanceof RecommendedItem) {
                        RecommendedItem recItem = (RecommendedItem)ri;
                        if(out != null) {
                            out.write(user + "," + recItem.getItemID() + "," + recItem.getValue() + "\n");
                        }

                        if(model != null) {
                            model.addPreference(Long.valueOf(user), Long.valueOf(recItem.getItemID()), Double.valueOf(1.0D * (double)recItem.getValue()));
                        }
                    }

                    if(ri instanceof ScoredId) {
                        ScoredId recItem1 = (ScoredId)ri;
                        if(out != null) {
                            out.write(user + "\t" + recItem1.getId() + "\t" + recItem1.getScore() + "\n");
                        }

                        if(model != null) {
                            model.addPreference(Long.valueOf(user), Long.valueOf(recItem1.getId()), Double.valueOf(recItem1.getScore()));
                        }
                    }
                }

                if(out != null) {
                    out.flush();
                    out.close();
                    return;
                }
            } catch (IOException var21) {
                System.out.println(var21.getMessage());
            }

        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException var20) {
                    var20.printStackTrace();
                }
            }

        }
    }
}
