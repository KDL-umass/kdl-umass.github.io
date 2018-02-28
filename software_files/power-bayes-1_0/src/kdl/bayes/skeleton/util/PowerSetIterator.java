/**
 * $Id: PowerSetIterator.java 267 2009-02-18 20:12:07Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.Assert;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * PowerSetIterator is an iterator over power sets of a given set
 * Author: mhay
 */
public class PowerSetIterator<T> implements Iterator<Set<T>> {
    protected static Logger log = Logger.getLogger(PowerSetIterator.class);
    private List<T> items;
    private List<Set<T>> subsets;
    private int size;

    public PowerSetIterator(List<T> items, int size) {
        Assert.condition(size <= items.size(),
                "Size must be <= the number of items");
        this.size = size;
        this.items = items;
        subsets = new ArrayList<Set<T>>();
        Set<T> subset = new HashSet<T>(size);
        int[] indexes = new int[size];
        int currIndex = 0;
        createSubsets(currIndex, indexes, subset);
    }

    public PowerSetIterator(Set<T> items, int size) {
        this(new ArrayList<T>(items), size);
    }

    // recursive algorithm
    private void createSubsets(int currIndex, int[] indexes, Set<T> currSubset) {
        // if subset has been constructed, add it to list of subsets
        if (currSubset.size() == size) {
            subsets.add(currSubset);
        }
        // otherwise, add the next element to the subset and recurse
        else {
            int endIndex = items.size() - (size - currSubset.size());
            for (int itemIdx = currIndex; itemIdx <= endIndex; itemIdx++) {
                Set<T> copy = new HashSet<T>(currSubset);
                copy.add(items.get(itemIdx));
                createSubsets(itemIdx + 1, indexes, copy);
            }
        }
    }

    public boolean hasNext() {
        return !subsets.isEmpty();
    }

    public Set<T> next() {
        Set<T> subset = subsets.get(0);
        subsets.remove(0);
        return subset;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}