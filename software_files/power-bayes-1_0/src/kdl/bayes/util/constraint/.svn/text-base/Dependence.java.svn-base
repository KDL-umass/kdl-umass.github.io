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

public class Dependence implements Constraint {

    int edge;
    int n;

    double weight;

    public Dependence(int x, int y, int n) {
        this.n = n;
        if (x < y) {
            edge = x * n + y;
        } else {
            edge = y * n + x;
        }

        weight = 1.0;

    }

    public Dependence(int x, int y, int n, double weight) {
        this(x, y, n);
        this.weight = weight;
    }

    public Dependence(int edge, int n) {
        this.edge = edge;
        this.n = n;
        weight = 1.0;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isValid(Assignment assignment, CSP csp) {
        if (!(csp instanceof BNCSP)) {
            return false;
        }
        if (assignment.hasVar(edge)) {
            String value = (String) assignment.getValue(edge);
            if ("E".equals(value)) {
                return false;
            }
        }
        return true;
    }

    public int getX() {
        return edge / n;
    }

    public int getY() {
        return edge % n;
    }

    public String toString() {
        int i = edge / n;
        int j = edge % n;
        return "[" + weight + "]( " + i + " -- " + j + " )";
    }
}
