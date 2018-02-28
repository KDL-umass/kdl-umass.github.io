/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes;

import kdl.bayes.skeleton.util.ZQueryIterator;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.StatUtil;
import kdl.bayes.util.adtree.ADTree;
import kdl.bayes.util.adtree.StatCache;
import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;


/**
 * This BayesNet class relies on an ADTree for computing statistics.
 * <p/>
 * Using an ADTree improves performance on repeated score computation (avoids multiple passes over the data) and
 * avoids large memory requirements when the number of parents of a variable is large. These improvements come with
 * the onetime cost of learning the ADTree and the memory overhead of storing the tree in memory.
 */
public class BayesNet {

    protected static Logger log = Logger.getLogger(BayesNet.class);

    private double equivSampleSize = 10;

    protected StatCache adTree;
    protected Instances instances;

    //Maintain multiple graph representations
    boolean[][] dag;
    boolean[][] skeleton;

    List<List<Integer>> parentSets;

    /**
     * Constructor to create a BayesNet from an XMLBIF
     *
     * @param xmlFilename
     */
    public BayesNet(String xmlFilename) {


    }

    public BayesNet(Instances instances) {
        adTree = new ADTree(instances);
        this.instances = instances;

        int numVars = instances.numAttributes();

        parentSets = new ArrayList<List<Integer>>();

        for (int i = 0; i < numVars; i++) {
            parentSets.add(new ArrayList<Integer>());
        }

        dag = new boolean[numVars][numVars];
        skeleton = new boolean[numVars][numVars];

    }

    public BayesNet(Instances instances, boolean[][] dag) {
        this(instances);

        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag[i].length; j++) {
                if (dag[i][j]) {
                    addEdge(i, j);
                }
            }
        }
    }

    public BayesNet(Instances instances, StatCache adTree) {
        this.instances = instances;
        this.adTree = adTree;

        //Init empty structure
        int numVars = instances.numAttributes();

        parentSets = new ArrayList<List<Integer>>();

        for (int i = 0; i < numVars; i++) {
            parentSets.add(new ArrayList<Integer>());
        }

        dag = new boolean[numVars][numVars];
        skeleton = new boolean[numVars][numVars];
    }

    public BayesNet(Instances instances, StatCache adTree, boolean[][] dag) {
        this.instances = instances;
        this.adTree = adTree;

        //Init empty structure
        int numVars = instances.numAttributes();

        parentSets = new ArrayList<List<Integer>>();

        for (int i = 0; i < numVars; i++) {
            parentSets.add(new ArrayList<Integer>());
        }

        this.dag = new boolean[numVars][numVars];
        skeleton = new boolean[numVars][numVars];

        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag[i].length; j++) {
                if (dag[i][j]) {
                    addEdge(i, j);
                }
            }
        }
    }

    public void addEdge(int from, int to) {
        List<Integer> parentSet = getParents(to);
        parentSet.add(from);

        dag[from][to] = true;

        skeleton[from][to] = true;
        skeleton[to][from] = true;
    }

    public StatCache getStatCache() {
        return adTree;
    }

    public boolean[][] getDag() {
        return dag;
    }

    public double getEquivSampleSize() {
        return equivSampleSize;
    }

    public Instances getInstances() {
        return instances;
    }

    public int getNrOfNodes() {
        return getNumVariables();
    }

    public int getNumVariables() {
        return instances.numAttributes();
    }

    public int getParentCardinality(int var) {
        List<Integer> parentSet = getParents(var);
        int card = 1;
        for (Integer parent : parentSet) {
            card = card * instances.attribute(parent).numValues();
        }
        return card;
    }

    public List<Integer> getParents(int node) {
        return parentSets.get(node);
    }

    public List<List<Integer>> getParentSets() {
        return parentSets;
    }

    public boolean[][] getPDag() {
        return GraphUtil.getCompletedPDAG(dag);
    }

    public boolean[][] getSkeleton() {
        return skeleton;
    }

    public double getVariableLogBDeuScore(int var) {
        double fLogScore = 0.0;
        int r_i = instances.attribute(var).numValues();
        int q_i = getParentCardinality(var);

        ZQueryIterator queryIter = new ZQueryIterator(getParents(var), instances);

        double n_ijk_prime = equivSampleSize / (r_i * q_i);
        double n_ij_prime = n_ijk_prime * r_i;

        while (queryIter.hasNext()) {
            Map<Integer, Integer> parentQuery = queryIter.next();

            double n_ij = 0;

            for (int iAttrValue = 0; iAttrValue < r_i; iAttrValue++) {
                HashMap<Integer, Integer> query = new HashMap<Integer, Integer>(parentQuery);
                query.put(var, iAttrValue);
                double n_ijk = adTree.getCount(query, var);
                n_ij += n_ijk;
                fLogScore += StatUtil.gammaLn(n_ijk + n_ijk_prime);
                fLogScore -= StatUtil.gammaLn(n_ijk_prime);
            }
            fLogScore += StatUtil.gammaLn(n_ij_prime);
            fLogScore -= StatUtil.gammaLn(n_ij_prime + n_ij);
        }
        return fLogScore;
    }

    public double logBDeuScore() {
        double fLogScore = 0.0;
        // i - attribute index
        // j - parent configuration index
        // k - attribute value index
        // r_i - cardinality of attribute i
        // q_i - cardinality of parent configurations for attribute i

        for (int iAttribute = 0; iAttribute < instances.numAttributes(); iAttribute++) {
            fLogScore += getVariableLogBDeuScore(iAttribute);
        }
        return fLogScore;
    }

    public double logProbability(Instances instances) {
        double ll = 0;
        Enumeration enumInsts = instances.enumerateInstances();
        while (enumInsts.hasMoreElements()) {
            Instance instance = (Instance) enumInsts.nextElement();
            ll += logProbability(instance);
        }
        return ll;
    }

    public double logProbability(Instance instance) {
        double logProb = 0;
        for (int iAttribute = 0; iAttribute < instance.numAttributes(); iAttribute++) {
            int r_i = instance.attribute(iAttribute).numValues();
            int q_i = getParentCardinality(iAttribute);

            HashMap<Integer, Integer> query = new HashMap<Integer, Integer>();

            List<Integer> parentSet = getParents(iAttribute);
            for (Integer parent : parentSet) {
                query.put(parent, (int) instance.value(parent));
            }

            double n_ij = adTree.getCount(query, iAttribute);

            double n_ijk_prime = equivSampleSize / (r_i * q_i);
            double n_ij_prime = n_ijk_prime * r_i;

            query.put(iAttribute, (int) instance.value(iAttribute));
            double n_ijk = adTree.getCount(query, iAttribute);

            double probability = (n_ijk + n_ijk_prime) / (n_ij + n_ij_prime);

            logProb += Math.log(probability);
        }
        return logProb;
    }

    public void removeEdge(int from, int to) {
        List<Integer> parentSet = getParents(to);
        parentSet.remove(new Integer(from));

        dag[from][to] = false;

        skeleton[from][to] = false;
        skeleton[to][from] = false;
    }

    /**
     * setADTree effectively resets the parameters e.g., after reading a BN from file.
     *
     * @param newCache
     */
    public void setStatCache(StatCache newCache) {
        adTree = newCache;
    }

    public void setEquivalentSampleSize(double ess) {
        equivSampleSize = ess;
    }

    public void setInstances(Instances newInstances) {
        this.instances = newInstances;
    }

}
