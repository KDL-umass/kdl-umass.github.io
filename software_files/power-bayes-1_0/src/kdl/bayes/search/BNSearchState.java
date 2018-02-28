/**
 * $Id: BNSearchState.java 273 2009-03-06 16:41:19Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.search;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.util.Assert;
import kdl.bayes.util.StatUtil;
import org.apache.log4j.Logger;
import weka.core.Instances;

/**
 * NOTE: this search state assumes that the predecessor state has an up-to-date
 * DAG.
 */
public class BNSearchState extends SearchState {
    protected static Logger log = Logger.getLogger(BNSearchState.class);
    protected static PowerBayesNet trueBn = null;
    public static final int INITIAL = -1;
    public static final int ADD = 0;
    public static final int DEL = 1;
    public static final int REV = 2;

    // search state
    protected int fromIdx = -1;
    protected int toIdx = -1;
    protected int op = -1;
    protected double score;
    protected double scoreChange;
    protected BNSearchState predecessor;

    // helper stuff, available when needed
    protected static Instances instances;     // assumed to be set by the initial state
    protected static double ess;
    protected boolean[][] dag = null;


    public PowerBayesNet initBayesNet(boolean[][] dag) {
        PowerBayesNet bayesNet = new PowerBayesNet(instances, dag);
        bayesNet.setEquivalentSampleSize(ess);
        return bayesNet;
    }

    public BNSearchState(PowerBayesNet bayesNet) {
        instances = bayesNet.m_Instances;
        ess = bayesNet.getEquivSampleSize();
        score = bayesNet.logBDeuScore();
        scoreChange = Double.NaN;
        predecessor = null;
        dag = bayesNet.getDag();
    }

    public BNSearchState(BNSearchState predecessor, int fromIdx, int toIdx, int op) {
        this.predecessor = predecessor;
        this.fromIdx = fromIdx;
        this.toIdx = toIdx;
        this.op = op;

        // compute score change
        PowerBayesNet tempBN = initBayesNet(predecessor.getDag());

        scoreChange = computeScoreChange(tempBN);

        score = predecessor.getScore() + scoreChange;

//        PowerBayesNet tempBN = new PowerBayesNet(bayesNet.m_Instances, bayesNet.getDag());
//        Assert.condition(StatUtil.equalDoubles(tempBN.logBDeuScore(), score),
//                "Incorrect score: " + score + " , " + tempBN.logBDeuScore());
    }

    // for efficiency, avoid recomputing the score change
    public BNSearchState(BNSearchState predecessor, int fromIdx, int toIdx, int op, double scoreChange) {
        this.predecessor = predecessor;
        this.fromIdx = fromIdx;
        this.toIdx = toIdx;
        this.op = op;
        this.scoreChange = scoreChange;
        score = predecessor.getScore() + scoreChange;

//        PowerBayesNet tempBN = new PowerBayesNet(bayesNet.m_Instances, bayesNet.getDag());
//        Assert.condition(StatUtil.equalDoubles(tempBN.logBDeuScore(), score),
//                "Incorrect score: " + score + " , " + tempBN.logBDeuScore());
    }

    private double computeScoreChange(PowerBayesNet bayesNet) {
        double scoreChange;
        // compute score for this state in terms of a change from the score of
        // the preceeding state (for efficiency)
        if (op == ADD) {
            scoreChange = addEdge(bayesNet, fromIdx, toIdx);
        } else if (op == DEL) {
            scoreChange = deleteEdge(bayesNet, fromIdx, toIdx);
        } else if (op == REV) {
            scoreChange = deleteEdge(bayesNet, fromIdx, toIdx);
            scoreChange += addEdge(bayesNet, toIdx, fromIdx);
        } else {
            throw new UnsupportedOperationException("Unrecognized operator " + op);
        }
        return scoreChange;
    }

    // returns change in score from adding edge
    private double addEdge(PowerBayesNet bayesNet, int fromIdx, int toIdx) {
        double change;

        Assert.condition(!bayesNet.getParentSet(toIdx).contains(fromIdx),
                "Cannot add edge (" + fromIdx + "," + toIdx + ") because already present.");
        double oldScore = bayesNet.getVariableLogBDeuScore(toIdx);
        bayesNet.addEdge(fromIdx, toIdx);
        double newScore = bayesNet.getVariableLogBDeuScore(toIdx);

        change = newScore - oldScore;
        return change;
    }

    // returns change in score from deleting edge
    private double deleteEdge(PowerBayesNet bayesNet, int fromIdx, int toIdx) {
        double change;

        Assert.condition(bayesNet.getParentSet(toIdx).contains(fromIdx),
                "Cannot delete edge (" + fromIdx + "," + toIdx + ") because it does not exist.");
        double oldScore = bayesNet.getVariableLogBDeuScore(toIdx);
        bayesNet.removeEdge(fromIdx, toIdx);
        double newScore = bayesNet.getVariableLogBDeuScore(toIdx);

        change = newScore - oldScore;
        return change;
    }

    public PowerBayesNet getBayesNet() {
        boolean[][] dag = getDag();
        return initBayesNet(dag);
    }

    public boolean[][] getDag() {
        if (dag == null) {
            initDag();
        }
        return dag;
    }

    private void initDag() {
        log.debug("creating dag for state " + this);
        boolean[][] tempDag = predecessor.getDag();
        dag = new boolean[tempDag.length][tempDag[0].length];
        for (int i = 0; i < tempDag.length; i++) {
            for (int j = 0; j < tempDag[i].length; j++) {
                dag[i][j] = tempDag[i][j];
            }
        }

        if (op == ADD) {
            dag[fromIdx][toIdx] = true;
        } else if (op == DEL) {
            dag[fromIdx][toIdx] = false;
        } else { // reverse
            dag[fromIdx][toIdx] = false;
            dag[toIdx][fromIdx] = true;
        }
    }

    public double getScore() {
//        PowerBayesNet tempBN = new PowerBayesNet(bayesNet.m_Instances, bayesNet.getDag());
//        Assert.condition(StatUtil.equalDoubles(tempBN.logBDeuScore(), score),
//                "Incorrect score: " + score + " , " + tempBN.logBDeuScore());
        return score;
    }

    public boolean isSame(SearchState other) {
        if (other instanceof BNSearchState) {
            BNSearchState otherState = (BNSearchState) other;
            boolean[][] otherDag = otherState.getDag();

            // to be equal, other state must match current state in terms
            // of the edge between fromIdx and toIdx

            // if it is an ADD, it must have edge from->to and not have to->from
            if (op == ADD && !(otherDag[fromIdx][toIdx] && !otherDag[toIdx][fromIdx])) {
                return false;
            }
            // if it is a DEL, it must not have edge from->to and also not have to->from
            else if (op == DEL && !(!otherDag[fromIdx][toIdx] && !otherDag[toIdx][fromIdx])) {
                return false;
            }
            // if it is a REV, it must not have edge from->to and must have have to->from
            else if (op == REV && !(!otherDag[fromIdx][toIdx] && otherDag[toIdx][fromIdx])) {
                return false;
            }

            // check the other edges, using predecessor's dag (avoids making my own)
            boolean[][] tempDag = predecessor.getDag();
            if (tempDag.length != otherDag.length) {
                return false;
            }
            if (tempDag[0].length != otherDag[0].length) {
                return false;
            }
            for (int i = 0; i < tempDag.length; i++) {
                for (int j = 0; j < tempDag[i].length; j++) {
                    if ((i == fromIdx && j == toIdx) || (i == toIdx && j == fromIdx)) {
                        continue;
                    }
                    if (tempDag[i][j] != otherDag[i][j]) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void makeCurrentState() {
    }

    // for experimental purposes only
    public void setTrueBn(PowerBayesNet trueBn) {
        this.trueBn = trueBn;
    }

    public String toString() {
        String str = "BNSearchState: ";

        if (op == ADD) {
            str += " adding (" + fromIdx + "," + toIdx + ")";
        } else if (op == DEL) {
            str += " deleting (" + fromIdx + "," + toIdx + ")";
        } else if (op == REV) {
            str += " reversing (" + fromIdx + "," + toIdx + ")";
        } else {
            str += " unknown";
        }

        str += " score=" + score;

        return str;
    }

    public String getScoreStr() {
        return "score=" + score;
    }

    public int getOp() {
        return op;
    }

    public int getFromIdx() {
        return fromIdx;
    }

    public int getToIdx() {
        return toIdx;
    }

    public double getScoreChange() {
        Assert.condition(!Double.isNaN(scoreChange), "Invalid score change");
        return scoreChange;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BNSearchState)) {
            return false;
        }
        BNSearchState rhs = (BNSearchState) obj;
        return StatUtil.equalDoubles(score, rhs.getScore());
    }

    public int compareTo(Object otherState) {
        return Double.compare(score, ((BNSearchState) otherState).getScore());
    }
}
