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
package org.grouplens.lenskit.nstrat.lenskit_native;

import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.nstrat.NeighborStrategy;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.model.NeighborIterationStrategy;

import java.io.BufferedWriter;

/**
 * Neighbor iteration strategy that considers all items to be candidate neighbors.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BasicNeighborIterationStrategy implements NeighborIterationStrategy {

    @Override
    public LongIterator neighborIterator(NeighborStrategy strategy, long item) {
        if (strategy.itemSimilarity.isSymmetric()) {
            return strategy.buildContext.getItems().iterator(item);
        } else {
            return strategy.buildContext.getItems().iterator();
        }
    }

    @Override
    public void recompute(NeighborStrategy strategy, Long itemId1, SparseVector vec1, Long itemId2Previous){

    }
}
