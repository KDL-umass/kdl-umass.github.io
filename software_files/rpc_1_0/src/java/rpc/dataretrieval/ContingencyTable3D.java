/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 11, 2009
 * Time: 12:27:08 PM
 */
package rpc.dataretrieval;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * A 3-dimensional table of counts of observations for pairs of variables across different configurations of
 * a third set of variables.
 */
public class ContingencyTable3D {

    private static Logger log = Logger.getLogger(ContingencyTable3D.class);

    private HashMap<Integer, ContingencyTable> counts;
    private HashMap<List<Object>, Integer> configIdMap;
    private int numConfigs = 0;

    /**
     * Initializes an empty 3-dimensional contingency table.
     */
    public ContingencyTable3D() {
        this.counts = new HashMap<Integer, ContingencyTable>();
        this.configIdMap = new HashMap<List<Object>, Integer>();
    }

    /**
     * Adds the number of observations of (val1, val2) for given configuration to the contingency table.
     * Will create new rows/columns/dimensions as needed.
     * @param val1 first value.
     * @param val2 second value.
     * @param config configuration of third set of variables.
     * @param count total number of observations for (val1, val2) within specified configuration.
     */
    public void addValues(double val1, double val2, List<Object> config, int count) {
        if (! this.configIdMap.containsKey(config)) {
            this.configIdMap.put(config, this.numConfigs);
            this.counts.put(this.numConfigs, new ContingencyTable());
            this.numConfigs++;
        }
        int configId = this.configIdMap.get(config);
        this.counts.get(configId).addValues(val1, val2, count);
    }

    /**
     * Gets a map from the configurations to their corresponding 2-dimensional contigency tables.
     * @return the map holding the contigency tables for each configuration of the third dimension.
     */
    public Map<List<Object>, ContingencyTable> getCounts() {
        Map<List<Object>, ContingencyTable> countRet = new HashMap<List<Object>, ContingencyTable>();
        for (List<Object> config : configIdMap.keySet()) {
            countRet.put(config, this.counts.get(this.configIdMap.get(config)));
        }
        return countRet;
    }

    /**
     * Gets the number of contingency tables in the third dimension.
     * @return the total number of contingency tables.
     */
    public int getNumDimensions() {
        return this.counts.size();
    }

    /**
     * Gets a 3-dimensional array from the internal map of configurations to 2D contingency tables.
     * @return the 3D array of counts for the 3-dimensional contingency table.
     */
    public double[][][] getTable() {
        //First, determine the number of x and y values across all 2D tables
        HashSet<Double> xKeys = new HashSet<Double>();
        HashSet<Double> yKeys = new HashSet<Double>();

        // cite: Categorical Data Analysis, Alan Agresti, 1990, Wiley
        Set<Integer> configsToSkip = new HashSet<Integer>();
        for (Integer configId : this.counts.keySet()) {
            ContingencyTable cont = this.counts.get(configId);
            if (cont.isAllSingleRowOrColumn()) {
                configsToSkip.add(configId);
            }
        }
        log.debug("configsToSkip: " + configsToSkip);

        for (Integer configId : this.counts.keySet()) {
            if (configsToSkip.contains(configId)) {
                continue;
            }
            HashMap<Double, HashMap<Double, Integer>> counts2DSub = this.counts.get(configId).getCounts();
            for (Double xKey : counts2DSub.keySet()) {
                xKeys.add(xKey);
                for (Double yKey : counts2DSub.get(xKey).keySet()) {
                    yKeys.add(yKey);
                }
            }
        }

        int xDim = xKeys.size();
        int yDim = yKeys.size();
        int zDim = this.counts.size() - configsToSkip.size();

        //Now, initialize and populate 3D table
        double[][][] countArray = new double[zDim][xDim][yDim];

        List<Double> xValList = new ArrayList<Double>(xKeys);
        List<Double> yValList = new ArrayList<Double>(yKeys);

        int i=0;
        for (Integer configId : this.counts.keySet()) {
            if (configsToSkip.contains(configId)) {
                continue;
            }            
            HashMap<Double, HashMap<Double, Integer>> counts2D = this.counts.get(configId).getCounts();
            for (int j=0; j<xValList.size(); j++) {
                Double xVal = xValList.get(j);
                for (int k=0; k<yValList.size(); k++) {
                    Double yVal = yValList.get(k);

                    if (! counts2D.containsKey(xVal)) {
                        countArray[i][j][k] = 0;
                    }
                    else {
                        Integer curCount = counts2D.get(xVal).get(yVal);
                        if (curCount == null) {
                            countArray[i][j][k] = 0;
                        }
                        else {
                            countArray[i][j][k] = curCount;
                        }
                    }
                }
            }
            i++;
        }

        return countArray;
    }

    /**
     * Returns a string representation to visualize the 3-dimensional contingency table.
     * @return the string representation.
     */    
    public String toString() {
        String ret = "";

        for (Integer configId : this.counts.keySet()) {
            ret += String.format(" %20s \n", configId);
            ret += this.counts.get(configId).toString();            
        }

        return ret;
    }
}
