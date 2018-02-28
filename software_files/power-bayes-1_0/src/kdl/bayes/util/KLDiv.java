/**
 * $Id: KLDiv.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * KLDiv
 * Author: mhay
 */
public class KLDiv {
    protected static Logger log = Logger.getLogger(KLDiv.class);
    private static final double logMinValue = Math.log(Double.MIN_VALUE) + Math.log(1000);

    /**
     * Q is the true distribution, P is the learned distribution,
     * measures 'distance' between Q and P
     * <p/>
     * NB: assumes the instances contain NO DUPLICATES
     *
     * @param q
     * @param p
     * @param instances
     * @return k-l divergence between q and p
     */
    public static double klDivergence(ProbDistribution q,
                                      ProbDistribution p,
                                      Instances instances) {
        double kldiv = 0.0;
        double kldivUsingLogs = 0.0;
        double minLogProb = Double.MAX_VALUE;
        double totalProb = 0.0;

        // find max
        double b_max = Double.NEGATIVE_INFINITY;
        Enumeration enumInsts = instances.enumerateInstances();
        while (enumInsts.hasMoreElements()) {
            Instance instance = (Instance) enumInsts.nextElement();
            double logProbQ = q.logProbability(instance);
            if (logProbQ > b_max) {
                b_max = logProbQ;
            }
        }

        double sum_b = 0.0;

        enumInsts = instances.enumerateInstances();
        while (enumInsts.hasMoreElements()) {
            Instance instance = (Instance) enumInsts.nextElement();
            double logProbQ = q.logProbability(instance);
            Assert.condition(logProbQ > logMinValue, "Log probability is too small, underflow will occur.");
            double probQ = Math.exp(logProbQ);
            totalProb += probQ;

            //todo: remove ME
            //log.debug("p=" + probQ + " instance=" + instance.toString());

            double b_m = logProbQ;
            double temp = b_m - b_max;
            sum_b += Math.exp(temp);

            if (logProbQ < minLogProb) {
                minLogProb = logProbQ;
            }
            double logProbP = p.logProbability(instance);
            logProbQ = logProbQ / Math.log(2); // convert to base 2
            logProbP = logProbP / Math.log(2); // convert to base 2
            kldiv += probQ * (logProbQ - logProbP);
            kldivUsingLogs += Math.exp(temp) * (logProbQ - logProbP);
        }

        log.debug("minimum log probability: " + minLogProb + " exp is " + Math.exp(minLogProb));
        log.debug("sum probability: " + totalProb + " " + Math.log(totalProb));
        log.debug("sum probability using logs: " + Math.exp(Math.log(sum_b) + b_max) + " " + (Math.log(sum_b) + b_max));
        double klUsingLogs;
        if (Double.compare(kldivUsingLogs, 0) > 0) {
            klUsingLogs = Math.exp(b_max + Math.log(kldivUsingLogs));
        } else {
            log.warn("KL Divergence is zero or negative");
            klUsingLogs = Math.exp(b_max) * (kldivUsingLogs);
        }
        log.debug("KL using logs " + klUsingLogs);
        return kldiv;
    }

    /**
     * There is surely a better way...
     *
     * @param instances
     * @return new instances object that contains distinct instances in argument
     */
    public static Instances eliminateDuplicates(Instances instances) {
        StringBuffer sb = new StringBuffer();
        String instancesStr = instances.toString();
        String[] parts = instancesStr.split("@data\n");
        String header = parts[0] + "@data\n";
        sb.append(header);

        // sort the instances
        instancesStr = parts[1];
        String[] lines = instancesStr.split("\n");
        Arrays.sort(lines);

        // eliminate duplicates
        int duplicateCount = 0;
        int uniqueCount = 0;
        String lastLine = null;
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            String line = lines[lineIdx];
            if (line == null || !line.equals(lastLine)) {
                sb.append(line + "\n");
                lastLine = line;
                uniqueCount++;
            } else {
                duplicateCount++;
            }
        }

        log.info("duplicates=" + duplicateCount + " unique=" + uniqueCount);
        Instances newInstances = null;
        try {
            newInstances = new Instances(new StringReader(sb.toString()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error: " + e);
            System.exit(-1);
        }
        return newInstances;
    }
}
