---
layout: post
title:  "Detecting Network Effects: Randomizing over Randomized Experiments"
date:   2017-10-05 15:27:02 -0400
categories: reading-group
---

## Paper: [Detecting Network Effects: Randomizing over Randomized Experiments](http://www.kdd.org/kdd2017/papers/view/detecting-network-effects-randomizing-over-randomized-experiments)

### Leader: [Kaleigh](https://people.cs.umass.edu/~kclary/)

[_Kaleigh_](https://people.cs.umass.edu/~kclary/):
Good intro to graph cluster randomization, some things missing (other, theory paper may have more information) - note that this has several different names, but is called “graph cluster randomization” in the Ugander(?) paper
Setup: running exps with clusters is expensive -- can you whether SUTVA holds and thus use the cheaper simple random assignment
Basic setup: have graph…

[Emma](https://cs.umass.edu/~etosch): clustering in batch or online?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): can do a sample; only thing they enforce is balanced size, can do this streaming the graph

[Kaleigh](https://people.cs.umass.edu/~kclary/): Had not seen “total treatment” effect as a term before -- appears to be the same as ATE

[Sam](https://people.cs.umass.edu/~switty/): this paper+graph cluster randomization in general, is the thing we’re trying to test uniform treatment on all individuals?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/) -- yes, trying to test global treatment and global control.<br/>
[Emma](https://cs.umass.edu/~etosch): what do you mean by global?<br/>
[Sam](https://people.cs.umass.edu/~switty/), [Dan](http://people.cs.umass.edu/~cork/): goal is to determine whether you apply the treatment/new setting universally -- as in, everyone gets the same setting later, rather than just some who then propagate effects through connectedness

[Kaleigh](https://people.cs.umass.edu/~kclary/): discussion of procedure for partitioning the space for the two randomization techniques. <br/>
[Emma](https://cs.umass.edu/~etosch), Sam, Dan: discussion about assignment procedure -- does it matter whether you partition-assignment per pass, or partition, then do passes in parallel (seems to not matter for simple random assignment; would matter for complete)<br/>

[Kaleigh](https://people.cs.umass.edu/~kclary/): graph randomization problems -- very specific problems (huge graph, associated assumptions) -- no discuss of the question: what is the threshold in graph size for these techniques to hold?<br/>

[Sam](https://people.cs.umass.edu/~switty/): are you assuming that graph structure is independent of features/covariates?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): not talked about -- big problem because people use things like county/country<br/> 
[Sam](https://people.cs.umass.edu/~switty/): when you randomize on clusters, do you break the relationship between network structure and covariates? (gives a sports example having to do with a latent covariate)<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): if your treatment is independent of the covariate used for clustering...that’s not the case -- for FB, there are layed communities<br/>
[Dan](http://people.cs.umass.edu/~cork/): this paper is asking if this particular clustering has values -- this paper would say that the clustering has no advantage, not whether it’s good<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): not…<br/>
[Dan](http://people.cs.umass.edu/~cork/): could use this…<br/>
[Sam](https://people.cs.umass.edu/~switty/): how much engineering goes into the design of these experiments?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): more discussion in Gui paper (another linkedin) -- introduces linear estimator...has nice properties -- aside from having partitions of the same size, don't are about structure or how you cut up the graph -- dean has work about variance and modularity of the graph -- question of how good your clustering is relative to … experiments...related to bias/variance tradeoff -- however, this mostly doesn’t get talked about, would prefer equally sized clusters and not care about modularity<br/>
[Emma](https://cs.umass.edu/~etosch): what do you mean by mod?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): how modular the graph structure is?<br/>
[Emma](https://cs.umass.edu/~etosch): is there a metric?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): there is a measure defined over a graph -- not used for the experimental design over graphs community -- number of connections over clusters IS modularity<br/>

[Kaleigh](https://people.cs.umass.edu/~kclary/): paper is about how useful graph cluster randomization is over “total random assignment” -- do both this comparison and a comparison across strata -- gets at different communities problem -- can deploy across pre-defined strata <br/>

[Sam](https://people.cs.umass.edu/~switty/): show graph where every node is in a cluster -- are there nodes that are excluded because they aren’t in a cluster enough-- is every entity assigned a cluster?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): yes<br/>
[Akansha](https://people.cs.umass.edu/~aatrey/): question about process<br/>

[Sam](https://people.cs.umass.edu/~switty/): what are they stratifying on?<br/>
[Akansha](https://people.cs.umass.edu/~aatrey/): units and connections?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): probably community size<br/>
[Amanda](https://people.cs.umass.edu/~agentzel/): incorporating stratification, section 2.4 -- discussion of how to construct strata<br/>

[Sam](https://people.cs.umass.edu/~switty/), E: discussion about stratification and global treatments and variance reduction<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): earlier use of global…<br/>
[Sam](https://people.cs.umass.edu/~switty/): figure 2 -- S+E continue discussion of global variance and whether it’s more or less when combining with stratification<br/>

[Kaleigh](https://people.cs.umass.edu/~kclary/): for a given clustering, how to make an experiment robust?<br/>

[Sam](https://people.cs.umass.edu/~switty/): apply T/C to a single cluster -- when you’re trying to get some stat that estimates global effect, are you only using nodes that are blocked by other nodes in the other cluster, or are you using nodes int eh periphery?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): original paper used ?-Thompson weighting -- only people in global treatment if e.g. 75% of friends treatments <br/>
[Sam](https://people.cs.umass.edu/~switty/): do these people excluded respond to treatment differently?<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): maybe you dont want to consider them because they are not actually representative -- but if there is some confounding variable that is influcing clustering but would also influence treatment, you are in trouble<br/>
[Sam](https://people.cs.umass.edu/~switty/): seems more common, reasonable thing to happen<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): alternative -- earlier linkedin estimator -- learn linear estimate hether you receive treatment or not and the proportion of your friends gives you estimate of both friends and your own effect -- can include everyone in your estimate<br/>
[Sam](https://people.cs.umass.edu/~switty/): could use that metric without graph cluster randomization<br/>
[Kaleigh](https://people.cs.umass.edu/~kclary/): yes, but the thing you care about is estimating global treatment or global control. Want to pile on samples from those two -- if you do total random assignment you have a more even distribution of friends who are treated. <br/>

[Sam](https://people.cs.umass.edu/~switty/): has anyone looked into trying to estimate local intervention<br/>
[Emma](https://cs.umass.edu/~etosch): like personalized? Discussion of eytan being interested in this...discussion about other companies and data...strava, dean’s work, snapchat person <br/>
