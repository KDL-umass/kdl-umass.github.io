/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import junit.framework.TestCase;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;

public class CSPStateTest extends TestCase {

    protected static Logger log = Logger.getLogger(CSPRandomizedGreedy.class);


    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    /**
     * Test to confirm that the minimization actually works.
     */
    public void testCSPState() {

        boolean[][] dag = new boolean[][]{{true, false, false}, {false, false, true}, {false, false, false}};

        CSPState state1 = new CSPState(dag, 45);
        CSPState state2 = new CSPState(dag, 37);

        Assert.condition(state1.compareTo(state2) > 0, "State 2 should be greater than state1");
        Assert.condition(state2.compareTo(state1) < 0, "State 2 should be greater than state1");

    }

    public void testEquals() {

        boolean[][] dag = new boolean[][]{{false, true, false}, {false, false, true}, {false, false, false}};

        CSPState state1 = new CSPState(dag, 45);
        CSPState state2 = new CSPState(dag, 37);

        Assert.condition(!state1.equals(state2), "State 1 and State 2 are not equal");

        dag = new boolean[][]{{false, true, true}, {false, false, false}, {false, false, false}};
        CSPState state3 = new CSPState(dag, 45);

        Assert.condition(!state1.equals(state3), "State 1 and state 3 are not equal");

        CSPState state4 = new CSPState(dag, 45);

        Assert.condition(state3.equals(state4), "State 3 and state 4 are equal");


    }


    public void testHashCode() {
        boolean[][] dag = new boolean[][]{{false, true, false}, {false, false, true}, {false, false, false}};

        CSPState state1 = new CSPState(dag, 45);
        CSPState state2 = new CSPState(dag, 37);

        Assert.condition(state1.hashCode() != state2.hashCode(), "State 1 and State 2 are not equal");

        dag = new boolean[][]{{false, true, true}, {false, false, false}, {false, false, false}};
        CSPState state3 = new CSPState(dag, 45);

        Assert.condition(state1.hashCode() != state3.hashCode(), "State 1 and state 3 are not equal");

        CSPState state4 = new CSPState(dag, 45);

        Assert.condition(state4.hashCode() == state3.hashCode(), "State 3 and state 4 are equal");

    }
}
