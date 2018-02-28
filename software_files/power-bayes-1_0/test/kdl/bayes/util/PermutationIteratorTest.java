/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermutationIteratorTest extends TestCase {

    protected static Logger log = Logger.getLogger(PermutationIterator.class);


    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testSwap() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(0);
        list.add(1);
        list.add(2);
        list.add(3);

        PermutationIterator iter = new PermutationIterator(list);

        iter.swap(0, 2, list);

        Assert.condition(list.get(0) == 2, "Should have swapped with 2");

        iter.swap(1, 2, list);

        Assert.condition(list.get(0) == 2, "Swap failed.");
        Assert.condition(list.get(1) == 0, "Swap failed.");
        Assert.condition(list.get(2) == 1, "Swap failed.");
        Assert.condition(list.get(3) == 3, "Swap failed.");

    }

    public void testGetPermutation() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(0);
        list.add(1);
        list.add(2);
        //list.add(3);

        PermutationIterator iter = new PermutationIterator(list);

        int numPermutations = 3 * 2;

        Set<List<Integer>> permuteSet = new HashSet<List<Integer>>();

        for (int i = 0; i < numPermutations; i++) {
            List permutation = iter.getPermutation(list, i);
            log.info(permutation);
            permuteSet.add(permutation);
        }

        Set<List<Integer>> trueSet = new HashSet<List<Integer>>();

        List<Integer> truePermute = new ArrayList<Integer>();
        truePermute.add(0);
        truePermute.add(1);
        truePermute.add(2);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(0);
        truePermute.add(2);
        truePermute.add(1);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(1);
        truePermute.add(0);
        truePermute.add(2);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(1);
        truePermute.add(2);
        truePermute.add(0);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(2);
        truePermute.add(0);
        truePermute.add(1);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(2);
        truePermute.add(1);
        truePermute.add(0);

        trueSet.add(truePermute);

        //log.info(permuteSet);
        //log.info(trueSet);

        Util.verifyCollections(trueSet, permuteSet);

    }

    public void testPermutationIterator() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(0);
        list.add(1);
        list.add(2);
        //list.add(3);

        PermutationIterator iter = new PermutationIterator(list);


        Set<List<Integer>> permuteSet = new HashSet<List<Integer>>();

        while (iter.hasNext()) {
            List permutation = (List) iter.next();
            log.info(permutation);
            permuteSet.add(permutation);
        }

        Set<List<Integer>> trueSet = new HashSet<List<Integer>>();

        List<Integer> truePermute = new ArrayList<Integer>();
        truePermute.add(0);
        truePermute.add(1);
        truePermute.add(2);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(0);
        truePermute.add(2);
        truePermute.add(1);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(1);
        truePermute.add(0);
        truePermute.add(2);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(1);
        truePermute.add(2);
        truePermute.add(0);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(2);
        truePermute.add(0);
        truePermute.add(1);

        trueSet.add(truePermute);

        truePermute = new ArrayList<Integer>();
        truePermute.add(2);
        truePermute.add(1);
        truePermute.add(0);

        trueSet.add(truePermute);

        //log.info(permuteSet);
        //log.info(trueSet);

        Util.verifyCollections(trueSet, permuteSet);
    }
}
