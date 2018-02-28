/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.Map;

public interface MatchingModule {

    Map<Integer, Integer> doMatching(Classifier model, Instances treatment, Instances nonTreatment) throws Exception;

}

