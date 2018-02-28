/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 3, 2009
 * Time: 10:40:16 AM
 */
package rpc.statistics;

import rpc.dataretrieval.DataRetrieval;
import rpc.dataretrieval.StatisticResults;
import rpc.dataretrieval.StatisticsEngine;
import rpc.dataretrieval.ContingencyTable3D;
import org.apache.log4j.Logger;

/**
 * The Cochran-Mantel-Haenszel (CHM) statistical test of independence between 2 discrete variables, conditioned on
 * some third set of discrete variables.
 */
public class CMHTest implements HypothesisTest {

    private static Logger log = Logger.getLogger(CMHTest.class);

    private DataRetrieval dr;

    /**
     * Initialize the CMH test.
     * @param dr the data retrieval object that contains the data for constructing
     * the 3-dimensional contingency table.
     */
    public CMHTest(DataRetrieval dr) {
        this.dr = dr;
    }

    /**
     * Construct the 3-dimensional contingency table, run the CMH test, and compute the resulting statistics.
     * @return the results of the CMH statistical test.
     */
    public StatisticResults getStatistic() {
        StatisticResults statRes;
        //design controlling for some configuration
        ContingencyTable3D cont = dr.getContingencyTable3D();
        double[][][] table = cont.getTable();

        if (table.length <= 1 && cont.getNumDimensions() <= 1) {
            return null; //there's only one configuration!
        }

        if (table.length <= 0 && cont.getNumDimensions() > 1) {
            //there were multiple configurations of the conditioning variables
            //but they were all contracted down to zero dimensions
            //so conclude independence
            statRes = new StatisticResults(-1, -1, 1, -1, 0, "Conditioning variable(s) completely remove dependence", false);
            return statRes;
        }

        if (table.length == 1 && cont.getNumDimensions() > 1) {
            //there were multiple configurations of the conditioning variables,
            //but only a single dimension remains, should run chi-square
            return new ChiSquareTest(table[0]).getStatistic();
        }

        if (table[0].length <= 1) {
            statRes = new StatisticResults(-1, -1, 1, -1, 0, "Treatment doesn't vary.  No causality", false);
            return statRes;
        }

        if (table[0][0].length <= 1) {
            statRes = new StatisticResults(-1, -1, 1, -1, 0, "Outcome doesn't vary.  No causality", false);
            return statRes;
        }

        StringBuffer sb = new StringBuffer();

        double sampleSize = 0.0;
        sb.append("array(c(");
        for (double[][] subTable : table) {
            for (double[] row : subTable) {
                for (double val : row) {
                    sb.append(val);
                    sb.append(",");
                    sampleSize += val;
                }
            }
        }

        sb.deleteCharAt(sb.length()-1);
        sb.append("), dim=c(");
        sb.append(table[0][0].length);
        sb.append(", ");
        sb.append(table[0].length);
        sb.append(", ");
        sb.append(table.length);
        sb.append("))");
        log.debug(sb);

        StatisticsEngine.eval("t <- mantelhaen.test(" + sb + ")");
        try {
            double statistic = StatisticsEngine.eval("t$statistic").asDouble();
            double dof = StatisticsEngine.eval("t$parameter").asDouble();
            double pval = StatisticsEngine.eval("t$p.value").asDouble();
            String method = StatisticsEngine.eval("t$method").asString();
            statRes = new StatisticResults(statistic, dof, pval, sampleSize, -1, method, false);

            //remove "t" from R space so that if subsequent test fails it won't reuse
            //results of previous test (No silent failures)
            StatisticsEngine.eval("remove(t)");

            if (statRes.statistic == 0.0 && statRes.degreesOfFreedom == 0.0 && statRes.pval == 0.0 && statRes.testName == null) {
                StatisticResults failedTest = new StatisticResults(-1, -1, 1, -1, 0, "Test failed, not enough data", false);
                failedTest.failed();
                return failedTest;
            }
            else {
                double phisq = phisquare(statistic, sampleSize, table[0].length, table[0][0].length);
                statRes.setPhiSquare(phisq);
                //statRes.setPhiSquare(weightedPhisquare(table, sampleSize));
                return statRes;
            }
        }
        catch (NullPointerException npe) {
            StatisticResults failedTest = new StatisticResults(-1, -1, 1, -1, 0, "Test failed, not enough data", false);
            failedTest.failed();
            return failedTest;
        }

    }

    private double phisquare(double cmhstat, double n, int rows, int columns) {
        double cc = Math.sqrt(cmhstat/(n+cmhstat));
        double r = Math.min(rows, columns);
        double ccmax = Math.sqrt((r-1)/r);
        return cc/ccmax;
    }

}
