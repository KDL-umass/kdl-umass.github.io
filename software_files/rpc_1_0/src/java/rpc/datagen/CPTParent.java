/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: May 9, 2009
 * Time: 6:56:50 PM
 */
package rpc.datagen;

import org.python.core.PyDictionary;

import java.util.*;

import rpc.util.PythonUtil;

/**
 * CPTParent is a conditional probability table consisting of a set of discrete probability distribution for
 * different combinations of parent values.
 */
public class CPTParent extends CPT {

    private HashMap<List<Object>, CPTPrior> table;

    /**
     * Construct a CPTParent and normalize the probabilities.
     * @param parentsToCPTs a map from a list of parent values to a conditional probability
     * table (a CPTPrior).
     */
    public CPTParent(HashMap<List<Object>, CPTPrior> parentsToCPTs) {
        this.table = parentsToCPTs;
        this.normalize();  // make sure probabilities add to 1
    }

    /**
     * Jython overload constructor: construct a CPTParent and normalize the probabilities.
     * @param parentsToCPTs a dictionary of parent values to probability distributions,
     * e.g., { (0, 1):{0:0.7, 1:0.3}, ... }.
     */
    public CPTParent(PyDictionary parentsToCPTs) {
        this.table = PythonUtil.mapFromCPTParentPyDict(parentsToCPTs);
        this.normalize();  // make sure probabilities add to 1
    }

    /**
     * Normalize the probabilities in each CPT to make sure they add up to 1.0.
     */
    public void normalize() {
        // for each set of parent values, normalize the CPT it points to
        for (Map.Entry<List<Object>, CPTPrior> ent : this.table.entrySet()) {
            List<Object> parent = ent.getKey();
            this.table.get(parent).normalize();
        }
    }

    /**
     * Randomly sample a value from the CPT corresponding to a set of parent values.
     * @param parents a list of values for each parent.
     * @return an integer value corresponding to the sampled item.
     */
    public Integer sample(List<Object> parents) {
        if (!this.table.containsKey(parents))
            return null;

        CPTPrior cpt = this.table.get(parents);
        return cpt.sample();

    }

    /**
     * Returns a string representation of the set of conditional probability tables.
     * @return the string representation.
     */
    public String toString() {
        return this.table.toString();
    }

}