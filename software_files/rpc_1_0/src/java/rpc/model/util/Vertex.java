/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 29, 2009
 * Time: 2:18:51 PM
 */
package rpc.model.util;

import rpc.schema.Schema;

/**
 * A vertex is an abstract representation of all versions of a particular variable (attribute or structure).
 * Because relational data can generate multiple aggregates and paths over instances of variables, the
 * vertex is used as a higher-level representation of a given variable (useful for edge orientation).
 */
public class Vertex {
    /**
     * The name of the vertex.
     */
    public String name;

    /**
     * Constructs a vertex corresponding to a given vertex name.
     * @param name the name of the vertex.
     */
    public Vertex(String name) {
        this.name = name;
    }

    /**
     * Constructs a new vertex as a copy of the given vertex.
     * @param oldVertex the vertex to copy.
     */
    public Vertex(Vertex oldVertex) {
        this.name = oldVertex.name;
    }

    /**
     * Check if the vertex represents an attribute variable.
     * @return true if the vertex represents an attribute variable; false otherwise.
     */
    public boolean isAttribute() {
        return !Schema.isRelationship(this.name);
    }

    /**
     * Check if the vertex represents a structural variable.
     * @return true if the vertex represents a structural variable; false otherwise.
     */
    public boolean isStructure() {
        return Schema.isRelationship(this.name);
    }

    /**
     * Check if the vertex represents an existence variable. Note that this is
     * identical to testing if it represents a structural variable, but it is clearer
     * in certain contexts.
     * @return true if the vertex represents an existence variable; false otherwise.
     */
    public boolean isExistence() {
       return Schema.isRelationship(this.name);
    }

    /**
     * Get the name of the source table for the variable that the vertex represents.
     * @return the name of the base table.
     */
    public String getBaseTable() {
        if (isAttribute()) {
            return this.name.split("\\.")[0];
        }
        else {
            return this.name;
        }
    }

    /**
     * Get the name of the attribute that the vertex represents.
     * @return the name of the attribute if the vertex represents an attribute variable
     * or the name of the base table if it represents a structural variable.
     */
    public String getAttribute() {
        if (isAttribute()) {
            return this.name.split("\\.")[1];
        }
        else {
            return this.name;
        }
    }

    /**
     * Returns a hash code for this vertex.  The hash code is equal to the hash code of the string representaiton
     * of the vertex.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Compares this vertex to the specified object. The result is true if and only if the vertices
     * have the same name.
     * @param o the object to compare this vertex against.
     * @return true if the vertices are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Vertex) {
            Vertex other = (Vertex) o;
            return this.name.equals(other.name);
        }
        return false;
    }

    /**
     * Returns a string representation consisting of the name of the vertex.
     * @return the string representation.
     */
    public String toString() {
        return this.name;
    }
}