/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 1:16:23 PM
 */
package rpc.design;

import rpc.schema.SchemaItem;
import rpc.dataretrieval.Aggregator;
import rpc.model.util.Variable;
import rpc.util.DesignUtil;

import java.util.List;
import java.util.ArrayList;

/**
 * The abstract path class stores the necessary information and basic methods for relational variables.
 * A relational variable consists of a base item, a path through the schema to some target item and
 * its cardinality, and a variable on that target item.
 */
public abstract class Path {

    /**
     * The cardinality of the path.
     */
    protected Cardinality cardinality;
    /**
     * The base schema item of the path.
     */
    protected SchemaItem baseItem;
    /**
     * The target schema item of the path.
     */
    protected SchemaItem target;

    /**
     * The variable associated with the path.
     */
    protected Variable var;

    /**
     * The path of schema items from the base to the target item.
     */
    protected List<SchemaItem> path;

    /**
     * A list of potential aggregators that can be applied to the path.
     */
    protected List<Aggregator> aggs;

    protected String stringRep = null;
    protected int hashCached = -1;

    /**
     * Initializes a path object.
     * @param baseItem the base schema item of the path.
     * @param target the target schema item of the path.
     * @param path the list of schema items from the base to the target.
     * @param card the cardinality of the path.
     */
    protected Path(SchemaItem baseItem, SchemaItem target, List<SchemaItem> path, Cardinality card) {
        this.baseItem = baseItem;
        this.target = target;
        this.path = path;
        this.cardinality = card;

        this.aggs = new ArrayList<Aggregator>();
    }

    /**
     * Compares this path to the specified object. The result is true if and only if the paths
     * have the same base items, target items, variables, cardinalities, and paths through the schema.
     * @param o the object to compare this path against.
     * @return true if the paths are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Path) {
            Path other = (Path) o;
            //everything must be the same!
            boolean isEqual = this.baseItem.equals(other.baseItem) &&
                    this.target.equals(other.target) &&
                    this.var.equals(other.var) &&
                    this.cardinality.equals(other.cardinality);

            return isEqual && DesignUtil.comparePaths(this.path, other.path);
        }
        else {
            return false;
        }
    }

    /**
     * Get the list of possible aggregators for the path.
     * @return the list of possible aggregators for the path.
     */
    public List<Aggregator> getAggregators() {
        return this.aggs;
    }

    /**
     * Get the cardinality {ONE, MANY} for the path.
     * @return the cardinality of the path.
     */
    public Cardinality getCardinality() {
        return this.cardinality;
    }

    /**
     * Get the base schema item of the path.
     * @return the base schema item of the path.
     */
    public SchemaItem getBaseItem() {
        return this.baseItem;
    }

    /**
     * Get the list of schema items corresponding to the path through the schema.
     * @return the list of schema items for the path from the base to the target.
     */
    public List<SchemaItem> getPath() {
        return this.path;
    }

    /**
     * Get the list of names of schema items corresponding to the path through the schema.
     * @return the list of names of schema items for the path from the base to the target.
     */
    public List<String> getPathNames() {
        List<String> names = new ArrayList<String>();
        for (SchemaItem si : this.path) {
            names.add(si.name);
        }
        return names;
    }

    /**
     * Get the number of hops along the path.  This is simply one less than the number of
     * items along the path.
     * @return the number of hops along the path.
     */
    public int getHopCount() {
        return this.path.size() - 1;
    }

    /**
     * Get the target schema item of the path.
     * @return the target schema item of the path.
     */
    public SchemaItem getTarget() {
        return this.target;
    }

    /**
     * Get the variable associated with the path.
     * @return the variable associated with the path.
     */
    public Variable getVariable() {
        return this.var;
    }

    private int getHashCode() {
        return this.toString().hashCode();
    }

    /**
     * Returns a hash code for this path.  The hash code is equal to the hash code of the string representaiton
     * of the path.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        if (this.hashCached == -1) {
            if (this.stringRep == null) {
                this.toString();
            }
            this.hashCached = this.getHashCode();
        }
        return this.hashCached;
    }

    protected abstract String getStringRep();

    /**
     * Returns a string representation of the path.
     * @return the string representation.
     */
    public String toString() {
        if (this.stringRep == null) {
            this.stringRep = getStringRep();
        }
        return this.stringRep;
    }

}