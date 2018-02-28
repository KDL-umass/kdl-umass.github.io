/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import edu.cmu.tetrad.graph.Edge;
import edu.cmu.tetrad.graph.Endpoint;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import junit.framework.TestCase;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.search.csp.Assignment;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraphUtilTest extends TestCase {


    protected static Logger log = Logger.getLogger(GraphUtilTest.class);


    private String testInput =
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

    public void testZBlocked() throws Exception {

        boolean[][] dag = new boolean[][]{{false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, true, true},
                {false, false, false, false, false},
                {false, false, false, false, false}};

        Set<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(4);

        boolean[] evidence = GraphUtil.zBlocked(dag, zIdxs);

        Assert.condition(evidence[4], "z should be marked");
        Assert.condition(evidence[0], "ancestors of z should be marked");
        Assert.condition(evidence[1], "ancestors of z should be marked");
        Assert.condition(evidence[2], "ancestors of z should be marked");
        Assert.condition(!evidence[3], "non-ancestors should not be marked");

        zIdxs.clear();
        zIdxs.add(0);

        evidence = GraphUtil.zBlocked(dag, zIdxs);

        Assert.condition(evidence[0], "z should be marked");
        Assert.condition(!evidence[3], "non-ancestors should not be marked");
        Assert.condition(!evidence[4], "non-ancestors should not be marked");
        Assert.condition(!evidence[1], "non-ancestors should not be marked");
        Assert.condition(!evidence[2], "non-ancestors should not be marked");

        zIdxs.clear();
        zIdxs.add(4);

        PowerBayesNet bn = new PowerBayesNet(new Instances(new StringReader(testInput)), dag);

        evidence = GraphUtil.zBlocked(bn, zIdxs);

        Assert.condition(evidence[4], "z should be marked");
        Assert.condition(evidence[0], "ancestors of z should be marked");
        Assert.condition(evidence[1], "ancestors of z should be marked");
        Assert.condition(evidence[2], "ancestors of z should be marked");
        Assert.condition(!evidence[3], "non-ancestors should not be marked");

        zIdxs.clear();
        zIdxs.add(0);

        evidence = GraphUtil.zBlocked(bn, zIdxs);

        Assert.condition(evidence[0], "z should be marked");
        Assert.condition(!evidence[3], "non-ancestors should not be marked");
        Assert.condition(!evidence[4], "non-ancestors should not be marked");
        Assert.condition(!evidence[1], "non-ancestors should not be marked");
        Assert.condition(!evidence[2], "non-ancestors should not be marked");

    }

    public void testDSeparated() {
        boolean[][] dag = new boolean[][]{{false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, true, true},
                {false, false, false, false, false},
                {false, false, false, false, false}};

        Set<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(2);

        Set<Integer> actual = GraphUtil.findDSeparatedNodes(dag, 1, zIdxs);

        Set<Integer> expected = new HashSet<Integer>();
        expected.add(3);
        expected.add(4);

        Util.verifyCollections(expected, actual);

    }

    public void testDSeparated2() {
        boolean[][] dag = new boolean[][]{{false, true, false},
                {false, false, true},
                {false, false, false}};

        Set<Integer> actual = GraphUtil.findDSeparatedNodes(dag, 0, new HashSet<Integer>());

        Set<Integer> expected = new HashSet<Integer>();
        //expected.add(2);

        log.info("Found: " + Arrays.deepToString(actual.toArray(new Integer[actual.size()])));

        Util.verifyCollections(expected, actual);

    }

    public void testEqualDag() {
        boolean[][] dag1 = new boolean[][]{{false, true, false}, {false, false, true}, {false, false, false}};
        boolean[][] dag3 = new boolean[][]{{false, true, false}, {false, false, true}, {false, false, false}};

        Assert.condition(GraphUtil.equalDag(dag1, dag3), "Dag should be equal");

        boolean[][] dag2 = new boolean[][]{{false, true, true}, {false, false, false}, {false, false, false}};

        Assert.condition(!GraphUtil.equalDag(dag1, dag2), "Dag should NOT be equal");

    }

    public void testHasDirectedCycle() {
        int[][] dag = new int[][]{{0, 1, 0}, {0, 0, 1}, {1, 0, 0}};

        Assert.condition(GraphUtil.hasDirectedCycle(dag), "This graph has a cycle: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 1, -1}, {0, 0, 1}, {-1, 0, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}, {0, 1, 0, 0}};

        Assert.condition(GraphUtil.hasDirectedCycle(dag), "This graph has a cycle: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, -1, -1, 0}, {-1, 0, 1, 0}, {-1, 0, 0, 1}, {0, 1, 0, 0}};

        Assert.condition(GraphUtil.hasDirectedCycle(dag), "This graph has a cycle: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 0, 0}, {0, 0, -1}, {1, -1, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 1, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 0, 0}, {1, 0, 1}, {1, 0, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 1, 0}, {0, 0, 0}, {1, 1, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));

        dag = new int[][]{{0, 1, 1}, {0, 0, 1}, {0, 0, 0}};

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "This graph has no cycles: " + GraphUtil.dagToString(dag));
    }

    public void testGetAssignment() {

        boolean[][] dag = new boolean[][]{{false, true, true}, {false, false, true}, {false, false, false}};

        Assignment testAssignment = GraphUtil.getAssignmentForDag(dag);

        Assert.condition("R".equals(testAssignment.getValue(1)), "The edge should point from 0->1");
        Assert.condition("R".equals(testAssignment.getValue(2)), "The edge should point from 0->2");
        Assert.condition("R".equals(testAssignment.getValue(5)), "The edge should point from 0->2");

        dag = new boolean[][]{{false, true, false, false}, {false, false, true, false}, {false, true, false, false}, {false, false, true, false}};

        testAssignment = GraphUtil.getAssignmentForDag(dag);

        Assert.condition("R".equals(testAssignment.getValue(1)), "The edge should point from 0->1");
        Assert.condition(!testAssignment.hasVar(5), "This edge should be empty due to bi-direction.");
        Assert.condition("L".equals(testAssignment.getValue(11)), "The edge should point from 3->2");


    }

    public void testCSeparated() {
        boolean[][] dag = new boolean[][]{{false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, true, true},
                {false, false, false, false, false},
                {false, false, false, false, false}};


        HashSet<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(2);

        Set<Integer> actual = GraphUtil.findCSeparatedNodes(dag, 0, zIdxs);
        log.info(Arrays.deepToString(actual.toArray(new Integer[actual.size()])));

        Set<Integer> expected = new HashSet<Integer>();
        expected.add(3);
        expected.add(4);

        Util.verifyCollections(expected, actual);

    }

    public void testCSeparated2() {
        //THis tests the problem case from the cancer db
        boolean[][] dag = new boolean[][]{{false, true, false, false}, {true, false, false, true}, {false, false, false, true}, {false, false, true, false}};

        HashSet<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(0);

        Set<Integer> actual = GraphUtil.findCSeparatedNodes(dag, 1, zIdxs);
        log.info(Arrays.deepToString(actual.toArray(new Integer[actual.size()])));

        Set<Integer> expected = new HashSet<Integer>();

        Util.verifyCollections(expected, actual);

    }

    public void testCSeparated3() {
        boolean[][] dag = new boolean[][]{{false, false, false}, {false, false, false}, {false, false, false}};

        HashSet<Integer> zIdxs = new HashSet<Integer>();
        zIdxs.add(1);

        Set<Integer> actual = GraphUtil.findCSeparatedNodes(dag, 0, zIdxs);
        log.info(Arrays.deepToString(actual.toArray(new Integer[actual.size()])));

        Set<Integer> expected = new HashSet<Integer>();
        expected.add(2);

        Util.verifyCollections(expected, actual);

    }

    public void testTopologicalSort() {
        boolean[][] dag = new boolean[][]{{false, true, true, false}, {false, false, false, true}, {false, false, false, true}, {false, false, false, false}};
        List<Integer> sortedList = GraphUtil.topologicalSort(dag);

        int zeroIdx = sortedList.indexOf((Integer) 0);
        int oneIdx = sortedList.indexOf((Integer) 1);
        int twoIdx = sortedList.indexOf((Integer) 2);
        int threeIdx = sortedList.indexOf((Integer) 3);

        log.info(Arrays.deepToString(sortedList.toArray()));

        Assert.condition(zeroIdx < oneIdx, "zero is before 1");
        Assert.condition(zeroIdx < twoIdx, "zero is before 2");
        Assert.condition(oneIdx < threeIdx, "1 is before 3");
        Assert.condition(twoIdx < threeIdx, "2 is before 3");

        dag = new boolean[][]{{false, false, false, false}, {true, false, false, false}, {true, false, false, false}, {true, true, false, false}};
        sortedList = GraphUtil.topologicalSort(dag);


        zeroIdx = sortedList.indexOf((Integer) 0);
        oneIdx = sortedList.indexOf((Integer) 1);
        twoIdx = sortedList.indexOf((Integer) 2);
        threeIdx = sortedList.indexOf((Integer) 3);

        log.info(Arrays.deepToString(sortedList.toArray()));

        Assert.condition(zeroIdx > oneIdx, "zero is after 1");
        Assert.condition(zeroIdx > twoIdx, "zero is after 2");
        Assert.condition(oneIdx > threeIdx, "1 is after 3");
        Assert.condition(twoIdx > threeIdx, "2 is after 3");

        dag = new boolean[][]{{false, false, true, false}, {false, false, true, false}, {false, false, false, true}, {false, false, false, false}};
        sortedList = GraphUtil.topologicalSort(dag);

        zeroIdx = sortedList.indexOf((Integer) 0);
        oneIdx = sortedList.indexOf((Integer) 1);
        twoIdx = sortedList.indexOf((Integer) 2);
        threeIdx = sortedList.indexOf((Integer) 3);

        log.info(Arrays.deepToString(sortedList.toArray()));

        Assert.condition(zeroIdx < twoIdx, "zero is before 2");
        Assert.condition(zeroIdx < threeIdx, "zero is after 3");
        Assert.condition(oneIdx < threeIdx, "1 is before a 3");
        Assert.condition(twoIdx < threeIdx, "2 is after 3");

        dag = new boolean[][]{{false, true, true, true, false}, {false, false, false, false, false}, {false, true, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}};
        sortedList = GraphUtil.topologicalSort(dag);
        log.info(Arrays.deepToString(sortedList.toArray()));
        Assert.condition(sortedList.size() == dag.length, "List should not contains more variables than appear in the graph.");

        zeroIdx = sortedList.indexOf((Integer) 0);
        oneIdx = sortedList.indexOf((Integer) 1);
        twoIdx = sortedList.indexOf((Integer) 2);
        threeIdx = sortedList.indexOf((Integer) 3);
        int fourIdx = sortedList.indexOf((Integer) 4);

        Assert.condition(oneIdx > twoIdx, "2 is a parent of 1");
        Assert.condition(oneIdx > zeroIdx, "0 is a parent of 1");
        Assert.condition(oneIdx > fourIdx, "0 is a parent of 1");
        Assert.condition(threeIdx > twoIdx, "2 is a parent of 3");
        Assert.condition(threeIdx > zeroIdx, "0 is a parent of 3");
        Assert.condition(threeIdx > fourIdx, "0 is a parent of 3");
        Assert.condition(twoIdx > zeroIdx, "0 is a parent of 2");
        Assert.condition(twoIdx > fourIdx, "4 is a parent of 2");
    }

    public void testGetParentSets() {
        boolean[][] dag = new boolean[][]{{false, true, true, false}, {false, false, false, true}, {false, false, false, true}, {false, false, false, false}};
        List<Set<Integer>> parentSets = GraphUtil.getParentSets(dag);

        log.info(Arrays.deepToString(parentSets.toArray()));

        Set<Integer> zeroSet = new HashSet<Integer>();
        Set<Integer> oneSet = new HashSet<Integer>();
        oneSet.add(0);
        Set<Integer> twoSet = new HashSet<Integer>();
        twoSet.add(0);
        Set<Integer> threeSet = new HashSet<Integer>();
        threeSet.add(1);
        threeSet.add(2);

        Util.verifyCollections(zeroSet, parentSets.get(0));
        Util.verifyCollections(oneSet, parentSets.get(1));
        Util.verifyCollections(twoSet, parentSets.get(2));
        Util.verifyCollections(threeSet, parentSets.get(3));

        dag = new boolean[][]{{false, true, true, false}, {false, false, true, false}, {false, false, false, true}, {false, false, false, false}};
        parentSets = GraphUtil.getParentSets(dag);

        Set<Integer> parentSet3 = parentSets.get(3);
        Assert.condition(parentSet3.size() == 1, "Should contain 2");
        log.info(Arrays.deepToString(parentSets.toArray()));

    }

    public void testOrderEdges() {
        boolean[][] dag = new boolean[][]{{false, true, true, false}, {false, false, false, true}, {false, false, false, true}, {false, false, false, false}};
        List<Integer> edgeOrder = GraphUtil.orderEdges(dag);

        log.info(Arrays.deepToString(edgeOrder.toArray()));

        Assert.condition(edgeOrder.indexOf(1) == 0, "First edge");
        Assert.condition(edgeOrder.indexOf(2) == 1, "second edge");
        Assert.condition(edgeOrder.indexOf(7) == 3, "third edge");
        Assert.condition(edgeOrder.indexOf(11) == 2, "Fourth edge");

        dag = new boolean[][]{{false, false, false, false}, {false, false, false, true}, {false, false, false, false}, {false, false, true, false}};

        edgeOrder = GraphUtil.orderEdges(dag);

        log.info(Arrays.deepToString(edgeOrder.toArray()));


    }

    public void testCompletedPDAG() {
        boolean[][] dag1 = new boolean[][]{{false, true, true, false}, {false, false, false, true}, {false, false, false, true}, {false, false, false, false}};
        boolean[][] dag2 = new boolean[][]{{false, false, true, false}, {true, false, false, true}, {false, false, false, true}, {false, false, false, false}};
        boolean[][] dag3 = new boolean[][]{{false, true, false, false}, {false, false, false, true}, {true, false, false, true}, {false, false, false, false}};

        boolean[][] pdag = GraphUtil.getCompletedPDAG(dag1);

        Assert.condition(pdag[0][1] && pdag[1][0], "0--1 is reversible.");
        Assert.condition(pdag[0][2] && pdag[2][0], "0--2 is reversible.");
        Assert.condition(pdag[1][3] && !pdag[3][1], "1--3 is compelled.");
        Assert.condition(pdag[2][3] && !pdag[3][2], "2--3 is compelled.");

        pdag = GraphUtil.getCompletedPDAG(dag2);

        Assert.condition(pdag[0][1] && pdag[1][0], "0--1 is reversible.");
        Assert.condition(pdag[0][2] && pdag[2][0], "0--2 is reversible.");
        Assert.condition(pdag[1][3] && !pdag[3][1], "1--3 is compelled.");
        Assert.condition(pdag[2][3] && !pdag[3][2], "2--3 is compelled.");

        pdag = GraphUtil.getCompletedPDAG(dag3);

        Assert.condition(pdag[0][1] && pdag[1][0], "0--1 is reversible.");
        Assert.condition(pdag[0][2] && pdag[2][0], "0--2 is reversible.");
        Assert.condition(pdag[1][3] && !pdag[3][1], "1--3 is compelled.");
        Assert.condition(pdag[2][3] && !pdag[3][2], "2--3 is compelled.");

        boolean[][] dag4 = new boolean[][]{{false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {false, false, false, false}};

        pdag = GraphUtil.getCompletedPDAG(dag4);

        Assert.condition(!(pdag[0][1] && pdag[1][0]), "0--1 is compelled.");
        Assert.condition(!(pdag[0][2] && pdag[2][0]), "0--2 is compelled.");
        Assert.condition(pdag[1][3] && !pdag[3][1], "1--3 is compelled.");
        Assert.condition(pdag[2][3] && !pdag[3][2], "2--3 is compelled.");


        boolean[][] dag5 = new boolean[][]{{false, false, false}, {true, false, false}, {false, false, false}};

        pdag = GraphUtil.getCompletedPDAG(dag5);

        Assert.condition((pdag[0][1] && pdag[1][0]), "0--1 is uncompelled.");

    }

    public void testConsistentDag() {

        boolean[][] pdag = new boolean[][]{{false, true, true, false}, {true, false, false, true}, {true, false, false, true}, {false, false, false, false}};

        boolean[][] cdag = GraphUtil.getConsistentDag(pdag);

        log.info(GraphUtil.dagToString(cdag));

        Assert.condition((cdag[0][1] && !cdag[1][0]) || (cdag[1][0] && !cdag[0][1]), " 0--1 should now be directed.");
        Assert.condition((cdag[0][2] && !cdag[2][0]) || (cdag[2][0] && !cdag[0][2]), " 0--2 should now be directed.");
        Assert.condition(!(cdag[1][0] && cdag[2][0]), " Should not create a new V");

        //From Chickering 2002 "Learning Equivalence class..." Figure 2a.
        pdag = new boolean[][]{{false, false, true, false}, {false, false, false, true}, {true, false, false, true}, {false, true, false, false}};
        cdag = GraphUtil.getConsistentDag(pdag);
        log.info(GraphUtil.dagToString(cdag));

        Assert.condition((cdag[3][1] && !cdag[1][3]) || (cdag[1][3] && !cdag[3][1]), " 1--3 should now be directed.");
        Assert.condition((cdag[0][2] && !cdag[2][0]) || (cdag[2][0] && !cdag[0][2]), " 0--2 should now be directed.");
        Assert.condition(!(cdag[1][3] && cdag[2][3]), " Should not create a new V");

        //From Chickering 2002 "Learning Equivalence class..." Figure 2c.
        pdag = new boolean[][]{{false, false, true, false}, {false, false, false, true}, {false, false, false, true}, {false, false, true, false}};
        cdag = GraphUtil.getConsistentDag(pdag);
        if (cdag != null) {
            log.info(GraphUtil.dagToString(cdag));
        }
        Assert.condition(cdag == null, "No consistent extension exists for this graph.");


    }

    public void testGetTetradGraph() {

        boolean[][] pdag = new boolean[][]{{false, true, true, false}, {true, false, false, true}, {true, false, false, true}, {false, false, false, false}};
        Graph graph = GraphUtil.getTetradGraph(pdag);

        Node n0 = graph.getNode("0");
        Node n1 = graph.getNode("1");
        Node n2 = graph.getNode("2");
        Node n3 = graph.getNode("3");

        Assert.condition(n0 != null && n1 != null && n2 != null && n3 != null, "All nodes should exist.");

        Edge n0n1 = graph.getEdge(n0, n1);
        Assert.condition(n0n1.getProximalEndpoint(n0) == Endpoint.TAIL && n0n1.getProximalEndpoint(n1) == Endpoint.TAIL, "Edge should be undirected.");

        Edge n0n2 = graph.getEdge(n0, n2);
        Assert.condition(n0n2.getProximalEndpoint(n0) == Endpoint.TAIL && n0n2.getProximalEndpoint(n2) == Endpoint.TAIL, "Edge should be undirected.");

        Edge n1n3 = graph.getEdge(n1, n3);
        Assert.condition(n1n3.getProximalEndpoint(n1) == Endpoint.TAIL && n1n3.getProximalEndpoint(n3) == Endpoint.ARROW, "Edge should be directed from 1 to 3.");

        Edge n2n3 = graph.getEdge(n2, n3);
        Assert.condition(n2n3.getProximalEndpoint(n2) == Endpoint.TAIL && n2n3.getProximalEndpoint(n3) == Endpoint.ARROW, "Edge should be directed from 1 to 3.");

        Assert.condition(graph.getEdge(n0, n3) == null, "No edge between 0 and 3.");
        Assert.condition(graph.getEdge(n1, n2) == null, "No edge between 1 and 2.");

    }

    public void testGetAncestors() {
        boolean[][] dag = new boolean[][]{{false, true, true, true, false}, {false, false, false, false, false}, {false, true, false, true, false}, {false, false, false, false, false}, {false, true, true, true, false}};
        Set<Integer> ancestors = GraphUtil.getAncestors(dag, 1);
        Assert.condition(ancestors.contains(0), "Ancestors must contain 0");
        Assert.condition(ancestors.contains(4), "Ancestors must contain 4");
        Assert.condition(ancestors.contains(2), "Ancestors must contain 2");

        ancestors = GraphUtil.getAncestors(dag, 0);
        Assert.condition(ancestors.size() == 1, "0 has only itself.");

        dag = new boolean[][]{{false, true, false, false, false}, {false, false, true, false, false}, {false, false, false, true, false}, {false, false, false, false, true}, {false, false, false, false, false}};
        ancestors = GraphUtil.getAncestors(dag, 4);

        Assert.condition(ancestors.size() == 5, "Ancestors must contain all nodes.");

    }

    public void testGetAncestorSubgraph() {
        boolean[][] dag = new boolean[][]{{false, false, false, false, false}, {false, false, false, false, false}, {true, true, false, true, false}, {false, false, false, false, false}, {true, true, true, true, false}};
        boolean[][] subgraph = GraphUtil.getAncestorSubgraph(dag, 1, 3);

        for (int j = 0; j < dag.length; j++) {
            Assert.condition(!subgraph[0][j] && !subgraph[j][0], "0 should be disconnected in the graph.");
        }

        dag = new boolean[][]{{false, true, true, false}, {false, false, true, false}, {false, false, false, true}, {false, false, false, false}};
        subgraph = GraphUtil.getAncestorSubgraph(dag, 1, 3);
        Assert.condition(subgraph[2][3], "Should be an edge between 2 and 3");
    }

    public void testFindMinimalDSeparators() {
        boolean[][] dag = new boolean[][]{{false, true, true, false}, {false, false, true, false}, {false, false, false, true}, {false, false, false, false}};

        Set<Integer> dSep = GraphUtil.findMinimalDSeparators(dag, 1, 3);
        log.info(Arrays.deepToString(dSep.toArray()));
        Assert.condition(dSep.size() == 1, "Should only contain a single node.");
        Assert.condition(dSep.contains(2), "Should only contain a single node,2.");

    }

    public void testFindColliders() {
        boolean[][] dag = new boolean[][]{{false, true, true, false}, {true, false, false, true}, {true, false, false, true}, {false, false, false, false}};

        Set<List<Integer>> colliders = GraphUtil.findColliders(dag);

        Assert.condition(colliders.size() == 1, "Should find only one collider");
        List<Integer> collider = colliders.iterator().next();
        Assert.condition(collider.get(0) == 1 && collider.get(1) == 2 && collider.get(2) == 3, "Collider should be 1 --> 3 <-- 2");

        dag = new boolean[][]{{false, false, false, false}, {true, false, true, true}, {true, true, false, true}, {false, false, false, false}};
        colliders = GraphUtil.findColliders(dag);
        Assert.condition(colliders.size() == 0, "Should find no colliders");

        dag = new boolean[][]{{false, false, false, false}, {true, false, false, true}, {true, false, false, true}, {false, false, false, false}};
        colliders = GraphUtil.findColliders(dag);
        Assert.condition(colliders.size() == 2, "Should find two colliders");


    }

}
