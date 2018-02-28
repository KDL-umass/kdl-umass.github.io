/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import org.apache.log4j.Logger;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZQueryIterator implements Iterator<Map<Integer, Integer>> {

    protected static Logger log = Logger.getLogger(ZQueryIterator.class);

    int[] zIdxs;
    Instances data;

    boolean prevUpdate = true;

    int[] currIdx;
    int[] numVals;

    public ZQueryIterator(int[] zIdxs, Instances data) {
        this.zIdxs = zIdxs;
        this.data = data;

        currIdx = new int[zIdxs.length];
        numVals = new int[zIdxs.length];

        for (int i = 0; i < zIdxs.length; i++) {
            currIdx[i] = 0;
            numVals[i] = data.attribute(zIdxs[i]).numValues();
        }

        prevUpdate = true;
    }

    public ZQueryIterator(List<Integer> zList, Instances data) {
        int[] zIdxs = new int[zList.size()];
        for (int i = 0; i < zList.size(); i++) {
            zIdxs[i] = zList.get(i);
        }

        this.zIdxs = zIdxs;
        this.data = data;

        currIdx = new int[zIdxs.length];
        numVals = new int[zIdxs.length];

        for (int i = 0; i < zIdxs.length; i++) {
            currIdx[i] = 0;
            numVals[i] = data.attribute(zIdxs[i]).numValues();
        }

        prevUpdate = true;
    }

    public boolean hasNext() {
        return prevUpdate;
    }

    public Map<Integer, Integer> next() {
        Map<Integer, Integer> query = new HashMap<Integer, Integer>();

        //Build query
        for (int i = 0; i < zIdxs.length; i++) {
            int zIdx = zIdxs[i];
            int valIdx = currIdx[i];
            query.put(zIdx, valIdx);
        }

        if (zIdxs.length > 0) {
            prevUpdate = update(0);
        } else {
            prevUpdate = false;
        }

        return query;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove is unsupported");
    }

    public boolean update(int col) {
        //Now update with propagation if necessary
        currIdx[col] = (currIdx[col] + 1) % numVals[col];

        if (currIdx[col] == 0) { //We hit the top so propagate to the next value
            if (col == zIdxs.length - 1) { //Went over the top on the last column.
                return false;
            }
            return update(col + 1);
        }
        return true;
    }


}
