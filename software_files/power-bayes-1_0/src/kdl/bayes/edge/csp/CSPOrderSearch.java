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
import kdl.bayes.util.constraint.Constraint;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.*;

public class CSPOrderSearch extends CSPEdgeOrientation {
    protected static Logger log = Logger.getLogger(CSPOrderSearch.class);

    double k = 0.5;
    int numRestarts = 5;

    public CSPOrderSearch(Instances trainInstances, SkeletonFinder skeleton, double k, int numRestarts) {
        super(trainInstances, skeleton);
        this.k = k;
        this.numRestarts = numRestarts;
    }

    public CSPOrderSearch(Instances trainInstances, SkeletonFinder skeleton, double k, int numRestarts, boolean doCheck, boolean useUnit) {
        super(trainInstances, skeleton, doCheck, useUnit);
        this.k = k;
        this.numRestarts = numRestarts;
    }

    public PowerBayesNet orientEdges() {
        return orientEdges(skeleton, skeleton.getConstraints());
    }

    public PowerBayesNet orientEdges(SkeletonFinder skel, Set<Constraint> constraints) {

        List<List<Integer>> globalBestOrders = new ArrayList<List<Integer>>();
        double globalBestScore = Double.POSITIVE_INFINITY;

        List<Integer> initOrder = new ArrayList<Integer>();
        int numVars = skel.getNumVariables();
        for (int i = 0; i < numVars; i++) {
            initOrder.add(i);
        }

        for (int i = 0; i < numRestarts; i++) {
            //Select an ordering for the nodes
            List<Integer> currOrder = new ArrayList<Integer>(initOrder);
            Collections.shuffle(currOrder, random);

            boolean[][] currGraph = orientSkeletonWithOrder(skel, currOrder);
            double currScore = countViolatedConstraints(currGraph, false, constraints);

            while (true) {
                log.debug("Starting step with score " + currScore);
                List<OrderState> improvedList = new ArrayList<OrderState>();

                PowerSetIterator swapIter = new PowerSetIterator(currOrder, 2);
                while (swapIter.hasNext()) {
                    Set<Integer> pair = (Set<Integer>) swapIter.next();

                    Iterator pairIterator = pair.iterator();

                    int item1 = (Integer) pairIterator.next();
                    int item2 = (Integer) pairIterator.next();

                    List<Integer> nextOrder = swap(currOrder, item1, item2);
                    boolean[][] successor = orientSkeletonWithOrder(skel, nextOrder);

                    double nextScore = countViolatedConstraints(successor, false, skeleton.getConstraints());

                    OrderState succState = new OrderState(nextOrder, nextScore);

                    //If successor is an improvement over the current state.
                    if (Double.compare(nextScore, currScore) < 0) {
                        log.debug("\t Found Improved score " + nextScore);
                        improvedList.add(succState);
                    }
                }

                if (improvedList.size() > 0) {
                    int size = (int) Math.round(Math.max(1, improvedList.size() * k));
                    Collections.shuffle(improvedList);
                    PriorityQueue<OrderState> C = new PriorityQueue<OrderState>();
                    for (int j = 0; j < size; j++) {
                        C.offer(improvedList.get(j));
                    }

                    OrderState best = C.poll();
                    currOrder = best.getOrder();
                    currScore = best.getScore();

                } else {
                    break;
                }
            }

            log.info("Finished run " + i + " with score " + currScore);
            //Update the bestList across restarts
            if (Double.compare(currScore, globalBestScore) < 0) {
                globalBestScore = currScore;
                globalBestOrders.clear();
                globalBestOrders.add(currOrder);
                log.debug("Updating Global best: score=" + globalBestScore + " (" + i + ")");
            } else if (Double.compare(currScore, globalBestScore) == 0) {
                globalBestOrders.add(currOrder);
                log.debug("Adding another best BN " + "(" + i + ")");
            }
        }

        finalScore = globalBestScore;
        List<PowerBayesNet> globalBestBNs = new ArrayList<PowerBayesNet>();
        for (List<Integer> bestOrder : globalBestOrders) {
            globalBestBNs.add(new PowerBayesNet(data, orientSkeletonWithOrder(skel, bestOrder)));
        }
        return breakTiesBDeu(globalBestBNs);
    }

    public List<Integer> swap(List<Integer> order, int item1, int item2) {
        List<Integer> newOrder = new ArrayList<Integer>(order);

        int item1Index = newOrder.indexOf(item1);
        int item2Index = newOrder.indexOf(item2);

        Collections.swap(newOrder, item1Index, item2Index);

        return newOrder;
    }

}
