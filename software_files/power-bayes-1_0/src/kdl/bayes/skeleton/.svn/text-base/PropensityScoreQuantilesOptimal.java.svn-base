/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import kdl.bayes.skeleton.matching.HungarianAlgorithm;
import kdl.bayes.skeleton.util.BayesData;
import kdl.bayes.skeleton.util.ThresholdModule;
import org.apache.log4j.Logger;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class PropensityScoreQuantilesOptimal extends SkeletonFinder {

    protected static Logger log = Logger.getLogger(PropensityScoreQuantilesRandom.class);

    public ThresholdModule threshModule;
    Attribute quantile;
    Attribute prob;

    Instances poolData;

    int numBins = 5;

    boolean doQuantile = true;
    boolean runChiSquare = false;

    Random rand = new Random();

    public PropensityScoreQuantilesOptimal(Instances trainData) {
        this(trainData, trainData, new ThresholdModule());
    }

    public PropensityScoreQuantilesOptimal(Instances trainData, Instances poolData) {
        this(trainData, poolData, new ThresholdModule());
    }

    public PropensityScoreQuantilesOptimal(Instances instances, Instances poolInstances, ThresholdModule module) {
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

        neighbors = buildCompleteGraph();

        List<List<Integer>> pairs = buildPairs(true);

        int statCalls = 0;

        //log.info(Arrays.deepToString(pairs.toArray()));

        //Consider every pair once.
        for (List<Integer> pair : pairs) {
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

            Instances treatment = new Instances("quantile", attributes, instances.numInstances());
            treatment.setClassIndex(xIdx);
            Instances nonTreatment = new Instances("nonTreatment", attributes, instances.numInstances());
            nonTreatment.setClassIndex(xIdx);

            try {
                rf.buildClassifier(rfInstances);

                for (int i = 0; i < rfInstances.numInstances(); i++) {
                    Instance instance = rfInstances.instance(i);

                    double[] probMap = rf.distributionForInstance(instance);
                    double prob = probMap[lcv];

                    Instance qInstance = new Instance(2);
                    Instance trainInstance = instances.instance(i);

                    qInstance = trainInstance.mergeInstance(qInstance);
                    qInstance.setDataset(treatment);

                    qInstance.setValue(treatment.numAttributes() - 2, prob);

                    if ((int) instance.value(xIdx) == lcv) {
                        treatment.add(qInstance);
                    } else {
                        nonTreatment.add(qInstance);
                    }
                }

                //Sort by prob then break into equal chunks.
                int binSize = (treatment.numInstances() / numBins) + 1;
                treatment.sort(treatment.numAttributes() - 2);
                for (int i = 0; i < treatment.numInstances(); i++) {
                    Instance qInstance = treatment.instance(i);
                    //log.info(qInstance.value(treatment.numAttributes()-2) + " " + i/binSize);
                    qInstance.setValue(treatment.numAttributes() - 1, i / binSize);
                }

                //Now, find near matches in terms of propensity score and add matches to treatment.
                int numTreatment = treatment.numInstances();
                int numNonTreatment = nonTreatment.numInstances();
                log.debug("Num treated: " + numTreatment);

                Instances rfPoolInstances = new Instances(poolData);
                rfPoolInstances.setClassIndex(xIdx);
                rfPoolInstances.deleteAttributeAt(yIdx);

                double[][] distances = new double[numTreatment][numNonTreatment];
                int classIndex = treatment.classIndex();


                for (int i = 0; i < numTreatment; i++) {
                    Instance treatUnit = treatment.instance(i);
                    int treatmentValue = (int) treatUnit.value(classIndex);
                    double treatProb = treatUnit.value(treatment.numAttributes() - 2);
                    for (int j = 0; j < numNonTreatment; j++) {

                        Instance nonTreatUnit = nonTreatment.instance(j);
                        int matchValue = (int) nonTreatUnit.value(classIndex);
                        double nonTreatProb = nonTreatUnit.value(treatment.numAttributes() - 2);


                        double distance = Double.POSITIVE_INFINITY;

                        if (treatmentValue != matchValue) {
                            distance = Math.abs(treatProb - nonTreatProb);
                        }
                        distances[i][j] = distance;
                    }
                }


                int[][] assignment = HungarianAlgorithm.hgAlgorithm(distances, "min");

                for (int i = 0; i < assignment.length; i++) {
                    int treatIdx = assignment[i][0];
                    int nonTreatIdx = assignment[i][1];

                    Instance treatInstance = treatment.instance(treatIdx);
                    Instance nonTreatInstance = nonTreatment.instance(nonTreatIdx);

                    //need to set bin according to the bin of the matched treatment instance.
                    int bin = (int) treatInstance.value(treatment.numAttributes() - 1);
                    nonTreatInstance.setValue(treatment.numAttributes() - 1, bin);
                    //then add match to the treatment instances for quantiles.
                    treatment.add(nonTreatInstance);
                }

                //log.info(treatment.numInstances() - numTreatment);

                //for(int i = numTreatment; i< treatment.numInstances() ; i++){
                //    log.info(treatment.instance(i).toString());
                //}


            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            //create a new BayesData with the new instances.
            double[] score;

            BayesData qData = new BayesData(treatment);
            int[] zIdxs = new int[]{};
            if (doQuantile) {
                zIdxs = new int[]{(treatment.numAttributes() - 1)};
            }
            // set the indices x,y, and new attr index
            qData.setIndexes(xIdx, yIdx, zIdxs);
            // run independence test
            qData.computeStats(); //Ignore power considerations for now
            //log.info(xName + "," + yName);
            score = qData.assoc(pValThreshold);
            //log.info("TOTAL: gStat: " + score[1] + " pVal: " + score[5]);
            statCalls++;

            double[] chiScore = new double[]{0, 0, 0, 0, 0, 0};
            if (runChiSquare) {
                //log.info("Reverting to chi-square x=" + xName + " y=" + yName + " n=" + treatment.numInstances());
                zIdxs = new int[]{};
                data.setIndexes(xIdx, yIdx, zIdxs);
                data.computeStats(); //Ignore power considerations for now
                chiScore = data.assoc(pValThreshold);
                statCalls++;
            }


            //log.info("POWER: algorithm=propensity-score x=" + data.getName(xIdx) + "y=" + data.getName(yIdx) + " score=" + score[0] +" n=" + treatment.numInstances() + " pVal=" + score[5]);

            // If independent then remove edge from neighbors.
            if (Double.compare(score[0], 0.0) <= 0) {
                if (runChiSquare && Double.compare(chiScore[0], 0.0) > 0) {
                    //If both tests are not independent then conclude dependence
                    //log.info("Adding based on chi-square " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                } else {
                    log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    //Remove from the corresponding neighbor sets and update skeleton information.
                    neighbors.get(xIdx).remove(new Integer(yIdx));
                    neighbors.get(yIdx).remove(new Integer(xIdx));

                    //Ignore sepset for now, will patch later
                    //save zIdxs as sepset(xIdx,yIdx);
                    setSepset(xIdx, yIdx, new HashSet<Integer>());
                    //setEffectSize(xIdx, yIdx, score[2]);
                    //setEdgeStatus(xIdx, yIdx, -1);
                }
            } else {
                log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
            }

        }

        return statCalls;  //To change body of implemented methods use File | Settings | File Templates.
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

}
