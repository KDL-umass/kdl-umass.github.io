/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.rules;

import junit.framework.TestCase;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.skeleton.FAS;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.*;

public class EdgeOrientationTest extends TestCase {
    protected static Logger log = Logger.getLogger(EdgeOrientationTest.class);

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

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testFindTriples() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        pc.setSepset(0, 2, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        Set<List<Integer>> triples = pc.findTriples();

        Set<List<Integer>> verifyTriples = new HashSet<List<Integer>>();
        List<Integer> triple1 = new ArrayList<Integer>();
        triple1.add(0);
        triple1.add(2);
        triple1.add(1);
        verifyTriples.add(triple1);

        List<Integer> triple2 = new ArrayList<Integer>();
        triple2.add(1);
        triple2.add(3);
        triple2.add(2);
        verifyTriples.add(triple2);

        Util.verifyCollections(verifyTriples, triples);
    }

    public void testFindColliders() throws Exception {

        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        pc.setSepset(0, 2, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        Set<List<Integer>> colliders = pc.findColliders();

        Set<List<Integer>> verifyColliders = new HashSet<List<Integer>>();
        List<Integer> testCollider = new ArrayList<Integer>();
        testCollider.add(0);
        testCollider.add(2);
        testCollider.add(1);


        verifyColliders.add(testCollider);

        Util.verifyCollections(verifyColliders, colliders);

    }

    public void testOrientEdges() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        pc.setSepset(0, 2, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.orientEdges();
        boolean[][] dag = edge.getPDag();

        Assert.condition(dag[0][1], "Edge Required here.");
        Assert.condition(!dag[1][0], "Edge Not Required here.");
        Assert.condition(dag[2][1], "Edge Required here.");
        Assert.condition(!dag[1][2], "Edge Not Required here.");
        //Assert.condition(!pdag[3][2], "Edge Not Required here.");

    }

    public void testOverlappingColliders() throws Exception {
        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);

        pc.setSepset(0, 2, null);
        pc.setSepset(2, 4, null);
        pc.setSepset(0, 4, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.orientEdges();
        boolean[][] dag = edge.getPDag();

        Assert.condition(dag[0][1], "Edge Required here.");
        Assert.condition(dag[2][1], "Edge Required here.");

        Assert.condition(dag[1][4], "Edge belongs here.");
        Assert.condition(dag[3][4], "Edge belongs here.");

        //Assert.condition(!pdag[2][3], "no edge here");

        Assert.condition(!dag[2][4], "no edge here");
        Assert.condition(!dag[4][2], "no edge here");

        Assert.condition(!dag[1][3], "no edge here");
        Assert.condition(!dag[3][1], "no edge here");

    }

    public void testRule1() throws Exception {

        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);

        pc.setSepset(0, 2, null);
        pc.setSepset(2, 4, null);
        pc.setSepset(0, 4, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.pdag[2][3] = true;

        log.info(Arrays.deepToString(edge.pdag));

        Assert.condition(edge.meekRule1(), "Should have oriented at least one edge");
        boolean[][] dag = edge.getPDag();
        Assert.condition(dag[3][4] && !dag[4][3], "Orient rule 1 should have added an edge between 2 and 3.");

        log.info(Arrays.deepToString(edge.pdag));

        Assert.condition(edge.meekRule1(), "Should have oriented at least one edge");
        dag = edge.getPDag();
        Assert.condition(dag[4][1] && !dag[1][4], "Orient rule 1 should have added an edge between 4 and 1.");

        log.info(Arrays.deepToString(edge.pdag));

        Assert.condition(edge.meekRule1(), "Should have oriented at least one edge");
        dag = edge.getPDag();
        Assert.condition(dag[1][0] && !dag[0][1], "Orient rule 1 should have added an edge between 1 and 0.");

        log.info(Arrays.deepToString(edge.pdag));

        edge.meekRule1();

        log.info(Arrays.deepToString(edge.pdag));

        Assert.condition(!edge.meekRule1(), "Should not have oriented any edges");


    }

    public void testIsDirectedPath() throws Exception {

        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);

        pc.setSepset(0, 2, null);
        pc.setSepset(2, 4, null);
        pc.setSepset(0, 4, null);

        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        boolean[][] dag = new boolean[5][5];
        dag[0][1] = true;
        dag[1][4] = true;
        dag[2][1] = true;
        dag[2][4] = true;
        dag[3][4] = true;


        Assert.condition(edge.isDirectedPath(dag, 0, 4), "Should be a path between 0 and 4");
        Assert.condition(edge.isDirectedPath(dag, 2, 4), "Should be a path between 2 and 4");
        Assert.condition(!edge.isDirectedPath(dag, 1, 3), "Should NOT be a path between 1 and 3");
        Assert.condition(!edge.isDirectedPath(dag, 0, 2), "Should NOT be a path between 0 and 2");
    }

    public void testRule2() throws Exception {
        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);
        pc.addEdge(2, 4);

        pc.setSepset(0, 2, null);
        //pc.setSepset(2,4, null);
        pc.setSepset(0, 4, null);


        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.orientColliders();

        Assert.condition(edge.meekRule2(), "Should have oriented an edge");
        boolean[][] pdag = edge.getPDag();
        Assert.condition(pdag[2][4], "Should have added an edge between 2 and 4");
        Assert.condition(!pdag[4][2], "Should not have added an edge between 4 and 2");
    }

    public void testMeekRule2() throws Exception {
        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);
        pc.addEdge(2, 4);

        pc.setSepset(0, 2, null);
        pc.setSepset(0, 3, null);
        //pc.setSepset(2,4, null);
        pc.setSepset(0, 4, null);


        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.orientColliders();

        log.info(Arrays.deepToString(bn.getDag()));

        Assert.condition(edge.meekRule2(), "Should have oriented an edge");
        boolean[][] pdag = edge.getPDag();
        Assert.condition(pdag[2][4], "Should have added an edge between 2 and 4");
        Assert.condition(!pdag[4][2], "Should not have added an edge between 4 and 2");

        Assert.condition(!edge.meekRule2(), "No more edges to be oriented.");

    }

    public void testMeekRule3() throws Exception {
        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(0, 2);
        pc.addEdge(1, 2);
        pc.addEdge(1, 3);
        pc.addEdge(2, 3);
        pc.addEdge(3, 4);
        pc.addEdge(2, 4);


        Set<Integer> sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(0, 3, sepset);

        sepset = new HashSet<Integer>();
        sepset.add(2);
        pc.setSepset(0, 4, sepset);

        sepset = new HashSet<Integer>();
        sepset.add(3);
        sepset.add(2);
        pc.setSepset(1, 4, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);
        EdgeOrientation edge = new EdgeOrientation(pc);

        edge.orientColliders();

        log.info(Arrays.deepToString(bn.getDag()));

        Assert.condition(edge.meekRule3(), "Should have oriented an edge");
        boolean[][] pdag = edge.getPDag();
        Assert.condition(pdag[2][1], "Should have added an edge between 2 and 4");
        Assert.condition(!pdag[1][2], "Should not have added an edge between 4 and 2");

        Assert.condition(!edge.meekRule3(), "No more edges to be oriented.");

    }

}


