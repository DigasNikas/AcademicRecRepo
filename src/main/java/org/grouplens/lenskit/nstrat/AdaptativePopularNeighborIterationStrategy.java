package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.util.*;

/**
 * Created by diogo on 11-07-2017.
 */
public class AdaptativePopularNeighborIterationStrategy implements NeighborIterationStrategy {

    private HashMap<Integer, Integer> pdf = null; //probability density function
    private int a = 0; //min value when neighbor is 1
    private int b = 0; //baseline value
    private double c = 0.0; //decay ratio
    private int sum = 0; //total number of entries from biggest item's neighbor
    private Integer[] number_entries_array_keyset;
    private Long[] items_array_set;

    @Override
    public LongIterator neighborIterator(NeighborStrategy strategy, long item){
        if (pdf == null) {
            //Do only once
            Set<Long> items_key_set = itemsVectorSize(strategy.buildContext).keySet();
            items_array_set = items_key_set.toArray(new Long[items_key_set.size()]);
            pdf = getHistogram(strategy);
            b = strategy.number_neighbors;
            //a = pdf.get(1) - b;
            //c = 1/(a/2.5);
            //a = arraykeyset[keyset.size()-1]
            //c = 1/(a/1500)
            Set<Integer> keyset = pdf.keySet();
            number_entries_array_keyset = keyset.toArray(new Integer[keyset.size()]);
            for (int i = 1; i<=b+1; i++){
                sum += number_entries_array_keyset[keyset.size()-i];
            }
        }

        SparseVector vec = strategy.buildContext.itemVector(item);
        int n_entries = vec.size();

        //For loop to find the range of entries and where to start/stop picking items
        int value = 0;
        int start_position = 0;
        int end_position = 0;
        for(int i = 0; i < number_entries_array_keyset.length; i++){
            int entry = number_entries_array_keyset[i];
            end_position += pdf.get(entry);
            if(entry < n_entries){
                start_position += pdf.get(entry);
                continue;
            }
            value += entry * pdf.get(entry);
            if (value >= sum){
                break;
            }
        }

        //For loop which actually collects the neighbor array
        List<Long> items_list = new ArrayList();
        for(int i = start_position; i != end_position; i++){
            //Check condition to avoid adding the item itself to his neighborhood
            if(items_array_set[i] == item) {
                if(start_position!=0){
                    //All items with just 1 entry screw the array access and wont miss the additional item
                    items_list.add(items_array_set[start_position - 1]);
                }
                continue;
            }
            items_list.add(items_array_set[i]);
        }

        //For loop to fill neighbor array if needed
        for(int i = 2; value < sum; i++){
            long new_item = items_array_set[start_position - i];
            items_list.add(new_item);
            SparseVector vec_aux = strategy.buildContext.itemVector(new_item);
            value += vec_aux.size();
        }

        //double n_neighbors = a * Math.exp(-n_entries * c) + b;

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
