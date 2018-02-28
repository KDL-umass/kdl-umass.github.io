/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import kdl.bayes.skeleton.SkeletonFinder;
import kdl.bayes.util.constraint.Constraint;

import java.util.Set;

public class TieredBNSearch {

    SkeletonFinder skeleton;

    Set<Constraint> constraints;

    BNCSP tier1csp;
    BNCSP tier2csp;
    BNCSP tier3csp;
    BNCSP tier4csp;

    Assignment indepAssignment;

    public TieredBNSearch(SkeletonFinder skeleton) {
        this.skeleton = skeleton;

        //Construct tiered csps here.

        //Construct indep assignment here
        indepAssignment = createIndepAssignment();


    }

    public BNCSP createTier1() {
        return null;
    }

    public Assignment createIndepAssignment() {

        Assignment indepAssignment = new Assignment();

        int numVertices = skeleton.getNumVariables();

        for (int i = 0; i < numVertices; i++) {
            for (int j = i + 1; j < numVertices; j++) {
                if (!skeleton.hasEdge(i, j)) {
                    int edgeVar = j * numVertices + i;
                    indepAssignment.setVar(edgeVar, "E");
                }
            }
        }

        return indepAssignment;
    }

    public Assignment search() {

        int N = 100;

        BranchAndBound tier1search = new BranchAndBound(tier1csp);
        Set<Assignment> tier1Sets = tier1search.search(N, 0, indepAssignment);

        Assignment tier1 = tier1Sets.toArray(new Assignment[tier1Sets.size()])[0];

        int conflicts = tier1csp.numConflicts(tier1);
        if (conflicts < N) {
            N = conflicts;
        }

        BranchAndBound tier2search = new BranchAndBound(tier2csp);
        Set<Assignment> tier2Sets = tier2search.search(N, 0, indepAssignment);
        Assignment tier2 = tier2Sets.toArray(new Assignment[tier2Sets.size()])[0];

        conflicts = tier2csp.numConflicts(tier2);
        if (conflicts < N) {
            N = conflicts;
        }

        BranchAndBound tier3search = new BranchAndBound(tier3csp);
        Set<Assignment> tier3Sets = tier3search.search(N, 0);
        Assignment tier3 = tier3Sets.toArray(new Assignment[tier3Sets.size()])[0];

        conflicts = tier3csp.numConflicts(tier3);
        if (conflicts < N) {
            N = conflicts;
        }

        BranchAndBound tier4search = new BranchAndBound(tier4csp);
        Set<Assignment> tier4Sets = tier4search.search(N, 0);
        Assignment tier4 = tier4Sets.toArray(new Assignment[tier4Sets.size()])[0];

        return tier4;


    }

}
