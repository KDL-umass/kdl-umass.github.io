/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.constraint;

import kdl.bayes.search.csp.Assignment;
import kdl.bayes.search.csp.BNCSP;
import kdl.bayes.search.csp.CSP;
import kdl.bayes.util.GraphUtil;

public class Acyclicity implements Constraint {

    public Acyclicity() {

    }

    public boolean isValid(Assignment assignment, CSP csp) {
        if (!(csp instanceof BNCSP)) {
            return false;
        }
        return !GraphUtil.hasDirectedCycle(((BNCSP) csp).getDag(assignment));
    }

    public String toString() {
        return "Acyclicity.";
    }

    public double getWeight() {
        return Double.MAX_VALUE;
    }
}
