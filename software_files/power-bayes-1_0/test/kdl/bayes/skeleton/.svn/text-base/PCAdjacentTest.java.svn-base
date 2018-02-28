/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import junit.framework.TestCase;
import kdl.bayes.skeleton.util.ThresholdModule;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class PCAdjacentTest extends TestCase {

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

    public void testGetCandidates() throws IOException {

        PCAdjacent pc = new PCAdjacent(new Instances(new StringReader(testInput)), new ThresholdModule());
        pc.addEdge(0, 1);
        pc.addEdge(1, 2);
        pc.addEdge(2, 3);

        log.info(Arrays.deepToString(pc.getSkeleton()));

        Set<Integer> adjPath = pc.getAdjPath(1, 3);

        log.info(adjPath);

        List<Integer> testCandidates = pc.getCandidates(1, 3);
        List<Integer> trueSet = new ArrayList<Integer>();
        trueSet.add(2);

        log.info("TEST: " + testCandidates);
        log.info("TRUE: " + trueSet);

        Util.verifyCollections(trueSet, testCandidates);


    }


}
