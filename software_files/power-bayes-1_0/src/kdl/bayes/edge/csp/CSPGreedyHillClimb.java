/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.skeleton.SkeletonFinder;
import kdl.bayes.util.GraphUtil;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class CSPGreedyHillClimb extends CSPEdgeOrientation {

    public CSPGreedyHillClimb(Instances trainInstances, SkeletonFinder skeleton) {
        super(trainInstances, skeleton);
    }

    public CSPGreedyHillClimb(Instances trainInstances, SkeletonFinder skeleton, boolean doCheck, boolean useUnit) {
        super(trainInstances, skeleton, doCheck, useUnit);
    }

    public PowerBayesNet orientEdges() {
        boolean[][] initialGraph = getRandomOrientation();
        PowerBayesNet currBN = new PowerBayesNet(data, initialGraph);
        double currScore = countViolatedConstraints(currBN.getDag(), false, skeleton.getConstraints());

        List<PowerBayesNet> bestBNs = new ArrayList<PowerBayesNet>();
        bestBNs.add(currBN);
        double bestScore = Double.POSITIVE_INFINITY;

        while (Double.compare(currScore, bestScore) < 0) {
            boolean[][] currDag = currBN.getDag();

            List<List<Integer>> compelledEdges = findDirectedEdges(currDag);

            boolean foundSuccessor = false;
            for (List<Integer> compelledEdge : compelledEdges) {
                int x = compelledEdge.get(0);
                int y = compelledEdge.get(1);

                boolean[][] reversed = GraphUtil.copyGraph(currDag);
                reversed[x][y] = false;
                reversed[y][x] = true;

                boolean[][] successor = GraphUtil.getConsistentDag(reversed);

                if (successor == null) {
                    continue;
                }

                double nextScore = countViolatedConstraints(successor, false, skeleton.getConstraints());
                PowerBayesNet nextBN = new PowerBayesNet(data, successor);

                if (Double.compare(nextScore, bestScore) < 0) {
                    bestScore = nextScore;
                    bestBNs.clear();
                    bestBNs.add(nextBN);
                }

                if (Double.compare(nextScore, bestScore) == 0) {
                    bestBNs.add(nextBN);
                }

            }

            if (!foundSuccessor) {
                break;
            }
        }
        finalScore = bestScore;
        return breakTiesBDeu(bestBNs);
    }
}
