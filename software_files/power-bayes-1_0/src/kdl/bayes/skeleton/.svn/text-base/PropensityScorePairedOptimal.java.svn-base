/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import kdl.bayes.skeleton.matching.HungarianAlgorithm;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.StatUtil;
import org.apache.log4j.Logger;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

public class PropensityScorePairedOptimal extends SkeletonFinder {

    protected static Logger log = Logger.getLogger(PropensityScorePairedOptimal.class);

    public ThresholdModule threshModule;
    Attribute quantile;
    Attribute prob;


    int numBins = 5;

    boolean doQuantile = true;


    public PropensityScorePairedOptimal(Instances trainData) {
        this(trainData, new ThresholdModule());
    }


    public PropensityScorePairedOptimal(Instances instances, ThresholdModule module) {
        super(instances);
        threshModule = module;


        FastVector values = new FastVector(numBins);

        for (int i = 0; i < numBins; i++) {
            int value = 65 + i;
            values.addElement(new String(new char[]{(char) value}));
        }

        //quantile = new Attribute("quantile", values);
        prob = new Attribute("prob");
    }


    public int computeNeighbors() {

        neighbors = buildCompleteGraph();

        List<List<Integer>> pairs = buildPairs(true);

        int statCalls = 0;

        //log.info(Arrays.deepToString(pairs.toArray()));

        //Consider every pair once.
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

                    qInstance.setValue(treatment.numAttributes() - 1, prob);

                    if ((int) instance.value(xIdx) == lcv) {
                        treatment.add(qInstance);
                    } else {
                        nonTreatment.add(qInstance);
                    }
                }

                int[][] counts = new int[2][2];


                //Now, find near matches in terms of propensity score and add matches to treatment.
                int numTreatment = treatment.numInstances();
                int numNonTreatment = nonTreatment.numInstances();
                log.debug("Num treated: " + numTreatment);

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

                    int treatValue = (int) treatInstance.value(yIdx);
                    int nonTreatValue = (int) nonTreatInstance.value(yIdx);

                    counts[treatValue][nonTreatValue]++;
                }


                //Compute McNemars statistic from counts

                double mcnemar = -1.0;

                int b = counts[0][1];
                int c = counts[1][0];

                if (b + c >= 30) {
                    mcnemar = Math.pow(b - c, 2) / (b + c + 1);
                } else {
                    mcnemar = Math.pow(Math.abs(b - c) - 1, 2) / (b + c + 1);
                }

                statCalls++;

                double pVal = StatUtil.chiSquareP(mcnemar, 1);
                log.debug(mcnemar + " " + pVal);

                if (Double.compare(pVal, 0.05) > 0) {
                    log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                    //Remove from the corresponding neighbor sets and update skeleton information.
                    neighbors.get(xIdx).remove(new Integer(yIdx));
                    neighbors.get(yIdx).remove(new Integer(xIdx));
                } else {
                    log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
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
}
