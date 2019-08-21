# AcademicRecRepo
## Recommendation Engine for Master Thesis based on [Lenskit](https://java.lenskit.org/) .

This work pursues to study the impact of different neighborhood strategies in the formulation of recommendations through collaborative methods. Several popularity strategies are studied and implemented using the framework [Lenskit](https://java.lenskit.org/) . The aim is to show the usefulness of popularity as a significant indication in the creation of recommendations.

The different algorithm configuration files can be find at _**etc**_ directory, where we set every parameter required. You can read more about it [here](https://java.lenskit.org/documentation/basics/configuration/) .

### Changes to Lenskit

The first changes focused on parallelizing the way similarities are computed. Due to some issues related with the size of datasets, several stages of the job had to be serialized as well.

![alt text][arch]

[arch]: images/architecture.png "Architecture"

#### Sequence Diagram

![alt text][seq]

[seq]: images/sequence_uml.PNG "Sequence"

#### Strategies
##### Fair Entries

![alt text][fair]

[fair]: images/fair_code.png "Fair Entries"