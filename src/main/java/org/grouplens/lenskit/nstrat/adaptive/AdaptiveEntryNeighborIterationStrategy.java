package org.grouplens.lenskit.nstrat.adaptive;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.nstrat.NeighborStrategy;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.io.*;
import java.util.*;

/**
 * Created by diogo on 11-07-2017.
 */
public class AdaptiveEntryNeighborIterationStrategy implements NeighborIterationStrategy{

    private int min_neig = 0; //baseline value
    private int sum = 0; //total number of entries from biggest item's neighbor
    private Long[] items_array_set = null;
    private BufferedWriter bufferedWriter;

    @Override
    public LongIterator neighborIterator(NeighborStrategy strategy, long item){
        if (items_array_set == null) {
            //Do only once
            try {
                File fileTwo = new File("teste");
                FileOutputStream fos = new FileOutputStream(fileTwo);
                PrintWriter pw = new PrintWriter(fos, true);
                bufferedWriter = new BufferedWriter(pw);
            }catch (Exception e){}
            Set<Long> items_key_set = itemsVectorSize(strategy.buildContext).keySet();
            items_array_set = items_key_set.toArray(new Long[items_key_set.size()]);
            min_neig = strategy.number_neighbors;
            for (int i = 1; i<=min_neig+1; i++){
                //Calculate the number of entries the most popular item uses in its neighborhood
                Long aux_item = items_array_set[items_key_set.size()-i];
                sum += strategy.buildContext.itemVector(aux_item).size();
            }
        }

        SparseVector vec = strategy.buildContext.itemVector(item);
        int n_entries = vec.size();

        int value = 0;
        int i = 0;
        int new_i = 0;
        List<Long> items_list = new ArrayList();
        while(value < sum) {
            Long aux_item;
            SparseVector vec_aux;
            int aux_entries;
            if (i < items_array_set.length) {
                aux_item = items_array_set[i];
                vec_aux = strategy.buildContext.itemVector(aux_item);
                aux_entries = vec_aux.size();
                if (aux_entries < n_entries){
                    i++;
                    continue;
                }
                else {
                    if(new_i == 0)
                        new_i = i-1;
                    value += aux_entries;
                    items_list.add(aux_item);
                }
                i++;
            }
            else{
                aux_item = items_array_set[new_i];
                vec_aux = strategy.buildContext.itemVector(aux_item);
                aux_entries = vec_aux.size();
                value += aux_entries;
                items_list.add(aux_item);
                new_i--;
            }
        }

        LongList items = LongUtils.asLongList(items_list);
        strategy.iterator = items.iterator();
        try {
            bufferedWriter.write(n_entries + "," + items.size() + "\n");
            bufferedWriter.flush();
        } catch (Exception e){}
        return strategy.iterator;
    }

    @Override
    public void recompute(NeighborStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2Previous){

    }

    private Map<Long,Integer> itemsVectorSize(ItemItemBuildContext context){
        LongIterator it = context.getItems().iterator();
        Map<Long,Integer> map = new HashMap<>();
        while(it.hasNext()){
            Long i = it.nextLong();
            SparseVector vec = context.itemVector(i);
            int count = vec.size();
            map.put(i,count);
        }
        map = sortByValue(map);

        return map;
    }

    private static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map ) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
