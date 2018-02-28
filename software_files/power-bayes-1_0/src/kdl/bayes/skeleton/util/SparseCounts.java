/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import java.util.HashMap;
import java.util.Map;

public class SparseCounts {

    int xCard;
    int yCard;
    int zCard;

    Map<Integer, Integer> counts;

    public SparseCounts(int xLen, int yLen, int zLen) {
        xCard = xLen;
        yCard = yLen;
        zCard = zLen;

        counts = new HashMap<Integer, Integer>();
    }

    public int get(int x, int y, int z) {
        return get(getKey(x, y, z));
    }

    public int get(Integer key) {
        if (counts.containsKey(key)) {
            return counts.get(key);
        }
        return 0;
    }

    public int getKey(int x, int y, int z) {
        return ((x * xCard) + y) + ((xCard * yCard) * z);
    }

    public void set(int x, int y, int z, int count) {
        counts.put(getKey(x, y, z), count);
    }

    public void set(Integer key, int count) {
        counts.put(key, count);
    }

    public int rows() {
        return xCard;
    }

    public int columns() {
        return yCard;
    }

    public int slices() {
        return zCard;
    }


}
