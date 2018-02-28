/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import junit.framework.TestCase;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;

import java.util.PriorityQueue;

public class SkeletonEdgeTest extends TestCase {

    protected static Logger log = Logger.getLogger(SkeletonEdgeTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testCompareTo() {
        PriorityQueue<SkeletonEdge> edgeList = new PriorityQueue<SkeletonEdge>();
        SkeletonEdge edge1 = new SkeletonEdge(0, 1, 0.25);
        SkeletonEdge edge2 = new SkeletonEdge(0, 2, 0.5);
        edgeList.add(edge1);
        edgeList.add(edge2);

        log.info(edge1.compareTo(edge2));

        SkeletonEdge poll1 = edgeList.poll();
        Assert.condition(poll1.getScore() == 0.5, "Should return the largest value first.");


    }

}
