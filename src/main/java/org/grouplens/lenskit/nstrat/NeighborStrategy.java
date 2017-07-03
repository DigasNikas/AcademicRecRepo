package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
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


/**
 * Created by diogo on 27-06-2017.
 */
public class NeighborStrategy {
    protected BufferedWriter bufferedWriter;
    public ItemItemBuildContext buildContext;
    protected Threshold threshold;
    public ItemSimilarity itemSimilarity;
    protected int minCommonUsers;
    public LongIterator iterator;
    protected int number_neighbors = 20;
    protected int state;
    protected List<Map.Entry<Long,Long>> used;

    public NeighborStrategy(){}

    public NeighborStrategy(ItemItemBuildContext context, ItemSimilarity itemSimilarity,
                            Threshold threshold, BufferedWriter bufferedWriter, int minCommonUsers){
        this.bufferedWriter = bufferedWriter;
        this.buildContext = context;
        this.threshold = threshold;
        this.itemSimilarity = itemSimilarity;
        this.minCommonUsers = minCommonUsers;
    }

    //public NeighborIterationStrategy get(){
        //return new LowestRatingItemNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        //return new HighestRatingItemNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        //return new LeastPopularItemNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        //return new MostPopularItemNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        //return new RandomNeighborIterationStrategy();
        /*if (similarity.isSparse()) {
            System.out.println("Sparse Strategy");
            return new SparseNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        } else {
            return new BasicNeighborIterationStrategy(buildContext, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        }*/
    //}

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


    protected boolean checkConditionFail(Long itemId1, SparseVector vec1, Long itemId2, SparseVector vec2){
        // if items are the same or items have insufficient users in common, skip them
        return (itemId1 == itemId2 || !LongUtils.hasNCommonItems(vec1.keySet(), vec2.keySet(), minCommonUsers));
    }

}
