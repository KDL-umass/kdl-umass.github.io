/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Produce all ordered permutations from a given list.
 * author: afast
 */

public class PermutationIterator implements Iterator {

    protected static Logger log = Logger.getLogger(PermutationIterator.class);
    private List items;
    private int currIndex = 0;
    private long numPermutations;

    public PermutationIterator(List items) {
        this.items = items;
        numPermutations = StatUtil.factorial(items.size());
    }

    protected List getPermutation(List s, int k) {
        List copy = new ArrayList(s);
        for (int i = 2; i <= s.size(); i++) {
            k = k / (i - 1);        // integer division
            swap((k % i), i - 1, copy);
        }
        return copy;
    }

    protected void swap(int pos1, int pos2, List s) {
        Object obj1 = s.get(pos1);
        s.add(pos2, obj1);
        Object obj2 = s.remove(pos2 + 1);
        s.add(pos1, obj2);
        s.remove(pos1 + 1);
    }

    public boolean hasNext() {
        return (currIndex < numPermutations);
    }

    public Object next() {
        Object permute = getPermutation(items, currIndex);
        currIndex++;
        return permute;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}


