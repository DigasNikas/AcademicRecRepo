package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.vectors.SparseVector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diogo on 27-06-2017.
 */
public class SingleListStrategy{

    protected List<Long> items_list = new ArrayList<>();
    protected LongList items;


    public void recompute(NeighborStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2Previous){
        while(true){
            if (strategy.number_neighbors + strategy.state >= items_list.size())
                return;
            long itemId2 = items_list.get(strategy.number_neighbors+strategy.state);
            strategy.state++;
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

}
