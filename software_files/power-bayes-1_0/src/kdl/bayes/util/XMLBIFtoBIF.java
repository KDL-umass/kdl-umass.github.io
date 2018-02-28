/**
 * $Id: XMLBIFtoBIF.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import kdl.bayes.PowerBayesNet;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.ParentSet;
import weka.classifiers.bayes.net.estimate.DiscreteEstimatorBayes;
import weka.estimators.Estimator;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * XMLBIFtoBIF reads in an XMLBIF file and writes out a .bif file
 * (for debugging purpsoses)
 * Author: mhay
 */
public class XMLBIFtoBIF {

    private static void checkBayesNet(BayesNet bn) {
        for (int i = 0; i < bn.m_Distributions.length; i++) {
            for (int j = 0; j < bn.m_Distributions[i].length; j++) {
                DiscreteEstimatorBayes est = (DiscreteEstimatorBayes) bn.m_Distributions[i][j];
                double totalCount = 0;
                for (int k = 0; k < est.getNumSymbols(); k++) {
                    totalCount += est.getCount(k);
                }
                Assert.condition(StatUtil.equalDoubles(1.0, totalCount), "Distribution does not sum to 1: " + totalCount);
            }
        }
    }

    // Recursive function to print the exponential number of entries in the CPT, e.g.:
    //  (Prole, Adolescent) 0.1, 0.9;
    private static String cptToString(BayesNet bn, int attrIdx, ParentSet ps, String[] parentValues, int parentIdx, int parentConfig) {
        StringBuffer sb = new StringBuffer();
        int nrOfParents = bn.getNrOfParents(attrIdx);
        if (parentIdx < nrOfParents) {
            int attrIdxOfParent = ps.getParent(parentIdx);
            int parentCard = bn.getCardinality(attrIdxOfParent);
            parentConfig *= parentCard;
            for (int i = 0; i < parentCard; i++) {
                parentValues[parentIdx] = bn.getNodeValue(attrIdxOfParent, i);
                sb.append(cptToString(bn, attrIdx, ps, parentValues, parentIdx + 1, parentConfig + i));
            }
        } else {
            sb.append(" (");
            for (int i = 0; i < parentValues.length; i++) {
                String parentValue = parentValues[i];
                sb.append(parentValue + ",");
            }
            sb.append(") ");
            Estimator estimator = bn.m_Distributions[attrIdx][parentConfig];
            for (int i = 0; i < bn.getCardinality(attrIdx); i++) {
                double prob = estimator.getProbability(i);
                String probStr = DecimalFormat.getInstance().format(prob);
                sb.append(probStr + ", ");
            }
            sb.append(";\n");
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        BayesNet bn = new PowerBayesNet(args[0]);
//        checkBayesNet(bn);
        System.out.println(printSummaryInfo(bn, args[1]));
        System.out.println(printBayesNet(bn));
    }

    public static String printBayesNet(BayesNet bn) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bn.getNrOfNodes(); i++) {
            // for each variable, prints:
            //            variable GoodStudent {
            //              type discrete [ 2 ] { True, False };
            //            }
            sb.append("variable " + bn.getNodeName(i) + " {");
            sb.append("\n");
            sb.append("  type discrete [ " + bn.getCardinality(i) + " ] { ");
            for (int j = 0; j < bn.getCardinality(i); j++) {
                sb.append(bn.getNodeValue(i, j) + ", ");
            }
            sb.append("};\n");
            sb.append("}\n");
        }

        // for each variable, prints:
        // probability ( GoodStudent | SocioEcon, Age ) {
        // [ values of GoodStudent ]
        // ... rows of CPT
        for (int i = 0; i < bn.getNrOfNodes(); i++) {
            sb.append("probability ( " + bn.getNodeName(i) + " | ");
            ParentSet parentSet = bn.getParentSet(i);
            for (int j = 0; j < parentSet.getNrOfParents(); j++) {
                sb.append(bn.getNodeName(parentSet.getParent(j)) + ", ");
            }
            sb.append(")\n");
            sb.append(" [");
            for (int j = 0; j < bn.getCardinality(i); j++) {
                sb.append(bn.getNodeValue(i, j) + ", ");
            }
            sb.append("];\n");
            sb.append("{\n");

            String[] parentValues = new String[parentSet.getNrOfParents()];
            sb.append(cptToString(bn, i, parentSet, parentValues, 0, 0));
            sb.append("}\n");
        }
        return sb.toString();
    }

    private static String printSummaryInfo(BayesNet bn, String bnName) {
        StringBuffer sb = new StringBuffer();
        int numVars = bn.getNrOfNodes();
        sb.append("num vars=" + numVars + " ");
        int[] inDegree = new int[numVars];
        int[] outDegree = new int[numVars];
        int[] degree = new int[numVars];
        int minDomain = Integer.MAX_VALUE;
        int maxDomain = Integer.MIN_VALUE;
        int numEdges = 0;

        for (int i = 0; i < numVars; i++) {
            int cardinality = bn.getCardinality(i);
            if (cardinality > maxDomain) {
                maxDomain = cardinality;
            }
            if (cardinality < minDomain) {
                minDomain = cardinality;
            }
            int numParents = bn.getNrOfParents(i);
            inDegree[i] = numParents;
            degree[i] += numParents;
            numEdges += numParents;
            for (int j = 0; j < numParents; j++) {
                int parentIdx = bn.getParent(i, j);
                outDegree[parentIdx] += 1;
                degree[parentIdx] += 1;
            }
        }
        Arrays.sort(inDegree);
        Arrays.sort(outDegree);
        Arrays.sort(degree);

        int maxInDegree = inDegree[numVars - 1];
        int maxOutDegree = outDegree[numVars - 1];
        int minPC = degree[0];
        int maxPC = degree[numVars - 1];
        return bnName + " " +
                numVars + " " +
                numEdges + " " +
                maxInDegree + "/" + maxOutDegree + " " +
                minPC + "/" + maxPC + " " +
                minDomain + "-" + maxDomain;
    }
}
