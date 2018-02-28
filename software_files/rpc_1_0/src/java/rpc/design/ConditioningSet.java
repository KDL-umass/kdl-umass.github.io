/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Sep 25, 2009
 * Time: 3:25:13 PM
 */
package rpc.design;

import rpc.dataretrieval.Aggregator;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Conditioning Set is a design element that specifices a set of path variables to condition on.
 */
public class ConditioningSet extends DesignElement {

    private List<Path> conditioningVariables;
    private Map<Path, Aggregator> conditioningAggs;

    /**
     * Instantiate a conditioning set object.
     * @param conditioningVariables the list of path variables to condition on.
     */
    public ConditioningSet(List<Path> conditioningVariables) {
        this.conditioningVariables = conditioningVariables;
        this.conditioningAggs = new HashMap<Path, Aggregator>();
    }

    /**
     * Compares this conditioning set to the specified object. The result is true if and only if the
     * conditioning sets have the exact same set of variables to condition on.
     * @param o the object to compare this conditioning set against.
     * @return true if the conditioning sets are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof ConditioningSet) {
            ConditioningSet other = (ConditioningSet) o;
            //everything must be the same!
            List<Path> otherConditioningVariables = other.getConditioningSet();
            boolean isEqual = this.conditioningVariables.size() == otherConditioningVariables.size();

            if (! isEqual) {
                return false;
            }

            //now check through the paths
            for (Path p : this.conditioningVariables) {
                if (! otherConditioningVariables.contains(p) ) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Get the set of conditioning path variables used.
     * @return a list of paths corresponding to the variables to condition on.
     */
    public List<Path> getConditioningSet() {
        return this.conditioningVariables;
    }

    /**
     * Get the aggregator for a specific path variable.
     * @param control the path to get the aggregator for.
     * @return the aggregator used for the given path.
     */
    public Aggregator getConditioningAggregate(Path control) {
        return this.conditioningAggs.get(control);
    }

    /**
     * Set an aggregator to use for a specific path.
     * @param control the path to set the aggregator for.
     * @param agg the aggregator.
     */
    public void setConditioningAggregate(Path control, Aggregator agg) {
        this.conditioningAggs.put(control, agg);
    }

    /**
     * Returns a string representation of the conditioning set.
     * @return the string representation.
     */
    public String toString() {
        return this.conditioningVariables.toString();
    }
}