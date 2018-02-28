/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 11, 2009
 * Time: 3:48:40 PM
 */
package rpc.dataretrieval;

/**
 * Extension of the abstract aggregator that uses the count operator.
 */
public class CountAggregator extends Aggregator {

    /**
     * Constructs a CountAggregator object over the supplied nested aggregator.
     * @param agg the inner aggregator object.
     */
    public CountAggregator(Aggregator agg){
        super(agg);
    }

    /**
     * Compares this CountAggregator to the specified object. The result is true if and only if the other object
     * is also a CountAggregator and the inner aggregators are equal.
     * @param o the object to compare this CountAggregator against.
     * @return true if the CountAggregators are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof CountAggregator) {
            CountAggregator other = (CountAggregator) o;
            return this.agg.equals(other.agg);
        }
        else {
            return false;
        }
    }

    /**
     * Returns a string representation consisting of count(X), where X is the string representation of the 
     * inner aggregator.
     * @return the string representation.
     */
    public String toString() {
        return "count(" + this.agg + ")";
    }

}