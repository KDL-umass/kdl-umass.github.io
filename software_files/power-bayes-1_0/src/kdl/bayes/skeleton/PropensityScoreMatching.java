/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import kdl.bayes.skeleton.matching.MatchingModule;
import kdl.bayes.skeleton.matching.StatisticModule;
import org.apache.log4j.Logger;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PropensityScoreMatching extends SkeletonFinder {

    protected static Logger log = Logger.getLogger(PropensityScoreMatching.class);

    MatchingModule match;
    StatisticModule stat;

    double treatmentRatio = 0.5;

    public PropensityScoreMatching(Instances trainData, MatchingModule match, StatisticModule stat) {
        super(trainData);
        this.match = match;
        this.stat = stat;
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

            //log.info("X: " + data.getName(xIdx) + " Y: " + data.getName(yIdx));
            String xName = data.getName(xIdx);
            String yName = data.getName(yIdx);

            //Set-up Random Forest model for prediction
            Instances rfInstances = new Instances(instances);
            instances.setClassIndex(xIdx);

            rfInstances.setClassIndex(xIdx);   //Should always work since X < Y
            rfInstances.deleteAttributeAt(yIdx);

            //Set-up treatment and non-treatment instances.
            FastVector attributes = new FastVector(instances.numAttributes());

            for (int i = 0; i < instances.numAttributes(); i++) {
                attributes.addElement(instances.attribute(i));
            }
            //attributes.addElement(prob);
            //attributes.addElement(quantile);

            Instances treatment = new Instances("treatment", attributes, instances.numInstances());
            treatment.setClassIndex(xIdx);
            Instances nonTreatment = new Instances("nonTreatment", attributes, instances.numInstances());
            nonTreatment.setClassIndex(xIdx);

            log.info("Treatment values: " + instances.attribute(xIdx).numValues());

            //Select treatment instances
            if (instances.attribute(xIdx).numValues() == 2) {
                //For binary variables select all instances with Least Common variable.
                int lcv = getLeastCommonValue(instances, xIdx);
                for (int i = 0; i < rfInstances.numInstances(); i++) {
                    Instance instance = rfInstances.instance(i);
                    if ((int) instance.value(xIdx) == lcv) {
                        treatment.add(instances.instance(i));
                    } else {
                        nonTreatment.add(instances.instance(i));
                    }
                }
            } else {
                //For non-binary treatment, select subset of instances at random following strategy of Lu et al. 2001
                instances.randomize(new Random());
                int numTreatment = (int) (instances.numInstances() * treatmentRatio);
                for (int i = 0; i < numTreatment; i++) {
                    treatment.add(instances.instance(i));
                }
                for (int j = numTreatment; j < instances.numInstances(); j++) {
                    nonTreatment.add(instances.instance(j));
                }
            }

            //Learn PS model and do matching.
            Map<Integer, Integer> matching = null;

            Instances rfTreatment = new Instances(treatment);
            rfTreatment.setClassIndex(xIdx);
            rfTreatment.deleteAttributeAt(yIdx);

            Instances rfNonTreatment = new Instances(nonTreatment);
            rfNonTreatment.setClassIndex(xIdx);
            rfNonTreatment.deleteAttributeAt(yIdx);


            try {
                //Learn Propensity score model for treatment
                RandomForest rf = new RandomForest();
                rf.buildClassifier(rfInstances);
                //Do matching with the matching module
                matching = match.doMatching(rf, rfTreatment, rfNonTreatment);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            double pVal = stat.getSignificance(xIdx, yIdx, treatment, nonTreatment, matching);

//            //Compute test statistic using the matches.
//            int numOutcomes = instances.attribute(yIdx).numValues();
//            //First get counts.
//            int[][] counts = new int[numOutcomes][numOutcomes];
//
//           for (Integer treatmentIndex : matching.keySet()) {
//                    int treatmentOutcome = (int) treatment.instance(treatmentIndex).value(yIdx);
//
//                    Instance match = nonTreatment.instance(matching.get(treatmentIndex));
//                    int matchOutcome = (int) match.value(yIdx);
//
//                    counts[treatmentOutcome][matchOutcome]++;
//                }
//
//            double pVal = StatUtil.stuartMaxwell(counts);
            statCalls++;

            if (Double.compare(pVal, 0.05) > 0) {
                log.debug("DECISION: Proved Independence. Removing edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
                //Remove from the corresponding neighbor sets and update skeleton information.
                neighbors.get(xIdx).remove(new Integer(yIdx));
                neighbors.get(yIdx).remove(new Integer(xIdx));
            } else {
                log.debug("DECISION: Dependence. Keeping edge between " + data.getName(xIdx) + " and " + data.getName(yIdx));
            }
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


}
