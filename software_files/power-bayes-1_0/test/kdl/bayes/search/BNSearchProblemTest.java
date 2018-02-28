/**
 * $Id: BNSearchProblemTest.java 273 2009-03-06 16:41:19Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: BNSearchProblemTest.java 273 2009-03-06 16:41:19Z afast $
 */

package kdl.bayes.search;

import junit.framework.TestCase;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * BNSearchProblemTest
 */
public class BNSearchProblemTest extends TestCase {

    protected static Logger log = Logger.getLogger(BNSearchProblemTest.class);
    private boolean[][] dag;
    private PowerBayesNet bnCycleTests;
    private BNSearchProblem problem;
    private Instances instances;
    private String testInput =
            "@relation TestBayesNet\n" +
                    "@attribute A {false,true}\n" +
                    "@attribute B {false,true}\n" +
                    "@attribute C {false,true}\n" +
                    "@data\n" +
                    "true,true,false\n";

    protected static String dagToString(boolean[][] dag) {
        String str = "Begin DAG (" + dag.length + "):\n";
        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag[i].length; j++) {
                str += (dag[i][j] ? "1 " : "0 ");
            }
            str += "\n";
        }
        return str + "end DAG";
    }

    protected static boolean equalDags(boolean[][] dag, boolean[][] otherDag) {
        if (dag.length != otherDag.length) {
            return false;
        }

        if (dag[0].length != otherDag[0].length) {
            return false;
        }

        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag[i].length; j++) {
                if (dag[i][j] != otherDag[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
        instances = new Instances(new StringReader(testInput));

        // initial bnCycleTests: E = { (0,1), (1,2) }
        dag = new boolean[][]{
                {false, true, false},
                {false, false, true},
                {false, false, false}
        };
        bnCycleTests = new PowerBayesNet(instances);
    }

    public void testCycle() {
        problem = new BNSearchProblem(bnCycleTests, dag);
        assertTrue(problem.containsCycle(bnCycleTests, 2, 0));
        assertFalse(problem.containsCycle(bnCycleTests, 0, 2));
    }

    public void testGetSuccessors() {
        problem = new BNSearchProblem(bnCycleTests, dag);

        Set expSuccessors = new HashSet();
        boolean[][] successor;

        // five successors to E = { (0,1), (1,2) }
        // E = { (1,2) }
        // E = { (0,1) }
        // E = { (0,1, (1,2), (0,2) }
        // E = { (1,0), (1,2) }
        // E = { (0,1), (2,1) }
        successor = new boolean[][]{
                {false, false, false},
                {false, false, true},
                {false, false, false}
        };
        expSuccessors.add(successor);

        successor = new boolean[][]{
                {false, true, false},
                {false, false, false},
                {false, false, false}
        };
        expSuccessors.add(successor);

        successor = new boolean[][]{
                {false, true, true},
                {false, false, true},
                {false, false, false}
        };
        expSuccessors.add(successor);

        successor = new boolean[][]{
                {false, false, false},
                {true, false, true},
                {false, false, false}
        };
        expSuccessors.add(successor);

        successor = new boolean[][]{
                {false, true, false},
                {false, false, false},
                {false, true, false}
        };
        expSuccessors.add(successor);


        Collection successors = problem.getSuccessors(problem.getInitialState());
        assertEquals(expSuccessors.size(), successors.size());

        for (Iterator succIter = successors.iterator(); succIter.hasNext();) {
            BNSearchState state = (BNSearchState) succIter.next();
            boolean foundInSucessors = false;
            for (Iterator expSuccIter = expSuccessors.iterator(); expSuccIter.hasNext();) {
                boolean[][] expDag = (boolean[][]) expSuccIter.next();
                if (equalDags(expDag, state.getDag())) {
                    foundInSucessors = true;
                }
            }
            if (!foundInSucessors) {
                log.debug(dagToString(state.getDag()));
            }
            assertTrue(foundInSucessors);
        }
    }

    public void testLongCycleWithEdgeReversal() {
        // add a valid edge, so E = { (0,1), (0,2), (1,2) }
        dag[0][2] = true;
        // reversing edge (0,2) is not okay, should introduce cycle
        problem = new BNSearchProblem(bnCycleTests, dag);
        assertTrue(problem.containsCycle(bnCycleTests, 0, 2, true));
    }

    public void testNoCycle2() {
        // initial bnCycleTests: E = { (1,0), (2,1) }
        boolean[][] anotherDag = new boolean[][]{
                {false, false, false},
                {true, false, false},
                {false, true, false}
        };
        problem = new BNSearchProblem(bnCycleTests, anotherDag);
        assertFalse(problem.containsCycle(bnCycleTests, 2, 0));
        assertTrue(problem.containsCycle(bnCycleTests, 0, 2));
    }
}
