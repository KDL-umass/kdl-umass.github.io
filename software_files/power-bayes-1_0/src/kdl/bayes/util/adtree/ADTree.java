/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.adtree;

import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

/**
 * ADTree implements the cacheing data structure described in:
 * <p/>
 * Cached Sufficient Statistics for Efficient Machine Learning with Large Datasets
 * Andrew Moore and Mary Soon Lee
 * JAIR 8 (1998) 67-91
 */

public class ADTree implements StatCache {

    private static Logger log = Logger.getLogger(ADTree.class);

    protected Instances data;
    protected ADNode root;

    protected int minCount = 0;


    public ADTree(Instances data) {

        log.info("Creating an ADTree");

        this.data = data;

        BitSet rows = new BitSet(data.numInstances());
        rows.set(0, data.numInstances());

        root = makeADTree(0, rows);
    }

    public ADTree(Instances data, int minCount) {

        log.info("Creating an ADTree (MC)");

        this.data = data;

        BitSet rows = new BitSet(data.numInstances());
        rows.set(0, data.numInstances());

        this.minCount = minCount;

        root = makeADTree(0, rows);
    }

    /**
     * Returns the count of instances appearing in the data for the given query where each query term
     * is of the form
     * [ attrIdx, attrValIdx ]
     *
     * @param query
     * @return
     */
    public double getCount(Map<Integer, Integer> query, int varIdx) {
        //Set<Integer> keyset = query.keySet();
        //log.info("Full Query: " + Arrays.deepToString(keyset.toArray()));
        //for (Integer key : keyset) {
        //    log.info("\tkey=" + key + " value=" + query.get(key) + "(" + data.numDistinctValues(key) + ")");
        //}
        return getCount(root, query);
    }

    protected double getCount(ADNode startNode, Map<Integer, Integer> query) {
        if (query.size() == 0) {
            return startNode.getCount();
        }

        List<Integer> keyList = new ArrayList<Integer>(query.keySet());
        Collections.sort(keyList);
        //log.info("Sub Query: " +  Arrays.deepToString(keyList.toArray()));
        //for (Integer key : keyList) {
        //    log.info("\tkey=" + key + " value=" + query.get(key));
        //}


        BitSet rows = startNode.getRows();
        if (rows != null) {
            //Iterate over the rows and count how many remaining instances match the query.
            int count = 0;
            int rowIdx = rows.nextSetBit(0);
            while (rowIdx >= 0) {
                Instance row = data.instance(rowIdx);
                //Iterate over the query
                boolean addCount = true;
                for (Integer attrIdx : keyList) {
                    int attrVal = query.get(attrIdx);

                    //Weka uses double but we can safely assume they are ints.
                    int v_ij = (int) row.value(attrIdx);
                    if (v_ij != attrVal) {
                        addCount = false;
                        break;
                    }
                }

                if (addCount) {
                    count++;
                }
                rowIdx = rows.nextSetBit(rowIdx + 1);
            }
            return count;
        }

        int attrIdx = keyList.get(0);
        int attrVal = query.get(attrIdx);

        //log.info("Query val:" + attrIdx + ", " + attrVal + "(" + data.attribute(attrIdx).numValues() + ")");

        VaryNode vNode = startNode.getChild(attrIdx);
        int mcv = vNode.getMCV();

        Map<Integer, Integer> subquery = new HashMap<Integer, Integer>(query);
        subquery.remove(attrIdx);

        if (attrVal == mcv) {
            int ct_k = 0;
            int numAttrVals = data.attribute(attrIdx).numValues();
            for (int k = 0; k < numAttrVals; k++) {
                if (k != mcv) {
                    ADNode child = vNode.getChild(k);
                    if (child != null) {
                        ct_k += getCount(child, subquery);
                    }
                }
            }
            return getCount(startNode, subquery) - ct_k;
        }

        ADNode child = vNode.getChild(attrVal);

        if (child == null) {
            return 0;
        }
        return getCount(child, subquery);
    }

    protected List<Integer> getRows() {
        List<Integer> rows = new ArrayList<Integer>();
        for (int i = 0; i < data.numInstances(); i++) {
            rows.add(i);
        }
        return rows;
    }

    protected ADNode makeADTree(int attrIndex, BitSet rows) {

        int size = rows.cardinality();
        ADNode node = new ADNode(attrIndex, size, data.numAttributes());
        if (size < minCount) {
            node.setRows(rows);
        } else {
            for (int j = attrIndex; j < data.numAttributes(); j++) {
                VaryNode vNode = makeVaryNode(j, rows);
                node.setChild(j, vNode);
            }
        }
        return node;
    }

    protected VaryNode makeVaryNode(int attrIndex, BitSet rows) {
        VaryNode vNode = new VaryNode(attrIndex, data.attribute(attrIndex).numValues());

        int n_i = data.attribute(attrIndex).numValues();

        List<BitSet> childnums = new ArrayList<BitSet>();

        for (int k = 0; k < n_i; k++) {
            childnums.add(new BitSet(data.numInstances()));
        }

        int rowIdx = rows.nextSetBit(0);
        while (rowIdx >= 0) {
            Instance row = data.instance(rowIdx);
            //Weka uses double we can safely assume they are ints.
            int v_ij = (int) row.value(attrIndex);
            childnums.get(v_ij).set(rowIdx);
            rowIdx = rows.nextSetBit(rowIdx + 1);
        }

        int maxValue = -1;
        int maxCount = -1;

        for (int k = 0; k < n_i; k++) {
            int size = childnums.get(k).cardinality();
            if (size > maxCount) {
                maxValue = k;
                maxCount = size;
            }
        }

        vNode.setMCV(maxValue);

        for (int k = 0; k < n_i; k++) {
            BitSet childRows = childnums.get(k);
            int size = childRows.cardinality();
            if (size == 0 || k == maxValue) {
                vNode.setChild(k, null);
            } else {
                ADNode child = makeADTree(attrIndex + 1, childRows);
                vNode.setChild(k, child);
            }
        }
        return vNode;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("\n");
        sb.append(root.toString());
        appendSubtree(sb, "\n", root);
        return sb.toString();
    }

    private StringBuffer appendSubtree(StringBuffer sb, String prefix, Node node) {
        Node[] children = node.getChildren();
        String newPrefix = prefix + "\t";
        for (Node childNode : children) {
            sb.append(newPrefix);
            if (childNode != null) {
                sb.append(childNode.toString());
                appendSubtree(sb, newPrefix, childNode);
            } else {
                sb.append("[NULL]");
            }

        }
        return sb;
    }
}
