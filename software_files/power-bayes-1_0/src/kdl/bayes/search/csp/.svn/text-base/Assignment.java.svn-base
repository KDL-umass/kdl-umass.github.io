/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Assignment implements Iterable {

    Map varValues;

    Object lastVar;
    Object lastValue;

    public Assignment() {
        varValues = new HashMap();

        lastVar = null;
        lastValue = null;
    }

    public Assignment(Assignment newAssignment) {
        varValues = new HashMap(newAssignment.varValues);

        lastVar = newAssignment.lastVar;
        lastValue = newAssignment.lastValue;
    }

    public int size() {
        return varValues.keySet().size();
    }

    public Iterator iterator() {
        return varValues.keySet().iterator();
    }

    public Object getLastVar() {
        return lastVar;
    }

    public Object getLastValue() {
        return lastValue;
    }

    public Object getValue(Object var) {
        return varValues.get(var);
    }

    public boolean hasVar(Object var) {
        return varValues.keySet().contains(var);
    }

    public void setVar(Object var, Object value) {
        lastVar = var;
        lastValue = value;
        varValues.put(var, value);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("[" + size() + "] ");

        for (Object o : varValues.keySet()) {
            sb.append("{" + o.toString() + "," + varValues.get(o).toString() + "}, ");
        }
        return sb.toString();
    }
}
