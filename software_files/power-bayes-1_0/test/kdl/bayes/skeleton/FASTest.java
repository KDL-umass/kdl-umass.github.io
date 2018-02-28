/**
 * $Id$
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import junit.framework.TestCase;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class FASTest extends TestCase {


    protected static Logger log = Logger.getLogger(FASTest.class);

    private String testInput =
            "@relation TestData\n" +
                    "@attribute A {T,F}\n" +
                    "@attribute B {T,F}\n" +
                    "@attribute C1 {T,F}\n" +
                    "@attribute C2 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "F,T,T,T\n" +
                    "T,T,F,F\n" +
                    "T,T,F,F\n" +
                    "T,T,F,F\n" +
                    "T,F,F,F\n";

    private String testInput2 =
            "@relation TestData\n" +
                    "@attribute A {T,F}\n" +
                    "@attribute B {T,F}\n" +
                    "@attribute C1 {T,F}\n" +
                    "@attribute C2 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "F,T,T,T\n" +
                    "F,T,F,F\n" +
                    "F,T,F,F\n" +
                    "T,F,F,F\n" +
                    "F,T,F,F\n";

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testPCSkeleton() throws IOException {

        FAS pc = new FAS(new Instances(new StringReader(testInput)), new ThresholdModule());
        pc.computeNeighbors();
        boolean[][] skeleton = pc.getSkeleton();
        log.debug(Arrays.deepToString(skeleton));

        List<Integer> c1neighbors = pc.getNeighbors(2);
        List<Integer> expectedNeighbors = new ArrayList<Integer>();
        expectedNeighbors.add(3);

        Util.verifyCollections(c1neighbors, expectedNeighbors);

        pc = new FAS(new Instances(new StringReader(testInput2)), new ThresholdModule());
        pc.computeNeighbors();
        skeleton = pc.getSkeleton();
        log.debug(Arrays.deepToString(skeleton));

        List<Integer> aneighbors = pc.getNeighbors(0);
        List<Integer> bneighbors = pc.getNeighbors(1);
        expectedNeighbors = new ArrayList<Integer>();
        expectedNeighbors.add(1);

        Util.verifyCollections(aneighbors, expectedNeighbors);

        expectedNeighbors = new ArrayList<Integer>();
        expectedNeighbors.add(0);

        Util.verifyCollections(bneighbors, expectedNeighbors);

    }

    public void testSepsetCreation() throws IOException {
        FAS pc = new FAS(new Instances(new StringReader(testInput)), new MMPCTestHelper());
        pc.computeNeighbors();
        boolean[][] skeleton = pc.getSkeleton();
        log.debug(Arrays.deepToString(skeleton));

        Set<Integer> pair = new HashSet<Integer>();
        pair.add(0);
        pair.add(2);
        Set<Integer> tf = pc.getSepset(pair);
        log.debug(tf);
        Assert.condition(tf.size() == 0, "Sepset has wrong size.");

        pair = new HashSet<Integer>();
        pair.add(0);
        pair.add(5);
        tf = pc.getSepset(pair);
        log.debug(tf);
        Assert.condition(tf.size() == 1, "Sepset has wrong size.");

        Set<Integer> expectedTF = new HashSet<Integer>();
        expectedTF.add(3);
        Util.verifyCollections(expectedTF, tf);

        pair = new HashSet<Integer>();
        pair.add(7);
        pair.add(8);
        Assert.condition(!pc.hasSepsetForEdge(pair), "Sepsets should not contain these variables as they are dependent");
        Assert.condition(skeleton[7][8], "Skeleton does not contain required edge");
        Assert.condition(skeleton[8][7], "Skeleton does not contain required edge");

        pair = new HashSet<Integer>();
        pair.add(0);
        pair.add(1);
        tf = pc.getSepset(pair);
        log.debug(tf);
        Assert.condition(tf.size() == 2, "Sepset has wrong size.");

        expectedTF = new HashSet<Integer>();
        expectedTF.add(3);
        expectedTF.add(4);
        Util.verifyCollections(expectedTF, tf);
    }

    public void testMaxSetSize() throws IOException {
        FAS pc = new FAS(new Instances(new StringReader(testInput)), new MMPCTestHelper());
        int maxSize = 1;
        pc.computeNeighbors(maxSize);
        boolean[][] skeleton = pc.getSkeleton();

        for (int i = 0; i < skeleton.length; i++) {
            for (int j = i + 1; j < skeleton.length; j++) {
                Set<Integer> pair = new HashSet<Integer>();
                pair.add(i);
                pair.add(j);
                Set<Integer> sepset = pc.getSepset(pair);
                if (sepset != null) {
                    Assert.condition(sepset.size() <= maxSize, "Found sepset larger than allowed max");
                }
            }
        }


    }

    public void testBuildPairs() throws IOException {
        FAS pc = new FAS(new Instances(new StringReader(testInput)), new MMPCTestHelper());

        log.info(pc.getNumVariables());

        List<List<Integer>> pairs = pc.buildPairs(false);

        for (List<Integer> pair : pairs) {
            log.info(pair.get(0) + "," + pair.get(1));
        }
    }

}
