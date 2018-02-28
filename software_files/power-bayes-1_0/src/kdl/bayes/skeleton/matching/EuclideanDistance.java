/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

public class EuclideanDistance implements DistanceModule {

    public double getDistance(double[] prob1, double[] prob2) {
        double sum = 0.0;

        for (int i = 0; i < prob1.length; i++) {
            sum += Math.pow((prob1[i] - prob2[i]), 2);
        }
        return Math.sqrt(sum);

    }
}
