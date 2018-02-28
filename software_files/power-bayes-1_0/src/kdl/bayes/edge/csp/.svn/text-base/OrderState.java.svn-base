/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import java.util.List;

public class OrderState implements Comparable {

    List<Integer> order;
    double score;

    /**
     * CSP implements a comparable interface for minimizing a score.
     * This is useful for finding the minimum from a set of scores as is used in RandomizedGreedySearch.
     *
     * @param dag
     * @param score
     */

    public OrderState(List<Integer> order, double score) {
        this.order = order;
        this.score = score;
    }

    public int compareTo(Object o) {
        if (!(o instanceof OrderState)) {
            return -1;
        }
        OrderState otherState = (OrderState) o;
        return Double.compare(score, otherState.score);
    }

    public List<Integer> getOrder() {
        return order;
    }

    public double getScore() {
        return score;
    }
}

