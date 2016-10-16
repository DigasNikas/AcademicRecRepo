package org.grouplens.lenskit.hello;

import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by diogo on 16-10-2016.
 */
class QueryThread extends Thread {

    private final int index;
    private final int thread_num;
    private final int thread_index;
    private static volatile int AmountRecs;
    private static volatile ItemRecommender irec;
    private static volatile DataAccessObject dao;
    private static volatile List<String> users;

    public QueryThread(int Index, int Thread_num, int amountRecs,
                       List<String> users_list,
                       ItemRecommender Irec, DataAccessObject Dao, int i) {
        index = Index;
        thread_num = Thread_num;
        thread_index = i;
        AmountRecs = amountRecs;
        irec = Irec;
        dao = Dao;
        users = users_list;
    }

    public void run() {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fw = new FileWriter("etc/test_output" + thread_index + ".txt");
            bufferedWriter = new BufferedWriter(fw);
            for (int i = index; i < index + thread_num ; i++) {
                int user = Integer.valueOf(users.get(i));
                String to_append = "";
                ResultList recs = irec.recommendWithDetails(user, AmountRecs, null, null);

                to_append = to_append + "\"" + user + "\"" + ",";
                int k = 0;
                for (Result item : recs) {
                    k++;
                    Entity itemData = dao.lookupEntity(CommonTypes.ITEM, item.getId());
                    String name = null;
                    if (itemData != null) {
                        name = itemData.maybeGet(CommonAttributes.NAME);
                    }
                    to_append = to_append + "(\"" + name + "\"" + "," + String.valueOf(item.getScore()) + ")";
                    if (k < AmountRecs)
                        to_append = to_append + ",";
                }
                to_append = to_append + "\n";

                bufferedWriter.write(to_append);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }try {
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
