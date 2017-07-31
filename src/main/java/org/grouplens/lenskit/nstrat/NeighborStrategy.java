package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;
import org.lenskit.util.collections.LongUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by diogo on 27-06-2017.
 */
public class NeighborStrategy {
    public BufferedWriter bufferedWriter;
    public ItemItemBuildContext buildContext;
    public Threshold threshold;
    public ItemSimilarity itemSimilarity;
    public int minCommonUsers;
    public LongIterator iterator;
    public int number_neighbors = 20;
    public int state;
    public List<Map.Entry<Long,Long>> used;

    public NeighborStrategy(){}

    public NeighborStrategy(ItemItemBuildContext context, ItemSimilarity itemSimilarity,
                            Threshold threshold, BufferedWriter bufferedWriter, int minCommonUsers){
        this.bufferedWriter = bufferedWriter;
        this.buildContext = context;
        this.threshold = threshold;
        this.itemSimilarity = itemSimilarity;
        this.minCommonUsers = minCommonUsers;
    }

    public LongIterator neighborIterator(NeighborIterationStrategy strategy, long item){
        state = 0;
        used = new ArrayList<>();
        iterator = strategy.neighborIterator(this, item);
        return iterator;
    }

    public void compute(NeighborIterationStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2, SparseVector vec2) {

        if (checkConditionFail(itemId1, vec1, itemId2, vec2))
            strategy.recompute(this, itemId1, vec1, itemId2);
        else {
            double sim = itemSimilarity.similarity(itemId1, vec1, itemId2, vec2);
            if (threshold.retain(sim)) {
                try {
                    bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim + "\n");
                    if (itemSimilarity.isSymmetric()) {
                        bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim + "\n");
                    }
                    bufferedWriter.flush();
                } catch (Exception e) {
                    System.err.println(e.toString());
                    e.printStackTrace(System.err);
                    System.exit(1);
                }
            } else strategy.recompute(this, itemId1, vec1, itemId2);
        }
    }


    public boolean checkConditionFail(Long itemId1, SparseVector vec1, Long itemId2, SparseVector vec2){
        // if items are the same or items have insufficient users in common, skip them
        return (itemId1 == itemId2 || !LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers));
    }

}
