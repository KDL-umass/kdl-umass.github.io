/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 23, 2009
 * Time: 11:11:18 AM
 */
package rpc.model.scoring;

import rpc.model.util.Model;
import rpc.model.util.VertexPair;
import rpc.model.util.Dependency;
import rpc.model.util.ModelSupport;
import rpc.model.OraclePC;
import rpc.design.Unit;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * Class that holds a collection of methods to analyze the results of running models.
 */
public final class ModelScoring {

    private static Logger log = Logger.getLogger(ModelScoring.class);

    private ModelScoring() {        
    }

    /**
     * Compare true and learned models by computing the number of edges that are either true positives, false
     * positives, true negatives, false negatives, directed in the wrong direction, undirected when should be
     * directed, and directed correctly.
     * @param trueModel the true model.
     * @param learnedModel the learned model.
     * @return a list of the counts of each type of error.
     */
    public static List<Integer> getErrorCounts(Model trueModel, Model learnedModel) {

        int numCorrectCompelled = 0;
        int FP = 0;
        int TN;
        int FN = 0;

        int numDirectionWrong = 0;  //Edge is directed in both but the direction is wrong
        int numUndirWhenDir = 0;    //Edge is undirected in the learned model but directed in the true model
        int numTrueCompelled = 0;   //Total number of dependencies in the true model

        Map<VertexPair, Set<Dependency>> trueDependencies = trueModel.getAllDependencies();

        for (Map.Entry<VertexPair, Set<Dependency>> me : trueDependencies.entrySet()) {
            VertexPair vp = me.getKey();
            Set<Dependency> deps = me.getValue();
            for (Dependency dep : deps) {
                if (dep.isTrivial()) {
                    continue;
                }
                numTrueCompelled++;

                if (learnedModel.checkDirectedDependence(vp, dep)){
                    numCorrectCompelled++;
                }
                else if (learnedModel.checkDependence(vp, dep) && learnedModel.checkDependence(vp.reverse(), dep.reverse())) {
                    numUndirWhenDir++;
                }
                else if (learnedModel.checkDirectedDependence(vp.reverse(), dep.reverse())) {
                    numDirectionWrong += 1;
                }
                else {
                    FN += 1;
                }
            }
        }

        Map<VertexPair, Set<Dependency>> learnedDependencies = learnedModel.getAllDependencies();

        //double count the false positives (one for each direction) and divide by 2 at the end
        for (Map.Entry<VertexPair, Set<Dependency>> me : learnedDependencies.entrySet()) {
            VertexPair vp = me.getKey();
            Set<Dependency> deps = me.getValue();
            for (Dependency dep : deps) {
                if (dep.isTrivial()) {
                    continue;
                }
                if (! trueModel.checkDependence(vp, dep) &&
                        ! trueModel.checkDependence(vp.reverse(), dep.reverse())) {
                    //true model doesn't contain this dependence (in either direction)

                    //check if we should double count
                    if (learnedModel.checkDependence(vp.reverse(), dep.reverse())) {
                        FP++;
                    }
                    else {
                        FP += 2;
                    }
                }
            }
        }
        FP /= 2;

        TN = learnedModel.getTotalDependencies() - numCorrectCompelled - numUndirWhenDir - numDirectionWrong - FP - FN;

        List<Integer> errorCounts = new ArrayList<Integer>();
        errorCounts.add(numCorrectCompelled);
        errorCounts.add(FP);
        errorCounts.add(TN);
        errorCounts.add(FN);
        errorCounts.add(numDirectionWrong);
        errorCounts.add(numUndirWhenDir );
        errorCounts.add(numTrueCompelled);

        //Error Count List
        // 0--> numCorrectCompelled
        // 1-->FP
        // 2-->TN
        // 3-->FN
        // 4-->numDirectionWrong
        // 5-->numUndirWhenDir
        // 6-->numTrueCompelled

        return errorCounts;
    }

    /**
     * Compare true and learned models at a finer granularity.  For each dependency in the true model, this determines
     * which edges were detected correctly, missed marginally, missed conditionally, insubstantive at the marginal
     * level or insubstantive at the conditional level given the thresholds for significance and strength of effect.
     * @param trueModel the true model.
     * @param learnedModel the learned model.
     * @param alpha the significance threshold.
     * @param strengthThresh the strenth of effect threshold.
     * @return a list of the counts of each type of error.
     */
    public static List<Integer> getSkeletonBreakdown(Model trueModel, Model learnedModel, double alpha, double strengthThresh) {
        int detected = 0;
        int missedMarginal = 0;
        int missedConditional = 0;
        int insubstantiveMarginal = 0;
        int insubstantiveConditional = 0;

        Map<VertexPair, Set<Dependency>> trueDependencies = trueModel.getAllDependencies();

        for (Map.Entry<VertexPair, Set<Dependency>> me : trueDependencies.entrySet()) {
            VertexPair vp = me.getKey();
            Set<Dependency> deps = me.getValue();
            for (Dependency dep : deps) {
                if (dep.isTrivial()) {
                    continue;
                }

                log.debug(dep.unit);
                double pval = getMarginalTestPVal(dep.unit);
                double phi = getMarginalTestStrength(dep.unit);
                if (learnedModel.checkDependence(vp, dep)) {
                    log.debug("detected dependence with pval " + pval + ", phi " + phi);
                    detected++;
                }
                else {
                    if (pval > alpha) {
                        log.debug("missed marginal dependence with pval " + pval + ", phi " + phi);
                        missedMarginal++;
                    }
                    else if (pval <= alpha && phi <= strengthThresh) {
                        log.debug("missed insubtantive marginal dependence with pval " + pval + ", phi " + phi);
                        insubstantiveMarginal++;
                    }
                    else {
                        double condPVal = getConditionalTestPVal(dep.unit);
                        double condPhi = getConditionalTestStrength(dep.unit);

                        if (condPVal > alpha) {
                            log.debug("incorrect conditional independence with pval " + condPVal + ", phi " + condPhi);
                            missedConditional++;
                        }
                        else if (condPVal <= alpha && condPhi <= strengthThresh) {
                            log.debug("incorrect insubtantive conditional dependence with pval " + condPVal + ", phi " + condPhi);
                            insubstantiveConditional++;
                        }
                    }
                }
            }
        }
        
        List<Integer> skeletonBreakdown = new ArrayList<Integer>();
        skeletonBreakdown.add(detected);
        skeletonBreakdown.add(missedMarginal);
        skeletonBreakdown.add(missedConditional);
        skeletonBreakdown.add(insubstantiveMarginal);
        skeletonBreakdown.add(insubstantiveConditional);

        //Skeleton Breakdown List
        // 0--> detected
        // 1-->missed marginal
        // 2-->missed conditional
        // 3-->insubstantive marginal
        // 4-->insubstantive conditional

        return skeletonBreakdown;
    }

    /**
     * Compute the skeleton precision, which ignores the direction of edges.
     * <p/>
     * Learned Skeleton correct / learned total edges.
     * @param errorCounts the list of the counts of each type of error.
     * @return the skeleton precision.
     */
    public static double getSPrecision(List<Integer> errorCounts) {
        int tp = errorCounts.get(0);
        int numDirectionWrong = errorCounts.get(4);
        int numUndirWhenDir = errorCounts.get(5);

        int fp = errorCounts.get(1);

        double skelCorrect = tp + numDirectionWrong + numUndirWhenDir;
        double learnedTotal = skelCorrect + fp;

        if (Double.compare(learnedTotal, 0.0) == 0) {
            return 1.0;
        }        

        return skelCorrect / learnedTotal;
    }

    /**
     * Compute the skeleton recall, which ignores the direction of edges.
     * <p/>
     * Learned skeleton correct / true total edges.
     * @param errorCounts the list of the counts of each type of error.
     * @return the skeleton recall.
     */
    public static double getSRecall(List<Integer> errorCounts) {
        int tp = errorCounts.get(0);
        int numDirectionWrong = errorCounts.get(4);
        int numDirWhenUndir = errorCounts.get(5);

        int fn = errorCounts.get(3);

        double skelCorrect = tp + numDirectionWrong + numDirWhenUndir;
        double trueTotal = skelCorrect + fn;

        if (Double.compare(trueTotal, 0.0) == 0) {
            return 1.0;
        }

        return skelCorrect / trueTotal;
    }

    /**
     * Compute the compelled precision, which uses the direction of edges.
     * <p/>
     * Learned compelled correct / learned total compelled;
     * @param errorCounts the list of the counts of each type of error.
     * @return the compelled precision.
     */
    public static double getCPrecision(List<Integer> errorCounts) {
        int numDirectionWrong = errorCounts.get(4);
        int numCorrectCompelled = errorCounts.get(0);

        double learnCompelled = numCorrectCompelled + numDirectionWrong;

        if (Double.compare(learnCompelled, 0.0) == 0) {
            return 1.0;
        }

        return numCorrectCompelled / learnCompelled;
    }

    /**
     * Compute the complled recall, which uses the direction of edges.
     * <p/>
     * Learned compelled correct / true total compelled
     * @param errorCounts the list of the counts of each type of error
     * @return the compelled recall.
     */
    public static double getCRecall(List<Integer> errorCounts) {
        double numCorrectCompelled = errorCounts.get(0);
        double numTrueCompelled = errorCounts.get(6);

        if (Double.compare(numTrueCompelled, 0.0) == 0) {
            return 1.0;
        }

        return numCorrectCompelled / numTrueCompelled;
    }

    /**
     * Compute the skeleton F measure.
     * <p/>
     * 2*skeleton precision*skeleton recall / (skeleton precision + skeleton recall)
     * @param errorCounts the list of the counts of each type of error.
     * @return the skeleton F measure.
     */
    public static double fSMeasure(List<Integer> errorCounts) {
        double sprecision = getSPrecision(errorCounts);
        double srecall = getSRecall(errorCounts);

        if (Double.compare((sprecision + srecall), 0.0) == 0) {
            return 1.0;
        }
        return 2*sprecision*srecall / (sprecision + srecall);
    }

    /**
     * Compute the compelled F measure.
     * <p/>
     * 2*compelled precision*compelled recall / (compelled precision + compelled recall)
     * @param errorCounts the list of the counts of each type of error.
     * @return the compelled F measure.
     */
    public static double fCMeasure(List<Integer> errorCounts) {
        double cprecision = getCPrecision(errorCounts);
        double crecall = getCRecall(errorCounts);

        if (Double.compare((cprecision + crecall), 0.0) == 0) {
            return 1.0;
        }
        return 2*cprecision*crecall / (cprecision + crecall);
    }

    /**
     * Compute the compelled recall if the model oriented with a correct skeleton and conditional independence
     * information.
     * @param modelSupport data structure containing set of all units, dependencies, and potential causes.
     * @param trueModel the true model.
     * @return the compelled recall after the oracle orients the model.
     */
    public static double getOracleCRecall(ModelSupport modelSupport, Model trueModel) {
        OraclePC opc = new OraclePC(modelSupport, trueModel);
        opc.identifySkeleton();
        opc.orientEdges(true);
        Model learnedModel = opc.getModel();
        return getCRecall(getErrorCounts(trueModel, learnedModel));
    }

    private static Map<String, Integer> edgeRuleFrequency = new HashMap<String, Integer>();
    /**
     * Records that a given edge orientation rule was used.
     * @param edgeOrientationRule the used edge orientation rule.
     */
    public static void usedRule(String edgeOrientationRule) {
        if (! edgeRuleFrequency.containsKey(edgeOrientationRule)) {
            edgeRuleFrequency.put(edgeOrientationRule, 0);
        }
        edgeRuleFrequency.put(edgeOrientationRule, edgeRuleFrequency.get(edgeOrientationRule) + 1);
    }

    /**
     * Get the number of times each edge orientation rule was used.
     * @return a map of each edge orientation rule to the number of times it was used.
     */
    public static Map<String, Integer> getEdgeRuleFrequencies() {
        return edgeRuleFrequency;
    }

    /**
     * Reset the frequencies of edge orientation rule usage.
     */
    private static void resetEdgeRuleFrequencies() {
        edgeRuleFrequency = new HashMap<String, Integer>();
    }


    private static Map<Unit, Double> marginalTestPVals = new HashMap<Unit, Double>();
    /**
     * Set the p-value obtained after running a marginal test of independence for a given unit.
     * @param u the unit.
     * @param d the p-value.
     */
    public static void setMarginalTestPVal(Unit u, Double d) {
        marginalTestPVals.put(u, d);
    }

    /**
     * Get the p-value obtained after running a marginal test of independence for a given unit.
     * @param u the unit.
     * @return the p-value.
     */
    public static Double getMarginalTestPVal(Unit u) {
        return marginalTestPVals.get(u);
    }

    /**
     * Reset the p-values obtained over all marginal tests of independence for all units.
     */
    private static void resetMarginalTestPVals() {
        marginalTestPVals = new HashMap<Unit, Double>();
    }

    private static Map<Unit, Double> marginalTestStrength = new HashMap<Unit, Double>();
    /**
     * Set the strength of effect obtained after running a marginal test of independence for a given unit.
     * @param u the unit.
     * @param d the strength of effect.
     */
    public static void setMarginalTestStrength(Unit u, Double d) {
        marginalTestStrength.put(u, d);
    }

    /**
     * Get the strength of effect obtained after running a marginal test of independence for a given unit.
     * @param u the unit.
     * @return the strength of effect.
     */
    public static Double getMarginalTestStrength(Unit u) {
        return marginalTestStrength.get(u);
    }

    /**
     * Reset the strength of effect values obtained over all marginal tests of independence for all units.
     */
    private static void resetMarginalTestStrengths() {
        marginalTestStrength = new HashMap<Unit, Double>();
    }

    private static Map<Unit, Double> conditionalTestPVals = new HashMap<Unit, Double>();
    /**
     * Set the p-value obtained after running a conditional test of independence for a given unit.
     * @param u the unit.
     * @param d the p-value.
     */
    public static void setConditionalTestPVal(Unit u, Double d) {
        conditionalTestPVals.put(u, d);
    }

    /**
     * Get the p-value obtained after running a conditional test of independence for a given unit.
     * @param u the unit.
     * @return the p-value.
     */
    public static Double getConditionalTestPVal(Unit u) {
        return conditionalTestPVals.get(u);
    }

    /**
     * Reset the p-values obtained over all conditional tests of independence for all units.
     */
    private static void resetConditionalTestPVals() {
        conditionalTestPVals = new HashMap<Unit, Double>();
    }

    private static Map<Unit, Double> conditionalTestStrength = new HashMap<Unit, Double>();
    /**
     * Set the strength of effect obtained after running a conditional test of independence for a given unit.
     * @param u the unit.
     * @param d the strength of effect.
     */
    public static void setConditionalTestStrength(Unit u, Double d) {
        conditionalTestStrength.put(u, d);
    }

    /**
     * Get the strength of effect obtained after running a conditional test of independence for a given unit.
     * @param u the unit.
     * @return the strength of effect.
     */
    public static Double getConditionalTestStrength(Unit u) {
        return conditionalTestStrength.get(u);
    }

    /**
     * Reset the strength of effect values obtained over all conditional tests of independence for all units.
     */
    private static void resetConditionalTestStrengths() {
        conditionalTestStrength = new HashMap<Unit, Double>();
    }

    /**
     * Reset all records of the p-values and strength of effects from running marginal and conditional tests, as well
     * as the frequency of used edge orientation rules.
     */
    public static void reset() {
        resetEdgeRuleFrequencies();
        resetMarginalTestPVals();
        resetMarginalTestStrengths();
        resetConditionalTestPVals();
        resetConditionalTestStrengths();
    }

}