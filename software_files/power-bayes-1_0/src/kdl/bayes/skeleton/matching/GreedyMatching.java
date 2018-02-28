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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GreedyMatching implements MatchingModule {

    DistanceModule distance;
    protected static Logger log = Logger.getLogger(GreedyMatching.class);


    public GreedyMatching(DistanceModule distance) {
        this.distance = distance;
    }

    public Map<Integer, Integer> doMatching(Classifier psModel, Instances treatment, Instances nonTreatment) throws Exception {

        Map<Integer, Integer> matching = new HashMap<Integer, Integer>();

        int classIndex = treatment.classIndex();

        Set<Integer> matchedList = new HashSet<Integer>();

        for (int i = 0; i < treatment.numInstances(); i++) {

            Instance treatmentInstance = treatment.instance(i);
            int treatmentValue = (int) treatmentInstance.value(classIndex);
            double[] probMap = psModel.distributionForInstance(treatmentInstance);

            double bestDiff = Double.MAX_VALUE;
            int bestInstance = -1;

            for (int j = 0; j < nonTreatment.numInstances(); j++) {
                if (matchedList.contains(j)) {
                    continue;
                }

                Instance matchInstance = nonTreatment.instance(j);
                int matchValue = (int) matchInstance.value(classIndex);

                if (matchValue == treatmentValue) {
                    continue;
                }

                double[] matchProb = psModel.distributionForInstance(matchInstance);

                double diff = distance.getDistance(probMap, matchProb);

                if (Double.compare(diff, bestDiff) < 0) {
                    bestDiff = diff;
                    bestInstance = j;
                }

                if (Double.compare(bestDiff, 0.0) == 0) {
                    break;
                }

            }

            if (bestInstance == -1) {
                //log.warn("Did not find a match.");
                continue;
            }

            matchedList.add(bestInstance);
            matching.put(i, bestInstance);
        }
        return matching;
    }

}
