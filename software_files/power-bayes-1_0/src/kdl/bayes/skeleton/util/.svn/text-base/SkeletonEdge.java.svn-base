/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.Assert;

public class SkeletonEdge implements Comparable {

    int var1;
    int var2;
    double score;

    public SkeletonEdge(int xIdx, int yIdx, double score) {
        var1 = xIdx;
        var2 = yIdx;
        this.score = score;
    }

    public int compareTo(Object o) {
        Assert.condition(o instanceof SkeletonEdge, "Can only compare two Skeleton Edges");
        return Double.compare(((SkeletonEdge) o).getScore(), this.score);
    }

    public int getVar1() {
        return var1;
    }

    public int getVar2() {
        return var2;
    }

    public double getScore() {
        return score;
    }

    public String toString() {
        return "[" + var1 + "," + var2 + "]=" + score;
    }
}
