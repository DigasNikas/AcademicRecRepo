package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by diogo on 23-10-2016.
 */
public class RandomNeighborIterationStrategy extends NeighborStrategy implements NeighborIterationStrategy {

    public RandomNeighborIterationStrategy(){}

    public RandomNeighborIterationStrategy(ItemItemBuildContext context, ItemSimilarity itemSimilarity,
                                           Threshold threshold, BufferedWriter bufferedWriter, int minCommonUsers) {
        super(context, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
    }

    @Override
    public LongIterator neighborIterator(long item) {
        Random rnd = new Random();
        LongSet items = LongUtils.randomSubset(buildContext.getItems(), number_neighbors, rnd);
        return items.iterator();
    }

    @Override
    public void recompute(Long itemId1, SparseVector vec1, Long itemId2Previous){
        List<Map.Entry<Long,Long>> used = new ArrayList<>();
        Long iterationCount = 0L;
        while(true) {
            iterationCount++;
            long itemId2 = generateNewRandom(itemId2Previous+iterationCount);
            Map.Entry<Long,Long> pair = new java.util.AbstractMap.SimpleEntry<>(itemId1,itemId2);
            if (used.contains(pair)) {
                continue;
            }
            used.add(pair);
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

    private Long generateNewRandom(Long item){
        Random rnd = new Random();
        rnd.setSeed(item);
        LongSet item2 = LongUtils.randomSubset(buildContext.getItems(),1,rnd);
        return item2.iterator().nextLong();
    }

    private boolean checkConditionFail(Long itemId1, SparseVector vec1, Long itemId2, SparseVector vec2){
        // if items are the same or items have insufficient users in common, skip them
        return (itemId1 == itemId2 || !LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers));
    }
}
