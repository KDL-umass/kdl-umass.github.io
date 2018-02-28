/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import kdl.bayes.util.constraint.Constraint;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class BranchAndBound {

    protected static Logger log = Logger.getLogger(BranchAndBound.class);

    CSP csp;

    int constraintChecks = 0;

    DecimalFormat df = new DecimalFormat("#.###");

    public BranchAndBound(CSP csp) {
        this.csp = csp;
    }

    public Set<Assignment> search(double N, double S) {
        return search(N, S, new Assignment());
    }

    /**
     * Runs branch-and-bound with the given parameters
     *
     * @param N - initial necessary bound, will get smaller as we find closer solutions
     * @param S - sufficient bound, will not change, 0 indicates all constraints can be satisfied.
     * @return
     */
    public Set<Assignment> search(double N, double S, Assignment initialAssignment) {

        log.info("Starting with necessary bound: " + N); //N should correspond to the empty network.
        log.info("Starting with sufficient bound: " + S); //N should correspond to the empty network.
        //log.info("Initial Assignment: " + initialAssignment);

        Set<Assignment> bestStates = new HashSet<Assignment>();

        Stack<Assignment> stateList = new Stack<Assignment>();
        stateList.push(initialAssignment);

        int numPruned = 0;
        int nodesExpanded = 0;
        double conflicts = 0.0;

        while (!stateList.isEmpty()) {
            Assignment currentAssignment = stateList.pop();
            nodesExpanded++;

            //log.info(N);
            conflicts = csp.getConflictWeight(currentAssignment);
            //conflicts = csp.numConflicts(currentAssignment);
            constraintChecks++;

            if (nodesExpanded % 10000 == 0) {
                log.info("tried " + nodesExpanded + " states.");
            }

            if (Double.compare(conflicts, N) > 0) {  //This branch is hopeless, so backtrack.
                //log.info("Pruning branch." + currentAssignment);

                numPruned++;
                continue;
            }

            //Got to the bottom of the tree, so do bookeeping and continue
            if (currentAssignment.size() == csp.numVars()) {

                //log.info("Hit bottom.");

                if (Double.compare(conflicts, 0) == 0) {
                    log.info("Clearing best states for (" + conflicts + ") "); //+ currentAssignment);
                    log.info("Num pruned: " + numPruned);
                    bestStates = new HashSet<Assignment>();
                    bestStates.add(currentAssignment);
                    return bestStates;
                }


                if (Double.compare(conflicts, N) == 0) {
                    if (!bestStates.contains(currentAssignment)) {
                        bestStates.add(currentAssignment);
                        log.info("Adding.  "); // + currentAssignment);
                    }
                }

                if (Double.compare(conflicts, N) < 0) {
                    bestStates.clear();
                    bestStates.add(currentAssignment);
                    N = conflicts;
                    log.info(df.format(N));
                    log.info("Clearing best states for (" + df.format(conflicts) + ") "); // + currentAssignment);
                    log.info("Num conflicts: " + csp.numConflicts(currentAssignment));
                }

                continue;

            }

            Object variable = csp.getUnassignedVariable(currentAssignment);
            for (Object value : csp.getOrderedValues(variable)) {
                Assignment testAssignment = new Assignment(currentAssignment);
                testAssignment.setVar(variable, value);
                stateList.push(testAssignment);
            }

        }

        //Now, choose among best states and return one.
        //Current solution is to pick one randomly, a better solution for
        //later is use BDeu to pick the highest score.
        //log.info("Num pruned: " + numPruned);

        Assignment finalAssignment = bestStates.toArray(new Assignment[bestStates.size()])[0];
        conflicts = csp.numConflicts(finalAssignment);

        log.info("Exhausted search space. Best solution has " + conflicts + " conflicts.");

        for (Constraint constraint : csp.getConstraints()) {
            if (!(constraint.isValid(finalAssignment, csp))) {
                log.info(constraint);
            }
        }

        log.info("Total nodes explored: " + nodesExpanded);
        log.info("Branches pruned: " + numPruned);

        return bestStates;

    }

    public int getConstraintChecks() {
        return constraintChecks;
    }

}
