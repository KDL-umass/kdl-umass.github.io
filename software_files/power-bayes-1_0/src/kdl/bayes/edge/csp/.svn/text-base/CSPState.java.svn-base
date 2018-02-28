/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import kdl.bayes.util.GraphUtil;

class CSPState implements Comparable {

    boolean[][] dag;
    double score;

    String opString;

    protected volatile int hashCode = 0;


    /**
     * CSP implements a comparable interface for minimizing a score.
     * This is useful for finding the minimum from a set of scores as is used in RandomizedGreedySearch.
     *
     * @param dag
     * @param score
     */

    public CSPState(boolean[][] dag, double score) {
        this.dag = dag;
        this.score = score;
    }

    public CSPState(boolean[][] dag, double score, String opString) {
        this.dag = dag;
        this.score = score;
        this.opString = opString;
    }

    public int compareTo(Object o) {
        if (!(o instanceof CSPState)) {
            return -1;
        }
        CSPState otherState = (CSPState) o;
        return Double.compare(score, otherState.score);
    }

    public boolean[][] getDag() {
        return dag;
    }

    public String getOpString() {
        return opString;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double newScore) {
        score = newScore;
        hashCode = 0;
        hashCode();
    }

    public boolean equals(Object o) {
        return o instanceof CSPState && (Double.compare(score, ((CSPState) o).score) == 0) && (GraphUtil.equalDag(dag, ((CSPState) o).dag));
    }


    public int hashCode() {
        final int multiplier = 29;
        if (hashCode == 0) {
            int code = 17;
            code = multiplier * code + (new Double(score)).hashCode();
            int numVars = dag.length;
            for (int i = 0; i < numVars; i++) {
                for (int j = 0; j < numVars; j++) {
                    code = multiplier * code + (dag[i][j] ? 1231 : 1237);
                }
            }
            hashCode = code;
        }
        return hashCode;

    }
}
