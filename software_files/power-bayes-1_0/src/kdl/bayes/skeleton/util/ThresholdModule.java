/**
 * $Id: ThresholdModule.java 278 2009-05-27 17:55:25Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import org.apache.log4j.Logger;

public class ThresholdModule {

    protected static Logger log = Logger.getLogger(ThresholdModule.class);

    protected long dofThreshold;

    /**
     * Default behavior is run every test.
     * Should be overridden by extending classes.
     *
     * @param data
     * @return
     */
    public boolean enoughData(BayesData data) {
        return true;
    }

    public boolean tableSizeOK(BayesData data) {
        return data.tableSize >= 0;
    }

    public long getDofThreshold() {
        return dofThreshold;
    }
}

