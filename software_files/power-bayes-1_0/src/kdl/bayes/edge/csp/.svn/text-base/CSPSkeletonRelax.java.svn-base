/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.csp;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.skeleton.FAS;
import kdl.bayes.skeleton.SkeletonFinder;
import kdl.bayes.util.constraint.Constraint;
import kdl.bayes.util.constraint.Dependence;
import kdl.bayes.util.constraint.dSeparation;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class CSPSkeletonRelax {

    protected static Logger log = Logger.getLogger(CSPSkeletonRelax.class);

    Instances data;
    CSPEdgeOrientation edgeOrientation;

    int statesScored = 0;
    double bdeu = -1;
    Set<Constraint> constraints;
    int trainIndex;

    public CSPSkeletonRelax(Instances trainInstances, CSPEdgeOrientation edgeOrientation, int trainIndex) {
        this.data = trainInstances;
        this.edgeOrientation = edgeOrientation;
        this.trainIndex = trainIndex;
    }

    public PowerBayesNet search() throws Exception {
        //Start with result of edge orientation on original skeleton.
        PowerBayesNet currBN = edgeOrientation.orientEdges();
        double currScore = currBN.logBDeuScore();

        FAS skeleton = (FAS) edgeOrientation.getSkeleton();

        RelaxState currState = new RelaxState(skeleton, currScore, new HashSet<Constraint>(), skeleton.getConstraints(), currBN);

        double bestScore = currScore;
        log.info("Initial score: " + currScore);

        RelaxState bestState = currState;
        boolean updated;

        do {
            updated = false;
            currScore = Double.NEGATIVE_INFINITY;
            SkeletonFinder currSkeleton = currState.getSkeleton();
            Set<Constraint> valid = currState.getValidConstraints();
            Set<Constraint> invalid = currState.getViolatedConstraints();

            log.info("Considering " + (invalid.size() + 1) + " violated constraints.");

            for (Constraint constraint : valid) {
                SkeletonFinder newSkeleton = new FAS((FAS) currSkeleton);
                Set<Constraint> newValid = new HashSet<Constraint>(valid);
                newValid.remove(constraint);
                Set<Constraint> newViolated = new HashSet<Constraint>(invalid);
                newViolated.add(constraint);

                log.debug(constraint.toString());

                //Toggle the edge corresponding to this constraint.
                if (constraint instanceof Dependence) {
                    Dependence d = (Dependence) constraint;
                    int x = d.getX();
                    int y = d.getY();
                    //Dependence means edge in current skeleton, so remove edge with empty sepset.
                    //NB: Empty sepset shouldn't cause any troubles as removing this edge shouldn't create any
                    // colliders.  If it does create a collider then there was a triangle in the current network.
                    newSkeleton.removeEdge(x, y);
                    newSkeleton.setSepset(x, y, null);
                }

                if (constraint instanceof dSeparation) {
                    dSeparation d = (dSeparation) constraint;
                    int x = d.getX();
                    int y = d.getY();

                    //Toggling dSeparation means to add edge maintaining existing sepset.
                    newSkeleton.addEdge(x, y);
                }

                PowerBayesNet newBn = edgeOrientation.orientEdges(newSkeleton, newValid);
                double newScore = newBn.logBDeuScore();

                log.debug("New Score: " + newScore + " Curr Score: " + currScore);

                if (Double.compare(newScore, currScore) >= 0) {
                    currState = new RelaxState(newSkeleton, newScore, newViolated, newValid, newBn);
                    currScore = newScore;

                    log.info("FOUND MISTAKE: constraint=" + constraint.toString() + " score=" + currScore);
                }
            }

            log.info("Best score at " + (invalid.size() + 1) + " :" + currScore);

            if (Double.compare(currScore, bestScore) > 0) {
                bestState = currState;
                bestScore = currScore;
                updated = true;
            }

            String xmlOutFilename = data.relationName() + ".relax.bn." + data.numInstances() + "." + trainIndex + "." + (invalid.size() + 1) + ".xml";

            PrintWriter outfile = new PrintWriter(new FileWriter(xmlOutFilename));
            outfile.print(bestState.getBN().toXMLBIF03());
            outfile.close();

        } while (updated);

        bdeu = bestState.getScore();
        log.info("Final score: " + bdeu);
        statesScored = edgeOrientation.getStatesScored();
        constraints = bestState.getValidConstraints();
        return bestState.getBN();

    }

    public double getScore() {
        return bdeu;
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

}
