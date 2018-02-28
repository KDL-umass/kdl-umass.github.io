/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.search.BNConstrainedSearchProblem;
import kdl.bayes.search.BNSearchProblem;
import kdl.bayes.search.BNSearchState;
import kdl.bayes.search.TabuHillClimb;
import kdl.bayes.skeleton.util.PowerThreshold;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PowerCV extends SkeletonFinder {
    List<Double> esThresholds;
    PowerBayesNet trueBN;

    public PowerCV(Instances instances, PowerBayesNet trueBN, List<Double> thresholds) {
        super(instances);
        esThresholds = thresholds;
        this.trueBN = trueBN;
    }

    public PowerCV(Instances instances, PowerBayesNet trueBN, boolean useFixedInteval) {
        super(instances);
        this.trueBN = trueBN;
        esThresholds = new ArrayList<Double>();
        double esMin = 0.0;
        double esIncrement = 0.0;
        if (useFixedInteval) {
            esMin = 0.05;
            esIncrement = esMin;
        } else {
            //Increments chosen to search between the minimum es to run some
            // tests and the es that matches the rule of thumb.
            // mins: 0.1612,0.1140,0.0806,0.0510
            // incs: 0.03520 0.03242 0.02940 0.02532
            if (instances.numInstances() == 500) {
                esMin = 0.1612;
                esIncrement = 0.0352;
            } else if (instances.numInstances() == 1000) {
                esMin = 0.1140;
                esIncrement = 0.03242;
            } else if (instances.numInstances() == 2000) {
                esMin = 0.0806;
                esIncrement = 0.0294;
            } else if (instances.numInstances() == 5000) {
                esMin = 0.0510;
                esIncrement = 0.0253;
            }
        }

        for (int i = 0; i < 5; i++) {
            esThresholds.add(esMin + (i * esIncrement));
        }
    }

    public PowerCV(Instances instances, PowerBayesNet trueBN) {
        this(instances, trueBN, false);
    }


    /**
     * Uses cross-validation to compute the best effect-size threshold for a given dataset.
     *
     * @return
     */
    public int computeNeighbors() {
        //Initialize train/test folds.
        List<Instances> trainFolds = new ArrayList<Instances>();
        List<Instances> testFolds = new ArrayList<Instances>();
        for (int i = 0; i < 10; i++) {
            Instances trainFold = instances.trainCV(10, i);
            trainFolds.add(trainFold);
            Instances testFold = instances.testCV(10, i);
            testFolds.add(testFold);
        }

        List<Double> bestEsByFold = new ArrayList<Double>();
        double totalEs = 0.0;

        //Now, run the Power algorithm on each trainFold with each threshold to determine which one performs best.
        for (int i = 0; i < trainFolds.size(); i++) {

            log.info("\tStarting Fold " + i);

            double maxEs = 0.0;
            double maxLikelihood = -Double.MAX_VALUE;

            Instances trainInstances = trainFolds.get(i);
            Instances testInstances = testFolds.get(i);

            for (int j = 0; j < esThresholds.size(); j++) {
                PowerBayesNet bn = new PowerBayesNet(trainInstances);
                double es = esThresholds.get(j);
                log.info("\t\tRunning with es=" + es);
                PowerThreshold threshold = new PowerThreshold(trainInstances.numInstances(), es, 0.95, 0.05);
                MMHC mmhc = new MMHC(trainInstances, new MMPC(threshold, true));
                int sk_statCalls = mmhc.computeNeighbors();
                boolean[][] skeleton = mmhc.getSkeleton();

                BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
                ((BNSearchState) problem.getInitialState()).setTrueBn(trueBN);

                bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
                statCalls += problem.getStatisticalCalls() + sk_statCalls;
                bn.estimateCPTs(trainInstances);
                double loglikelihood = bn.logProbability(testInstances);

                //log.info("loglikelihood: " + loglikelihood + " Max:" + maxLikelihood);

                if (Double.compare(loglikelihood, maxLikelihood) > 0) {
                    //log.info("Updating ES");
                    maxLikelihood = loglikelihood;
                    maxEs = es;
                }
            }

            bestEsByFold.add(maxEs);
            totalEs += maxEs;

        }

        log.info(Arrays.deepToString(bestEsByFold.toArray()));
        log.info(totalEs);

        double es = totalEs / 10;

        log.info("ES: es=" + es + " n=" + instances.numInstances());

        PowerThreshold threshold = new PowerThreshold(instances.numInstances(), es, 0.95, 0.05);
        MMHC mmhc = new MMHC(instances, new MMPC(threshold, true));

        int finalStatCalls = mmhc.computeNeighbors();

        this.neighbors = mmhc.neighbors;

        return statCalls + finalStatCalls;
    }

}
