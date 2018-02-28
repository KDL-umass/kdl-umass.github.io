/**
 * $Id: BNConstrainedSearchWithOracleProblem.java 273 2009-03-06 16:41:19Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.search;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.util.Assert;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * BNConstrainedSearchWithOracleProblem
 */
public class BNConstrainedSearchWithOracleProblem extends BNConstrainedSearchProblem {
    protected static Logger log = Logger.getLogger(BNConstrainedSearchWithOracleProblem.class);
    private boolean[][] trueGraph;

    public BNConstrainedSearchWithOracleProblem(boolean[][] trueGraph, PowerBayesNet bn, boolean[][] constraintGraph) {
        super(bn, constraintGraph);
        this.trueGraph = trueGraph;
    }

    public BNConstrainedSearchWithOracleProblem(PowerBayesNet bn, boolean[][] constraintGraph) {
        super(bn, constraintGraph);
    }

    public BNConstrainedSearchWithOracleProblem(PowerBayesNet bn, boolean[][] initialDag, boolean[][] constraintGraph) {
        super(bn, initialDag, constraintGraph);
    }

    // look throught the states tied for best score
    public SearchState breakTies(List bestStates) {
        List allowedStates = getAllowedStates(bestStates);
        return super.breakTies(allowedStates);
    }

    /**
     * The edge oracle allows all proposed changes except for adding an edge in both directions
     * when one edge direction occurs in the true network. In this case, the other direction is
     * excluded.
     *
     * @param bestStates
     * @return
     */
    public List<SearchState> getAllowedStates(List bestStates) {
        List<SearchState> copyBestStates = new ArrayList<SearchState>(bestStates);
        List<SearchState> allowedStates = new ArrayList<SearchState>();

        while (copyBestStates.size() > 0) {

            BNSearchState state = (BNSearchState) copyBestStates.remove(0);
            int op = state.getOp();

            if (op == state.DEL) {
                allowedStates.add(state); //Allowed to delete true structure
            } else if (op == state.REV) {
                allowedStates.add(state); //Allowed to reverse away from true structure
            } else if (op == state.ADD) {

                int fromIdx = state.getFromIdx();
                int toIdx = state.getToIdx();

                boolean isAllowed = isInTrueNetwork(op, fromIdx, toIdx);

                // look for its opposite: op, toIdx, fromIdx
                // and only include the "right" one in allowed states
                BNSearchState otherState = null;
                for (int j = 0; j < copyBestStates.size(); j++) {
                    otherState = (BNSearchState) copyBestStates.get(j);
                    if (otherState.getOp() == op &&
                            otherState.getFromIdx() == toIdx &&
                            otherState.getToIdx() == fromIdx) {
                        break;
                    }
                }

                // cases:
                // its opposite is not found, state is allowed
                // its opposite is found and
                //     state is legal, opp is legal should NEVER happen
                //     state is illegal, opp is legal do not add state
                //     state is legal, opp is illegal add state
                //     state is illegal, opp is illegal add state
                if (otherState == null) {
                    allowedStates.add(state);
                } else {
                    copyBestStates.remove(otherState);
                    // if this state is allowed
                    boolean isOtherAllowed = isInTrueNetwork(otherState.getOp(),
                            otherState.getFromIdx(),
                            otherState.getToIdx());
                    Assert.condition(!(isAllowed && isOtherAllowed),
                            "A state and its opposite cannot both be allowed");
                    if (!isAllowed && isOtherAllowed) {
                        allowedStates.add(otherState);
                    } else if (isAllowed && !isOtherAllowed) {
                        allowedStates.add(state);
                    } else if (!isAllowed && !isOtherAllowed) {
                        allowedStates.add(state);
                        allowedStates.add(otherState);
                    }
                }
            } else {
                throw new IllegalArgumentException("Unrecognized operator " + op);
            }
        }

        if (allowedStates.size() == 0) {
            allowedStates = bestStates;  // all states were illegal reverses
        }

        log.debug("No. tied " + bestStates.size() + " pruned to " + allowedStates.size());
        return allowedStates;
    }

    private boolean isInTrueNetwork(int op, int fromIdx, int toIdx) {
        if (op == BNSearchState.ADD) {
            return trueGraph[fromIdx][toIdx];
        } else {
            return trueGraph[toIdx][fromIdx];
        }
    }
}
