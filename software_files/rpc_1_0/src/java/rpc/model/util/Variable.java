/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 2:27:43 PM
 */
package rpc.model.util;

import rpc.schema.SchemaItem;

/**
 * Abstract skeleton of a variable, which has a source item from the schema.
 */
public abstract class Variable {

    /**
     * The name of the variable.
     */
    protected String name;
    /**
     * The item in the schema that is the source of the variable.
     */
    protected SchemaItem source;

    /**
     * Initialize a variable by setting the source.
     * @param source the item in the schema that is the source of the variable.
     */
    protected Variable(SchemaItem source) {
        this.source = source;
    }

    /**
     * Compares this variable to the specified object. The result is true if and only if the variables
     * have the same source table and name.
     * @param o the object to compare this Variable against.
     * @return true if the Variables are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Variable) {
            Variable other = (Variable) o;
            return this.source.equals(other.source) && this.name.equals(other.name);
        }
        return false;
    }

    /**
     * Returns a hash code for this variable.  The hash code is equal to the hash code of the string representaiton
     * of the variable.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Get the source schema item of the variable
     * @return the source schema item.
     */
    public SchemaItem getSource() {
        return this.source;
    }

    /**
     * Get the name of the variable.
     * @return the name.
     */
    public String name() {
        return this.name;
    }

    /**
     * Returns a string representation consisting of sourceName.varName.
     * @return the string representation.
     */
    public String toString() {
        return this.source.name + "." + this.name;
    }
}
