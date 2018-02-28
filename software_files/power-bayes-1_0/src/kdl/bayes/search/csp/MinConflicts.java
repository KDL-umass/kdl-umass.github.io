/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import org.apache.log4j.Logger;

import java.util.Random;

public class MinConflicts {

    protected static Logger log = Logger.getLogger(MinConflicts.class);

    CSP csp;

    int constraintChecks = 0;

    Random random;

    public MinConflicts(CSP csp) {
        this.csp = csp;
        random = new Random();
    }

    /**
     * Implements the Min-Conflicts algorithm from Figure 5.8 in AI: Modern Approach by
     * Russell and Norvig (2nd edition)
     *
     * @param currAssignment
     * @return
     */
    public Assignment search(int numSteps) {

        Assignment current = csp.getCompleteAssignment();

        //Assert.condition(!GraphUtil.hasDirectedCycle(((BNCSP)csp).getDag(current)), "Complete assignment should not contain a cycle." );

        int numConflicts = csp.numConflicts(current);

        log.info("Initial Conflicts: " + numConflicts);

        if (numConflicts == 0) {
            return current;
        }

        Assignment bestAssignment = current;

        for (int i = 0; i < numSteps; i++) {

            log.info("Num Steps: " + i + "(" + numConflicts + ")");

            Object var = csp.getRandomVariable();

            Object bestValue = current.getValue(var);
            int minConflicts = Integer.MAX_VALUE;

            for (Object value : csp.getOrderedValues(var)) {
                current.setVar(var, value);
                int conflicts = csp.numConflicts(current);

                //log.info("Updating [" + var + "," + value + "]" + "(" + conflicts + ")");

                if (conflicts < minConflicts) {
                    minConflicts = conflicts;
                    bestValue = value;
                }
            }

            current.setVar(var, bestValue);

            if (minConflicts == 0) {
                return current;
            }

            if (minConflicts < numConflicts) {
                bestAssignment = current;
                numConflicts = minConflicts;
            }
        }
        return bestAssignment;

    }

    public int getConstraintChecks() {
        return constraintChecks;
    }

}
