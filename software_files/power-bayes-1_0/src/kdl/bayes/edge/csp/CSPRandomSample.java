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
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class CSPRandomSample extends CSPEdgeOrientation {

    protected static Logger log = Logger.getLogger(CSPRandomSample.class);

    int numRestarts = 10000;

    public CSPRandomSample(Instances trainInstances, SkeletonFinder skeleton) {
        super(trainInstances, skeleton);
    }

    public CSPRandomSample(Instances trainInstances, SkeletonFinder skeleton, int numRestarts) {
        super(trainInstances, skeleton);

        this.numRestarts = numRestarts;
    }

    public CSPRandomSample(Instances trainInstances, SkeletonFinder skeleton, int numRestarts, boolean doCheck, boolean useUnit) {
        super(trainInstances, skeleton, doCheck, useUnit);
        this.numRestarts = numRestarts;
    }

    public PowerBayesNet orientEdges() {

        List<PowerBayesNet> bestBNs = new ArrayList<PowerBayesNet>();
        double bestScore = Double.POSITIVE_INFINITY;

        for (int i = 0; i < numRestarts; i++) {
            boolean[][] randomOrientation = getRandomOrientation();
            PowerBayesNet nextBN = new PowerBayesNet(data, randomOrientation);
            double nextScore = countViolatedConstraints(nextBN.getDag(), false, skeleton.getConstraints());

            if (Double.compare(nextScore, bestScore) < 0) {
                bestScore = nextScore;
                bestBNs.clear();
                bestBNs.add(nextBN);
            }

            if (Double.compare(nextScore, bestScore) == 0) {
                bestBNs.add(nextBN);
            }

        }

        finalScore = bestScore;
        return breakTiesBDeu(bestBNs);

    }
}
