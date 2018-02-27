---
layout: page
---

KDL has developed and released several open source software packages as part of our research efforts:

[**Relational Causal Discovery**](relational_causal_discovery.html)

Relational Causal Discovery (RCD) is a sound and complete algorithm for learning causal models from relational data.  RCD employs a novel rule, called relational bivariate orientation, that can can detect the orientation of a bivariate dependency with no assumptions on the underlying distribution.  Combined with relational extensions to the rules utilized by the PC algorithm, RCD is provably sound and complete under the causal sufficiency assumption.  Given a database and schema, RCD outputs a partially directed model that represents the equivalence class of statistically indistinguishable relational causal models.

[**Relational PC**](_software/relational_pc.md)

Relational PC (RPC) is an implementation of the PC machine learning algorithm designed for use with relational data. RPC goes beyond learning statistical associations to discover causal dependencies in relational data. Given a database and schema, RPC outputs a partially directed DAPER model that represents the equivalence class of statistically indistinguishable causal models. The algorithm retains the same essential strategies employed by PC for identifying causal structure, but includes several key innovations that enable learning in relational domains.

[**AIQ**](_software/aiq.md)

AIQ 1.0 (automated identification of quasi-experiments) is a proof-of-concept prototype system for automatically discovering quasi-experiments for causal inference. AIQ identifies possible quasi-experiments that can be performed on a specified data set with the aim of finding causal relationships.

[**PowerBayes**](_software/power_bayes.md)

PowerBayes 1.0 is a package for structure learning of Bayesian networks containing implementations of many common structure-learning algorithms and new algorithms using constraint satisfaction for learning models with improved structural accuracy.

[**Proximity**](_software/proximity.md)

Proximity is an open-source system for relational knowledge discovery, incorporating major research findings from the Knowledge Discovery Laboratory including model corrections for statistical biases inherent in relational data such as autocorrelation and degree disparity. Proximity implements QGraph, a visual query language designed to support knowledge discovery on large graph databases.
