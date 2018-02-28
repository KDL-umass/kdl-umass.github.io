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
import kdl.bayes.skeleton.util.PowerSetIterator;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.constraint.Constraint;
import kdl.bayes.util.constraint.dSeparation;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.*;

public abstract class CSPEdgeOrientation {

    protected static Logger log = Logger.getLogger(CSPEdgeOrientation.class);


    SkeletonFinder skeleton;
    Instances data;

    boolean doNecessaryCheck = true;
    boolean useUnitWeight = true;

    double finalScore = -1;

    int statesScored = 0;

    protected static final Random random = new Random();

    public CSPEdgeOrientation(Instances trainInstances, SkeletonFinder skeleton) {
        this.skeleton = skeleton;
        this.data = trainInstances;
    }

    public CSPEdgeOrientation(Instances trainInstances, SkeletonFinder skeleton, boolean doCheck, boolean useUnit) {
        this(trainInstances, skeleton);
        doNecessaryCheck = doCheck;
        useUnitWeight = useUnit;
    }

    public abstract PowerBayesNet orientEdges();

    public PowerBayesNet orientEdges(SkeletonFinder skeleton, Set<Constraint> constraints) {
        return orientEdges();
    }

    public double getScore() {
        return finalScore;
    }

    public PowerBayesNet breakTiesBDeu(List<PowerBayesNet> bestBNs) {

        log.info("Breaking ties between " + bestBNs.size() + " networks.");

        PowerBayesNet bestBN = bestBNs.get(0);

        if (bestBNs.size() == 0) {
            return bestBN;
        }

        double bestScore = bestBN.logBDeuScore();

        for (PowerBayesNet bn : bestBNs) {
            double currScore = bn.logBDeuScore();
            if (Double.compare(currScore, bestScore) > 0) {
                bestBN = bn;
                bestScore = currScore;
            }
        }

        return bestBN;
    }

    public PowerBayesNet breakTiesRandomly(List<PowerBayesNet> bestBNs) {
        Collections.shuffle(bestBNs, random);
        return bestBNs.get(0);
    }

    /**
     * Find directed edges returns only compelled edges for a PDAG and all edges for a DAG.
     *
     * @param pdag
     * @return
     */
    public List<List<Integer>> findDirectedEdges(boolean[][] pdag) {
        List<List<Integer>> edgeList = new ArrayList<List<Integer>>();
        int numVars = pdag.length;
        for (int i = 0; i < numVars; i++) {
            for (int j = 0; j < numVars; j++) {
                if (pdag[i][j] && !pdag[j][i]) {
                    List<Integer> edge = new ArrayList<Integer>();
                    edge.add(i);
                    edge.add(j);
                    edgeList.add(edge);
                }
            }
        }
        return edgeList;
    }

    public double countViolatedConstraints(boolean[][] dag, boolean printViolations, Set<Constraint> constraints) {

        statesScored++;

        //Acyclic graphs are not allowed.
        if (GraphUtil.hasDirectedCycle(dag)) {
            return Double.POSITIVE_INFINITY;
        }

        double violatedWeight = 0.0;
        for (Constraint constraint : constraints) {
            if (constraint instanceof dSeparation) {
                dSeparation d = (dSeparation) constraint;
                int xIdx = d.getX();
                int yIdx = d.getY();

                Set<Integer> zIdxs = d.getZ();

                //Right just check to see if Z is sufficient for d-separation but may not be necessary.
                if (!isDSeparated(dag, xIdx, yIdx, zIdxs, doNecessaryCheck)) {
                    if (printViolations) {
                        log.info(d);
                    }
                    if (useUnitWeight) {
                        violatedWeight += 1.0;
                    } else {
                        violatedWeight += d.getWeight();
                    }
                }
            }
        }
        return violatedWeight;
    }

    /**
     * Checks whether x _||_ y | z in this dag.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isDSeparated(boolean[][] dag, int x, int y, Set<Integer> z) {
        return isDSeparated(dag, x, y, z, true);
    }

    public boolean isDSeparated(boolean[][] dag, int x, int y, Set<Integer> z, boolean checkNecessary) {
        //Check if Z is sufficient to cSeparate x and y
        boolean sufficient = GraphUtil.findDSeparatedNodes(dag, x, z).contains(y);
        //Now check if necessary, by determining if a subset of Z is also sufficient to cSeparate.
        if (sufficient && z.size() > 0 && checkNecessary) {
            for (int size = z.size() - 1; size >= 0; size--) {
                //for (int size = 0; size < z.size(); size++) {
                PowerSetIterator iter = new PowerSetIterator(z, size);
                while (iter.hasNext()) {
                    Set<Integer> subset = (Set<Integer>) iter.next();
                    boolean notNecessary = GraphUtil.findDSeparatedNodes(dag, x, subset).contains(y);
                    if (notNecessary) {
                        return false;
                    }
                }
            }
        }
        return sufficient;
    }


    /**
     * Creates a random acyclic orientation for the provided skeleton.
     * Used as an initial network for search.
     *
     * @return
     */
    public boolean[][] getRandomOrientation() {
        return getRandomOrientation(skeleton);
    }

    public boolean[][] getRandomOrientation(SkeletonFinder skel) {
        //Select an ordering for the nodes
        List<Integer> nodeOrder = new ArrayList<Integer>();
        int numVars = skel.getNumVariables();
        for (int i = 0; i < numVars; i++) {
            nodeOrder.add(i);
        }
        Collections.shuffle(nodeOrder, random);

        return orientSkeletonWithOrder(skel, nodeOrder);
    }

    public boolean[][] orientSkeletonWithOrder(SkeletonFinder skel, List<Integer> nodeOrder) {
        int numVars = skel.getNumVariables();
        boolean[][] initialGraph = new boolean[numVars][numVars];

        for (int x = 0; x < numVars; x++) {
            for (int y = x + 1; y < numVars; y++) {
                if (skel.hasEdge(x, y)) {
                    int xPos = nodeOrder.indexOf(x);
                    int yPos = nodeOrder.indexOf(y);

                    if (xPos < yPos) {
                        initialGraph[x][y] = true;
                    } else {
                        initialGraph[y][x] = true;
                    }
                }
            }
        }

        return initialGraph;
    }


    public boolean[][] getDag() {
        return null;
    }

    public int getStatesScored() {
        return statesScored;
    }

    public SkeletonFinder getSkeleton() {
        return skeleton;
    }

}
