/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 3, 2009
 * Time: 10:35:42 AM
 */
package rpc.statistics;

import rpc.dataretrieval.StatisticResults;
import rpc.dataretrieval.StatisticsEngine;
import rpc.dataretrieval.DataRetrieval;
import org.apache.log4j.Logger;

/**
 * The chi-square statistical test of independence for 2 discrete variables.
 */
public class ChiSquareTest implements HypothesisTest {

    private static Logger log = Logger.getLogger(ChiSquareTest.class);

    private double[][] cont;

    /**
     * Initialize the chi-square test by creating the 2-dimensional contingency table.
     * @param dr the data retrieval object that contains the data for constructing the contingency table.
     */
    public ChiSquareTest(DataRetrieval dr) {
        this.cont = dr.getContingencyTable().getTable();
    }

    /**
     * Initialize the chi-square test by providing the 2-dimensional contingency table.
     * @param cont the 2-dimensional contingency table.
     */
    public ChiSquareTest(double[][] cont) {
        //this constructor useful for calling when defaulting to chisquare from CMH test
        this.cont = cont;
    }

    /**
     * Run the chi-square statistical test and compute the resulting statistics.
     * @return the results of the chi-square statistical test.
     */
    public StatisticResults getStatistic() {        
        StatisticsEngine.eval("source('./src/java/rpc/statistics/g.test.r')");

        StringBuffer sb = new StringBuffer();

        double sampleSize = 0.0;
        sb.append("matrix(c(");
        for (double[] row : this.cont) {
            for (double val : row) {
                sb.append(val);
                sb.append(",");
                sampleSize += val;
            }
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("), nrow=");
        sb.append(this.cont[0].length);
        sb.append(", ncol=");
        sb.append(this.cont.length);
        sb.append(")");

        StatisticResults statRes;
        if (this.cont.length <= 1 || this.cont[0].length <= 1) {
            //don't compute a test statistic
            //either treatment or outcome variable is constant -> no association!
            statRes = new StatisticResults(-1, -1, 1, -1, 0, "Treatment or outcome doesn't vary.  No causality", false);
        }
        else {
            log.debug(sb);
            // compute the chi-sq test and get the pval
            StatisticsEngine.eval("t <- chisq.test(" + sb + ")");

            if (StatisticsEngine.eval("is.nan(t$statistic)").asBool().isTRUE()) {
                StatisticsEngine.eval("t <- g.test(" + sb + ")");
                double statistic = StatisticsEngine.eval("t$statistic").asDouble();
                double dof = StatisticsEngine.eval("t$parameter").asDouble();
                double pval = StatisticsEngine.eval("t$p.value").asDouble();
                String method = StatisticsEngine.eval("t$method").asString();
                double phisq = phisquare(statistic, sampleSize, this.cont.length, this.cont[0].length);
                statRes = new StatisticResults(statistic, dof, pval, sampleSize, phisq, method, true);
            }
            else {
                double statistic = StatisticsEngine.eval("t$statistic").asDouble();
                double dof = StatisticsEngine.eval("t$parameter").asDouble();
                double pval = StatisticsEngine.eval("t$p.value").asDouble();
                String method = StatisticsEngine.eval("t$method").asString();
                double phisq = phisquare(statistic, sampleSize, this.cont.length, this.cont[0].length);
                statRes = new StatisticResults(statistic, dof, pval, sampleSize, phisq, method, true);
            }
        }
        //remove "t" from R space so that if subsequent test fails it won't reuse
        //results of previous test (No silent failures)
        StatisticsEngine.eval("remove(t)");

        if (statRes.statistic == 0.0 && statRes.degreesOfFreedom == 0.0 && statRes.pval == 0.0 && statRes.testName == null) {
            StatisticResults failedTest = new StatisticResults(-1, -1, 1, -1, 0, "Test failed, not enough data", false);
            failedTest.failed();
            return failedTest;
        }
        else {
            return statRes;
        }

    }

    private double phisquare(double chisquare, double n, int rows, int columns) {
        double cc = Math.sqrt(chisquare/(n+chisquare));
        double r = Math.min(rows, columns);
        double ccmax = Math.sqrt((r-1)/r);
        return cc/ccmax;                                                                                                
    }
}