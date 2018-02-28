/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.matching;

import kdl.bayes.skeleton.util.BayesData;
import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Map;

/**
 * Chi-square stat implements an unpaired test for matched pairs.
 */

public class ChiSquareStat implements StatisticModule {

    protected static Logger log = Logger.getLogger(ChiSquareStat.class);
    double pValThreshold = 0.05;

    public ChiSquareStat() {

    }

    public ChiSquareStat(double pVal) {
        pValThreshold = pVal;
    }


    public double getSignificance(int xIdx, int yIdx, Instances treatment, Instances nonTreatment, Map<Integer, Integer> matching) {
        //First combine treatment and non-treatment matches into a single Instances
        Instances instances = new Instances(treatment);

        //log.info(matching.values().size());

        //for(Integer key : matching.keySet()){
        //    log.info("key: " + key + " value: " + matching.get(key));
        //}

        for (Integer key : matching.keySet()) {

            int match = matching.get(key);
            log.info("key: " + key + " value: " + match);
            Instance matchedInstance = nonTreatment.instance(match);
            instances.add(matchedInstance);
        }

        BayesData qData = new BayesData(instances);

        log.info("x=" + qData.getName(xIdx) + " y=" + qData.getName(yIdx) + " n=" + instances.numInstances());
        log.info("treatment=" + treatment.numInstances() + " nonTreatment=" + nonTreatment.numInstances());

        int[] zIdxs = new int[]{};
        // set the indices x,y, and new attr index
        qData.setIndexes(xIdx, yIdx, zIdxs);
        // run independence test
        qData.computeStats(); //Ignore power considerations for now
        //log.info(xName + "," + yName);
        double[] score = qData.assoc(pValThreshold);


        return score[5];


    }
}
