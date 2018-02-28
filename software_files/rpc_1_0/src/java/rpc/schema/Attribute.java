/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 3:12:06 PM
 */
package rpc.schema;

import jpl.Query;

/**
 * Attribute class holds information about an attribute of a schema item.
 */
public class Attribute implements Comparable<Attribute> {

    /**
     * The name of the attribute.
     */
    public String name;

    private SchemaItem baseTable;
    private SchemaItem refTable;

    private boolean isPrimaryKey;
    private boolean isForeignKey;

    private boolean isCategorical;

    /**
     * Construct an attribute object by setting its name and its source schema item.
     * @param name the name of the attribute.
     * @param baseItem the source schema item.
     */
    public Attribute(String name, SchemaItem baseItem) {
        this.name = name;
        this.baseTable = baseItem;

        this.isCategorical = this.getIsCategorical();
    }

    private boolean getIsCategorical() {
        String s = String.format("categorical(%s)", this.name);
        return Query.hasSolution(s);
    }

    /**
     * Checks whether or not the attribute is categorical and should not be discretized.
     * @return true if set to categorical; false otherwise.
     */
    public boolean isCategorical() {
        return this.isCategorical;        
    }

    /**
     * Compares this attribute with another attribute. The two are the same
     * if and only if they have the same name.
     * @param other the other attribute to compare against.
     * @return the comparison between the two attributes' names.
     */
    public int compareTo(Attribute other) {
        return this.name.compareTo(other.name);
    }

    /**
     * Compares this attribute to the specified object. The result is true if and only if the
     * attributes have the same name and the same base schema item.
     * @param o the object to compare this attribute against.
     * @return true if the attributes are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Attribute) {
            Attribute other = (Attribute) o;

            //two attributes are equal if they have the same base table and same name
            SchemaItem otherItem = other.getBaseTable();
            return this.baseTable.equals(otherItem) && this.name.equals(other.name);
        }
        else {
            return false;
        }
    }

    /**
     * Returns a hash code for this attribute.  The hash code is equal to the hash code of
     * its string representation.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Get the base schema item for the attribute.
     * @return the base schema item.
     */
    public SchemaItem getBaseTable() {
        return this.baseTable;
    }

    /**
     * Get the schema item the attribute refers to (set if the attribute is a foreign key).
     * @return the referenced schema item.
     */
    public SchemaItem getRefTable() {
        return this.refTable;
    }

    /**
     * Checks whether or not the attribute is a foreign key.
     * @return true if the attribute is a foreign key; false otherwise.
     */
    public boolean isForeignKey() {
        return this.isForeignKey;
    }

    /**
     * Checks whether or not the attribute is a primary key.
     * @return true if the attribute is a primary key; false otherwise.
     */
    public boolean isPrimaryKey() {
        return this.isPrimaryKey;
    }

    /**
     * Set whether or not the attribute is a foreign key.
     * @param isForeignKey flag setting whether or not the attribute is a foreign key.
     */
    public void setIsForeignKey(boolean isForeignKey) {
        this.isForeignKey = isForeignKey;
    }

    /**
     * Set whether or not the attribute is a primary key.
     * @param isPrimaryKey flag setting whether or not the attribute is a primary key.
     */
    public void setIsPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }
    
    /**
     * Set the reference table (if attribute is foreign key).
     * @param item the referenced schema item.
     */
    public void setRefTable(SchemaItem item) {
        this.refTable = item;
    }

    /**
     * Returns a string representation consisting of the name of the base schema item and the
     * name of the attribute.
     * @return the string representation.
     */
    public String toString() {
        return this.baseTable.name + "." + this.name;
    }
}