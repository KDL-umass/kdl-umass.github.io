/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jun 2, 2009
 * Time: 1:52:41 PM
 */
package rpc.dataretrieval;

/**
 * Extension of the abstract aggregator that uses the mode operator.
 */
public class ModeAggregator extends Aggregator {

    /**
     * Constructs a ModeAggregator object over the supplied nested aggregator.
     * @param agg the inner aggregator object.
     */
    public ModeAggregator(Aggregator agg){
        super(agg);
    }

    /**
     * Compares this ModeAggregator to the specified object. The result is true if and only if the other object
     * is also a ModeAggregator and the inner aggregators are equal.
     * @param o the object to compare this ModeAggregator against.
     * @return true if the ModeAggregators are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof ModeAggregator) {
            ModeAggregator other = (ModeAggregator) o;
            return this.agg.equals(other.agg);
        }
        else {
            return false;
        }
    }

    /**
     * Returns a string representation consisting of mode(X), where X is the string representation of the
     * inner aggregator.
     * @return the string representation.
     */
    public String toString() {
        return "mode(" + this.agg + ")";
    }

}