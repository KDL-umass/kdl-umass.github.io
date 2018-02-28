/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import kdl.bayes.util.constraint.Constraint;

import java.util.*;

public class GenericCSP implements CSP {

    List<Object> variables;
    Map<Object, List<Object>> possibleValues;
    Map<Object, List<Object>> remainingValues;

    Set<Constraint> constraints;


    public GenericCSP(Map<Object, List<Object>> vars, Set<Constraint> newConstraints) {
        variables = new ArrayList<Object>(vars.keySet());

        possibleValues = new HashMap<Object, List<Object>>(vars);
        remainingValues = new HashMap<Object, List<Object>>(vars);

        constraints = newConstraints;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public List getOrderedValues(Object var) {
        if (var == null) {
            return new ArrayList();
        }
        return possibleValues.get(var);
    }

    public int numVars() {
        return variables.size();
    }

    public Assignment getCompleteAssignment() {
        Assignment newAssignment = new Assignment();
        for (Object var : possibleValues.keySet()) {
            List<Object> values = possibleValues.get(var);
            Collections.shuffle(values);
            newAssignment.setVar(var, values.get(0));
        }
        return newAssignment;
    }

    public boolean isConsistent(Assignment varAssign) {
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(varAssign, this)) {
                return false;
            }
        }
        return true;
    }

    public int numConflicts(Assignment varAssign) {
        int numViolated = 0;
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(varAssign, this)) {
                numViolated++;
            }
        }
        return numViolated;
    }

    public double getConflictWeight(Assignment varAssign) {
        return numConflicts(varAssign);
    }

    public Object getRandomVariable() {
        Random rand = new Random();
        int varIdx = rand.nextInt(variables.size());
        return variables.get(varIdx);
    }

    public Object getUnassignedVariable(Assignment varAssign) {
        return variables.get(varAssign.size());
    }
}
