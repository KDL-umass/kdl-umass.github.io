/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 1:32:27 PM
 */
package rpc.dataretrieval;

import rpc.model.util.Variable;

/**
 * This class provides a skeletal implementation of an Aggregator to be used as a function over set-valued attributes.
 */
public abstract class Aggregator {

    /**
     * The nested aggregator.
     */
    protected Aggregator agg;
    /**
     * The base variable at the core of the aggregator.
     */
    protected Variable var;
    /**
     * The source table on which the variable is defined.
     */
    protected String tableName;

    /**
     * Default constructor to make extensions that don't use the main constructor work (e.g., NopAggregator).
     */
    protected Aggregator() {
    }

    /**
     * Constructs an aggregator object to become a nested aggregator.
     * @param agg the inner aggregator object.
     */
    public Aggregator(Aggregator agg){
        this.agg = agg;
    }

    /**
     * Recursively sets the name of the base source table.
     * @param name the name of the table.
     */
    public void setTableName(String name) {
        this.agg.setTableName(name);
    }

    /**
     * Recursively resets the name of the base source table to be the original table name.
     */
    public void resetTableName() {
        this.agg.resetTableName();
    }

    /**
     * Recursively gets the name of the source table.
     * @return the name of the source table.
     */
    public String getTableName() {
        return this.agg.getTableName();
    }

    /**
     * Recursively gets the name of the variable.
     * @return the name of the variable.
     */
    public String getVarName() {
        return this.agg.getVarName();
    }

    /**
     * Compares this aggregator to the specified object. The result is true if and only if the aggregators operate
     * over the same variable and source table names.
     * @param o the object to compare this Aggregator against.
     * @return true if the Aggregators are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Aggregator) {
            Aggregator other = (Aggregator) o;
            return this.var.equals(other.var) && this.tableName.equals(other.tableName);
        }
        else {
            return false;
        }
    }

    /**
     * Returns a hash code for this aggregator.  The hash code is equal to the hash code of the string representaiton
     * of the aggregator. 
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Returns a string representation consisting of tableName.varName.
     * @return the string representation.
     */
    public String toString() {
        return this.tableName + "." + this.var.name();
    }

}