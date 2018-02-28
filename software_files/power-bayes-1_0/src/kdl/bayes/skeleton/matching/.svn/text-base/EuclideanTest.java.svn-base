/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

import junit.framework.TestCase;
import kdl.bayes.util.Assert;
import kdl.bayes.util.StatUtil;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;

public class EuclideanTest extends TestCase {

    protected static Logger log = Logger.getLogger(EuclideanTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testEuclidean() {

        double[] prob1 = new double[]{1, 0};
        double[] prob2 = new double[]{0, 1};

        EuclideanDistance ed = new EuclideanDistance();

        double diff = ed.getDistance(prob1, prob2);

        Assert.condition(StatUtil.equalDoubles(diff, Math.sqrt(2), 0.0001), "Should be square-root of 2");

        prob1 = new double[]{1, 1};
        prob2 = new double[]{0, 1};
        diff = ed.getDistance(prob1, prob2);

        Assert.condition(StatUtil.equalDoubles(diff, 1, 0.0001), "Should be 1");

        prob1 = new double[]{1, 1, 1};
        prob2 = new double[]{0, 0, 0};
        diff = ed.getDistance(prob1, prob2);

        Assert.condition(StatUtil.equalDoubles(diff, Math.sqrt(3), 0.0001), "Should be sqrt(3)");


    }
}
