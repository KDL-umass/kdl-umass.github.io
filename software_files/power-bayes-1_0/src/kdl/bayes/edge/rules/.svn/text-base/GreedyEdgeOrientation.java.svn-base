/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.rules;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.skeleton.SkeletonFinder;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class GreedyEdgeOrientation extends EdgeOrientation {

    protected static Logger log = Logger.getLogger(GreedyEdgeOrientation.class);

    PowerBayesNet bn;

    public GreedyEdgeOrientation(PowerBayesNet bn, SkeletonFinder skeleton) {
        super(skeleton);
        this.bn = bn;
    }

    public PowerBayesNet getBayesNet() {
        return bn;
    }

    public void orientColliders() {
        Set<List<Integer>> colliders = skeleton.findColliders();
        PriorityQueue<Collider> queue = initQueue(colliders);
        Collider bestMove = queue.poll();
        while (bestMove != null) {
            bestMove.apply(bn);
            updateQueue(queue, bn);
            bestMove = queue.poll();
        }
    }

    public PriorityQueue<Collider> initQueue(Set<List<Integer>> colliders) {

        PriorityQueue<Collider> queue = new PriorityQueue<Collider>();

        for (List<Integer> collider : colliders) {
            int xIdx = collider.get(0);
            int zIdx = collider.get(1);
            int yIdx = collider.get(2);

            //Orient triple: X - Y - Z as X -> Y <- Z
            Collider newCollider = new Collider(xIdx, yIdx, zIdx);
            newCollider.updateScore(bn);

            queue.add(newCollider);
        }
        return queue;
    }

    public void updateQueue(PriorityQueue<Collider> queue, PowerBayesNet bn) {
        List<Collider> toRemove = new ArrayList<Collider>();
        for (Collider collider : queue) {
            if (collider.isValid(bn)) {
                collider.updateScore(bn);
            } else {
                toRemove.add(collider);
                //queue.remove(collider);
            }
        }
        queue.removeAll(toRemove);

    }


}
