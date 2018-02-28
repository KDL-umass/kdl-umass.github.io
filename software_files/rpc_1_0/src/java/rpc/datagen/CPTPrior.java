/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: May 9, 2009
 * Time: 6:15:09 PM
 */
package rpc.datagen;

import org.python.core.PyDictionary;

import java.util.*;

import rpc.util.PythonUtil;

/**
 * CPTPrior is a conditional probability table consisting of a single discrete probability distribution.
 */
public class CPTPrior extends CPT {

    private HashMap<Integer, Double> table;
    private Random random = new Random();

    /**
     * Construct a CPTPrior and normalize the probabilities.
     * @param table a map from integer values to their corresponding probabilities.
     */
    public CPTPrior(HashMap<Integer, Double> table) {
        this.table = table;
        this.normalize();  // make sure probabilities add to 1
    }

    /**
     * Jython overload constructor: construct a CPTPrior and normalize the probabilities.
     * @param table a python dictionary specifying integer values and their corresponding probabilities.
     */
    public CPTPrior(PyDictionary table) {
        this.table = PythonUtil.mapFromIntegerDoublePyDict(table);
        this.normalize();  // make sure probabilities add to 1
    }

    /**
     * Normalize the probabilities to make sure they add up to 1.0.
     */
    public void normalize() {
        double total = 0.0;

        for(Double value: this.table.values()) {
            total += value;
        }

        for (Map.Entry ent : this.table.entrySet()) {
            this.table.put((Integer)ent.getKey(), 1.0*(Double)ent.getValue()/total);
        }
    }

    /**
     * Randomly sample an item from the CPT
     * @return an integer value corresponding to the sampled item.
     */
    public Integer sample() {
        double rand = random.nextDouble();
        double total = 0.0;
        for (Map.Entry ent : this.table.entrySet()) {
            total = total + (Double)ent.getValue();
            if (total >= rand) {
                return (Integer)ent.getKey();
            }

        }

        // we are guaranteed a return in the for loop, but just in case
        return null;
    }

    /**
     * Returns a string representation of the conditional probability table.
     * @return the string representation.
     */
    public String toString() {
        return this.table.toString();
    }
}