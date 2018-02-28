/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.adtree;

public class VaryNode implements Node {

    ADNode[] children;
    int mcv; //Stores the index of the most common value
    int varIdx;

    public VaryNode(int varIdx, int childrenSize) {
        this.varIdx = varIdx;
        children = new ADNode[childrenSize];
    }

    public ADNode getChild(int i) {
        return children[i];
    }

    public Node[] getChildren() {
        return children;
    }

    public void setChild(int i, ADNode child) {
        children[i] = child;
    }

    public int getMCV() {
        return mcv;
    }

    public void setMCV(int newMCV) {
        mcv = newMCV;
    }

    public String toString() {
        return "VaryNode: mcv=" + mcv;
    }

}
