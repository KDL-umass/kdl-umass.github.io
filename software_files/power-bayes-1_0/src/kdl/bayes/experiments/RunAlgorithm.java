/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.experiments;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.edge.csp.CSPEdgeOrientation;
import kdl.bayes.edge.csp.CSPRandomizedGreedy;
import kdl.bayes.edge.csp.CSPSkeletonRelax;
import kdl.bayes.edge.rules.EdgeOrientation;
import kdl.bayes.search.BNConstrainedSearchProblem;
import kdl.bayes.search.BNSearchProblem;
import kdl.bayes.search.BNSearchState;
import kdl.bayes.search.TabuHillClimb;
import kdl.bayes.skeleton.*;
import kdl.bayes.skeleton.util.BayesDataCMH;
import kdl.bayes.skeleton.util.DefaultThreshold;
import kdl.bayes.util.Assert;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.SHDistance;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;
import java.util.Map;

public class RunAlgorithm {
    protected static Logger log = Logger.getLogger(RunAlgorithm.class);

    public static Map<Integer, Double> esMap = new HashMap<Integer, Double>();

    public static final String algPCRules = "pc";
    public static final String algPCRulesCMH = "pc-cmh";
    public static final String algPCAdjacent = "pc-adj";
    public static final String algPCHybrid = "pc-hybrid";
    public static final String algMMHC = "mmhc";
    public static final String algGreedySearch = "gs";
    public static final String algEdgeOpt = "edge-opt";
    public static final String algRelax = "relax";
    public static final String algPowerPCCV = "pc-cv-power";
    public static final String algPowerCV = "mmhc-cv-power";
    public static final String algPCQuantilesGreedy = "pc-quantiles-greedy";
    public static final String algTrueBn = "true-bn";
    public static final String algLearnParams = "learn-params";

    public static void main(String[] args) throws Exception {
        Util.initLog4J();

        // inputs:
        // bn network name
        // index of training sample
        // sample size
        // [threshold]
        // [path]
        if (args.length < 5) {
            printUsage();
            System.exit(-1);
        }


        String algorithm = args[0];
        String netName = args[1];
        int trainIndex = Integer.parseInt(args[2]);
        int sampleSize = Integer.parseInt(args[3]);
        String path = args[4];

        initEsMap();

        String prefix = path + netName + "/" + netName + ".";


        /////////////////////////////
        // Do one time setup things.
        /////////////////////////////

        // load training data (e.g. insurance/insurance.train.1.500 )
        String trainfile = prefix +
                "train." + trainIndex + "." + sampleSize;

        Instances trainInstances = new Instances(new FileReader(trainfile));

        // load true gold standard Bayes net (for computing SHD)
        String xmlFile = prefix + "xml";

        if (netName.equals("synthetic") || netName.equals("binary")) {
            xmlFile = prefix + trainIndex + ".xml";
        }

        PowerBayesNet trueBn = new PowerBayesNet(xmlFile);

        int[][] pdag;

        long sk_runtime = 0;
        int sk_statCalls = 0;
        int sk_shd = 0;
        int sk_fp = 0;
        int sk_fn = 0;
        int sk_tp = 0;


        Format format = new DecimalFormat("#.###");

        ////////////////////////////////
        ////////////////////////////////


        double effectSize = 0.1;

        // final bn structure measures
        double bdeu = -1.0, loglikelihood = -1.0, csw = -1.0;
        long runtime;
        int statCalls, shd, fp, fn, dir, ud, du, tp;


        log.info("Starting: netName=" + netName +
                " trainIndex=" + trainIndex +
                " sampleSize=" + sampleSize +
                " algorithm=" + algorithm +
                (Double.compare(effectSize, -1.0) != 0 ? " effectSize=" + effectSize : "")
        );

        statCalls = -1;
        runtime = -1;

        PowerBayesNet bn;
        if (algorithm.equals(algPCRules)) {

            FAS pc = new FAS(trainInstances, new DefaultThreshold(trainInstances.numInstances()));

            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            EdgeOrientation edge = new EdgeOrientation(pc);

            runtime = System.currentTimeMillis();
            edge.orientEdges();
            runtime = System.currentTimeMillis() - runtime + sk_runtime;
            pdag = edge.getIntPdag();
            bn = new PowerBayesNet(trainInstances, edge.getPDag());


        } else if (algorithm.equals(algPCRulesCMH)) {

            //Runs pairwise CMH test
            FAS pc = new FAS(trainInstances, new DefaultThreshold(trainInstances.numInstances()), new BayesDataCMH(trainInstances));

            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            EdgeOrientation edge = new EdgeOrientation(pc);

            runtime = System.currentTimeMillis();
            edge.orientEdges();
            runtime = System.currentTimeMillis() - runtime + sk_runtime;

            pdag = edge.getIntPdag();


            bn = new PowerBayesNet(trainInstances, edge.getPDag());

            BayesDataCMH.re.end();

        } else if (algorithm.equals(algPCAdjacent)) {

            bn = new PowerBayesNet(trainInstances);

            PCAdjacent pc = new PCAdjacent(trainInstances, new DefaultThreshold(trainInstances.numInstances()));
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            statCalls = problem.getStatisticalCalls() + sk_statCalls;

            pdag = GraphUtil.convert(bn.getPDag());
        } else if (algorithm.equals(algPCHybrid)) {

            bn = new PowerBayesNet(trainInstances);

            FAS pc = new FAS(trainInstances, new DefaultThreshold(trainInstances.numInstances()));

            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            statCalls = problem.getStatisticalCalls() + sk_statCalls;

            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algMMHC)) {

            bn = new PowerBayesNet(trainInstances);

            MMHC mmhc = new MMHC(trainInstances, new MMPC(new DefaultThreshold(trainInstances.numInstances())));
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = mmhc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = mmhc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            statCalls = problem.getStatisticalCalls() + sk_statCalls;
            pdag = GraphUtil.convert(bn.getPDag());


        } else if (algorithm.equals(algEdgeOpt)) {

            FAS pc = new FAS(trainInstances, new DefaultThreshold(trainInstances.numInstances()));
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");

            CSPEdgeOrientation cspEdge = new CSPRandomizedGreedy(trainInstances, pc, 0.5, 3, true, true);

            runtime = System.currentTimeMillis();
            bn = cspEdge.orientEdges();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algRelax)) {


            FAS pc = new FAS(trainInstances, new DefaultThreshold(trainInstances.numInstances()));
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();


            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            CSPEdgeOrientation cspEdge = new CSPRandomizedGreedy(trainInstances, pc, 0.5, 3, true, true);
            CSPSkeletonRelax relax = new CSPSkeletonRelax(trainInstances, cspEdge, trainIndex);

            runtime = System.currentTimeMillis();
            bn = relax.search();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;

            statCalls = cspEdge.getStatesScored();
            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algPowerPCCV)) {

            bn = new PowerBayesNet(trainInstances);

            PowerPCCV pcCV = new PowerPCCV(trainInstances, trueBn);
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pcCV.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pcCV.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");

            BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            statCalls = problem.getStatisticalCalls() + sk_statCalls;
            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algPowerCV)) {

            bn = new PowerBayesNet(trainInstances);

            PowerCV pcCV = new PowerCV(trainInstances, trueBn);
            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pcCV.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pcCV.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");

            BNSearchProblem problem = new BNConstrainedSearchProblem(bn, skeleton);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            bn = ((BNSearchState) TabuHillClimb.search(problem)).getBayesNet();
            runtime = System.currentTimeMillis() - runtime;
            runtime += sk_runtime;
            statCalls = problem.getStatisticalCalls() + sk_statCalls;
            pdag = GraphUtil.convert(bn.getPDag());


        } else if (algorithm.equals(algPCQuantilesGreedy)) {

            PCQuantilesGreedy pc = new PCQuantilesGreedy(trainInstances);
            pc.setRunChiSquare(true);

            sk_runtime = System.currentTimeMillis();
            sk_statCalls = pc.computeNeighbors();
            sk_runtime = System.currentTimeMillis() - sk_runtime;

            boolean[][] skeleton = pc.getSkeleton();

            SHDistance sk_distance = new SHDistance(skeleton, trueBn.getNeighborsAdjList());
            sk_shd = sk_distance.getDistance();
            sk_fp = sk_distance.getNumFalsePositives();
            sk_fn = sk_distance.getNumFalseNegatives();
            sk_tp = sk_distance.getNumTruePositives();
            Assert.condition(sk_distance.getNumDirectionWrong() +
                    sk_distance.getNumDirWhenUndir() +
                    sk_distance.getNumUndirWhenDir() == 0, "Skeleton has directed edges!");


            EdgeOrientation edge = new EdgeOrientation(pc);

            runtime = System.currentTimeMillis();
            edge.orientEdges();
            runtime = System.currentTimeMillis() - runtime + sk_runtime;
            pdag = edge.getIntPdag();
            bn = new PowerBayesNet(trainInstances, edge.getPDag());

        } else if (algorithm.equals(algTrueBn)) {
            bn = trueBn;
            // true bayes net does not need to be smoothed, yet we smooth it
            // so that results are comparable.  We divide laplace by the
            // sampleSize b/c the CPTs in the true bayes net do not hold
            // counts but rather probabilities.
            bn.setEquivalentSampleSize(0);
            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algLearnParams)) {
            bn = trueBn;
            bn.estimateCPTs(trainInstances);
            pdag = GraphUtil.convert(bn.getPDag());

        } else if (algorithm.equals(algGreedySearch)) {
            bn = new PowerBayesNet(trainInstances);

            BNSearchProblem problem = new BNSearchProblem(bn);
            ((BNSearchState) problem.getInitialState()).setTrueBn(trueBn);

            runtime = System.currentTimeMillis();
            BNSearchState bnFinalState = ((BNSearchState) TabuHillClimb.search(problem));
            runtime = System.currentTimeMillis() - runtime;
            statCalls = problem.getStatisticalCalls();

            bn = bnFinalState.getBayesNet();

            pdag = GraphUtil.convert(bn.getPDag());

        } else {
            printUsage();
            throw new IllegalArgumentException("Unrecognized algorithm: " + algorithm);
        }

        SHDistance distance;
        distance = new SHDistance(pdag, GraphUtil.convert(trueBn.getPDag()));
        log.debug("pdag: " + GraphUtil.dagToString(pdag));
        //log.info("correct graph: " + GraphUtil.dagToString(trueBn.getPDag()));


        shd = distance.getDistance();
        fp = distance.getNumFalsePositives();
        fn = distance.getNumFalseNegatives();
        dir = distance.getNumDirectionWrong();
        ud = distance.getNumUndirWhenDir();
        du = distance.getNumDirWhenUndir();
        tp = distance.getNumTruePositives();

        double sPrec = distance.getSPrecision();
        double sRecall = distance.getSRecall();

        double cPrec = distance.getCPrecision();
        double cRecall = distance.getCRecall();

        if (!algorithm.equals(algPCRulesCMH) && !algorithm.equals(algPCRules)) {
            bn.estimateCPTs(trainInstances);
        }


        if (!algorithm.equals(algPCRules) && !algorithm.equals(algPCRulesCMH)) {
            bdeu = bn.logBDeuScore();

            // load test data
            //String testfile = prefix + "test.500000";
            String testfile = prefix + "test.10000";

            if (netName.equals("synthetic")) {
                testfile = prefix + "test." + trainIndex + ".10000";
            }

            Instances testInstances = new Instances(new FileReader(testfile));
            loglikelihood = bn.logProbability(testInstances);
        }


        log.info("RESULTS: netName=" + netName +
                " trainIndex=" + trainIndex +
                " sampleSize=" + sampleSize +
                " algorithm=" + algorithm +
                (Double.compare(effectSize, -1.0) != 0 ? " effectSize=" + effectSize : "") +
                " sk_runtime=" + sk_runtime +
                " sk_statCalls=" + sk_statCalls +
                " sk_shd=" + sk_shd +
                " sk_fp=" + sk_fp +
                " sk_fn=" + sk_fn +
                " sk_tp=" + sk_tp +
                " bdeu=" + bdeu +
                " loglikelihood=" + loglikelihood +
                " runtime=" + runtime +
                " statCalls=" + statCalls +
                " shd=" + shd +
                " fp=" + fp +
                " fn=" + fn +
                " dir=" + dir +
                " ud=" + ud +
                " du=" + du +
                " tp=" + tp +
                " csw=" + csw +
                " sp=" + format.format(sPrec) +
                " sr=" + format.format(sRecall) +
                " cp=" + format.format(cPrec) +
                " cr=" + format.format(cRecall)
        );


    }


    private static void printUsage() {
        log.info("RunAlgorithm expects: algorithm net_name train_index sample_size path");
        log.info("\tValid algorithms are:");
        log.info("\t\t" + algPCRules);
        log.info("\t\t" + algPCRulesCMH);
        log.info("\t\t" + algPCAdjacent);
        log.info("\t\t" + algPCHybrid);
        log.info("\t\t" + algMMHC);
        log.info("\t\t" + algGreedySearch);
        log.info("\t\t" + algEdgeOpt);
        log.info("\t\t" + algRelax);
        log.info("\t\t" + algPowerPCCV);
        log.info("\t\t" + algPowerCV);
        log.info("\t\t" + algPCQuantilesGreedy);
        log.info("\t\t" + algTrueBn);
        log.info("\t\t" + algLearnParams);
    }

    private static void initEsMap() {
        esMap.put(500, 0.2183);
        esMap.put(1000, 0.1518);
        esMap.put(2000, 0.1204);
        esMap.put(5000, 0.0766);
    }


}
