/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 3, 2009
 * Time: 10:32:51 AM
 */
package rpc.statistics;

import rpc.dataretrieval.StatisticResults;

/**
 * Hypothesis test interface for statistical tests of independence.
 */
public interface HypothesisTest {

    /**
     * All hypothesis tests must be able to compute a statistic results object consisting
     * of the test statistic, p-value, sample size, and strength of effect.
     * @return the result of the statistical test.
     */
    public StatisticResults getStatistic();

}
