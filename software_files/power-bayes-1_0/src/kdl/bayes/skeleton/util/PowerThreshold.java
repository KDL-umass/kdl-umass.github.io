/**
 * $Id: PowerThreshold.java 241 2008-05-05 17:32:41Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.StatUtil;

public class PowerThreshold extends ThresholdModule {

    public PowerThreshold(double sampleSize, double effectSize, double powerThreshold, double pVal) {
        dofThreshold = StatUtil.dofThresholdForEffect(sampleSize, effectSize, powerThreshold, pVal);
    }

    public boolean enoughData(BayesData data) {
        return tableSizeOK(data) && data.traditionalDof < dofThreshold;
    }
}
