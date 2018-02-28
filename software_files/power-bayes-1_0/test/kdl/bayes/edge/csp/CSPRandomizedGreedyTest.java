/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import junit.framework.TestCase;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.skeleton.FAS;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.HashSet;

public class CSPRandomizedGreedyTest extends TestCase {

    protected static Logger log = Logger.getLogger(CSPRandomizedGreedyTest.class);


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

    public void testOrientEdges() throws Exception {

        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 2);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(2, 4);

        HashSet<Integer> zIdxs = new HashSet<Integer>();
        pc.setSepset(0, 1, zIdxs);
        pc.addConstraint(0, 1, zIdxs, true, 0.65);

        zIdxs.add(2);
        pc.setSepset(0, 3, zIdxs);
        pc.addConstraint(0, 3, zIdxs, true, 0.832);

        pc.setSepset(1, 3, zIdxs);
        pc.addConstraint(1, 3, zIdxs, true, 0.77);

        zIdxs.clear();
        zIdxs.add(2);
        zIdxs.add(4);
        pc.setSepset(0, 4, zIdxs);
        pc.addConstraint(0, 3, zIdxs, true, 0.832);

        zIdxs.clear();
        pc.setSepset(1, 4, zIdxs);

        zIdxs.clear();
        zIdxs.add(2);
        pc.setSepset(3, 4, zIdxs);
        pc.addConstraint(3, 4, zIdxs, true, 0.34);

        // To create a CSPEdgeOrientation Object.

        CSPEdgeOrientation cspEdge = new CSPRandomizedGreedy(instances, pc, 0.5, 2);

        PowerBayesNet bn = cspEdge.orientEdges();

        log.info(GraphUtil.getTetradGraph(bn.getDag()));


    }
}
