/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import org.apache.log4j.Logger;

public class Backtracking {

    protected static Logger log = Logger.getLogger(Backtracking.class);

    CSP csp;

    int constraintChecks = 0;

    public Backtracking(CSP csp) {
        this.csp = csp;
    }

    public Assignment search() {
        Assignment emptyAssign = new Assignment();
        return search(emptyAssign);
    }

    /**
     * Implements the Recursive Backtracking algorithm from Figure 5.3 in AI: Modern Approach by
     * Russell and Norvig (2nd edition)
     *
     * @param currAssignment
     * @return
     */
    protected Assignment search(Assignment currAssignment) {

        if (currAssignment.size() == csp.numVars()) {
            return currAssignment;
        }

        Object variable = csp.getUnassignedVariable(currAssignment);

        for (Object value : csp.getOrderedValues(variable)) {
            Assignment testAssignment = new Assignment(currAssignment);
            testAssignment.setVar(variable, value);
            //log.info("Test Assignment: " + testAssignment);
            constraintChecks++;

            if (constraintChecks % 1000 == 0) {
                log.info("Finished " + constraintChecks);
            }

            //If testAssignment is valid then continue searching
            if (csp.isConsistent(testAssignment)) {
                Assignment result = search(testAssignment);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public int getConstraintChecks() {
        return constraintChecks;
    }

}
