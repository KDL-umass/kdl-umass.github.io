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
import kdl.bayes.util.constraint.Constraint;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.*;

public class CSPRandomizedGreedy extends CSPEdgeOrientation {

    protected static Logger log = Logger.getLogger(CSPRandomizedGreedy.class);

    double k = 0.5;
    int numRestarts = 10000;

    public CSPRandomizedGreedy(Instances trainInstances, SkeletonFinder skeleton, double k, int numRestarts) {
        super(trainInstances, skeleton);
        this.k = k;
        this.numRestarts = numRestarts;
    }

    public CSPRandomizedGreedy(Instances trainInstances, SkeletonFinder skeleton, double k, int numRestarts, boolean doCheck, boolean useUnit) {
        super(trainInstances, skeleton, doCheck, useUnit);
        this.k = k;
        this.numRestarts = numRestarts;
    }

    public PowerBayesNet orientEdges() {
        return orientEdges(skeleton, skeleton.getConstraints());
    }

    public PowerBayesNet orientEdges(SkeletonFinder skel, Set<Constraint> constraints) {

        List<PowerBayesNet> globalBestBNs = new ArrayList<PowerBayesNet>();
        double globalBestScore = Double.POSITIVE_INFINITY;

        Set<List<Integer>> triples = skeleton.findTriples();
        log.info("Found " + triples.size() + " triples.");

        for (int i = 0; i < numRestarts; i++) {

            boolean[][] initialGraph = getRandomOrientation(skel);
            PowerBayesNet currBN = new PowerBayesNet(data, initialGraph);
            double currScore = countViolatedConstraints(currBN.getDag(), false, constraints);

            //Run Randomized Greedy Search
            while (true) {
                log.debug("Starting step with score " + currScore);
                List<CSPState> improvedList = new ArrayList<CSPState>();   // B from KES paper
                boolean[][] currDag = currBN.getDag();


                Set<CSPState> possibleSuccessors = new HashSet<CSPState>();
                //Create successors by toggling triples to be a collider or not.
                // triple<x,y,z> ==> x --- z --- y
                int numAdded = 0;
                for (List<Integer> triple : triples) {
                    int x = triple.get(0);
                    int y = triple.get(1);
                    int z = triple.get(2);

                    //Check each case and generate corresponding successors.
                    // Case 1: BreakV
                    // x --> z <-- y  =>  x <-- z <-- y
                    //                    x --> z --> y
                    //                    x <-- z --> y
                    if (currDag[x][z] && currDag[y][z]) {
                        boolean[][] successor = GraphUtil.copyGraph(currDag);
                        successor[x][z] = false;
                        successor[z][x] = true;
                        CSPState succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;

                        successor = GraphUtil.copyGraph(currDag);
                        successor[y][z] = false;
                        successor[z][y] = true;
                        succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;

                        successor = GraphUtil.copyGraph(currDag);
                        successor[x][z] = false;
                        successor[z][x] = true;
                        successor[y][z] = false;
                        successor[z][y] = true;
                        succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;
                    }
                    // Case 2a: MakeV
                    // x --> z --> y =>  x --> z <-- y
                    else if (currDag[x][z] && currDag[z][y]) {
                        boolean[][] successor = GraphUtil.copyGraph(currDag);
                        successor[z][y] = false;
                        successor[y][z] = true;
                        CSPState succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;
                    }
                    // Case 2b: MakeV
                    // x <-- z <-- y =>  x --> z <-- y
                    else if (currDag[z][x] && currDag[y][z]) {
                        boolean[][] successor = GraphUtil.copyGraph(currDag);
                        successor[z][x] = false;
                        successor[x][z] = true;
                        CSPState succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;
                    }
                    // Case 3: InvertV
                    // x <-- z --> y =>  x --> z <-- y
                    else if (currDag[z][x] && currDag[z][y]) {
                        boolean[][] successor = GraphUtil.copyGraph(currDag);
                        successor[z][x] = false;
                        successor[x][z] = true;

                        successor[z][y] = false;
                        successor[y][z] = true;
                        CSPState succState = new CSPState(successor, 0.0);
                        possibleSuccessors.add(succState);
                        numAdded++;
                    }
                }


                for (CSPState succState : possibleSuccessors) {
                    double nextScore = countViolatedConstraints(succState.getDag(), false, constraints);
                    succState.setScore(nextScore);

                    //If successor is an improvement over the current state.
                    if (Double.compare(nextScore, currScore) < 0) {
                        log.debug("\t Found Improved score " + nextScore);
                        improvedList.add(succState);
                    }
                }

                if (improvedList.size() > 0) {
                    int size = (int) Math.round(Math.max(1, improvedList.size() * k));
                    Collections.shuffle(improvedList);
                    PriorityQueue<CSPState> C = new PriorityQueue<CSPState>();
                    for (int j = 0; j < size; j++) {
                        C.offer(improvedList.get(j));
                    }

                    CSPState best = C.poll();
                    currBN = new PowerBayesNet(data, best.getDag());
                    currScore = best.getScore();

                } else {
                    break;
                }

            }
            log.info("Finished run " + i + " with score " + currScore);
            //Update the bestList across restarts
            if (Double.compare(currScore, globalBestScore) < 0) {
                globalBestScore = currScore;
                globalBestBNs.clear();
                globalBestBNs.add(currBN);
                log.info("Updating Global best: score=" + globalBestScore + " (" + i + ")");
            } else if (Double.compare(currScore, globalBestScore) == 0) {
                globalBestBNs.add(currBN);
                log.debug("Adding another best BN " + "(" + i + ")");
            }
        }

        finalScore = globalBestScore;
        return breakTiesBDeu(globalBestBNs);
    }


}
