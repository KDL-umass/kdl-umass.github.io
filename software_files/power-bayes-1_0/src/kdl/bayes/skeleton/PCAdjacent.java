/**
 * $
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
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.*;

public class PCAdjacent extends SkeletonFinder {

    protected static Logger log = Logger.getLogger(PCAdjacent.class);

    public ThresholdModule threshModule;

    public PCAdjacent(Instances instances) {
        this(instances, new ThresholdModule());
    }

    public PCAdjacent(Instances instances, ThresholdModule module) {
        super(instances);
        threshModule = module;
    }

    /**
     * This constructor is useful for using the MMPCTestHelper class for testing.
     *
     * @param instances
     * @param data
     */
    public PCAdjacent(Instances instances, BayesData data) {
        this(instances, new ThresholdModule());
        this.data = data;
        this.numVariables = data.numAttributes();
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
            log.info("Starting setSize = " + setSize);
            List<List<Integer>> pairsCopy = new ArrayList<List<Integer>>(pairs);
            for (List<Integer> pair : pairsCopy) {
                int xIdx = pair.get(0);
                int yIdx = pair.get(1);

                log.debug("TARGET: " + data.getName(xIdx));
                log.debug("OTHER: " + data.getName(yIdx));

                //List<Integer> adjacencies = new ArrayList<Integer>(neighbors.get(xIdx));
                List<Integer> adjacencies = getCandidates(xIdx, yIdx);

                //If we have already tried all possible tests, then stop looking.
                if (adjacencies.size() < setSize) {
                    log.debug("DECISION: No more tests to be run. Assuming dependence between " + data.getName(xIdx) + " and " + data.getName(yIdx) + "\n");
                    //already added all constraints.
                    pairs.remove(pair);
                    continue;
                }

                Set<Set<Integer>> pairZeroDOFsubsets = new HashSet<Set<Integer>>();
                if (zeroDofSubsets.containsKey(pair)) {
                    pairZeroDOFsubsets = zeroDofSubsets.get(pair);
                }

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

                    double estPower = 0.0;
                    try {
                        estPower = StatUtil.power(pValThreshold, instances.numInstances(), 0.1, data.computeTraditionalDegreesOfFreedom(xIdx, yIdx, zIdxs));
                    } catch (Exception e) {
                        log.warn("Found rare statistical power exception. Ignoring.");
                    }

                    //run indep test
                    if (!threshModule.enoughData(data)) {
                        log.debug("DID NOT RUN:" + "(" + data.getName(xIdx) + ";" + data.getName(yIdx) + "|" + data.getNames(zIdxs) + ")");
                        //addConstraint(xIdx, yIdx, subset, false, estPower); //true, means found indep. in data.
                        continue; //try the next set;
                    }
                    enoughData = true;
                    data.computeStats();

                    if (data.hasZeroDof()) {
                        log.debug("Found 0 DOF.");
                        if (zIdxs.length >= 0) {
                            pairZeroDOFsubsets.add(subset);
                        }
                        //Following TETRAD
                        //We are done with this pair
                        pairs.remove(pair);
                        break;
                        //continue;
                    }

                    score = data.assoc(pValThreshold);

                    if (Double.compare(score[0], 0.0) <= 0) {
                        log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx) + " using " + data.getNames(zIdxs) + "\n");
                        addConstraint(xIdx, yIdx, subset, true, estPower); //true, means found in data.

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
                    addConstraint(xIdx, yIdx, subset, false, 1 - pValThreshold); //true, means found indep. in data.

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

    protected List<Integer> getCandidates(int xIdx, int yIdx) {
        List<Integer> neighborList = new ArrayList<Integer>(neighbors.get(xIdx));
        log.debug("NEIGHBORS (" + neighborList.size() + ") : " + data.getNames(neighborList));
        List<Integer> adjPath = new ArrayList<Integer>(getAdjPath(xIdx, yIdx));
        log.debug("ADJ.PATH  (" + adjPath.size() + ") : " + data.getNames(adjPath));
        List<Integer> adjacencies = new ArrayList<Integer>();
        for (Integer neighbor : neighborList) {
            if (adjPath.contains(neighbor)) {
                adjacencies.add(neighbor);
            }
        }
        log.debug("CANDIDATES:  (" + adjacencies.size() + ") : " + data.getNames(adjacencies));
        Assert.condition(!adjacencies.contains(xIdx), "The neighbors of x should not contain x");
        //Assert.condition(adjacencies.remove(new Integer(yIdx)), "X should contain Y");

        if (adjacencies.size() <= neighborList.size() - 2) {
            log.debug("Found a smaller candidate set using only adjacencies.");
        }
        return adjacencies;
    }
}
