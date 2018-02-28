/**
 * $Id: SHDistanceTest.java 267 2009-02-18 20:12:07Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: SHDistanceTest.java 267 2009-02-18 20:12:07Z afast $
 */

package kdl.bayes.util;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 * SHDistanceTest
 */
public class SHDistanceTest extends TestCase {
    protected static Logger log = Logger.getLogger(SHDistanceTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testDistance() throws Exception {
        // E = { (0,1), (1,2) }
        boolean[][] graph = new boolean[][]{
                {false, true, false},
                {false, false, true},
                {false, false, false}};
        // E = { (0,2), (2,1) }
        boolean[][] correctGraph = new boolean[][]{
                {false, false, true},
                {false, false, false},
                {false, true, false}};
        SHDistance shd = new SHDistance(graph, correctGraph);
        assertEquals(1, shd.getNumFalsePositives());
        assertEquals(1, shd.getNumFalseNegatives());
        assertEquals(1, shd.getNumDirectionWrong());
        assertEquals(0, shd.getNumUndirWhenDir());
        assertEquals(0, shd.getNumDirWhenUndir());
        assertEquals(2, shd.getNumTrueCompelled());

        Assert.condition(Double.compare(shd.getSPrecision(), 0.5) == 0, "1/2 edges is correct.");
        Assert.condition(Double.compare(shd.getSRecall(), 0.5) == 0, "1/2 edges is correct.");
        Assert.condition(Double.compare(shd.getCPrecision(), 0.0) == 0, "0/2 edges are correct.");
        Assert.condition(Double.compare(shd.getCRecall(), 0.0) == 0, "0/2 edges are correct.");
    }

    public void testDistance2() throws Exception {
        // E = { (0,1), (1,2), (2,1), (0,2) }
        boolean[][] graph = new boolean[][]{
                {false, true, true},
                {false, false, true},
                {false, true, false}};
        // E = { (0,1), (1,0), (0,2), (2,0), (2,1) }
        boolean[][] correctGraph = new boolean[][]{
                {false, true, true},
                {true, false, false},
                {true, true, false}};
        SHDistance shd = new SHDistance(graph, correctGraph);
        assertEquals(0, shd.getNumFalsePositives());
        assertEquals(0, shd.getNumFalseNegatives());
        assertEquals(0, shd.getNumDirectionWrong());
        assertEquals(1, shd.getNumUndirWhenDir());
        assertEquals(2, shd.getNumDirWhenUndir());
        assertEquals(1, shd.getNumTrueCompelled());

        Assert.condition(Double.compare(shd.getSPrecision(), 1) == 0, "3/3 edges are correct.");
        Assert.condition(Double.compare(shd.getSRecall(), 1) == 0, "3/3 edges is correct.");
        Assert.condition(Double.compare(shd.getCPrecision(), 0.0) == 0, "0/3 edges are correct.");
        Assert.condition(Double.compare(shd.getCRecall(), 0.0) == 0, "0/3 edges are correct.");
    }

    public void testDistance3() throws Exception {
        // E = { (0,1), (0,2) }
        boolean[][] graph = new boolean[][]{
                {false, true, true},
                {false, false, false},
                {false, false, false}};
        // E = { (0,1), (0,2), (2,0), (2,1) }
        boolean[][] correctGraph = new boolean[][]{
                {false, true, true},
                {false, false, false},
                {true, true, false}};

        SHDistance shd = new SHDistance(graph, correctGraph);

        Assert.condition(StatUtil.equalDoubles(shd.getSPrecision(), 1, 0.01), "2/2 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getSRecall(), 0.6666, 0.01), "2/3 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getCPrecision(), 0.5, 0.0001), "1/2 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getCRecall(), 0.5, 0.0001), "1/2 edges are correct.");
    }

    public void testCRecall() {
        boolean[][] graph = {
                {false, true, true, false, false},
                {true, false, false, true, false},
                {false, false, false, true, false},
                {false, false, false, false, false},
                {false, false, true, false, false}};
        boolean[][] correctGraph = {
                {false, true, true, false, false},
                {true, false, false, true, false},
                {true, false, false, true, true},
                {false, false, false, false, false},
                {false, false, true, false, false}};

        SHDistance shd = new SHDistance(graph, correctGraph);

        log.info(shd.getCPrecision());
        log.info(shd.getCRecall());

        Assert.condition(StatUtil.equalDoubles(shd.getSPrecision(), 1, 0.01), "5/5 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getSRecall(), 1, 0.01), "5/5 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getCPrecision(), 0.5, 0.0001), "2/4 edges are correct.");
        Assert.condition(StatUtil.equalDoubles(shd.getCRecall(), 1, 0.0001), "2/2 edges are correct.");

        graph = new boolean[][]{{false, false, true, false, false, false}, {false, false, true, false, false, false}, {true, true, false, false, true, true}, {false, false, false, false, false, false}, {false, false, true, false, false, false}, {false, false, true, false, false, false}};
        correctGraph = new boolean[][]{{false, false, true, false, false, false}, {false, false, true, false, false, false}, {false, false, false, false, true, true}, {false, false, false, false, true, false}, {false, false, false, false, false, false}, {false, false, false, false, false, false}};

        shd = new SHDistance(graph, correctGraph);

        log.info(shd.getCPrecision());
        log.info(shd.getCRecall());
    }

    public void testBidirectedEdges() {
        int[][] correctGraph = {{0, 1, 1, 0}, {0, 0, 1, 1}, {0, 1, 0, 1}, {0, 0, 0, 0}};
        int[][] graph = {{0, 1, 0, 1}, {0, 0, 2, 1}, {0, 2, 0, 2}, {0, 1, 2, 0}};

        SHDistance shd = new SHDistance(graph, correctGraph);

        assertEquals(1, shd.getNumFalsePositives());
        assertEquals(1, shd.getNumFalseNegatives());
        assertEquals(1, shd.getNumDirectionWrong());
        assertEquals(1, shd.getNumUndirWhenDir());
        assertEquals(1, shd.getNumDirWhenUndir());
        assertEquals(4, shd.getNumTrueCompelled());

    }
}
