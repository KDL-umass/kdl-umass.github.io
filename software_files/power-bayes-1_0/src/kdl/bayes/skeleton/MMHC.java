/**
 * $Id: MMHC.java 247 2008-05-06 14:32:54Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.skeleton;

import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MMHC
 * For details, see:
 * The Max-Min Hill-Climbing Bayesian Network Structure Learning Algorithm
 * Ioannis Tsamardinos, Laura E. Brown, Constantin F. Aliferis
 * Machine Learning Journal, 2006
 * Author: mhay
 */
public class MMHC extends SkeletonFinder {
    protected static Logger log = Logger.getLogger(MMHC.class);

    private boolean useOptimization;  // optimization 5, section 6 of tsamardinos2006the-max-min
    public MMPC neighborFinder;

    public MMHC(Instances instances, MMPC pcFinder) {
        this(instances, pcFinder, false);
    }

    public MMHC(Instances instances, MMPC pcFinder, boolean useOptimization) {
        super(instances);
        this.useOptimization = useOptimization;
        this.neighborFinder = pcFinder;
    }

    /**
     * Run the MMPC Algorithm from Tsamardinos et al. for every variable
     * Utilizes optimizations 1-4, with 5 being optional.
     *
     * @return number of statistical calls
     */
    public int computeNeighbors() {
        runMMHC();
        enforceAndConsistency();
        return statCalls;
    }

    public void runMMHC() {
        neighbors = new ArrayList<List<Integer>>();
        Set<Integer> variables = new HashSet<Integer>();
        for (int varIdx = 0; varIdx < numVariables; varIdx++) {
            variables.add(varIdx);
        }

        for (int tIdx = 0; tIdx < numVariables; tIdx++) {
            // when building the cpc for target T, we make use of the CPCs we've
            // already built for other variables X.  We look at the CPC for X,
            // if T is in it, then go ahead and include X in the CPC for T.
            // However, if T is not in it, then exclude X from further
            // consideration.
            List<Integer> xIdxsToExclude = new ArrayList<Integer>();
            List<Integer> xIdxsToInclude = new ArrayList<Integer>();

            //for (Iterator<List<Integer>> iterator = neighbors.iterator(); iterator.hasNext();) {
            //    int xIdx = iterator.next();
            //    List<Integer> cpcForX = neighbors.get(xIdx);
            for (int xIdx = 0; xIdx < neighbors.size(); xIdx++) {
                List<Integer> cpcForX = neighbors.get(xIdx);
                if (cpcForX.contains(tIdx)) {
                    xIdxsToInclude.add(xIdx);
                } else {
                    xIdxsToExclude.add(xIdx);
                }
            }

            if (!useOptimization) {
                xIdxsToInclude = new ArrayList<Integer>();
                //xIdxsToExclude = new ArrayList<Integer>();
            }
            long cpc_runtime = System.currentTimeMillis();
            List<Integer> cpc = neighborFinder.computeCPC(data, tIdx, xIdxsToInclude, xIdxsToExclude);
            cpc_runtime = System.currentTimeMillis() - cpc_runtime;
            int numStatCalls = neighborFinder.getNumStatisticalCalls();
            int buildCPCsize = neighborFinder.getBuildCPCSize();
            statCalls += numStatCalls;
            runtime += cpc_runtime;
            neighbors.add(tIdx, cpc);
            log.debug("TIMING:" + " target=" + data.getName(tIdx) + " cpcSize=" + cpc.size() + " buildCPCsize=" + buildCPCsize + " runtime=" + cpc_runtime + " statCalls=" + numStatCalls);
        }
    }


}
