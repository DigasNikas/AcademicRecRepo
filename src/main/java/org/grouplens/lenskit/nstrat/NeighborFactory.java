package org.grouplens.lenskit.nstrat;

import org.grouplens.lenskit.nstrat.adaptive.AdaptiveEntryNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.adaptive.ExponentialDecayNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.lenskit_native.SparseNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.random.RandomNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.single_list.HighestRatingItemNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.single_list.LeastPopularItemNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.single_list.LowestRatingItemNeighborIterationStrategy;
import org.grouplens.lenskit.nstrat.single_list.MostPopularItemNeighborIterationStrategy;
import org.lenskit.knn.item.model.NeighborIterationStrategy;

/**
 * Created by diogo on 31-07-2017.
 */
public class NeighborFactory {

    public NeighborFactory(){

    }

    public NeighborIterationStrategy GetNeighborStrategy(int strat) {
        switch (strat){
            case 1:
                System.out.println("Random Strategy\n");
                return new RandomNeighborIterationStrategy();
            case 2:
                System.out.println("Highest Rating Strategy\n");
                return new HighestRatingItemNeighborIterationStrategy();
            case 3:
                System.out.println("Lowest Rating Strategy\n");
                return new LowestRatingItemNeighborIterationStrategy();
            case 4:
                System.out.println("Least Popular Strategy\n");
                return new LeastPopularItemNeighborIterationStrategy();
            case 5:
                System.out.println("Most Popular Strategy\n");
                return new MostPopularItemNeighborIterationStrategy();
            case 6:
                System.out.println("Exponential Decay Strategy");
                return new ExponentialDecayNeighborIterationStrategy();
            case 7:
                System.out.println("Adaptive Entries Strategy");
                return new AdaptiveEntryNeighborIterationStrategy();
            case 8:
                System.out.println("Lenskit Sparse Strategy");
                return new SparseNeighborIterationStrategy();
            default:
                System.out.println("Caution NO STRATEGY configured!");
                return new SparseNeighborIterationStrategy();
        }
    }
}
