/**
 * $Id: SkeletonAnalysisTest.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: SkeletonAnalysisTest.java 237 2008-04-07 16:54:03Z afast $
 */

package kdl.bayes.util;

import junit.framework.TestCase;
import kdl.bayes.PowerBayesNet;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: afast
 * Date: May 4, 2007
 * Time: 3:37:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SkeletonAnalysisTest extends TestCase {
    protected static Logger log = Logger.getLogger(SkeletonAnalysisTest.class);

    private String testInstances =
            "@relation test1\n" +
                    "@attribute attr0 {false,true}\n" +
                    "@attribute attr1 {false,true}\n" +
                    "@attribute attr2 {false,true}\n" +
                    "@data\n" +
                    "true,false,true\n" +
                    "false,true,false";


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

        Instances instances = new Instances(new StringReader(testInstances));
        PowerBayesNet trueBn = new PowerBayesNet(instances, correctGraph);
        PowerBayesNet gsBn = new PowerBayesNet(instances, graph);

        Map<String, Set<String>> cpc = new HashMap();
        Set<String> attr0Neighbors = new HashSet();
        attr0Neighbors.add("attr1");
        attr0Neighbors.add("attr2");
        Set<String> attr1Neighbors = new HashSet();
        attr1Neighbors.add("attr0");
        Set<String> attr2Neighbors = new HashSet();
        attr2Neighbors.add("attr0");

        cpc.put("attr0", attr0Neighbors);
        cpc.put("attr1", attr1Neighbors);
        cpc.put("attr2", attr2Neighbors);


        Map<String, Integer[]> testMap = SkeletonAnalysis.writeOverlapToDotMMPC("/tmp/test.dot", trueBn, gsBn, cpc);

        Integer[] attr0score = testMap.get("attr0");
        assertEquals((int) attr0score[0], 0); //all
        assertEquals((int) attr0score[1], 1); //mt
        assertEquals((int) attr0score[2], 0); //gt
        assertEquals((int) attr0score[3], 1); //gm
        assertEquals((int) attr0score[4], 0); //m
        assertEquals((int) attr0score[5], 0); //g
        assertEquals((int) attr0score[6], 0); //t

        Integer[] attr1score = testMap.get("attr1");
        assertEquals((int) attr1score[0], 0);
        assertEquals((int) attr1score[1], 0);
        assertEquals((int) attr1score[2], 1);
        assertEquals((int) attr1score[3], 1);
        assertEquals((int) attr1score[4], 0);
        assertEquals((int) attr1score[5], 0);
        assertEquals((int) attr1score[6], 0);

        Integer[] attr2score = testMap.get("attr2");
        assertEquals((int) attr2score[0], 0);
        assertEquals((int) attr2score[1], 1);
        assertEquals((int) attr2score[2], 1);
        assertEquals((int) attr2score[3], 0);
        assertEquals((int) attr2score[4], 0);
        assertEquals((int) attr2score[5], 0);
        assertEquals((int) attr2score[6], 0);

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
    }


}
