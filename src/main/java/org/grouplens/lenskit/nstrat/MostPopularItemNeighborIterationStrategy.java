package org.grouplens.lenskit.nstrat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.io.BufferedWriter;
import java.util.*;


/**
 * Created by diogo on 26-10-2016.
 */
public class MostPopularItemNeighborIterationStrategy extends NeighborStrategy implements NeighborIterationStrategy{

    @Override
    public LongIterator neighborIterator(long item) {
        if (iterator == null) {
            Set<Long> key_set = itemsVectorSize(buildContext).keySet();
            Set<Long> subset = ImmutableSet.copyOf(Iterables.limit(key_set, 200));
            List<Long> list = new ArrayList<Long>(subset);
            LongList items = LongUtils.asLongList(list);
            iterator = items.iterator();
        }
        return iterator;
    }

    public void recompute(Long itemId1, SparseVector vec1, Long itemId2Previous){

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

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map ) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
