package org.grouplens.lenskit.nstrat.random;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.nstrat.NeighborStrategy;
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
public class RandomNeighborIterationStrategy implements NeighborIterationStrategy {


    @Override
    public LongIterator neighborIterator(NeighborStrategy strategy, long item) {
        Random rnd = new Random();
        LongSet items = LongUtils.randomSubset(strategy.buildContext.getItems(), strategy.number_neighbors, rnd);
        return items.iterator();
    }

    @Override
    public void recompute(NeighborStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2Previous){
        Long iterationCount = 0L;
        while(true) {
            iterationCount++;
            long itemId2 = generateNewRandom(strategy, itemId2Previous+iterationCount);
            Map.Entry<Long,Long> pair = new java.util.AbstractMap.SimpleEntry<>(itemId1,itemId2);
            if (strategy.used.contains(pair)) {
                continue;
            }
            strategy.used.add(pair);
            SparseVector vec2 = strategy.buildContext.itemVector(itemId2);
            if(strategy.checkConditionFail(itemId1, vec1, itemId2, vec2))
                continue;

            double sim = strategy.itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
            if (strategy.threshold.retain(sim)) {
                try {
                    strategy.bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim+"\n");
                    if (strategy.itemSimilarity.isSymmetric()) {
                        strategy.bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim+"\n");
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

    private Long generateNewRandom(NeighborStrategy strategy, Long item){
        Random rnd = new Random();
        rnd.setSeed(item);
        LongSet item2 = LongUtils.randomSubset(strategy.buildContext.getItems(),1,rnd);
        return item2.iterator().nextLong();
    }

}
