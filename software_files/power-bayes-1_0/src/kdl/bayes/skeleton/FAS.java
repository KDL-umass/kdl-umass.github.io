/**
 * $Id: FAS.java 282 2009-08-18 02:42:20Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import kdl.bayes.skeleton.util.BayesData;
import kdl.bayes.skeleton.util.PowerSetIterator;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.Assert;
import kdl.bayes.util.StatUtil;
import kdl.bayes.util.Util;
import kdl.bayes.util.constraint.Dependence;
import weka.core.Instances;

import java.util.*;

/**
 * Implements Phase 1 of the PC algorithm to find the skeleon of a Bayesian network
 * See Causation, Prediction, and Search by Spirtes, Glymour and Scheines (2000) for more info.
 */
public class FAS extends SkeletonFinder {

    public ThresholdModule threshModule;

    public FAS(Instances instances) {
        this(instances, new ThresholdModule());
    }

    public FAS(Instances instances, ThresholdModule module) {
        super(instances);
        threshModule = module;
    }

    public FAS(FAS skeleton) {
        super(skeleton);
        threshModule = skeleton.threshModule;
    }

    /**
     * This constructor is useful for using the MMPCTestHelper class for testing.
     *
     * @param instances
     * @param data
     */

    public FAS(Instances instances, ThresholdModule module, BayesData data) {
        this(instances, module);
        this.data = data;
        this.numVariables = data.numAttributes();
    }

    public FAS(Instances instances, BayesData data) {
        this(instances, new ThresholdModule(), data);
    }

    public int computeNeighbors() {
        return computeNeighbors(Integer.MAX_VALUE);
    }

    /**
     * Do the actual search using Fast Adjacency Search. Iterate pairs in lexicographic order.
     *
     * @return the number of stat calls needed.
     */
    public int computeNeighbors(int maxSetSize) {

        neighbors = buildCompleteGraph();
        int setSize = 0; //size of current conditioning set

        List<List<Integer>> pairs = buildPairs(false);

        Map<List<Integer>, Set<Set<Integer>>> zeroDofSubsets = new HashMap<List<Integer>, Set<Set<Integer>>>();

        while (!pairs.isEmpty()) {
            log.info(setSize);
            List<List<Integer>> pairsCopy = new ArrayList<List<Integer>>(pairs);
            for (List<Integer> pair : pairsCopy) {
                int xIdx = pair.get(0);
                int yIdx = pair.get(1);

                //log.info("X (T): " + data.getName(xIdx));
                //log.info("Y (X): " + data.getName(yIdx));
                String xName = data.getName(xIdx);
                String yName = data.getName(yIdx);

                //log.info(setSize);
                List<Integer> adjacencies = new ArrayList<Integer>(neighbors.get(xIdx));
                //log.info(Arrays.deepToString(adjacencies.toArray()));
                //Original implementation used distinct *unordered* pairs. For correctness, this required adding
                // both sets to the adjacencies.
                //adjacencies.addAll(neighbors.get(yIdx));
                Assert.condition(!adjacencies.contains(xIdx), "The neighobrs of x should not contain x");

                if (!adjacencies.contains(yIdx)) {
                    log.debug("Found independence in the opposite direction. So remove");
                    pairs.remove(pair);
                    continue;
                }

                Assert.condition(adjacencies.remove((Integer) yIdx), "The neighobrs of x should contain y");

                //If we have already tried all possible tests, then stop looking.
                if (adjacencies.size() < setSize) {
                    log.debug("DECISION: No more tests to be run. Assuming dependence between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    //Add a dependence constraint only if the other direction has already been removed to avoid duplicates.
                    List<Integer> opp = new ArrayList<Integer>();
                    opp.add(yIdx);
                    opp.add(xIdx);

                    if (!pairs.contains(opp)) {
                        constraints.add(new Dependence(xIdx, yIdx, numVariables));
                    }
                    pairs.remove(pair);
                    continue;
                }

                Set<Set<Integer>> pairZeroDOFsubsets = new HashSet<Set<Integer>>();
                if (zeroDofSubsets.containsKey(pair)) {
                    pairZeroDOFsubsets = zeroDofSubsets.get(pair);
                }

                double minPower = 1.0;

                Iterator iter = new PowerSetIterator(adjacencies, setSize);
                boolean enoughData = false;
                double[] score = null;
                while (iter.hasNext()) {
                    Set<Integer> subset = (Set<Integer>) iter.next();


                    if (containsSubset(subset, pairZeroDOFsubsets)) {
                        log.debug("Contains 0 DOF subset.");
                        continue;
                    }

                    //Set data appropriately
                    int[] zIdxs = Util.convertSetToArray(subset);
                    data.setIndexes(yIdx, xIdx, zIdxs);
                    data.computeStats();
                    //log.info("TESTING:" + "(" + data.getName(xIdx) + ";" + data.getName(yIdx) + "|" + data.getNames(zIdxs) + ")");

                    double estPower = 1.0;
                    try {
                        estPower = StatUtil.power(pValThreshold, instances.numInstances(), 0.1, data.empiricalDof);
                    } catch (Exception e) {
                        log.warn("Found rare statistical power exception. Ignoring.");
                    }

                    if (estPower < minPower) {
                        minPower = estPower;
                    }
                    //run indep test
                    if (!threshModule.enoughData(data)) {
                        log.debug("ASSOC: netName=" + data.name + " gStat=-1" + " score=-1" + " pVal=0.0" + " tIdx=" + data.getName(xIdx) + " xIdx=" + data.getName(yIdx) + " zIdxs=" + (zIdxs.length > 0 ? data.getNames(zIdxs, ".") : "none") + " sizeZ=" + zIdxs.length + " edf=" + data.empiricalDof + " tdf=" + data.traditionalDof + " n=" + data.sampleSize + " v=-1" + " r=-1");
                        log.debug("DID NOT RUN:" + "(" + data.getName(xIdx) + ";" + data.getName(yIdx) + "|" + data.getNames(zIdxs) + ")" + "[" + data.traditionalDof + "]");
                        //Did not run test, should not rely on result, use power as weight (assumes small value).
                        //addConstraint(xIdx,yIdx,subset, false, estPower);
                        //Probably don't want to add these guys as they slow things down and provide minimal information
                        continue; //try the next set;
                    }
                    enoughData = true;

                    //data.computeStatsMatrix();

                    if (data.hasZeroDof()) {
                        log.debug("Found 0 DOF.");
                        if (zIdxs.length >= 0) {
                            pairZeroDOFsubsets.add(subset);
                        }
                        //Following TETRAD
                        //We are done with this pair
                        log.debug("Found 0 DOF between " + xName + " and " + yName + "|" + data.getNames(zIdxs));
                        log.debug("INDEPENDENCE ACCEPTED: " + xName + " _||_ " + yName + (zIdxs.length > 0 ? " | " + data.getNames(zIdxs, ", ") : "") + " p=1.0 g^2=0 df=0");
                        addConstraint(xIdx, yIdx, subset, true, estPower); //true, means found independence in data.
                        //Remove from the corresponding neighbor sets and update skeleton information.
                        neighbors.get(xIdx).remove(new Integer(yIdx));
                        neighbors.get(yIdx).remove(new Integer(xIdx));
                        //save zIdxs as sepset(xIdx,yIdx);
                        setSepset(xIdx, yIdx, subset);
                        setEffectSize(xIdx, yIdx, -1.0);
                        setEdgeStatus(xIdx, yIdx, -1);

                        pairs.remove(pair);
                        break;
                        //continue;
                    }

                    score = data.assoc(pValThreshold);
                    //log.info("POWER: algorithm=pc x=" + data.getName(xIdx) + "y=" + data.getName(yIdx)  +" n=" + instances.numInstances() + " score=" + score[0] + " pVal=" + score[5]);

                    if (Double.compare(score[0], 0.0) <= 0) {
                        log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx) + " using " + data.getNames(zIdxs));
                        //Ran test, concluded independence, use 1-Beta (power) as weight

                        log.debug("ind(" + xName.toLowerCase() + "," + yName.toLowerCase() + ", [" + data.getNames(zIdxs, ",").toLowerCase() + "]).");

                        addConstraint(xIdx, yIdx, subset, true, estPower); //true, means found independence in data.
                        //Remove from the corresponding neighbor sets and update skeleton information.
                        neighbors.get(xIdx).remove(new Integer(yIdx));
                        neighbors.get(yIdx).remove(new Integer(xIdx));
                        //save zIdxs as sepset(xIdx,yIdx);
                        setSepset(xIdx, yIdx, subset);
                        setEffectSize(xIdx, yIdx, score[2]);
                        setEdgeStatus(xIdx, yIdx, -1);

                        //We are done with this pair
                        pairs.remove(pair);
                        break;
                    }
                    //Ran test, did not conclude independence, use 1-alpha as confidence.
                    //addConstraint(xIdx, yIdx, subset, false, 1-pValThreshold); //false, means independence not found in data.
                    log.debug("dep(" + xName.toLowerCase() + "," + yName.toLowerCase() + ", [" + data.getNames(zIdxs, ",").toLowerCase() + "]).");

                }

                if (!enoughData) {
                    //We were unable to run any tests from this level
                    //If we haven't removed the pair then we will be unable
                    //to remove it at a higher set size. Therefore, assume
                    //dependence.
                    if (score != null) {
                        setEffectSize(xIdx, yIdx, score[2]);
                    } else {
                        setEffectSize(xIdx, yIdx, 0);
                    }
                    //Pair is dependent but only weakly so.
                    log.debug("DECISION: Assuming Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    List<Integer> opp = new ArrayList<Integer>();
                    opp.add(yIdx);
                    opp.add(xIdx);

                    if (!pairs.contains(opp)) {
                        constraints.add(new Dependence(xIdx, yIdx, numVariables, minPower));
                    }

                    setEdgeStatus(xIdx, yIdx, 0);
                    pairs.remove(pair);
                }

                if (pairs.contains(pair)) {
                    log.debug("DECISION: Not independant. Try next conditioning set size.\n");
                }

            }

            //Include a mechanism for breaking early.
            if (setSize >= maxSetSize) {
                break;
            }

            setSize++;
        }
        //neighbors should now contain the constraint-graph
        return data.getNumStatisticalCalls();
    }

}
