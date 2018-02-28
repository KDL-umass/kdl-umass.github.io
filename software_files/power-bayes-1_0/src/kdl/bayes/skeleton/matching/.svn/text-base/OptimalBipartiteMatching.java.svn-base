/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Map;

public class OptimalBipartiteMatching implements MatchingModule {

    protected static Logger log = Logger.getLogger(OptimalBipartiteMatching.class);

    DistanceModule distance;

    public OptimalBipartiteMatching(DistanceModule distance) {
        this.distance = distance;
    }

    public Map<Integer, Integer> doMatching(Classifier psModel, Instances treatment, Instances nonTreatment) throws Exception {

        //First, step create 2d array for input into HungarianAlgorithm
        int numTreatment = treatment.numInstances();
        int numNonTreatment = nonTreatment.numInstances();

        int classIndex = treatment.classIndex();

        double[][] distances = new double[numTreatment][numNonTreatment];

        for (int i = 0; i < numTreatment; i++) {
            Instance treatUnit = treatment.instance(i);
            int treatmentValue = (int) treatUnit.value(classIndex);
            double[] treatProb = psModel.distributionForInstance(treatUnit);

            for (int j = 0; j < numNonTreatment; j++) {

                Instance nonTreatUnit = nonTreatment.instance(j);
                double[] nonTreatProb = psModel.distributionForInstance(nonTreatUnit);

                int matchValue = (int) nonTreatUnit.value(classIndex);

                if (matchValue == treatmentValue) {
                    continue;
                }


                distances[i][j] = distance.getDistance(treatProb, nonTreatProb);
            }
        }

        int[][] assignment = HungarianAlgorithm.hgAlgorithm(distances, "min");

        //log.info(assignment.length);
        //log.info(numTreatment);
        //log.info(numNonTreatment);

        Map<Integer, Integer> matching = new HashMap<Integer, Integer>();

        for (int i = 0; i < assignment.length; i++) {
            matching.put(assignment[i][0], assignment[i][1]);
        }

        return matching;

    }

}
