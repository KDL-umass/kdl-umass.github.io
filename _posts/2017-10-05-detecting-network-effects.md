---
layout: post
title:  "Detecting Network Effects: Randomizing over Randomized Experiments"
date:   2017-10-05 15:27:02 -0400
categories: reading-group
---

Paper: [Detecting Network Effects: Randomizing over Randomized Experiments](http://www.kdd.org/kdd2017/papers/view/detecting-network-effects-randomizing-over-randomized-experiments)
Leader: Kayleigh

Good intro to graph cluster randomization, some things missing (other, theory paper may have more information) - note that this has several different names, but is called “graph cluster randomization” in the Ugander(?) paper
Setup: running exps with clusters is expensive -- can you whether SUTVA holds and thus use the cheaper simple random assignment
Basic setup: have graph…

Emma: clustering in batch or online?
Kayleigh: can do a sample; only thing they enforce is balanced size, can do this streaming the graph

Kayleigh:
Had not seen “total treatment” effect as a term before -- appears to be the same as ATE

Sam: this paper+graph cluster randomization in general, is the thing we’re trying to test uniform treatment on all individuals?
Kayleigh -- yes, trying to test global treatment and global control.
Emma: what do you mean by global?
Sam, Dan: goal is to determine whether you apply the treatment/new setting universally -- as in, everyone gets the same setting later, rather than just some who then propagate effects through connectedness

Kayleigh: discussion of procedure for partitioning the space for the two randomization techniques. 
Emma, Sam, Dan: discussion about assignment procedure -- does it matter whether you partition-assignment per pass, or partition, then do passes in parallel (seems to not matter for simple random assignment; would matter for complete)

Kayleigh: graph randomization problems -- very specific problems (huge graph, associated assumptions) -- no discuss of the question: what is the threshold in graph size for these techniques to hold?

Sam: are you assuming that graph structure is independent of features/covariates?
Kayleigh: not talked about -- big problem because people use things like county/country 
Sam: when you randomize on clusters, do you break the relationship between network structure and covariates? (gives a sports example having to do with a latent covariate)
K: if your treatment is independent of the covariate used for clustering...that’s not the case -- for FB, there are layed communities
D: this paper is asking if this particular clustering has values -- this paper would say that the clustering has no advantage, not whether it’s good
K: not…
D: could use this…
S: how much engineering goes into the design of these experiments?
K: more discussion in Gui paper (another linkedin) -- introduces linear estimator...has nice properties -- aside from having partitions of the same size, don't are about structure or how you cut up the graph -- dean has work about variance and modularity of the graph -- question of how good your clustering is relative to … experiments...related to bias/variance tradeoff -- however, this mostly doesn’t get talked about, would prefer equally sized clusters and not care about modularity
E: what do you mean by mod?
K: how modular the graph structure is?
E: is there a metric?
K: there is a measure defined over a graph -- not used for the experimental design over graphs community -- number of connections over clusters IS modularity

K: paper is about how useful graph cluster randomization is over “total random assignment” -- do both this comparison and a comparison across strata -- gets at different communities problem -- can deploy across pre-defined strata 

S: show graph where every node is in a cluster -- are there nodes that are excluded because they aren’t in a cluster enough-- is every entity assigned a cluster?
K: yes
A: question about process

S: what are they stratifying on?
A: units and connections?
K: probably community size
A’: incorporating stratification, section 2.4 -- discussion of how to construct strata

S, E: discussion about stratification and global treatments and variance reduction
K: earlier use of global…
S: figure 2 -- S+E continue discussion of global variance and whether it’s more or less when combining with stratification

K: for a given clustering, how to make an experiment robust?

S: apply T/C to a single cluster -- when you’re trying to get some stat that estimates global effect, are you only using nodes that are blocked by other nodes in the other cluster, or are you using nodes int eh periphery?
K: original paper used ?-Thompson weighting -- only people in global treatment if e.g. 75% of friends treatments 
S: do these people excluded respond to treatment differently?
K: maybe you dont want to consider them because they are not actually representative -- but if there is some confounding variable that is influcing clustering but would also influence treatment, you are in trouble
S: seems more common, reasonable thing to happen
K: alternative -- earlier linkedin estimator -- learn linear estimate hether you receive treatment or not and the proportion of your friends gives you estimate of both friends and your own effect -- can include everyone in your estimate
S: could use that metric without graph cluster randomization
K: yes, but the thing you care about is estimating global treatment or global control. Want to pile on samples from those two -- if you do total random assignment you have a more even distribution of friends who are treated. 

S: has anyone looked into trying to estimate local intervention
E: like personalized? Discussion of eytan being interested in this...discussion about other companies and data...strava, dean’s work, snapchat person 
