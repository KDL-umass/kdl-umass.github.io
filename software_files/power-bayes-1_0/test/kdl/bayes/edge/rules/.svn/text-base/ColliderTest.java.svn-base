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
import java.util.PriorityQueue;

public class ColliderTest extends TestCase {

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

    public void testCompareTo() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        FAS pc = new FAS(instances, new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

//        pc.setSepset(0, 2, null);
//
//        Set<Integer> sepset = new HashSet<Integer>();
//        sepset.add(2);
//        pc.setSepset(1, 3, sepset);

        PowerBayesNet bn = new PowerBayesNet(instances);

        Collider collider1 = new Collider(0, 1, 2);
        Collider collider2 = new Collider(1, 2, 3);

        collider1.updateScore(bn);
        double score1 = collider1.getScore();

        collider2.updateScore(bn);
        double score2 = collider2.getScore();

        log.info("Score 1: " + score1);
        log.info("Score 2: " + score2);

        log.info("Compare:" + collider1.compareTo(collider2));
        log.info("Compare:" + collider2.compareTo(collider1));

        PriorityQueue<Collider> queue = new PriorityQueue<Collider>();
        queue.add(collider1);
        queue.add(collider2);

        Assert.condition(collider2.compareTo(queue.peek()) == 0, "First score out should equal collider2");

        while (!queue.isEmpty()) {
            Collider c = queue.poll();
            log.info(c.getScore());
        }

    }


}
