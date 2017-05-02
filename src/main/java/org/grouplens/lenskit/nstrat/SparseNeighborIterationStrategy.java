/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.knn.item.model;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;

import java.io.BufferedWriter;

/**
 * Neighbor iteration strategy that looks at the items co-rated with the specified item.  It may
 * return more than those items, however, if it looks like it might be faster to not filter.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SparseNeighborIterationStrategy implements NeighborIterationStrategy {
    private boolean onlyAfter;
    private BufferedWriter bufferedWriter;

    @Override
    public LongIterator neighborIterator(ItemItemBuildContext context, long item, boolean onlyAfter,
                                         Threshold threshold, BufferedWriter bufferedWriter) {
        this.onlyAfter = onlyAfter;
        this.bufferedWriter = bufferedWriter;
        long lowerBound = onlyAfter ? item : Long.MIN_VALUE;
        return new AdaptiveSparseItemIterator(context, context.itemVector(item).keySet(), lowerBound);
    }
    @Override
    public void recompute(Long itemId1, Long itemId2, SparseVector vec1, double sim){
        try {
            bufferedWriter.write(itemId1 + "," + itemId2 + "," + sim+"\n");
            if (onlyAfter) {
                bufferedWriter.write(itemId2 + "," + itemId1 + "," + sim+"\n");
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

}
