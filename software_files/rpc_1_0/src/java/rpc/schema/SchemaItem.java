/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 5, 2009
 * Time: 3:12:35 PM
 */
package rpc.schema;

import org.apache.log4j.Logger;

import java.util.*;

import jpl.Query;
import jpl.Term;

/**
 * Abstract class that provides methods about a schema item in a schema.
 */
public abstract class SchemaItem {

    private static Logger log = Logger.getLogger(SchemaItem.class);

    /**
     * The name of the schema item.
     */
    public String name;
    protected Attribute primaryKey;

    protected Map<String, Attribute> attributes;

    private List<String> attrList;

    /**
     * Add an attribute to the schema item.
     * @param attr the attribute to add to the schema item.
     */
    public void addAttribute(Attribute attr) {
        if (attr.getBaseTable().equals(this)) {
            this.attributes.put(attr.name, attr);
        }
        else {
            log.warn("Cannot assign attribute (" + attr + ") to a different base table (" + this + ")");
        }
    }

    /**
     * Compares this schema item to the specified object. The result is true if and only if the schema
     * items have the same name.
     * @param o the object to compare this schema item against.
     * @return true if the schema items are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof SchemaItem) {
            SchemaItem other = (SchemaItem) o;

            return this.name.equals(other.name);
        }
        else {
            return false;
        }
    }

    /**
     * Get the name of the primary key for the schema item.  Will either
     * use the default schemaitemname_id or, if specified, take it from the
     * Prolog schema specification file.
     * @return the name of the primary key for the schema item.
     */
    protected String getPrimaryKeyName() {   
        String s = String.format("primaryKey(PKey, %s)", this.name);
        Hashtable solution = Query.oneSolution(s);
        String primaryKeyName;
        if (solution == null) {
            primaryKeyName = name.toLowerCase() + "_id";
        }
        else {
            primaryKeyName = ((Term) solution.get("PKey")).name();
        }
        return primaryKeyName;
    }

    /**
     * Returns a hash code for this schema item.  The hash code is equal to the hash code of the name.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Get the primary key attribute for this schema item.
     * @return the primary key attribute.
     */
    public Attribute getPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * Get the attribute of this schema item with the given name.
     * @param attrName the name of the attribute to return.
     * @return the attribute.
     */
    public Attribute getAttribute(String attrName) {
        return this.attributes.get(attrName);
    }

    /**
     * Get the set of all attributes for this schema item.
     * @return the set of all attributes for this schema item.
     */
    public Set<Attribute> getAllAttributes() {
        return new HashSet<Attribute>(this.attributes.values());
    }

    /**
     * Set the list of attribute names (in a given order) that corresponds to
     * the order of columns in the SQL create table statement
     * @param attrList the ordered list of attribute names.
     */
    public void setSQLAttrs(List<String> attrList) {
        this.attrList = attrList;
    }

    /**
     * Get the list of attribute names that corresponds to
     * the order of columns in the SQL create table statement
     * @return the ordered list of attribute names.
     */
    public List<String> getSQLAttrs() {
        return this.attrList;
    }

    /**
     * Returns a string representation consisting of the name of the schema item.
     * @return the string representation.
     */
    public String toString() {
        return this.name;
    }

}