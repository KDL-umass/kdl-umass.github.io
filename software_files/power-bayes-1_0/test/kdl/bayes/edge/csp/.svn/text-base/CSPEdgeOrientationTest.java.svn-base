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
import kdl.bayes.util.Assert;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;

public class CSPEdgeOrientationTest extends TestCase {

    protected static Logger log = Logger.getLogger(CSPEdgeOrientationTest.class);


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

    public void testGetInitialOrientation() throws Exception {

        Instances instances = new Instances(new StringReader(testInput2));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(0, 2);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);
        pc.addEdge(1, 4);
        pc.addEdge(3, 4);

        // To create a CSPEdgeOrientation Object.

        CSPEdgeOrientation cspEdge = new CSPGreedyHillClimb(instances, pc);

        PowerBayesNet bn = new PowerBayesNet(instances, cspEdge.getRandomOrientation());

        boolean[][] dag = bn.getDag();

        Assert.condition(!GraphUtil.hasDirectedCycle(dag), "Should be acyclic.");

        log.info(GraphUtil.dagToTetradString(dag));

    }
}
