/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 1:38:18 PM
 */
package rpc.dataretrieval;

import rpc.model.util.Variable;

/**
 * Extension of the abstract aggregator that uses no operator.  A NopAggregator must be the base
 * aggregator for all other aggregator objects.
 */
public class NopAggregator extends Aggregator {

    /**
     * Constructs a NopAggregator object over the supplied base variable.
     * @param var the base variable to aggregate over.
     */
    public NopAggregator(Variable var) {
        this.var = var;
        this.tableName = var.getSource().name;
    }

    /**
     * Sets the name of the base source table.
     * @param name the new name for the base source table.
     */
    public void setTableName(String name) {
        this.tableName = name;
    }

    /**
     * Resets the name of the base source table to be the original table name.
     */
    public void resetTableName() {
        this.tableName = this.var.getSource().name;
    }

    /**
     * Gets the name of the source table.
     * @return the name of the source table.
     */
    public String getTableName() {
        return this.tableName;
    }

    /**
     * Gets the name of the variable.
     * @return the name of the variable.
     */
    public String getVarName() {
        return this.var.name();
    }


}