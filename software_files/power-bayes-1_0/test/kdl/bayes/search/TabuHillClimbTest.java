/**
 * $Id: TabuHillClimbTest.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: TabuHillClimbTest.java 237 2008-04-07 16:54:03Z afast $
 */

package kdl.bayes.search;

import junit.framework.TestCase;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;

public class TabuHillClimbTest extends TestCase {
    protected static Logger log = Logger.getLogger(TabuHillClimbTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testSearch() {
        TestSearchProblem problem = new TestSearchProblem();
        TestSearchState state = (TestSearchState) TabuHillClimb.search(problem, 1, 2);
        assertEquals(0, state.getX());
        assertEquals(2, state.getY());
        assertEquals(4, problem.getScore(state), 0.0000001);
    }


    public void testLowMemorySearch() {
        TestSearchProblem problem = new TestSearchProblem();
        TestSearchState state = (TestSearchState) TabuHillClimb.search(problem, 6, 1);
        assertEquals(1, state.getX());
        assertEquals(1, state.getY());
        assertEquals(3, problem.getScore(state), 0.0000001);
    }

    public void testSearchSmallSpace() {
        TestSearchProblem problem = new TestSearchProblem();
        TestSearchState state = (TestSearchState) TabuHillClimb.search(problem, 6, 6);
        assertEquals(0, state.getX());
        assertEquals(2, state.getY());
        assertEquals(4, problem.getScore(state), 0.0000001);
    }
}
