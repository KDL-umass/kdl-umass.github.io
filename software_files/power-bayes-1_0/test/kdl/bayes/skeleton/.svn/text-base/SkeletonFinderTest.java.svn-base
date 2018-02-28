/**
 * $Id$
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import junit.framework.TestCase;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import kdl.bayes.util.constraint.Constraint;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SkeletonFinderTest extends TestCase {

    protected static Logger log = Logger.getLogger(SkeletonFinderTest.class);

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
                    "@attribute C3 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T,T\n" +
                    "T,F,T,T,T\n" +
                    "T,F,T,T,T\n" +
                    "F,T,T,T,T\n" +
                    "T,T,F,F,T\n" +
                    "T,T,F,F,T\n" +
                    "T,T,F,F,T\n" +
                    "T,F,F,F,T\n";

    private String testInput3 =
            "@relation TestData\n" +
                    "@attribute A {T,F}\n" +
                    "@attribute B {T,F}\n" +
                    "@attribute C1 {T,F}\n" +
                    "@attribute C2 {T,F}\n" +
                    "@attribute C3 {T,F}\n" +
                    "@attribute C4 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T,T,F\n" +
                    "T,F,T,T,T,F\n" +
                    "T,F,T,T,T,F\n" +
                    "F,T,T,T,T,F\n" +
                    "T,T,F,F,T,F\n" +
                    "T,T,F,F,T,F\n" +
                    "T,T,F,F,T,F\n" +
                    "T,F,F,F,T,F\n";

    private String testInput4 =
            "@relation TestData\n" +
                    "@attribute A {T,F}\n" +
                    "@attribute B {T,F}\n" +
                    "@attribute C1 {T,F}\n" +
                    "@attribute C2 {T,F}\n" +
                    "@attribute C3 {T,F}\n" +
                    "@attribute C4 {T,F}\n" +
                    "@attribute C5 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T,T,F,T\n" +
                    "T,F,T,T,T,F,T\n" +
                    "T,F,T,T,T,F,T\n" +
                    "F,T,T,T,T,F,T\n" +
                    "T,T,F,F,T,F,T\n" +
                    "T,T,F,F,T,F,T\n" +
                    "T,T,F,F,T,F,T\n" +
                    "T,F,F,F,T,F,T\n";

    private boolean[][] testGraph = {{false, true, true, false},
            {true, false, false, true},
            {true, false, false, true},
            {false, true, true, false}};

    private boolean[][] testGraph2 = {{false, true, true, false},
            {true, false, false, true},
            {true, false, false, false},
            {false, true, false, false}};

    private boolean[][] testGraph3 = {{false, true, true, false, true},
            {true, false, true, true, false},
            {true, true, false, true, false},
            {false, true, true, false, false},
            {true, false, false, false, false}};

    private boolean[][] testGraph4 = {
            {false, true, false, true, false},
            {true, false, true, false, false},
            {false, true, false, true, false},
            {true, false, true, false, true},
            {false, false, false, true, false}};

    private boolean[][] testGraph5 = {
            {false, false, true, true, true},
            {false, false, true, true, true},
            {true, true, false, true, true},
            {true, true, true, false, true},
            {true, true, true, true, false}};

    private boolean[][] testGraph6 = {
            {false, true, true, false, false, false},
            {true, false, false, true, false, false},
            {true, false, false, true, false, false},
            {false, true, true, false, true, false},
            {false, false, false, true, false, true},
            {false, false, false, false, true, false}};

    private boolean[][] testGraph7 = {
            {false, true, true, false, false, false, false},
            {true, false, false, true, false, false, false},
            {true, false, false, true, false, false, false},
            {false, true, true, false, true, false, false},
            {false, false, false, true, false, true, true},
            {false, false, false, false, true, false, false},
            {false, false, false, false, true, false, false}};


    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testBuildCompleteGraph() throws IOException {
        Instances data = new Instances(new StringReader(testInput));
        FAS pc = new FAS(data);
        List<List<Integer>> completeGraph = pc.buildCompleteGraph();

        List<Integer> aNeighbors = completeGraph.get(0);
        List<Integer> aExpected = new ArrayList<Integer>();
        aExpected.add(1);
        aExpected.add(2);
        aExpected.add(3);
        Util.verifyCollections(aExpected, aNeighbors);

        List<Integer> bNeighbors = completeGraph.get(1);
        List<Integer> bExpected = new ArrayList<Integer>();
        bExpected.add(0);
        bExpected.add(2);
        bExpected.add(3);
        Util.verifyCollections(bExpected, bNeighbors);

        List<Integer> c1Neighbors = completeGraph.get(2);
        List<Integer> c1Expected = new ArrayList<Integer>();
        c1Expected.add(0);
        c1Expected.add(1);
        c1Expected.add(3);
        Util.verifyCollections(c1Expected, c1Neighbors);

        List<Integer> c2Neighbors = completeGraph.get(3);
        List<Integer> c2Expected = new ArrayList<Integer>();
        c2Expected.add(0);
        c2Expected.add(1);
        c2Expected.add(2);
        Util.verifyCollections(c2Expected, c2Neighbors);

    }


    public void testGetAdjPath() throws IOException {
        Instances data = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(data);

        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph4.length; i++) {
            for (int j = i + 1; j < testGraph4[i].length; j++) {
                if (testGraph4[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }


        Set<Integer> adjPath = pc.getAdjPath(0, 4);
        HashSet<Integer> trueSet = new HashSet<Integer>();
        trueSet.add(1);
        trueSet.add(2);
        trueSet.add(3);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);

    }

    public void testGetAdjPath2() throws IOException {
        Instances data = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(data);

        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph3.length; i++) {
            for (int j = i + 1; j < testGraph3[i].length; j++) {
                if (testGraph3[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }


        Set<Integer> adjPath = pc.getAdjPath(0, 3);
        HashSet<Integer> trueSet = new HashSet<Integer>();
        trueSet.add(1);
        trueSet.add(2);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);

        pc.clearAllEdges();
        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph2.length; i++) {
            for (int j = i + 1; j < testGraph2[i].length; j++) {
                if (testGraph2[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }

        adjPath = pc.getAdjPath(0, 3);
        trueSet = new HashSet<Integer>();
        trueSet.add(1);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);
    }


    public void testGetAdjPath3() throws IOException {
        Instances data = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(data);

        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph5.length; i++) {
            for (int j = i + 1; j < testGraph5[i].length; j++) {
                if (testGraph5[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }


        Set<Integer> adjPath = pc.getAdjPath(0, 3);
        HashSet<Integer> trueSet = new HashSet<Integer>();
        trueSet.add(4);
        trueSet.add(2);
        trueSet.add(1);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);

        data = new Instances(new StringReader(testInput3));
        pc = new FAS(data);
        for (int i = 0; i < testGraph6.length; i++) {
            for (int j = i + 1; j < testGraph6[i].length; j++) {
                if (testGraph6[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }


        adjPath = pc.getAdjPath(0, 5);
        trueSet = new HashSet<Integer>();
        trueSet.add(4);
        trueSet.add(2);
        trueSet.add(1);
        trueSet.add(3);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);
    }

    public void testGetAdjPath4() throws IOException {
        Instances data = new Instances(new StringReader(testInput4));
        FAS pc = new FAS(data);

        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph7.length; i++) {
            for (int j = i + 1; j < testGraph7[i].length; j++) {
                if (testGraph7[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }


        Set<Integer> adjPath = pc.getAdjPath(0, 5);
        HashSet<Integer> trueSet = new HashSet<Integer>();
        trueSet.add(4);
        trueSet.add(2);
        trueSet.add(1);
        trueSet.add(3);
        log.info(trueSet);
        log.info(adjPath);
        Util.verifyCollections(trueSet, adjPath);
    }


    public void testGetSkeleton() throws IOException {
        Instances data = new Instances(new StringReader(testInput));
        FAS pc = new FAS(data);

        //Need to set testGraph as skeleton
        for (int i = 0; i < testGraph.length; i++) {
            for (int j = i + 1; j < testGraph[i].length; j++) {
                if (testGraph[i][j]) {
                    pc.addEdge(i, j);
                }
            }
        }

        List<Integer> neigh0 = pc.getNeighbors(0);
        List<Integer> expected = new ArrayList<Integer>();
        expected.add(1);
        expected.add(2);
        Util.verifyCollections(expected, neigh0);

        List<Integer> neigh1 = pc.getNeighbors(1);
        expected = new ArrayList<Integer>();
        expected.add(0);
        expected.add(3);
        Util.verifyCollections(expected, neigh1);

        List<Integer> neigh2 = pc.getNeighbors(2);
        expected = new ArrayList<Integer>();
        expected.add(0);
        expected.add(3);
        Util.verifyCollections(expected, neigh2);

        List<Integer> neigh3 = pc.getNeighbors(3);
        expected = new ArrayList<Integer>();
        expected.add(1);
        expected.add(2);
        Util.verifyCollections(expected, neigh3);
    }

    public void testSaveReadSkeleton() throws IOException {
        Instances data = new Instances(new StringReader(testInput));
        FAS skeleton = new FAS(data);
        skeleton.addEdge(0, 1);
        skeleton.addEdge(0, 2);
        skeleton.addEdge(1, 3);
        skeleton.addEdge(2, 3);

        Set<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(1);
        zIdxs.add(2);
        skeleton.addConstraint(0, 3, zIdxs, true, 0.25);
        skeleton.setSepset(0, 3, zIdxs);

        zIdxs = new HashSet<Integer>();
        zIdxs.add(0);

        skeleton.addConstraint(1, 2, zIdxs, true, 0.75);
        skeleton.setSepset(1, 2, zIdxs);


        String filename = "test.skeleton.output.txt";

        skeleton.save(filename);

        FAS skeleton2 = new FAS(data);
        skeleton2.read(filename);

        for (int i = 0; i < skeleton2.numVariables; i++) {
            for (int j = i + 1; j < skeleton2.numVariables; j++) {
                Assert.condition(skeleton.hasEdge(i, j) == skeleton2.hasEdge(i, j), "Skeletons should have the same edge.");

                if (!skeleton.hasEdge(i, j)) {
                    Set<Integer> pair = new HashSet<Integer>();
                    pair.add(i);
                    pair.add(j);
                    Set<Integer> sepset1 = skeleton.getSepset(pair);
                    Set<Integer> sepset2 = skeleton2.getSepset(pair);

                    Assert.condition(sepset1.size() == sepset2.size(), "Two sepsets must be the same size.");

                    sepset1.removeAll(sepset2);

                    Assert.condition(sepset1.size() == 0, "Sepset 1 must contain all of sepset 2");
                }

            }
        }

        Set<Constraint> constraints = skeleton.getConstraints();
        Set<Constraint> constraints2 = skeleton.getConstraints();

        for (Constraint constraint : constraints) {
            Assert.condition(constraints2.contains(constraint), "All constraints should be equal.");
        }

    }
}
