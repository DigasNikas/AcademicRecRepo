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
 * Created by diogo on 06-11-2016.
 */
public class HighestRatingItemNeighborIterationStrategy implements NeighborIterationStrategy{
    private BufferedWriter bufferedWriter;
    private ItemSimilarity itemSimilarity;
    private Threshold threshold;

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, ItemSimilarity itemSimilarity,
                                         Threshold threshold, BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
        this.itemSimilarity = itemSimilarity;
        this.threshold = threshold;
        Set<Long> key_set = itemsMeanRating(context).keySet();
        List<Long> list = Arrays.asList(key_set.toArray(new Long[200]));
        LongList items = LongUtils.asLongList(list);
        return items.iterator();
    }

    @Override
    public void compute(Long itemId1, Long itemId2, double sim) {
        try {
            bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim + "\n");
            if (itemSimilarity.isSymmetric()) {
                bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim + "\n");
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        // might be needed to recompute, if sim < threshold
    }

    private Map<Long,Double> itemsMeanRating(ItemItemBuildContext context){
        Map<Long,Double> map = new HashMap<>();
        for(LongIterator out_iterator = context.getItems().iterator(); out_iterator.hasNext(); ){
            Long i = out_iterator.nextLong();
            SparseVector vec = context.itemVector(i);
            double size = vec.size();
            double sum = 0;
            for(Iterator in_iterator = vec.values().iterator(); in_iterator.hasNext();){
                double k = (double) in_iterator.next();
                sum += k;
            }
            Double mean = sum / size;
            map.put(i,mean);
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
                return (o2.getValue()).compareTo(o1.getValue());  //Descending
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
