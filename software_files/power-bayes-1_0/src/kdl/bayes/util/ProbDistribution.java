/**
 * $Id: ProbDistribution.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import weka.core.Instance;

/**
 * ProbabilityDistribution
 * Author: mhay
 */
public interface ProbDistribution {

    public double logProbability(Instance instance);
}
