/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.adtree;

import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StatTable creates set of CPTs for each variable from a BayesNet read from file.
 * A learned BayesNet should use an ADTree as a stat cache.  As soon as the structure changes,
 * then a StatTable will need to be replaced with an ADTree.
 */

public class StatTable implements StatCache {

    private static Logger log = Logger.getLogger(StatTable.class);

    List<double[][]> cpts;
    Instances instances;
    List<List<Integer>> parentSets;

    /**
     * Build a stat table use data from an XMLBIF file.  Values are iterated in order appearing
     * in the instances.
     *
     * @param instances
     * @param dag
     * @param probTablesFromFile
     */
    public StatTable(Instances instances, List<List<Integer>> parentSets, List<String> probTablesFromFile) {

        this.instances = instances;
        this.parentSets = parentSets;
        this.cpts = new ArrayList<double[][]>();

        for (int var = 0; var < instances.numAttributes(); var++) {
            List<Integer> parents = parentSets.get(var);
            int parentCard = getCardinality(parents);
            int numValues = instances.attribute(var).numValues();

            double[][] cpt = new double[numValues][parentCard];

            String[] probs = probTablesFromFile.get(var).split(" ");

            for (int i = 0; i < parentCard; i++) {
                for (int j = 0; j < numValues; j++) {
                    int index = (i * numValues) + j;
                    cpt[j][i] = Double.parseDouble(probs[index]);
                }
            }
            cpts.add(cpt);
        }

    }

    private int getCardinality(List<Integer> vars) {
        int card = 1;
        for (Integer var : vars) {
            card = card * instances.attribute(var).numValues();
        }

        return card;
    }


    public double getCount(Map<Integer, Integer> query, int varIdx) {

        //If varIdx is not in the query, then a marginal count is desired. For the table case that is the number
        //of parent configurations as for each configuration the probs sum to 1.
        if (!query.keySet().contains(varIdx)) {
            return 1;
        }

        List<Integer> parentSet = parentSets.get(varIdx);

        double iCPT = 0;
        for (int i = 0; i < parentSet.size(); i++) {
            int parentIdx = parentSet.get(i);
            iCPT = iCPT * instances.attribute(parentIdx).numValues() +
                    query.get(parentIdx);
        }

        int varVal = query.get(varIdx);

        return cpts.get(varIdx)[varVal][(int) iCPT];
    }
}
