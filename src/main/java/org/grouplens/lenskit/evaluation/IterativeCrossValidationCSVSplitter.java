//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.grouplens.lenskit.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;
import net.recommenders.rival.split.splitter.CrossValidationSplitter;
import org.apache.commons.io.FileUtils;

public class IterativeCrossValidationCSVSplitter<U, I> extends CrossValidationSplitter<U, I> {
    private String outPath;

    public IterativeCrossValidationCSVSplitter(int nFold, boolean perUsers, long seed, String outPath) {
        super(nFold, perUsers, seed);
        this.outPath = outPath;
    }

    public DataModelIF<U, I>[] split(DataModelIF<U, I> data) {
        try {
            File e = new File(this.outPath);
            if(!e.exists()) {
                FileUtils.forceMkdir(e);
            }

            FileWriter[] splits_1 = new FileWriter[2 * this.nFolds];
            FileWriter[] splits_2 = new FileWriter[2 * this.nFolds];

            int i;
            for(i = 0; i < this.nFolds; ++i) {
                String n = this.outPath + "train_" + i + ".csv";
                String user = this.outPath + "test_" + i + ".csv";
                splits_1[2 * i] = new FileWriter(n);
                splits_1[2 * i + 1] = new FileWriter(user);
            }
            // Lenskit needs YML files to read (boring)
            for(i = 0; i < this.nFolds; ++i) {
                String yml_n = this.outPath + "train_" + i + ".yml";
                String yml_user = this.outPath + "test_" + i + ".yml";
                splits_2[2 * i] = new FileWriter(yml_n);
                splits_2[2 * i + 1] = new FileWriter(yml_user);
                splits_2[2 * i].write("ratings:\n"+" file: train_" + i + ".csv\n"+" format: csv\n"+" header: true\n"+" entity_type: rating");
                splits_2[2 * i + 1].write("ratings:\n"+" file: test_" + i + ".csv\n"+" format: csv\n"+" header: true\n"+" entity_type: rating");
                splits_2[2 * i].flush();
                splits_2[2 * i + 1].flush();
            }

            int curFold;
            if(this.perUser) {
                i = 0;
                Iterator var18 = data.getUsers().iterator();

                while(var18.hasNext()) {
                    Object var20 = var18.next();
                    ArrayList var21 = new ArrayList(((Map)data.getUserItemPreferences().get(var20)).keySet());
                    Collections.shuffle(var21, this.rnd);

                    for(Iterator var22 = var21.iterator(); var22.hasNext(); ++i) {
                        Object var23 = var22.next();
                        Double var24 = (Double)((Map)data.getUserItemPreferences().get(var20)).get(var23);
                        int var25 = i % this.nFolds;

                        for(curFold = 0; curFold < this.nFolds; ++curFold) {
                            FileWriter var26 = splits_1[2 * curFold];
                            if(curFold == var25) {
                                var26 = splits_1[2 * curFold + 1];
                            }

                            if(var26 != null) {
                                if(var24 != null) {
                                    var26.write(var20 + "," + var23 + "," + var24);
                                }

                                var26.write("\n");
                                var26.flush();
                            }
                        }
                    }
                }
            } else {
                ArrayList var16 = new ArrayList(data.getUsers());
                Collections.shuffle(var16, this.rnd);
                int var17 = 0;
                Iterator var19 = var16.iterator();

                while(var19.hasNext()) {
                    Object user1 = var19.next();
                    ArrayList items = new ArrayList(((Map)data.getUserItemPreferences().get(user1)).keySet());
                    Collections.shuffle(items, this.rnd);

                    for(Iterator item = items.iterator(); item.hasNext(); ++var17) {
                        Object item1 = item.next();
                        Double pref = (Double)((Map)data.getUserItemPreferences().get(user1)).get(item1);
                        curFold = var17 % this.nFolds;

                        for(int i1 = 0; i1 < this.nFolds; ++i1) {
                            FileWriter f_writer = splits_1[2 * i1];
                            if(i1 == curFold) {
                                f_writer = splits_1[2 * i1 + 1];
                            }

                            if(f_writer != null) {
                                if(pref != null) {
                                    f_writer.write(user1 + "," + item1 + "," + pref);
                                }

                                f_writer.write("\n");
                                f_writer.flush();
                            }
                        }
                    }
                }
            }

            for(i = 0; i < this.nFolds; ++i) {
                splits_1[2 * i].close();
                splits_1[2 * i + 1].close();
            }
        } catch (IOException var15) {
            var15.printStackTrace();
        }

        return null;
    }

    public TemporalDataModelIF<U, I>[] split(TemporalDataModelIF<U, I> data) {
        try {
            File e = new File(this.outPath);
            if(!e.exists()) {
                e.mkdir();
            }

            FileWriter[] splits = new FileWriter[2 * this.nFolds];

            int i;
            for(i = 0; i < this.nFolds; ++i) {
                String n = this.outPath + "train_" + i + ".csv";
                String user = this.outPath + "test_" + i + ".csv";
                splits[2 * i] = new FileWriter(n);
                splits[2 * i + 1] = new FileWriter(user);
            }

            int curFold;
            if(this.perUser) {
                i = 0;
                Iterator var21 = data.getUsers().iterator();

                while(var21.hasNext()) {
                    Object var23 = var21.next();
                    ArrayList var24 = new ArrayList(((Map)data.getUserItemPreferences().get(var23)).keySet());
                    Collections.shuffle(var24, this.rnd);

                    for(Iterator var25 = var24.iterator(); var25.hasNext(); ++i) {
                        Object var26 = var25.next();
                        Double var27 = (Double)((Map)data.getUserItemPreferences().get(var23)).get(var26);
                        Set var28 = null;
                        if(data.getUserItemTimestamps().containsKey(var23) && ((Map)data.getUserItemTimestamps().get(var23)).containsKey(var26)) {
                            var28 = (Set)((Map)data.getUserItemTimestamps().get(var23)).get(var26);
                        }

                        int var29 = i % this.nFolds;

                        for(curFold = 0; curFold < this.nFolds; ++curFold) {
                            FileWriter var30 = splits[2 * curFold];
                            if(curFold == var29) {
                                var30 = splits[2 * curFold + 1];
                            }

                            if(var30 != null) {
                                if(var27 != null) {
                                    var30.write(var23 + "," + var26 + "," + var27);
                                }

                                if(var28 != null) {
                                    Iterator var31 = var28.iterator();

                                    while(var31.hasNext()) {
                                        Long var32 = (Long)var31.next();
                                        var30.write("," + var32);
                                    }
                                }

                                var30.write("\n");
                                var30.flush();
                            }
                        }
                    }
                }
            } else {
                ArrayList var19 = new ArrayList(data.getUsers());
                Collections.shuffle(var19, this.rnd);
                int var20 = 0;
                Iterator var22 = var19.iterator();

                while(var22.hasNext()) {
                    Object user1 = var22.next();
                    ArrayList items = new ArrayList(((Map)data.getUserItemPreferences().get(user1)).keySet());
                    Collections.shuffle(items, this.rnd);

                    for(Iterator item = items.iterator(); item.hasNext(); ++var20) {
                        Object item1 = item.next();
                        Double pref = (Double)((Map)data.getUserItemPreferences().get(user1)).get(item1);
                        Set time = null;
                        if(data.getUserItemTimestamps().containsKey(user1) && ((Map)data.getUserItemTimestamps().get(user1)).containsKey(item1)) {
                            time = (Set)((Map)data.getUserItemTimestamps().get(user1)).get(item1);
                        }

                        curFold = var20 % this.nFolds;

                        for(int i1 = 0; i1 < this.nFolds; ++i1) {
                            FileWriter f_writer = splits[2 * i1];
                            if(i1 == curFold) {
                                f_writer = splits[2 * i1 + 1];
                            }

                            if(f_writer != null) {
                                if(pref != null) {
                                    f_writer.write(user1 + "," + item1 + "," + pref);
                                }

                                if(time != null) {
                                    Iterator t = time.iterator();

                                    while(t.hasNext()) {
                                        Long t1 = (Long)t.next();
                                        f_writer.write("," + t1);
                                    }
                                }

                                f_writer.write("\n");
                                f_writer.flush();
                            }
                        }
                    }
                }
            }

            for(i = 0; i < this.nFolds; ++i) {
                splits[2 * i].close();
                splits[2 * i + 1].close();
            }
        } catch (IOException var18) {
            var18.printStackTrace();
        }

        return null;
    }
}
