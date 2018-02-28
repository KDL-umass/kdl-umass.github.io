/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.StatUtil;

public class SplitPowerThreshold extends ThresholdModule {

    long pairwiseThreshold;

    public SplitPowerThreshold(double sampleSize, double pairwiseEffect, double effectSize, double powerThreshold, double pVal) {
        pairwiseThreshold = StatUtil.dofThresholdForEffect(sampleSize, pairwiseEffect, powerThreshold, pVal);
        dofThreshold = StatUtil.dofThresholdForEffect(sampleSize, effectSize, powerThreshold, pVal);
    }

    public boolean enoughData(BayesData data) {
        if (data.zIdxs.length > 0) {
            return tableSizeOK(data) && data.traditionalDof < dofThreshold;
        }

        log.debug("Using pairwise threshold.");
        return tableSizeOK(data) && data.traditionalDof < pairwiseThreshold;
    }

}
