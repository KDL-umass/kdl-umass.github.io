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
import kdl.bayes.util.constraint.Constraint;

import java.util.HashSet;
import java.util.Set;

public class RelaxState implements Comparable {

    SkeletonFinder skeleton;
    double bdeuScore;
    Set<Constraint> violatedConstraints;
    Set<Constraint> validConstraints;
    PowerBayesNet bn;

    public RelaxState(SkeletonFinder skeleton, double score, Set<Constraint> violated, Set<Constraint> valid) {
        this.skeleton = skeleton;
        bdeuScore = score;
        violatedConstraints = new HashSet<Constraint>(violated);
        validConstraints = new HashSet<Constraint>(valid);
    }

    public RelaxState(SkeletonFinder skeleton, double score, Set<Constraint> violated, Set<Constraint> valid, PowerBayesNet bn) {
        this.skeleton = skeleton;
        bdeuScore = score;
        violatedConstraints = new HashSet<Constraint>(violated);
        validConstraints = new HashSet<Constraint>(valid);
        this.bn = bn;
    }

    public SkeletonFinder getSkeleton() {
        return skeleton;
    }

    public double getScore() {
        return bdeuScore;
    }

    public Set<Constraint> getViolatedConstraints() {
        return violatedConstraints;
    }

    public Set<Constraint> getValidConstraints() {
        return validConstraints;
    }

    public int compareTo(Object o) {
        if (o instanceof RelaxState) {
            RelaxState other = (RelaxState) o;
            return Double.compare(bdeuScore, other.getScore());
        }
        return -1;
    }

    public PowerBayesNet getBN() {
        return bn;
    }


}
