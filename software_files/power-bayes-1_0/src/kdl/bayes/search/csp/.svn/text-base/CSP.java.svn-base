/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import kdl.bayes.util.constraint.Constraint;

import java.util.List;
import java.util.Set;

public interface CSP {


    public Object getUnassignedVariable(Assignment varAssign);

    public boolean isConsistent(Assignment varAssign);

    public Assignment getCompleteAssignment();

    public Set<Constraint> getConstraints();

    public int numConflicts(Assignment varAssign);

    public double getConflictWeight(Assignment varAssign);

    public int numVars();

    public List getOrderedValues(Object var);

    public Object getRandomVariable();

}
