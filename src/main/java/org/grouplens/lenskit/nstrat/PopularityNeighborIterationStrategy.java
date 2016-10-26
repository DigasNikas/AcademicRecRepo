package org.grouplens.lenskit.nstrat;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.NeighborIterationStrategy;

import java.io.BufferedWriter;

/**
 * Created by diogo on 26-10-2016.
 */
public class PopularityNeighborIterationStrategy implements NeighborIterationStrategy{

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, boolean onlyAfter) {
        return context.getItems().iterator();
    }
    @Override
    public void recompute(BufferedWriter bufferedWriter, Long itemId1, Long item, SparseVector vec1,
                          ItemItemBuildContext buildContext, ItemSimilarity itemSimilarity, Threshold threshold){
    }

}
