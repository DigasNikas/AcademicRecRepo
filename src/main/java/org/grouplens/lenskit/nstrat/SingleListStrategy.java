package org.grouplens.lenskit.nstrat;

import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by diogo on 27-06-2017.
 */
public class SingleListStrategy extends NeighborStrategy implements NeighborIterationStrategy{

    protected List<Long> items_list;

    public SingleListStrategy() {
    }

    public SingleListStrategy(ItemItemBuildContext context, ItemSimilarity itemSimilarity,
                              Threshold threshold, BufferedWriter bufferedWriter, int minCommonUsers) {
        super(context, itemSimilarity, threshold, bufferedWriter, minCommonUsers);
        this.items_list = new ArrayList<>();
    }

    @Override
    public void recompute(Long itemId1, SparseVector vec1, Long itemId2Previous){
        int state = 0;
        while(true){
            long itemId2 = items_list.get(number_neighbors+state);
            state++;
            SparseVector vec2 = buildContext.itemVector(itemId2);
            if(super.checkConditionFail(itemId1, vec1, itemId2, vec2))
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

}
