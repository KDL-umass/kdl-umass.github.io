/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 3:31:37 PM
 */
package rpc.dataretrieval;

import java.util.*;

/**
 * A 2-dimensional table of counts of observations for pairs of variables.
 */
public class ContingencyTable {

    private HashMap<Double, HashMap<Double, Integer>> counts;

    /**
     * Initializes an empty 2-dimensional contingency table.
     */
    public ContingencyTable() {
        this.counts = new HashMap<Double, HashMap<Double, Integer>>();
    }

    /**
     * Adds the number of observations of (val1, val2) to the contingency table.
     * Will create new rows/columns as needed.
     * @param val1 first value.
     * @param val2 second value.
     * @param count total number of observations for (val1, val2).
     */
    public void addValues(double val1, double val2, int count) {
        if (! this.counts.containsKey(val1)) {
            this.counts.put(val1, new HashMap<Double, Integer>());
        }

        if (! this.counts.get(val1).containsKey(val2)) {
            this.counts.get(val1).put(val2, 0);
        }

        this.counts.get(val1).put(val2, this.counts.get(val1).get(val2) + count);
    }

    /**
     * Compares this contingency table to the specified object. The result is true if and only if all counts of
     * observations of pairs of variables are the same.
     * @param o the object to compare this ContigencyTable against.
     * @return true if the ContigencyTables are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof ContingencyTable) {
            ContingencyTable other = (ContingencyTable) o;
            HashMap<Double, HashMap<Double, Integer>> otherCounts = other.getCounts();
            if (this.counts.size() != otherCounts.size()) {
                return false;
            }
            for (Double d1 : this.counts.keySet()) {
                if (! otherCounts.containsKey(d1)) {
                    return false;
                }
                for (Double d2 : this.counts.get(d1).keySet()) {
                    if (! otherCounts.get(d1).containsKey(d2)) {
                        return false;
                    }
                    if (! this.counts.get(d1).get(d2).equals(otherCounts.get(d1).get(d2))) {
                        return false;
                    }
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Gets a nested map of variable1 -> (variable2 -> count)
     * @return the hashmap holding the counts of each pair of values.
     */
    public HashMap<Double, HashMap<Double, Integer>> getCounts() {
        return this.counts;
    }

    /**
     * Gets a 2-dimensional array from the internal map of counts.
     * @return the 2D array of counts for the contingency table.
     */
    public double[][] getTable() {
        Set<Double> val1s = this.counts.keySet();
        Set<Double> val2s = new HashSet<Double>();
        for (Double val1 : val1s) {
            val2s.addAll(this.counts.get(val1).keySet());
        }

        double[][] countArray = new double[val1s.size()][];
        for (int i=0; i<val1s.size(); i++) {
            countArray[i] = new double[val2s.size()];
        }

        List<Double> val1List = new ArrayList<Double>(val1s);
        List<Double> val2List = new ArrayList<Double>(val2s);

        for (int i=0; i<val1List.size(); i++) {
            for (int j=0; j<val2List.size(); j++) {
                Integer curCount = this.counts.get(val1List.get(i)).get(val2List.get(j));
                if (curCount == null) {
                    countArray[i][j] = 0;
                }
                else {
                    countArray[i][j] = curCount;
                }
            }
        }        

        return countArray;
    }

    /**
     * Gets the total number of observations.
     * @return the sum of all entries in the contingency table.
     */
    public int size() {
        int size = 0;

        for (Double key1 : this.counts.keySet()) {
            for (Double key2 : this.counts.get(key1).keySet()) {
                size += this.counts.get(key1).get(key2);
            }
        }

        return size;
    }

    /**
     * Checks if there are any rows or columns that consist of a single value.
     * @return true if there exists such a row or column; false otherwise.
     */
    public boolean isAllSingleRowOrColumn() {
        Set<Double> rowsWithValues = new HashSet<Double>();
        Set<Double> colsWithValues = new HashSet<Double>();

        Set<Double> val1s = this.counts.keySet();
        Set<Double> val2s = new HashSet<Double>();
        for (Double val1 : val1s) {
            val2s.addAll(this.counts.get(val1).keySet());
        }

        for (Double row : val1s) {
            for (Double col : val2s) {
                if (this.counts.get(row).containsKey(col) && this.counts.get(row).get(col) > 0) {
                    rowsWithValues.add(row);
                    colsWithValues.add(col);
                }
            }
        }

        return rowsWithValues.size() <= 1 || colsWithValues.size() <= 1;
    }

    /**
     * Returns a string representation to visualize the contingency table.
     * @return the string representation.
     */
    public String toString() {
        String ret = "";

        Set<Double> val1s = this.counts.keySet();
        Set<Double> val2s = new HashSet<Double>();
        for (Double val1 : val1s) {
            val2s.addAll(this.counts.get(val1).keySet());
        }

        List<Double> val1List = new ArrayList<Double>(val1s);
        List<Double> val2List = new ArrayList<Double>(val2s);

        Map<Double, Integer> columnMarginals = new HashMap<Double, Integer>();

        //column labels
        ret += String.format(" %10s ", "");
        for (Double val2 : val2List) {
            ret += String.format("  %10s  ", val2);
            columnMarginals.put(val2, 0);
        }
        ret += "\n";

        //rows and row marginals
        for (Double val1 : val1List) {
            ret += String.format(" %10s ", val1);
            int rowMarginal = 0;
            for (Double val2 : val2List) {
                Integer cell = this.counts.get(val1).get(val2);
                Integer curCount = cell == null ? 0 : cell;

                ret += String.format("[ %10d ]", curCount);
                rowMarginal += curCount;
                columnMarginals.put(val2, columnMarginals.get(val2) + curCount);
            }
            ret += String.format("  %10d  ", rowMarginal);
            ret += "\n";
        }

        //column marginals
        ret += "\n";
        ret += String.format(" %10s ", "");
        for (Double val2 : val2List) {
            ret += String.format("  %10d  ", columnMarginals.get(val2));
        }

        //total
        ret += String.format("  %10d  ", this.size());

        return ret;
    }

}
