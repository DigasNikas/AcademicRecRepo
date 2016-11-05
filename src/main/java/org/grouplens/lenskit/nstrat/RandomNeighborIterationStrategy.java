package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
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

    private List<Map.Entry<Long,Long>> used = new ArrayList<>();

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, boolean onlyAfter) {
        int number = 20;
        Random rnd = new Random();
        rnd.setSeed(item);
        LongSet items = LongUtils.randomSubset(context.getItems(),number, rnd);
        return items.iterator();
    }
    @Override
    public void recompute(BufferedWriter bufferedWriter, Long itemId1, Long item, SparseVector vec1,
                          ItemItemBuildContext buildContext, ItemSimilarity itemSimilarity, Threshold threshold, double empty){
        Long iterationCount = 0L;
        AGAIN:
        while(true) {
            iterationCount++;
            Long itemId2 = generateNewRandom(item+iterationCount,buildContext);
            Map.Entry<Long,Long> pair = new java.util.AbstractMap.SimpleEntry<>(itemId1,itemId2);
            if (used.contains(pair))
                continue AGAIN;
            SparseVector vec2 = buildContext.itemVector(itemId2);
            double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);

            if (threshold.retain(sim)) {
                if (itemSimilarity.isSymmetric()) {
                    try {
                        bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim+"\n");
                        used.add(pair);
                    } catch (Exception e) {
                        System.err.println(e.toString());
                        e.printStackTrace(System.err);
                        System.exit(1);
                    }
                }
                return;
            } else {
                continue AGAIN;
            }
        }
    }

    private Long generateNewRandom(Long item, ItemItemBuildContext context){
        Random rnd = new Random();
        rnd.setSeed(item);
        LongSet item2 = LongUtils.randomSubset(context.getItems(),1, rnd);
        return item2.iterator().nextLong();
    }


}
