# AcademicRecRepo
Recommendation Engine for Master Thesis based on Lenskit.

This project is prepared to run a Item-Item Colaborative Filtering algorithm querying
by user. As part of the project different strategies have been implemented. These strategies follow the NeighborIterationStrategy interface and for now to select which strategy is active one should change the DefaultNeighborIterationStrategyProvider.java file.  

The algorithm configuration is at the basket.groovy file, where we set every parameter
required to run.
