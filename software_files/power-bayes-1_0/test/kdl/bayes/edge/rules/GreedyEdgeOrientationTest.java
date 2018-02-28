/**
 * $
 *
 * Part of the open-source PowerBayes system
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
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class GreedyEdgeOrientationTest extends TestCase {

    protected static Logger log = Logger.getLogger(ColliderTest.class);

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

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testOrientColliders() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        pc.setSepset(0, 2, null);
        pc.setSepset(1, 3, null);

        PowerBayesNet bn = new PowerBayesNet(instances);
        GreedyEdgeOrientation edge = new GreedyEdgeOrientation(bn, pc);

        edge.orientColliders();
        bn = edge.getBayesNet();
        boolean[][] dag = bn.getDag();

        Assert.condition(!dag[1][0], "Edge Not Required here.");
        Assert.condition(!dag[2][1], "Edge Not Required here.");
        Assert.condition(dag[1][2], "Edge Required here.");
        Assert.condition(dag[3][2], "Edge Required here.");
    }

    public void testUpdateQueue() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        pc.setSepset(0, 2, null);
        pc.setSepset(1, 3, null);

        PowerBayesNet bn = new PowerBayesNet(instances);
        GreedyEdgeOrientation edge = new GreedyEdgeOrientation(bn, pc);

        Set<List<Integer>> colliders = pc.findColliders();
        PriorityQueue<Collider> queue = edge.initQueue(colliders);

        Collider collider1 = queue.poll();
        collider1.apply(bn);
        Assert.condition(!queue.isEmpty(), "Queue should not be empty.");
        Collider collider2 = queue.peek();
        log.info(collider2);
        edge.updateQueue(queue, bn);
        Assert.condition(queue.isEmpty(), "Queue should be empty.");


    }

}
