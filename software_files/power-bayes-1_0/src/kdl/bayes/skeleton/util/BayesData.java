/**
 * $Id: BayesData.java 281 2009-08-15 18:23:09Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.Assert;
import kdl.bayes.util.StatUtil;
import kdl.bayes.util.adtree.ADTree;
import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * See MMHC
 * <p/>
 * BayesData responsible for accessing data, computing counts,
 * measuring associations, etc.
 */
public class BayesData {
    protected static Logger log = Logger.getLogger(BayesData.class);

    public Instances instances;
    public int sampleSize;
    protected int numStatisticalCalls;
    protected int xIdx = -1;
    protected int tIdx = -1;
    protected int[] zIdxs = null;
    protected int[][][] counts = null;
    private SparseCounts countsMatrix = null;
    private Map<Integer, Map<Integer, Map<Integer, Integer>>> countsMap = null;
    public long traditionalDof;
    public long tableSize;
    public long empiricalDof;

    public String name = "empty";

    //public static Rengine re = null;

    public BayesData() {
        instances = null;
        sampleSize = 0;

//        if (re == null) {
//            log.info("Initializing R engine.");
//            String[] args = new String[]{"--no-save", "--no-restore", "--no-readline"};
//            re = new Rengine(args, false, null);
//            if (!re.waitForR()) {
//                Assert.condition(false, "Unable to open R");
//            }
//            re.eval("options(warn=-1);");
//        }

    }

    public BayesData(Instances instances) {
        numStatisticalCalls = 0;
        this.instances = instances;
        sampleSize = instances.numInstances();

        name = instances.relationName().toLowerCase();
        //log.info(name);

//        if (re == null) {
//            log.info("Initializing R engine.");
//            String[] args = new String[]{"--no-save", "--no-restore", "--no-readline"};
//            re = new Rengine(args, false, null);
//            if (!re.waitForR()) {
//                Assert.condition(false, "Unable to open R");
//            }
//            re.eval("options(warn=-1);");
//        }


    }

    public void setIndexes(int xIdx, int tIdx, int[] zIdxs) {
        this.xIdx = xIdx;
        this.tIdx = tIdx;
        this.zIdxs = zIdxs;
        traditionalDof = computeTraditionalDegreesOfFreedom(xIdx, tIdx, zIdxs);
        tableSize = computeCardinality(xIdx, tIdx, zIdxs);
    }

    public ADTree getADTree() {
        throw new UnsupportedOperationException("ADTree is no longer supported");
    }

    /**
     * Assoc(X,T; Z)
     * Returns a measure of association between x and t conditioned
     * on the set of variables Z.  Score ranges between 0 and
     * pValueThreshold.
     *
     * @param alpha
     * @return score a measure of association between x and t given Z
     */
    public double[] assoc(double alpha) {
        numStatisticalCalls++;

        //if (empiricalDof == 0) {
        if (empiricalDof == 0) {
            log.debug("Found 0 DOF.");
            return new double[]{-1, -1, -1, -1, -1, -1};  // cannot compute association with zero dof
        }

        double gStat = StatUtil.gStatistic(counts);

        double n = instances.numInstances();
        double r = Math.min(computeCardinality(xIdx), computeCardinality(tIdx));
        double v = StatUtil.cramerV(gStat, n, r);
        double es = v * Math.sqrt(r - 1); //Effect size from v (Cohen 1988 pg. 223)

        // compute p-value (a number between 0 and 1)
        double pValue = StatUtil.chiSquareP(gStat, empiricalDof);

        // convert p-value into a "score" where minimum score is zero
        // and lower p-values get higher score
        // this score ranges from 0 and pValueThreshold
        double score = Math.max(0, alpha - pValue);

        //log.info(countsToString(counts));

        log.debug("ASSOC: netName=" + name + " gStat=" + gStat + " score=" + score + " pVal=" + pValue + " tIdx=" + getName(tIdx) + " xIdx=" + getName(xIdx) + " zIdxs=" + (zIdxs.length > 0 ? getNames(zIdxs, ".") : "none") + " sizeZ=" + zIdxs.length + " edf=" + empiricalDof + " tdf=" + traditionalDof + " n=" + n + " v=" + v + " r=" + r);
        if (Double.compare(score, 0) == 0) {
            log.debug("INDEPENDENCE ACCEPTED: " + getName(tIdx) + " _||_ " + getName(xIdx) + (zIdxs.length > 0 ? " | " + getNames(zIdxs, ", ") : "") + " p=" + pValue + " g^2=" + gStat + " df=" + empiricalDof);
        }
        return new double[]{score, gStat, es, zIdxs.length, 1, pValue}; //score, gStat, effect size, conditioning set size, actually ran test, pValue
    }

    public int computeCardinality(int attributeIdx) {
        return instances.attribute(attributeIdx).numValues();
    }

    public int computeCardinality(int[] attributeIdxs) {
        int zCardinality = 1;
        for (int i = 0; i < attributeIdxs.length; i++) {
            int parentIdx = attributeIdxs[i];
            zCardinality *= computeCardinality(parentIdx);
        }
        return zCardinality;
    }

    protected long computeCardinality(int xIdx, int tIdx, int[] zIdxsSubset) {
        return computeCardinality(xIdx) * computeCardinality(tIdx) *
                computeCardinality(zIdxsSubset);
    }

    /**
     * computes a multiway contingency table for attributes X,Y and Z
     * where Z is a set of attributes
     *
     * @param xIdx
     * @param yIdx
     * @param zIdxs
     * @return three dimensional array N[i][j][k] is the number of times
     *         X=i, Y=j, and Z took on the configuration k
     */
    public int[][][] computeCounts(int xIdx, int yIdx, int[] zIdxs) {
        int zCardinality = computeCardinality(zIdxs);

        int xCardinality = computeCardinality(xIdx);
        int yCardinality = computeCardinality(yIdx);
        int[][][] table = new int[xCardinality][yCardinality][zCardinality];

        Enumeration enumInsts = instances.enumerateInstances();
        while (enumInsts.hasMoreElements()) {
            Instance instance = (Instance) enumInsts.nextElement();
            computeCountsForInstance(xIdx, yIdx, zIdxs, instance, table);
        }


        return table;
    }

    private void computeCountsForInstance(int xIdx, int yIdx, int[] zIdxs, Instance instance, int[][][] table) {
        double zConfigIdx = 0;
        for (int i = 0; i < zIdxs.length; i++) {
            int zIdx = zIdxs[i];
            double zValueIdx = instance.value(zIdx);
            zConfigIdx = zConfigIdx * instance.attribute(zIdx).numValues() +
                    zValueIdx;
        }

        double xValueIdx = instance.value(xIdx);
        double yValueIdx = instance.value(yIdx);
        // Weka uses doubles, but we can safely assume they are ints
        table[((int) xValueIdx)][((int) yValueIdx)][((int) zConfigIdx)] += 1;
    }

    public long computeTraditionalDegreesOfFreedom(int xIdx, int tIdx, int[] zIdxsSubset) {
        return (computeCardinality(xIdx) - 1) * (computeCardinality(tIdx) - 1) *
                computeCardinality(zIdxsSubset);
    }

    /**
     * Computes degrees of freedom according to:
     * Spirtes, Glymour, Scheines (1993).  Causation, Prediction, and Search.
     * For each configuration of the Zs, you have a two dimensional table for X and T.
     * The total dof is the sum of dofs for each X-T table.
     * <p/>
     * For each X-T table, dof is (r-1)*(c-1)-(# zero cells assuming independence).  The
     * only way a cell is zero under independence is if the whole row or column (or both)
     * is zeros.
     *
     * @param xIdx
     * @param tIdx
     * @param zIdxs
     * @param counts
     * @return degrees of freedom
     */
    public long computeDegreesOfFreedom(int xIdx, int tIdx, int[] zIdxs, int[][][] counts) {
        int numRows = computeCardinality(xIdx);
        int numCols = computeCardinality(tIdx);
        int cardinalityZ = computeCardinality(zIdxs);

        Assert.condition(numRows == counts.length, "Cardinality mismatch");
        Assert.condition(numCols == counts[0].length, "Cardinality mismatch");
        Assert.condition(cardinalityZ == counts[0][0].length, "Cardinality mismatch");

        long dof = 0;
        for (int zValueIdx = 0; zValueIdx < cardinalityZ; zValueIdx++) {
            int[] totalByColumn = new int[numCols];
            int[] totalByRow = new int[numRows];
            for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
                for (int colIdx = 0; colIdx < numCols; colIdx++) {
                    int count = counts[rowIdx][colIdx][zValueIdx];
                    totalByColumn[colIdx] += count;
                    totalByRow[rowIdx] += count;
                }
            }

            int numNonEmptyRows = 0;
            for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
                if (totalByRow[rowIdx] > 0) {
                    numNonEmptyRows++;
                }
            }
            int numNonEmptyCols = 0;
            for (int colIdx = 0; colIdx < numCols; colIdx++) {
                if (totalByColumn[colIdx] > 0) {
                    numNonEmptyCols++;
                }
            }

            int tableDof = Math.max(0, (numNonEmptyRows - 1)) *
                    Math.max(0, (numNonEmptyCols - 1));
            dof += tableDof;
        }
        Assert.condition(dof >= 0, "Degrees of freedom (" + dof + ") must be non-negative.");
        return dof;
    }


    protected long computeDegreesOfFreedomMatrix(int xIdx, int tIdx, int[] zIdxs, SparseCounts counts) {
        int numRows = computeCardinality(xIdx);
        int numCols = computeCardinality(tIdx);
        int cardinalityZ = computeCardinality(zIdxs);

        Assert.condition(numRows == counts.rows(), "Cardinality mismatch");
        Assert.condition(numCols == counts.columns(), "Cardinality mismatch");
        Assert.condition(cardinalityZ == counts.slices(), "Cardinality mismatch");

        long dof = 0;
        for (int zValueIdx = 0; zValueIdx < cardinalityZ; zValueIdx++) {
            int[] totalByColumn = new int[numCols];
            int[] totalByRow = new int[numRows];
            for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
                for (int colIdx = 0; colIdx < numCols; colIdx++) {
                    int count = (int) counts.get(rowIdx, colIdx, zValueIdx);
                    totalByColumn[colIdx] += count;
                    totalByRow[rowIdx] += count;
                }
            }

            int numNonEmptyRows = 0;
            for (int rowIdx = 0; rowIdx < numRows; rowIdx++) {
                if (totalByRow[rowIdx] > 0) {
                    numNonEmptyRows++;
                }
            }
            int numNonEmptyCols = 0;
            for (int colIdx = 0; colIdx < numCols; colIdx++) {
                if (totalByColumn[colIdx] > 0) {
                    numNonEmptyCols++;
                }
            }

            int tableDof = Math.max(0, (numNonEmptyRows - 1)) *
                    Math.max(0, (numNonEmptyCols - 1));
            dof += tableDof;
        }

        Assert.condition(dof >= 0, "Degrees of freedom (" + dof + ") must be non-negative.");
        return dof;
    }

    /**
     * Computes degrees of freedom according to:
     * Steck and Jaakkola.  On the dirichlet prior and Bayesian regularization.  NIPS 2002
     * Basically, for each configuration of the Zs and T, you have a column of X values.
     * For column, we count the number of non-zero cells and subtract one from it.
     * <p/>
     * If all of the cells are non-empty, this *almost* works out to the usual definition
     * of degrees of freedom: (no. of Z configurations)*(no. of T configuations)*(X configs - 1)
     * <p/>
     * //todo: so that it works out to the usual definition for case of all non-empty
     *
     * @param xIdx
     * @param tIdx
     * @param zIdxs
     * @param counts
     * @return degrees of freedom
     */
    protected long computeSteckNumberOfParameters(int xIdx, int tIdx, int[] zIdxs, int[][][] counts) {
        int cardinalityX = computeCardinality(xIdx);
        int cardinalityT = computeCardinality(tIdx);
        int cardinalityZ = computeCardinality(zIdxs);

        Assert.condition(cardinalityX == counts.length, "Cardinality mismatch");
        Assert.condition(cardinalityT == counts[0].length, "Cardinality mismatch");
        Assert.condition(cardinalityZ == counts[0][0].length, "Cardinality mismatch");

        long dof = 0;
        for (int tValueIdx = 0; tValueIdx < cardinalityT; tValueIdx++) {
            for (int zValueIdx = 0; zValueIdx < cardinalityZ; zValueIdx++) {
                int numNonZeros = 0;
                for (int xValueIdx = 0; xValueIdx < cardinalityX; xValueIdx++) {
                    if (counts[xValueIdx][tValueIdx][zValueIdx] > 0) {
                        numNonZeros++;
                    }
                }
                dof += Math.max(0, (numNonZeros - 1));
            }
        }
        Assert.condition(dof >= 0, "Degrees of freedom (" + dof + ") must be non-negative.");
        //Dof should be the Expected Number of Parameters (per Steck and Jakkola) minus (|X|-1)*|Z|)
        //to account for the difference between parameters and degrees of freedom.
        //long numZeros = ((cardinalityX-1)*cardinalityT*cardinalityZ) - dof;
        //dof = dof - (cardinalityX) + xZeros + 1;
        return dof;
    }

    /**
     * Computation for DOF following the principles defined in:
     * Steck and Jaakkola.  On the dirichlet prior and Bayesian regularization.  NIPS 2002
     * <p/>
     * SUM_t,x,z(N_t,x,z) -  SUM_x,z(N_x,z) -  SUM_t,z(N_t,z) + SUM_z(N_z)
     * <p/>
     * Effectively, # of non-zero cells - # of non-zero x,z columns - # of non-zero t,z columns + # of non-zero z tables.
     * <p/>
     * If all of the cells are defined then this works out to the traditional DOF;
     * (|T|-1) * (|X|-1) * (|Z|)
     *
     * @param xIdx
     * @param tIdx
     * @param zIdxs
     * @param counts
     * @return
     */
    protected long computeEffectiveDegreesOfFreedom(int xIdx, int tIdx, int[] zIdxs, int[][][] counts) {
        int cardinalityX = computeCardinality(xIdx);
        int cardinalityT = computeCardinality(tIdx);
        int cardinalityZ = computeCardinality(zIdxs);

        Assert.condition(cardinalityX == counts.length, "Cardinality mismatch");
        Assert.condition(cardinalityT == counts[0].length, "Cardinality mismatch");
        Assert.condition(cardinalityZ == counts[0][0].length, "Cardinality mismatch");

        int numAllNonZeros = 0;
        int numXZNonZeros = 0;
        int numTZNonZeros = 0;
        int numZNonZeros = 0;
        for (int zValueIdx = 0; zValueIdx < cardinalityZ; zValueIdx++) {
            boolean hasZValue = false;
            for (int xValueIdx = 0; xValueIdx < cardinalityX; xValueIdx++) {
                boolean hasXZValue = false;
                for (int tValueIdx = 0; tValueIdx < cardinalityT; tValueIdx++) {
                    if (counts[xValueIdx][tValueIdx][zValueIdx] > 0) {
                        numAllNonZeros++;
                        hasZValue = true;
                        hasXZValue = true;
                    }
                }
                if (hasXZValue) {
                    numXZNonZeros++;
                }
            }
            if (hasZValue) {
                numZNonZeros++;
            }

            for (int tValueIdx = 0; tValueIdx < cardinalityT; tValueIdx++) {
                boolean hasTZValue = false;
                for (int xValueIdx = 0; xValueIdx < cardinalityX; xValueIdx++) {
                    if (counts[xValueIdx][tValueIdx][zValueIdx] > 0) {
                        hasTZValue = true;
                    }
                }
                if (hasTZValue) {
                    numTZNonZeros++;
                }
            }

        }
        long dof = numAllNonZeros - numXZNonZeros - numTZNonZeros + numZNonZeros;
        //Assert.condition(dof >= 0, "Degrees of freedom (" + dof + ") must be non-negative.");
        return Math.max(dof, 0);

    }

    // handy for debug statements
    public String getName(int variableIdx) {
        return instances.attribute(variableIdx).name();
    }

    public String getNames(int[] variableIdxs) {
        return getNames(variableIdxs, " ");
    }

    public String getNames(int[] variableIdxs, String sep) {
        if (variableIdxs.length == 0) {
            return "";
        }
        String names = getName(variableIdxs[0]);
        for (int i = 1; i < variableIdxs.length; i++) {
            int varIdx = variableIdxs[i];
            names += sep + getName(varIdx);
        }
        return names;
    }

    public String getNames(Set<Integer> currentCPC) {
        String names = "";
        for (Iterator<Integer> iterator = currentCPC.iterator(); iterator.hasNext();) {
            names += getName(iterator.next()) + " ";
        }
        return names;
    }

    public String getNames(List<Integer> currentCPC) {
        String names = "";
        for (Iterator<Integer> iterator = currentCPC.iterator(); iterator.hasNext();) {
            names += getName(iterator.next()) + " ";
        }
        return names;
    }

    /**
     * Performs independence test to see if X and T are independent.
     *
     * @param alpha
     * @return true if they are independent
     */
    public boolean independent(double alpha) {
        // assumes that there is enough data to perform this test                                 
        return StatUtil.equalDoubles(assoc(alpha)[0], 0);
    }

    public double mutualInfo() {
        numStatisticalCalls++;
        double mi = 0.0; //StatUtil.mutualInformation();
        log.debug("MI: mi=" + mi + " tIdx=" + getName(tIdx) + " xIdx=" + getName(xIdx) + " zIdxs=" + (zIdxs.length > 0 ? getNames(zIdxs, ".") : "none") + " sizeZ=" + zIdxs.length);
        return mi;
    }


    public int numAttributes() {
        return instances.numAttributes();
    }

    public int getNumStatisticalCalls() {
        return numStatisticalCalls;
    }

    public boolean hasZeroDof() {
        return empiricalDof == 0;
    }

    public void computeStats() {
        log.debug("TEST: (" + getName(tIdx) + ";" + getName(xIdx) + " | " + getNames(zIdxs) + ")");
        log.debug(computeCardinality(xIdx, tIdx, zIdxs));
        counts = computeCounts(xIdx, tIdx, zIdxs);
        empiricalDof = (int) computeDegreesOfFreedom(xIdx, tIdx, zIdxs, counts);
    }

    protected String countsToString(int[][][] counts) {
        StringBuffer sb = new StringBuffer();


        for (int z = 0; z < counts[0][0].length; z++) {
            sb.append("\n****************\n");
            for (int x = 0; x < counts.length; x++) {
                sb.append("\t|" + x);
            }
            sb.append("\n");

            double[][] table = new double[2][2];

            for (int t = 0; t < counts[0].length; t++) {
                sb.append(t);
                for (int x = 0; x < counts.length; x++) {
                    sb.append("\t|" + counts[x][t][z]);

                    if (counts.length == 2 && counts[0].length == 2) {
                        table[x][t] = counts[x][t][z];
                    }

                }

                sb.append("\n");
            }

            if (counts.length == 2 && counts[0].length == 2) {
                double gStat = StatUtil.gStatistic(table);
                double pVal = StatUtil.chiSquareP(gStat, 1);
                sb.append("gStat: ").append(gStat).append(" pVal: ").append(pVal);
            }
        }

        return sb.toString();
    }
}
