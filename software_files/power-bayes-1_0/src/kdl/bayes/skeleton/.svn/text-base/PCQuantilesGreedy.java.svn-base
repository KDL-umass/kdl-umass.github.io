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
import kdl.bayes.util.Util;
import kdl.bayes.util.constraint.Dependence;
import org.apache.log4j.Logger;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

public class PCQuantilesGreedy extends SkeletonFinder {

    protected static Logger log = Logger.getLogger(PCQuantilesGreedy.class);


    public ThresholdModule threshModule;
    Attribute quantile;
    Attribute prob;

    Instances poolData;

    int numBins = 5;

    boolean doQuantile = true;
    boolean runChiSquare = false;

    double psSignificance = pValThreshold;

    public PCQuantilesGreedy(Instances trainData) {
        this(trainData, trainData, new ThresholdModule());
    }

    public PCQuantilesGreedy(Instances trainData, Instances poolData) {
        this(trainData, poolData, new ThresholdModule());
    }

    public PCQuantilesGreedy(Instances instances, Instances poolInstances, ThresholdModule module) {
        super(instances);
        threshModule = module;

        poolData = poolInstances;

        FastVector values = new FastVector(numBins);

        for (int i = 0; i < numBins; i++) {
            int value = 65 + i;
            values.addElement(new String(new char[]{(char) value}));
        }

        quantile = new Attribute("quantile", values);
        prob = new Attribute("prob");
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
        List<List<Integer>> pairs = buildPairs(true);
        int statCalls = 0;

        Map<List<Integer>, Set<Set<Integer>>> zeroDofSubsets = new HashMap<List<Integer>, Set<Set<Integer>>>();

        //log.info(Arrays.deepToString(pairs.toArray()));

        int setSize = 0; //size of current conditioning set

        while (!pairs.isEmpty()) {
            log.info(setSize);
            List<List<Integer>> pairsCopy = new ArrayList<List<Integer>>(pairs);


            //Consider every pair once for each setSize.
            for (List<Integer> pair : pairsCopy) {
                int xIdx = pair.get(0);  // Treatment
                int yIdx = pair.get(1);  // Outcome

                int lcv = getLeastCommonValue(instances, xIdx);

                //log.info("X (T): " + data.getName(xIdx));
                //log.info("Y (X): " + data.getName(yIdx));
                String xName = data.getName(xIdx);
                String yName = data.getName(yIdx);

                Instances rfInstances = new Instances(instances);
                instances.setClassIndex(xIdx);
                poolData.setClassIndex(xIdx);

                rfInstances.setClassIndex(xIdx);   //Should always work since X < Y
                rfInstances.deleteAttributeAt(yIdx);

                RandomForest rf = new RandomForest();
//            rf.setNumFeatures(1);
//            rf.setNumTrees(1);

                //NaiveBayesSimple rf = new NaiveBayesSimple();
                //Logistic rf = new Logistic();

                FastVector attributes = new FastVector(instances.numAttributes() + 1);

                for (int i = 0; i < instances.numAttributes(); i++) {
                    attributes.addElement(instances.attribute(i));
                }
                attributes.addElement(prob);
                attributes.addElement(quantile);

                Instances qInstances = new Instances("quantile", attributes, instances.numInstances());

                try {
                    rf.buildClassifier(rfInstances);

                    for (int i = 0; i < rfInstances.numInstances(); i++) {
                        Instance instance = rfInstances.instance(i);
                        if ((int) instance.value(xIdx) == lcv) {

                            double[] probMap = rf.distributionForInstance(instance);
                            double prob = probMap[lcv];

                            Instance qInstance = new Instance(2);
                            Instance trainInstance = instances.instance(i);

                            qInstance = trainInstance.mergeInstance(qInstance);
                            qInstance.setDataset(qInstances);

                            qInstance.setValue(qInstances.numAttributes() - 2, prob);

                            qInstances.add(qInstance);
                        }
                    }

                    //Sort by prob then break into equal chunks.
                    int binSize = (qInstances.numInstances() / numBins) + 1;
                    qInstances.sort(qInstances.numAttributes() - 2);
                    for (int i = 0; i < qInstances.numInstances(); i++) {
                        Instance qInstance = qInstances.instance(i);
                        //log.info(qInstance.value(qInstances.numAttributes()-2) + " " + i/binSize);
                        qInstance.setValue(qInstances.numAttributes() - 1, i / binSize);
                    }

                    //Now, find near matches in terms of propensity score and add matches to qInstances.
                    Set<Integer> matchedList = new HashSet<Integer>();
                    int numTreatment = qInstances.numInstances();
                    log.debug("Num treated: " + numTreatment);

                    Instances rfPoolInstances = new Instances(poolData);
                    rfPoolInstances.setClassIndex(xIdx);
                    rfPoolInstances.deleteAttributeAt(yIdx);

                    for (int i = 0; i < numTreatment; i++) {
                        //log.info(i);
                        Instance treatmentInstance = qInstances.instance(i);
                        double prob = treatmentInstance.value(qInstances.numAttributes() - 2);
                        int bin = (int) treatmentInstance.value(qInstances.numAttributes() - 1);

                        double bestDiff = 5.0;
                        double bestProb = prob;
                        int bestInstance = -1;

                        for (int j = 0; j < poolData.numInstances(); j++) {
                            Instance poolInstance = rfPoolInstances.instance(j);

                            //Need instances that did not receive treatment.
                            //So if instance did receive treatment then continue
                            if ((int) poolInstance.value(xIdx) == lcv) {
                                continue;
                            }

                            double[] probMap = rf.distributionForInstance(poolInstance);
                            double matchProb = probMap[lcv];

                            double diff = Math.abs(prob - matchProb);

                            if (Double.compare(diff, bestDiff) < 0 && !matchedList.contains(j)) {
                                bestDiff = diff;
                                bestInstance = j;
                                bestProb = matchProb;
                            }
                        }

                        if (bestInstance < 0) {
                            //log.info("Didn't find a match.");
                            continue;
                        }

                        matchedList.add(bestInstance);

                        Instance binInstance = new Instance(2);

                        Instance copyMatch = poolData.instance(bestInstance).mergeInstance(binInstance);


                        copyMatch.setValue(qInstances.numAttributes() - 2, bestProb);
                        copyMatch.setValue(qInstances.numAttributes() - 1, bin);

                        //log.info(copyMatch.numAttributes());
                        //log.info(copyMatch);

                        copyMatch.setDataset(qInstances);
                        qInstances.add(copyMatch);

                    }

                    //log.info(qInstances.numInstances() - numTreatment);

                    //for(int i = numTreatment; i< qInstances.numInstances() ; i++){
                    //    log.info(qInstances.instance(i).toString());
                    //}


                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                ////////////////////////////////
                // After computing PS for each pair, find the conditioning variables and run the conditioning.
                ////////////////////////////////

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

                double minPower = 1.0;

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


                    BayesData qData = new BayesData(qInstances);
                    int[] zIdxs;
                    if (doQuantile) {

                        int sizeZ = subset.size() + 1;
                        zIdxs = new int[sizeZ];

                        zIdxs[0] = qInstances.numAttributes() - 1;
                        int i = 1;
                        for (int z : subset) {
                            zIdxs[i] = z;
                            i++;
                        }
                    } else {
                        zIdxs = Util.convertSetToArray(subset);
                    }
                    // set the indices x,y, and new attr index
                    qData.setIndexes(xIdx, yIdx, zIdxs);
                    // run independence test
                    qData.computeStats(); //Ignore power considerations for now
                    //log.info(xName + "," + yName);
                    score = qData.assoc(psSignificance);
                    //log.info("TOTAL: gStat: " + score[1] + " pVal: " + score[5]);
                    statCalls++;

                    double[] chiScore = new double[]{0, 0, 0, 0, 0, 0};
                    if (runChiSquare) {
                        //log.info("Reverting to chi-square x=" + xName + " y=" + yName + " n=" + qInstances.numInstances());
                        zIdxs = new int[]{};
                        data.setIndexes(xIdx, yIdx, Util.convertSetToArray(subset)); //Run test without conditioning on the quantile.
                        data.computeStats(); //Ignore power considerations for now
                        chiScore = data.assoc(pValThreshold);
                        statCalls++;
                    }


                    //log.info("POWER: algorithm=propensity-score x=" + data.getName(xIdx) + "y=" + data.getName(yIdx) + " score=" + score[0] +" n=" + qInstances.numInstances() + " pVal=" + score[5]);

                    // If independent then remove edge from neighbors.
                    if (Double.compare(score[0], 0.0) <= 0) {
                        if (runChiSquare && Double.compare(chiScore[0], 0.0) > 0) {
                            //If either test concludes not independent then conclude dependence
                            //log.info("Adding based on chi-square " + data.getName(xIdx) + " and " + data.getName(yIdx));
                            log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                        } else {
                            log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                            //Remove from the corresponding neighbor sets and update skeleton information.
                            neighbors.get(xIdx).remove(new Integer(yIdx));
                            neighbors.get(yIdx).remove(new Integer(xIdx));
                            addConstraint(xIdx, yIdx, subset, true, -1.0); //true, means found independence in data.
                            //Ignore sepset for now, will patch later
                            //save zIdxs as sepset(xIdx,yIdx);
                            setSepset(xIdx, yIdx, new HashSet<Integer>());
                            //setEffectSize(xIdx, yIdx, score[2]);
                            //setEdgeStatus(xIdx, yIdx, -1);
                            pairs.remove(pair);
                            break;
                        }
                    } else {
                        log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    }

                }
            }

            //Include a mechanism for breaking early.
            if (setSize >= maxSetSize) {
                break;
            }

            setSize++;
        }
        return statCalls;
    }

    public int getLeastCommonValue(Instances instances, int attrIndex) {
        Attribute attr = instances.attribute(attrIndex);
        int[] valueCount = new int[attr.numValues()];


        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            int valueIndex = (int) instance.value(attrIndex);
            valueCount[valueIndex]++;
        }
        int bestIndex = 0;
        int bestCount = Integer.MAX_VALUE;
        for (int j = 0; j < valueCount.length; j++) {
            if (valueCount[j] < bestCount && valueCount[j] > 0) {
                bestIndex = j;
                bestCount = valueCount[j];
            }
        }

        return bestIndex;
    }

    public void setDoQuantile(boolean value) {
        doQuantile = value;
    }

    public void setRunChiSquare(boolean value) {
        runChiSquare = value;
    }

    public void setPSSignificance(double sig) {
        psSignificance = sig;
    }


}
