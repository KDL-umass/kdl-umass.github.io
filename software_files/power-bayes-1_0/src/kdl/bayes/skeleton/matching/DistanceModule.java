/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

public interface DistanceModule {

    double getDistance(double[] treatment, double[] nonTreatment);

}
