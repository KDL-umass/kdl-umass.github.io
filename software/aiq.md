---
permalink: aiq.html
layout: page
---

<h1> AIQ </h1>
<p> </p>

AIQ 1.0 (automated identification of quasi-experiments) is a proof-of-concept prototype system for automatically discovering quasi-experiments for causal inference. AIQ identifies possible quasi-experiments that can be performed on a specified data set with the aim of finding causal relationships.

Quasi-experiments exploit fortuitous circumstances in non-experimental data that enable causal inference, even in the absence of familiar control and randomization methods. AIQ automatically identifies appropriate experimental units and associated variables (possible causes and effects) and suggests quasi-experimental designs for evaluation and refinement by a human experimenter.

AIQ 1.0 is written in Prolog and runs in the SWI Prolog interpreter. The AIQ distribution includes

- Prolog source code
- Example data schema files
- README file containing instructions for defining new data schemas and using AIQ

**[Download AIQ 1.0 distribution](./software_files/aiq_1_0)**

AIQ is designed and implemented by the [Knowledge Discovery Laboratory](https://kdl.cs.umass.edu) in the [College of Information and Computer Sciences](https://www.cics.umass.edu/) at the [University of Massachusetts Amherst](https://umass.edu). See “Automatic Identification of Quasi-Experimental Designs for Discovering Causal Knowledge” (Jensen, et al., KDD 2008) for additional information on AIQ.
