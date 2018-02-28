/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.constraint;

import junit.framework.TestCase;
import kdl.bayes.search.csp.Assignment;
import kdl.bayes.search.csp.BNCSP;
import kdl.bayes.search.csp.Backtracking;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class dSeparationTest extends TestCase {

    protected static Logger log = Logger.getLogger(dSeparationTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testHasSufficientPaths() {
        HashSet<Integer> z = new HashSet<Integer>();
        z.add(1);
        z.add(2);

        dSeparation dSep = new dSeparation(0, 3, z);


        int[][] dag = new int[][]{{0, -1, -1, 0}, {-1, 0, 0, 1}, {-1, 0, 0, 1}, {0, 0, 0, 0}};

        Assert.condition(dSep.hasSufficientPaths(dag, 0, 3, 2), "There should be two paths.");

        dag = new int[][]{{0, -1, -1, 0, 0}, {-1, 0, 0, 1, 0}, {-1, 0, 0, 1, 0}, {0, 0, 0, 0, 0}, {1, 0, 0, 0, 0}};

        Assert.condition(dSep.hasSufficientPaths(dag, 4, 3, 2), "There should be two paths.");

        Assert.condition(dSep.hasSufficientPaths(dag, 4, 2, 1), "There should be one paths.");

        Assert.condition(!dSep.hasSufficientPaths(dag, 3, 2, 3), "There are not 3 paths.");


    }

    public void testIsValid() {

        List<String> varNames = new ArrayList<String>();
        varNames.add("one");
        varNames.add("two");
        varNames.add("five");

        Set<Constraint> constraints = new HashSet<Constraint>();

        HashSet<Integer> z = new HashSet<Integer>();
        z.add(0);

        dSeparation dSep = new dSeparation(1, 2, z);
        constraints.add(dSep);

        BNCSP csp = new BNCSP(varNames, constraints);
        Assignment testAssignment = new Assignment();
        Assert.condition(dSep.isValid(testAssignment, csp), "No commitments means valid");

        csp.reset();
        testAssignment.setVar(5, "E");
        Assert.condition(dSep.isValid(testAssignment, csp), "should be valid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "L");
        Assert.condition(!dSep.isValid(testAssignment, csp), "should be invalid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "R");
        Assert.condition(!dSep.isValid(testAssignment, csp), "should be invalid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "E");
        testAssignment.setVar(1, "L");
        Assert.condition(dSep.isValid(testAssignment, csp), "should be valid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "E");
        testAssignment.setVar(1, "R");
        testAssignment.setVar(2, "R");
        Assert.condition(dSep.isValid(testAssignment, csp), "should be valid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(1, "L");
        testAssignment.setVar(2, "R");
        Assert.condition(dSep.isValid(testAssignment, csp), "should be valid");

        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "E");
        testAssignment.setVar(1, "L");
        testAssignment.setVar(2, "R");
        Assert.condition(dSep.isValid(testAssignment, csp), "should be valid");


        csp.reset();
        testAssignment = new Assignment();
        testAssignment.setVar(5, "E");
        testAssignment.setVar(1, "E");
        testAssignment.setVar(2, "R");
        Assert.condition(!dSep.isValid(testAssignment, csp), "should be invalid");
    }

    /**
     * This is the problem case from cancer:
     * 0 -- 1 --> 3 -- 2 ( 0 -- 2 is Empty)
     * This should be invalid but returns valid. There is no assignment of the edge
     * between 2 and 3 which results 1_||2 | 0
     */
    public void testProblemCase() {
        List<String> varNames = new ArrayList<String>();
        varNames.add("one");
        varNames.add("two");
        varNames.add("three");
        varNames.add("six");

        Set<Constraint> constraints = new HashSet<Constraint>();

        HashSet<Integer> z = new HashSet<Integer>();
        z.add(0);

        dSeparation dSep = new dSeparation(1, 2, z);
        constraints.add(dSep);

        BNCSP csp = new BNCSP(varNames, constraints);
        Assignment testAssignment = new Assignment();

        testAssignment.setVar(2, "E");
        testAssignment.setVar(7, "R");
        testAssignment.setVar(3, "E");
        testAssignment.setVar(6, "E");

        Assert.condition(!dSep.isValid(testAssignment, csp), "should be invalid");

    }

    public void testDivergingDsep() {

        List<String> varNames = new ArrayList<String>();
        varNames.add("cloudy");
        varNames.add("sprinkler");
        varNames.add("rain");
        varNames.add("wet-grass");

        Set<Constraint> constraints = new HashSet<Constraint>();
        HashSet<Integer> z = new HashSet<Integer>();
        z.add(0);

        dSeparation constraint1 = new dSeparation(1, 2, z);

        constraints.add(constraint1);

        HashSet<Integer> z2 = new HashSet<Integer>();
        z2.add(1);
        z2.add(2);

        dSeparation constraint2 = new dSeparation(0, 3, z2);

        constraints.add(constraint2);

        BNCSP csp = new BNCSP(varNames, constraints);

        Backtracking bt = new Backtracking(csp);

        Assignment testAssignment = new Assignment();
        testAssignment.setVar(1, "R");
        testAssignment.setVar(2, "R");
        testAssignment.setVar(6, "E");
        testAssignment.setVar(3, "E");
        testAssignment.setVar(7, "R");
        testAssignment.setVar(11, "R");

        Assert.condition(constraint1.isValid(testAssignment, csp), "This constraint should be valid");
        Assert.condition(constraint2.isValid(testAssignment, csp), "This constraint should be valid");
        Assert.condition(new Acyclicity().isValid(testAssignment, csp), "This constraint should be valid");


    }


}
