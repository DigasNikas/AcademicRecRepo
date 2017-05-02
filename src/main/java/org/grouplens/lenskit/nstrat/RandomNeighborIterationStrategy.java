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
    private BufferedWriter bufferedWriter;
    private List<Map.Entry<Long,Long>> used = new ArrayList<>();
    private ItemItemBuildContext buildContext;
    private Threshold threshold;
    private ItemSimilarity itemSimilarity;

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, ItemSimilarity itemSimilarity,
                                         Threshold threshold, BufferedWriter bufferedWriter) {
        this.bufferedWriter = bufferedWriter;
        this.buildContext = context;
        this.threshold = threshold;
        this.itemSimilarity = itemSimilarity;
        int number = 20;
        Random rnd = new Random();
        rnd.setSeed(item);
        LongSet items = LongUtils.randomSubset(context.getItems(),number, rnd);
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
    }

    @Override
    public void recompute(Long itemId1, Long itemId2, SparseVector vec1, double sim){
        Long iterationCount = 0L;
        while(true) {
            iterationCount++;
            itemId2 = generateNewRandom(itemId2+iterationCount);
            Map.Entry<Long,Long> pair = new java.util.AbstractMap.SimpleEntry<>(itemId1,itemId2);
            if (used.contains(pair))
                continue;
            SparseVector vec2 = buildContext.itemVector(itemId2);
            sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);

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
        LongSet item2 = LongUtils.randomSubset(buildContext.getItems(),1, rnd);
        return item2.iterator().nextLong();
    }


}
