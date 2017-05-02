package org.grouplens.lenskit.nstrat;

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
public class LeastPopularItemNeighborIterationStrategy implements NeighborIterationStrategy{
    private boolean onlyAfter;
    private BufferedWriter bufferedWriter;

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, ItemSimilarity itemSimilarity,
                                         Threshold threshold, BufferedWriter bufferedWriter) {
        this.onlyAfter = itemSimilarity.isSymmetric();
        this.bufferedWriter = bufferedWriter;
        Set<Long> key_set = itemsVectorSize(context).keySet();
        List<Long> list = Arrays.asList(key_set.toArray(new Long[200]));
        LongList items = LongUtils.asLongList(list);
        return items.iterator();
    }
    @Override
    public void recompute(Long itemId1, Long itemId2, SparseVector vec1, double sim){
        try {
            bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim+"\n");
            if (onlyAfter) {
                bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim+"\n");
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
            System.exit(1);
        }
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
