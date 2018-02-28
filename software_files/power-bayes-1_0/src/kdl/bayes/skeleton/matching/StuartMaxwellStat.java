/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

import kdl.bayes.util.StatUtil;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map;

public class StuartMaxwellStat implements StatisticModule {

    public double getSignificance(int xIdx, int yIdx, Instances treatment, Instances nonTreatment, Map<Integer, Integer> matching) {
//Compute test statistic using the matches.
        int numOutcomes = treatment.attribute(yIdx).numValues();
        //First get counts.
        int[][] counts = new int[numOutcomes][numOutcomes];

        for (Integer treatmentIndex : matching.keySet()) {
            int treatmentOutcome = (int) treatment.instance(treatmentIndex).value(yIdx);

            Instance match = nonTreatment.instance(matching.get(treatmentIndex));
            int matchOutcome = (int) match.value(yIdx);

            counts[treatmentOutcome][matchOutcome]++;
        }

        return StatUtil.stuartMaxwell(counts);

    }

}
