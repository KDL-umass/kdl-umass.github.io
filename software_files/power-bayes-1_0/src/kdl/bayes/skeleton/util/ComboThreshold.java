/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

public class ComboThreshold extends ThresholdModule {

    PowerThreshold powerThreshold;
    DefaultThreshold defaultThreshold;

    public ComboThreshold(double sampleSize, double effectSize, double powerThreshold, double pVal) {
        this.powerThreshold = new PowerThreshold(sampleSize, effectSize, powerThreshold, pVal);
        this.defaultThreshold = new DefaultThreshold((int) sampleSize);
    }

    public boolean enoughData(BayesData data) {
        return powerThreshold.enoughData(data) && defaultThreshold.enoughData(data);
    }

}
