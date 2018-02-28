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

import java.util.*;

public class RandomMatching implements MatchingModule {

    protected static Logger log = Logger.getLogger(RandomMatching.class);
    Random rand = new Random();

    public RandomMatching() {
    }

    public Map<Integer, Integer> doMatching(Classifier psModel, Instances treatment, Instances nonTreatment) throws Exception {

        Map<Integer, Integer> matching = new HashMap<Integer, Integer>();

        int classIndex = treatment.classIndex();

        Set<Integer> matchedList = new HashSet<Integer>();
        nonTreatment.randomize(rand);

        for (int i = 0; i < treatment.numInstances(); i++) {
            Instance treatmentInstance = treatment.instance(i);
            int treatmentValue = (int) treatmentInstance.value(classIndex);

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

                bestInstance = j;
            }

            if (bestInstance > -1) {
                matchedList.add(bestInstance);
                matching.put(i, bestInstance);
            }
        }
        return matching;
    }


}
