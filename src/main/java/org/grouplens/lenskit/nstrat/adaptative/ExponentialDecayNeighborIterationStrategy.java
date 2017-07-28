package org.grouplens.lenskit.nstrat.adaptative;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.nstrat.NeighborStrategy;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.util.*;

/**
 * Created by diogo on 28-07-2017.
 */
public class ExponentialDecayNeighborIterationStrategy implements NeighborIterationStrategy {

    private HashMap<Integer, Integer> pdf = null; //probability density function
    private int a = 0; //min value when neighbor is 1
    private int b = 0; //baseline value
    private double c = 0.0; //decay ratio
    private Integer[] entries_array_set;
    private Long[] items_array_set;

    @Override
    public LongIterator neighborIterator(NeighborStrategy strategy, long item){
        if (pdf == null) {
            //Do only once
            Set<Long> items_key_set = itemsVectorSize(strategy.buildContext).keySet();
            items_array_set = items_key_set.toArray(new Long[items_key_set.size()]);
            pdf = getHistogram(strategy);
            Set<Integer> entries_key_set = pdf.keySet();
            entries_array_set = entries_key_set.toArray(new Integer[entries_key_set.size()]);
            a = entries_array_set[entries_key_set.size()-1];
            b = strategy.number_neighbors;
            c = 1/(a/15.0);
            //a = pdf.get(1) - b;
            //c = 1/(a/2.5);
        }

        SparseVector vec = strategy.buildContext.itemVector(item);
        int n_entries = vec.size();
        double n_neighbors = a * Math.exp(-n_entries * c) + b;

        //Get position where items with same number of entries start in our list
        int start_position = 0;
        for(int i = 0; i < entries_array_set.length; i++){
            int entry = entries_array_set[i];
            start_position += pdf.get(entry);
            if(entry > n_entries){
                break;
            }
        }

        List<Long> items_list = new ArrayList();
        int left_i = start_position-1;
        for(int right_i = start_position; right_i < start_position + n_neighbors; right_i++){
            Long aux_item;
            if (right_i < items_array_set.length){
                aux_item = items_array_set[right_i];
                if(aux_item != item)
                    items_list.add(aux_item);
            }
            else{
                aux_item = items_array_set[left_i];
                if(aux_item != item)
                    items_list.add(aux_item);
                left_i--;
            }
        }

        LongList items = LongUtils.asLongList(items_list);
        strategy.iterator = items.iterator();
        return strategy.iterator;
    }

    @Override
    public void recompute(NeighborStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2Previous){

    }

    public HashMap<Integer, Integer> getHistogram(NeighborStrategy strategy){
        HashMap<Integer, Integer> pdf = new HashMap<>();
        LongSortedSet items = strategy.buildContext.getItems();
        LongIterator ite = items.iterator();
        while (ite.hasNext()){
            Long item = ite.nextLong();
            SparseVector vec = strategy.buildContext.itemVector(item);
            int n_entries = vec.size();
            if (pdf.containsKey(n_entries)){
                int value = pdf.get(n_entries);
                pdf.replace(n_entries, value+1);
            }
            else pdf.put(n_entries, 1);
        }
        return pdf;
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
