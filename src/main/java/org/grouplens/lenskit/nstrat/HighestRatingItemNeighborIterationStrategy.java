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
 * Created by diogo on 06-11-2016.
 */
public class HighestRatingItemNeighborIterationStrategy extends NeighborStrategy implements NeighborIterationStrategy{

    private List<Long> items_list = new ArrayList<>();

    public HighestRatingItemNeighborIterationStrategy() {
    }

    public HighestRatingItemNeighborIterationStrategy(ItemItemBuildContext context, ItemSimilarity itemSimilarity,
                                                      Threshold threshold, BufferedWriter bufferedWriter, int minCommonUsers) {
        super(context, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
    }

    @Override
    public LongIterator neighborIterator(long item) {
        if (iterator == null) {
            Set<Long>key_set = itemsMeanRating(buildContext).keySet();
            items_list.addAll(key_set);
            Set<Long> subset = ImmutableSet.copyOf(Iterables.limit(key_set, number_neighbors));
            List<Long> list = new ArrayList<Long>(subset);
            LongList items = LongUtils.asLongList(list);
            iterator = items.iterator();
        }
        return iterator;
    }

    public void recompute(Long itemId1, SparseVector vec1, Long itemId2Previous){
        int state = 0;
        while(true){
            long itemId2 = items_list.get(number_neighbors+state);
            state++;
            SparseVector vec2 = buildContext.itemVector(itemId2);
            if(checkConditionFail(itemId1, vec1, itemId2, vec2))
                continue;

            double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
            if (threshold.retain(sim)) {
                try {
                    bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim+"\n");
                    if (itemSimilarity.isSymmetric()) {
                        bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim+"\n");
                    }
                } catch (Exception e) {
                    System.err.println(e.toString());
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
                return;
            }
        }
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

    private boolean checkConditionFail(Long itemId1, SparseVector vec1, Long itemId2, SparseVector vec2){
        // if items are the same or items have insufficient users in common, skip them
        return (itemId1 == itemId2 || !LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers));
    }

}
