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

import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.nstrat.*;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.lenskit.util.ScoredIdAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.lenskit.inject.Transient;
import org.lenskit.knn.item.ItemSimilarity;
import org.lenskit.knn.item.ItemSimilarityThreshold;
import org.lenskit.knn.item.MinCommonUsers;
import org.lenskit.knn.item.ModelSize;
import org.lenskit.util.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.*;

import static org.grouplens.lenskit.util.ItemItemModelProviderAdjutant.buildRows;
import static org.grouplens.lenskit.util.ItemItemModelProviderAdjutant.finishRows;
import static org.grouplens.lenskit.util.ItemItemModelProviderAdjutant.rowsWriter;

/**
 * Build an item-item CF model from rating data.
 * This builder takes a very simple approach. It does not allow for vector
 * normalization and truncates on the fly.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@NotThreadSafe
public class ItemItemModelProvider implements Provider<ItemItemModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemModelProvider.class);

    private final ItemSimilarity itemSimilarity;
    private volatile static ItemItemBuildContext buildContext;
    private final Threshold threshold;
    private final NeighborIterationStrategy neighborStrategy;
    private final int minCommonUsers;
    private final int modelSize;
    private static volatile int items_done;
    private static volatile int nitems;

    @Inject
    public ItemItemModelProvider(@Transient ItemSimilarity similarity,
                                 @Transient ItemItemBuildContext context,
                                 @Transient @ItemSimilarityThreshold Threshold thresh,
                                 @Transient NeighborIterationStrategy nbrStrat,
                                 @MinCommonUsers int minCU,
                                 @ModelSize int size) {
        itemSimilarity = similarity;
        buildContext = context;
        threshold = thresh;
        neighborStrategy = nbrStrat;
        minCommonUsers = minCU;
        modelSize = size;
        items_done = 0;
        nitems = 0;
    }

    @Override
    public SimilarityMatrixModel get() {
        LongSortedSet allItems = buildContext.getItems();
        nitems = allItems.size();

        logger.info("building item-item model for {} items", nitems);
        logger.debug("using similarity function {}", itemSimilarity);
        logger.debug("similarity function is {}",
                itemSimilarity.isSparse() ? "sparse" : "non-sparse");
        logger.debug("similarity function is {}",
                itemSimilarity.isSymmetric() ? "symmetric" : "non-symmetric");

        ProgressLogger progress = ProgressLogger.create(logger)
                .setCount(nitems)
                .setLabel("item-item model build")
                .setWindow(50)
                .start();

        //int n_threads = Runtime.getRuntime().availableProcessors();
        int n_threads = 1;
        Thread Pool[] = new Thread[n_threads];

        int previous_items = 0;
        logger.info("Building {} SimilarityThreads", n_threads);
        for (int i = 0; i < n_threads; i++) {

            double items_by = nitems / ((double) n_threads);
            int items_by_thread = (int) items_by;

            if (i < n_threads - 1) {
                LongIterator outer = allItems.subSet(previous_items, previous_items + items_by_thread).iterator();
                Pool[i] = new Thread(new SimilarityThread(outer, i));
            } else {
                int k = (nitems - ((n_threads - 1) * items_by_thread));
                LongIterator outer = allItems.subSet(previous_items, previous_items + items_by_thread + k).iterator();
                Pool[i] = new Thread(new SimilarityThread(outer, i));
            }
            previous_items = previous_items + items_by_thread;
            Pool[i].start();

        }
        logger.info("Threads Running");
        Stopwatch timer;
        timer = Stopwatch.createStarted();
        try {
            for (int j = 0; j < n_threads; j++) {
                Pool[j].join();
            }
            for (int j = 0; j < n_threads; j++) {
                Pool[j] = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer.stop();
        logger.info("Thread computation done in {}", timer);

        progress.finish();

        //Get rid of builContext to save memory
        buildContext = null;

        logger.info("Building Object from similarities files");

        Stopwatch timerX;
        timerX = Stopwatch.createStarted();

        Long2ObjectMap<ScoredIdAccumulator> rows = buildRows(allItems, n_threads, modelSize);
        timerX.stop();
        logger.info("built object in {}", timerX);

        logger.info("Writing Object in rows.tmp");
        rowsWriter(rows);
        int size = rows.size();
        logger.info("{}", size);

        //Get rid of rows to save memory
        rows = null;
        Long2ObjectMap<Long2DoubleMap> rows2 = new Long2ObjectOpenHashMap<>(size);

        logger.info("Finishing SimilarityMatrixModel");
        return new SimilarityMatrixModel(finishRows(rows2));
    }

    class SimilarityThread extends Thread {
        private final LongIterator outer;
        private final int thread_index;
        public BufferedWriter bufferedWriter = null;

        public SimilarityThread(LongIterator Outer, int i) {
            outer = Outer;
            thread_index = i;
        }

        public void run() {

            try {
                File fileTwo = new File("etc/similarities" + thread_index + ".tmp");
                FileOutputStream fos = new FileOutputStream(fileTwo);
                PrintWriter pw = new PrintWriter(fos);
                bufferedWriter = new BufferedWriter(pw);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            Stopwatch timer;
            timer = Stopwatch.createStarted();
            int inside_items = 0;

            NeighborStrategy strategy = new NeighborStrategy(buildContext, itemSimilarity,
                    threshold, bufferedWriter, minCommonUsers);
            NeighborIterationStrategy n_strategy = new NeighborFactory().GetNeighborStrategy(6);

            while (outer.hasNext()) {
                final long itemId1 = outer.nextLong();
                SparseVector vec1 = buildContext.itemVector(itemId1);
                if (vec1.size() < minCommonUsers) {
                    // if it doesn't have enough users, it can't have enough common users
                    inside_items++;
                    continue;
                }

                LongIterator itemIter = strategy.neighborIterator(n_strategy, itemId1);

                while (itemIter.hasNext()) {
                    long itemId2 = itemIter.nextLong();
                    SparseVector vec2 = buildContext.itemVector(itemId2);
                    strategy.compute(n_strategy, itemId1, vec1, itemId2, vec2);
                }
                inside_items++;
            }
            try {
                timer.stop();
                FileWriter writer = new FileWriter("etc/BasketRun.log", true);
                BufferedWriter Writer = new BufferedWriter(writer);
                Writer.write("Thread "
                        + thread_index + " computed "
                        + inside_items + " out of "
                        + nitems + " in "
                        + timer + "\n");
                Writer.flush();
                bufferedWriter.flush();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }
}
