/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.constraint;

import kdl.bayes.search.csp.Assignment;
import kdl.bayes.search.csp.CSP;

public class ValueInequality implements Constraint {

    Object var1;
    Object var2;

    double weight;


    public ValueInequality(Object var1, Object var2) {
        this.var1 = var1;
        this.var2 = var2;
        weight = 1.0;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isValid(Assignment assignment, CSP csp) {
        if (assignment.hasVar(var1) && assignment.hasVar(var2)) {
            return !assignment.getValue(var1).equals(assignment.getValue(var2));
        }
        return true;
    }

}
