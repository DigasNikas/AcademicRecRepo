package org.grouplens.lenskit.hello;

import org.lenskit.api.ItemBasedItemRecommender;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by diogo on 16-10-2016.
 */
class QueryItem2ItemThread extends Thread {

    private final int index;
    private final int thread_num;
    private final int thread_index;
    private static volatile int AmountRecs;
    private static volatile ItemBasedItemRecommender irec;
    private static volatile DataAccessObject dao;
    private static volatile List<Long> items;

    public QueryItem2ItemThread(int Index, int Thread_num, int amountRecs,
                                List<Long> users_list,
                                ItemBasedItemRecommender Irec, DataAccessObject Dao, int i) {
        index = Index;
        thread_num = Thread_num;
        thread_index = i;
        AmountRecs = amountRecs;
        irec = Irec;
        dao = Dao;
        items = users_list;
    }

    public void run() {
        BufferedWriter bufferedWriter = null;
        try {
            FileWriter fw = new FileWriter("etc/test_item2item_output" + thread_index + ".txt");
            bufferedWriter = new BufferedWriter(fw);
            for (int i = index; i < index + thread_num ; i++) {
                Long main_item = items.get(i);
                Set<Long> aux_set = new HashSet<>();
                aux_set.add(main_item);
                String to_append = "";
                ResultList recs = irec.recommendRelatedItemsWithDetails(aux_set, AmountRecs, null, null);

                to_append = to_append + "\"" + main_item + "\"" + ",";
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
