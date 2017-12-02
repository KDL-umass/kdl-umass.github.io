---
layout: post
title: "Discovering Causal Signals in Images"
date: 2017-10-12
categories: reading-group
---
# Paper [Discovering Causal Signals in Images]
## Leader: [Javier](https://people.cs.umass.edu/~jburroni/)
Javier: 2 reasons
1) related to XAI stuff
2) kernal stuff reminds me of david arbour's stuff

interaction things are super interesting. conceptually this is the thing we want to do. good to think about. 
in particular, liked using intervention -- good, aligned with what we are doing
reasoning about non iid sitautions -- need to be robust to context
- extremely important for why causal inferencec matters: good statemetn for why AI mechanisms need to be causally explainable

DAvid: robustness is one of the reasons why we want explanation?
J: yes! we want causal explanation because of robustness
Dan: causal explanation...robustness...scenes
David: example: driving in nebraska, totally flat, at night -- object in the middle of the read -- took way to long to realise that he was looking at a mattress -- surprised because it's *out of context* -- why did it take me so long to realize? context -- an argument against a causal explanation?
K: connecting to the paper example
David: not just correlation -- roads are causal; car in water is not
Dan: roads as hints; not necessary, but helps
David: complicated argument -- actually roads are causal for cars; if you intervene and add a road you will get more cars
[cross talk]
Dan: necessary but not sufficient
Javier: huge causal impact (esp. in this country)
David: roads cause traffic; e.g. widening a road causes more cars

Javier: rest of paper...little disappointing; intro is really promising, usual stuff. rest of paper is an application of additive noise causal models...like ideas...last paragraph section 2.1 p4 -- tracking catalogue of causal footprint, use NN to learn causal footprint -- mean embedding kernal classifier -- different NN for learning and classification is cool -- results section -- concept that is common in additive noise causal model -- have anticausal relations -- cool that define context object features, super relevent to e.g. pedestrian detection, fire hydrant is to pedestrian as road is to car -- two alt. hypothesis about fire hydrant (composition of object features get its confused for pedestrians?) -- contrast two concepts with the causal and anticausal relations
Sam: all based on hypothesis that object features are related to causal deps, context features less so
Javier: yes -- e.g. a person has arms...fire hydrants are the place where a person is...

Javier: context/obejct stuff is interesting and worth following up on

Sam: can you speak to figure 5? 
Javier: for object score, it is *always* the case that anticausal relationship is...mean is higher than causal, while context is more mixed. but if you pay attention...relationships are not as...not looking at results (?)
Sam: only reason to look at these models is to show significance?
K,J: yes they are
Sam: Where?
K,J: NCC
K: do require the probability of causation or something like that...sums to one if you do the reverse (the inverse/codomain?)
David: are they using the additive noise model directly or as inspiration?
Javier: Yes, using additive noise model...
David: NN is how they produce the synthetic data...
Javier: ...

Sam: do they have explicitly labeled causal dependences?
David: with synthetic, yes
(discussion of using the synthetic data for learning the NN and then use the pre-trained network and additive model on real data)

Sam: i understand additive noise model sequence of argument -- problems with the model? something doesn't seem right -- at what point in their argument does the thing have problems? indepedence of features...noise? I have qualms with the nois. Independence between data source nad mechnanism seems fine to me. ASsumption that there is a relationship between teh cause and the noise seems arbitrary
David: those are not the things that bother me. Not sure about (1) the assumption that *either* X causes Y or Y causes X -- assumes that there is not spurious correlaitons
Sam: isnt this faithfulness?
David: no -- faithfulness says that causation leads to depednence. (1) causal sufficiency; (2) there might be confounders that we arent going to think about (3) there are colliders that we are not going to think about -- fundamental problems in causal inference not addressed -- (4) empirical evidence is not strong: small data set used for training and test, garden of forking paths -- when you use data ot make choices, youre overfitting. would be better if you divided the corpus a la pedro domingos' dissertation. that's the kind of testing im looking for. want something that goes beyond yes or no. yes/no is the weakest -- want strength of effect. we are not saying that if we intervene...not even a hypothesis of no causation
Javier: this is the thing they say
Sam:  not accounting for spurious corelation
David: no, not accounting for spurious causation
[discussion of what "spurious correlation" is/would be]
David: thinks they are saying, no correlation without causation
JAvier: spurious correlation is not correlation -- its randomness in data
Sam: definitely something to the idea of looking beyond independence effects
David: looking at the form of the error distribution; if we take a realtively simple bayes net and there are multiple hops between itnerveneing variables (and there are ALWAYS intervening variables, e.g. smoking and cancer) -- if you have any kind of complexity between treatment and outcome, the error distributions youre going to get ,even if you have simple functional forms, you will get crazy distibutions
Emma: comments about connections to checkcell and the related grant between emery and alexandra -- fundamental approach of looking at outputs vs. inputs and the partitioning of a potentially continuous space into discrete spaces.

Javier: [a bunch of interesting-sounding stuff that i missed out on]
David: can look at variance of causes vs outcomes to tell..
...
Javier: related to the a paper in pointers aout funding causing research or research causing funding, uses additive models
K: but it should be a feedback model!
David: additive noise approach, bivariate?? is the natural extension of an error made by causality researchers a long time ago -- like GES, would choose GES over PC because it takes into account strneght of dependence while taking account of structure, leaving out weak dependence
Sam: bayesian approach is appealing because you can specify more assumptions in a generative model than you can in independence tests...
Javier: GES does an independence test in a way
David: It has a threshold...it has a posterior
Javier: not only that...the score functions that are used, could use the same thing in PC
Sam: i thught the score function refers to the posterior over a graph structure
Javier: no, score function is a bayes thing to compute contingency tables.compute likelihood ration over a model that assumes indpeendnece. have beta binomanl distribution, assume model that has indpendence, still a beta binomial -- same thing you do when you have a score function like K2...the only thing they do is the prior they put on the beta
Sam: isnt a likelihood odds ratio fundamentally different from a hypothesis test?
J: of course! testing independnece, realted to chi squared test, which is a gamma, if you compute the ratio of chi squared, which is a sum of betas
David: very skeptical of the bivariate work, suprirsed it has gotten this far without skepiticism
Sam: making seemingly innocusou assumptions...been thinking about natural exps, making assumption that there are instpances that are generated from a distribution that differs from onormal ghenerative process is a reasonable one..could enable learning that you couldnt do without that assumption
David: identifying those instances uses information from outside that data...have to use external information ...and we believe those will generalize to the rest of the disribution...e.g. twin studies -- easy to idetnify sets of multiple siblings AND until the last 20 years we believed that twins were roughly distributed across the population (tech has changed this) -- there are good arguemtns for both assumptions you need -- instruments are another instance of this, influences some variable -- all identified from outside the data set. do we believe that assumption before we go in? to what extend can we do machine learning with natural experiments or a wider class of quasi-experiments? is there a way of envoding that informaiton such taht you can use it?

K: have thoughts about ANN, strength of aNN work is using the idea that we can leverage noise in a particular way --not "assuming a bivariate model..." too idealized, doesnt work in practice -- does work for instruments does work for downstream variables, those assumptions make two system identification not interesting. could probably ocmmon sense it out anyway. other placces where you can use this noise as part of a larger model...direction work from this lab -- that is interesting...where they apply it here is interesting but a little...hacky. 
Javier: a carefully chosen function...whihc means a hack
K: intro was fantastic, accessible, motivates the question well. great example for why XAI project. rest of the paper is well explained, but not reolutionary
David: definitely well written, not sure about the applicability of the ideas

Sam: made some artbitrary decisions about generive process behind the additive nise model. mixture of k gaussians...reasonable...hermite spline..whats to say that the funciton this represents is generalizable to images?
Javier: dont know
K: could still imagine that...a word embedding is not that difference from the kind of embedding they did.if you could define a function...from the original words or features, you could do it for nnlp also. that function from context to original data is a little harder to do, but i dont think its impossible. 
Javier: i missed something...
K: context here is pretty easy to identify, in terms of an image. the saem is true for words in an embedding -- an active area of research. screatch...?
David: bank is the thing that every one uses, different meanings disambigusated by context
K: im sure they do something similar where they use an embeddeing and use context around it
J: yes, that is the idea of projecting it into an infinite dimensional space, you can represent what you want
k: could generalize from images to words...
J: sam was asking soething different regarding synthetic data set
Sam: specifying a funciton between x and y tersm -- how do we know that that generalizes to causal dependences you see in images. am i missing something?
Javer: no; arbitrary is okay, so long as you say so
David: okay so long as you have strong empirical evience
Javier: i dont know what the pair x and y are. could be e.g. a person and an arm -- a feature, not a pixel. idea with this artbitary synthetic data set is just to keep the footprint of the causal relation. 
[unsatisfied sounds]

David: one of the things i was puzzled by...implementation decisions...obejct score and context score, subtracting out the general features from the object score. rather than using the context...a choice that i didnt understand why they made it, brouht up garden of forking paths proble...fiure five is all f the empirical ebidence...is there something surprising that theres something idfferent between object and context scores...look at example image in figure 4 object have boxes with content vs boxes without content
Javier: no statistical difference between the two is a bigger point
David: should not be making strong conclusions from figure five
Sam: could conclude that object score is generally higher than context score
David: could do a sign test
Sam: context score is a larger scale
David: agree with javier, confidence intervals are so huge they dont tell you anything

