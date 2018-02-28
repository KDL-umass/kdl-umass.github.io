/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.adtree;

import java.util.BitSet;

public class ADNode implements Node {

    double count;
    int startAttrIdx;
    VaryNode[] children;
    BitSet rows = null;

    public ADNode(int startAttrIdx, double count, int numVars) {
        this.count = count;
        children = new VaryNode[numVars - startAttrIdx];
        this.startAttrIdx = startAttrIdx;
    }

    /**
     * Constructor for nodes with fewer than minCount instances.
     *
     * @param startAttrIdx
     * @param count
     * @param rows
     */
    public ADNode(int startAttrIdx, double count, BitSet rows) {
        this.count = count;
        children = null;
        this.startAttrIdx = startAttrIdx;
        this.rows = rows;
    }

    public double getCount() {
        return count;
    }

    public void setCount(int newCount) {
        this.count = newCount;
    }

    public VaryNode getChild(int i) {
        return children[i - startAttrIdx];
    }

    public Node[] getChildren() {
        return children;
    }

    public void setChild(int i, VaryNode child) {
        children[i - startAttrIdx] = child;
    }

    public void setRows(BitSet rows) {
        this.rows = rows;
        children = null;
    }

    public BitSet getRows() {
        return rows;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("ADNode: c=" + count);
        return sb.toString();
    }

}
