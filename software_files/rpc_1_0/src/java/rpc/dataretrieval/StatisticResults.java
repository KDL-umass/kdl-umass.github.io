/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 19, 2009
 * Time: 10:50:54 AM
 */
package rpc.dataretrieval;

import org.apache.log4j.Logger;

/**
 * The StatisticsResults object holds information about the results of a statistical test, including the test statistic,
 * degrees of freedom, p-value, sample size, and strength of effect.
 */
public class StatisticResults {

    private static Logger log = Logger.getLogger(StatisticResults.class);

    /**
     * The test statistic.
     */
    public double statistic;
    /**
     * The degrees of freedom.
     */
    public double degreesOfFreedom;
    /**
     * The p-value.
     */
    public double pval;
    /**
     * The sample size.
     */
    public double sampleSize;
    /**
     * The phi square strength of effect.
     */
    public double phisquare;
    /**
     * The name of the statistical test used to produce these results.
     */
    public String testName;
    /**
     * Can we compute power for the test? (Currently only supported with chi-square test.)
     */
    public boolean computePower;
    private boolean failed = false;

    /**
     * Constructs a StatisticResults object that holds information about the result of a statistical test.
     * @param stat the test statistic.
     * @param dof the degrees of freedom.
     * @param pval the p-value.
     * @param sampleSize the sample size.
     * @param phiSquare the strength of effect.
     * @param testName the name of the statistical test.
     * @param computePower whether or not power can be computed.
     */
    public StatisticResults(double stat, double dof, double pval, double sampleSize, double phiSquare,
                            String testName, boolean computePower) {
        this.statistic = stat;
        this.degreesOfFreedom = dof;
        this.pval = pval;
        this.sampleSize = sampleSize;
        this.phisquare = phiSquare;
        this.testName = testName;
        this.computePower = computePower;
    }

    /**
     * Check if the statistical test failed.
     * @return true if the test failed; false otherwise.
     */
    public boolean isFailed() {
        return this.failed;
    }

    /**
     * Compute the power of the statistical test.  Currently only supports chi-square test.
     * @return the statistical power of the test.
     */
    public double power() {
        if (this.computePower) {
            //Chi-square effect size w = sqrt( X2/(X2+N)  / (1 - X2/(X2+N) ) )
            double temp = this.statistic / (this.statistic + this.sampleSize);
            double effectSizeW = Math.sqrt(temp / (1.0-temp));
            StatisticsEngine.eval("library(pwr)");
            StatisticsEngine.eval("p <- pwr.chisq.test(" + effectSizeW + ", " + this.sampleSize  + ", " +
                    this.degreesOfFreedom + ")");
            return StatisticsEngine.eval("p$power").asDouble();
        }
        else {
            log.error("Cannot determine power, not a chisquare test");
            return -1;
        }
    }

    /**
     * Set the strength of effect for the statistical test.
     * @param phiSquare the phi square strength of effect.
     */
    public void setPhiSquare(double phiSquare) {
        this.phisquare = phiSquare;
    }

    /**
     * Denotes the fact that the statistical test failed.
     */
    public void failed() {
        this.failed = true;
    }

    /**
     * Returns a string representation containing information about the results of the statistical test.
     * @return the string representation.
     */
    public String toString() {
        return "[" + this.testName + ", " + this.statistic + "\n" +
                    this.degreesOfFreedom + ", " + this.pval + ", " + this.phisquare + ", " + this.sampleSize + "]";
    }
    
}