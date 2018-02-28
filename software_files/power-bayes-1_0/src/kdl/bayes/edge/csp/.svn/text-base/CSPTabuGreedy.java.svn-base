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

public class CSPTabuGreedy extends CSPEdgeOrientation {
    protected static Logger log = Logger.getLogger(CSPTabuGreedy.class);

    int numRestarts = 1;
    int maxMemory = 350;
    int maxSteps = 20;

    Set<Constraint> nextViolatedConstraints;

    public CSPTabuGreedy(Instances trainInstances, SkeletonFinder skeleton, int maxSteps) {
        super(trainInstances, skeleton);
        this.maxSteps = maxSteps;
        nextViolatedConstraints = new HashSet<Constraint>();
    }

    public CSPTabuGreedy(Instances trainInstances, SkeletonFinder skeleton, int maxSteps, boolean doCheck, boolean useUnit) {
        super(trainInstances, skeleton, doCheck, useUnit);
        this.maxSteps = maxSteps;
        nextViolatedConstraints = new HashSet<Constraint>();
    }

    public PowerBayesNet orientEdges() {
        return orientEdges(skeleton, skeleton.getConstraints());
    }

    public PowerBayesNet orientEdges(SkeletonFinder skel, Set<Constraint> constraints) {

        List<PowerBayesNet> bestBNs = new ArrayList<PowerBayesNet>();
        double bestScore = Double.POSITIVE_INFINITY;
        List<CSPState> visitedStates = new ArrayList<CSPState>();

        Set<List<Integer>> triples = skeleton.findTriples();

        for (int i = 0; i < numRestarts; i++) {

            int stepsDownhill = 0;
            int step = 1;

            boolean[][] initialDag = getRandomOrientation(skel);
            double initialScore = countViolatedConstraints(initialDag, false, constraints);
            if (bestBNs.size() == 0) {
                bestBNs.add(new PowerBayesNet(data, initialDag));
                bestScore = initialScore;
            }

            log.info("Starting run " + i + " with score " + initialScore);

            CSPState currState = new CSPState(initialDag, initialScore);

            double lastScore = initialScore;

            while (stepsDownhill <= maxSteps) {

                boolean[][] currDag = currState.getDag();
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
                    //                           x --> z --> y
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
                log.debug("Found " + possibleSuccessors.size() + " possible successors (out of " + numAdded + ").");
                PriorityQueue<CSPState> successors = new PriorityQueue<CSPState>();

                for (CSPState succState : possibleSuccessors) {
                    boolean alreadyVisited = false;
                    for (CSPState visitedState : visitedStates) {
                        if (GraphUtil.equalDag(visitedState.getDag(), succState.getDag())) {
                            alreadyVisited = true;
                            log.debug("Found a previously visited state.");
                            break;
                        }
                    }

                    if (!alreadyVisited) {
                        double nextScore = countViolatedConstraints(succState.getDag(), false, constraints);
                        succState.setScore(nextScore);
                        successors.offer(succState);
                    }

                }
                log.debug("Found " + successors.size() + " successors.");

                //No successors that we haven't already visited.
                if (successors.size() == 0) {
                    break;
                }

                currState = successors.poll();
                double succScore = currState.getScore();
                visitedStates.add(currState);

                while (visitedStates.size() > maxMemory) {
                    visitedStates.remove(0);
                }

                //If the best successor is better than anything we have found so far.
                if (Double.compare(succScore, bestScore) < 0) {
                    bestBNs.clear();
                    bestScore = succScore;
                    stepsDownhill = 0;
                    bestBNs.add(new PowerBayesNet(data, currState.getDag()));
                    log.info("Found new best state at step " + step + " with score " + bestScore);
                } else {
                    if (stepsDownhill == 0) {
                        log.debug("Hit plateau at step " + step);
                    }
                    stepsDownhill++;
                }
                log.debug("Best successor score= " + succScore + " at step " + step);

                /*if (Double.compare(lastScore, succScore) == 0) {
                    countViolatedConstraints(currState.getDag(), false, constraints);
                    if (!currViolatedConstraints.containsAll(nextViolatedConstraints) ||
                            !nextViolatedConstraints.containsAll(currViolatedConstraints)) {
                        log.info("Found different equivalence class with same score.");
                        Assert.condition(false, "Found a different equivalence class.");
                    }
                }

                lastScore = succScore;
                currViolatedConstraints = new HashSet<Constraint>(nextViolatedConstraints); */

                step++;
            }

            log.info("Finished run " + i + " with score " + currState.getScore() + " in " + step + " steps.");
        }

        finalScore = bestScore;
        return breakTiesBDeu(bestBNs);
    }

}
