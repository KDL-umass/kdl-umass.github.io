---
layout: pub
title: Inferring network effects in relational data
topic: Causal Modeling
authors: D. Arbour, D. Garant, and D. Jensen
venue: 22nd ACM SIGKDD International Conference on Knowledge Discovery and Data Mining
year: 2016
pdfurl: http://www.kdd.org/kdd2016/papers/files/rfp0878-arbourA.pdf
permalink: 2016_inferring_network_effects_in_relational_data.html
abstract: We present Relational Covariate Adjustment (RCA), a general
method for estimating causal effects in relational data. Relational Covariate Adjustment is implemented through two high-level operations: identification of an adjustment set and relational regression adjustment. The former is achieved through an extension of Pearl’s back-door criterion to relational domains. We demonstrate how this extended definition can be used to estimate causal effects in the presence
of network interference and confounding. RCA is agnostic to functional form, and it can easily model both discrete and continuous treatments as well as estimate the effects of a wider array of network interventions than existing experimental approaches. We show that RCA can yield robust estimates of causal effects using common regression models without extensive parameter tuning. Through a series of simulation experiments on a variety of synthetic and realworld network structures, we show that causal effects estimated on observational data with RCA are nearly as accurate as those estimated from well-designed network experiments.
---

@inproceedings{Arbour:2016:INE:2939672.2939791,
 author = {Arbour, David and Garant, Dan and Jensen, David},
 title = {Inferring Network Effects from Observational Data},
 booktitle = {Proceedings of the 22Nd ACM SIGKDD International Conference on Knowledge Discovery and Data Mining},
 series = {KDD '16},
 year = {2016},
 isbn = {978-1-4503-4232-2},
 location = {San Francisco, California, USA},
 pages = {715--724},
 numpages = {10},
 url = {http://doi.acm.org/10.1145/2939672.2939791},
 doi = {10.1145/2939672.2939791},
 acmid = {2939791},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {causality, relational learning},
} 