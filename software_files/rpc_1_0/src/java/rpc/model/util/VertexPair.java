/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 23, 2009
 * Time: 11:32:42 AM
 */
package rpc.model.util;

/**
 * A wrapper object that holds two underlying Vertex objects with a given order.
 */
public class VertexPair {

    /**
     * Vertex 1.
     */
    public Vertex v1;
    /**
     * Vertex 2.
     */
    public Vertex v2;

    /**
     * Constructs a new VertexPair given two underlying Vertex objects.
     * @param v1 Vertex 1.
     * @param v2 Vertex 2.
     */
    public VertexPair(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    /**
     * Constructs a new VertexPair as a copy of the given VertexPair.
     * @param oldVP the VertexPair to copy.
     */
    public VertexPair(VertexPair oldVP) {
        this.v1 = oldVP.v1;
        this.v2 = oldVP.v2;
    }

    /**
     * Returns a hash code for this VertexPair.  The hash code is equal to the hash code of the string representaiton
     * of the VertexPair.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Compares this VertexPair to the specified object. The result is true if and only if the VertexPairs
     * consist of the same two underlying Vertex objects.
     * @param o the object to compare this VertexPair against.
     * @return true if the VertexPairs are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof VertexPair) {
            VertexPair other = (VertexPair) o;
            return this.v1.equals(other.v1) && this.v2.equals(other.v2);
        }
        return false;
    }

    /**
     * Construct a new VertexPair by reversing the positions of the two underlying Vertex objects.
     * @return a new VertexPair consisting of (v2, v1).
     */
    public VertexPair reverse() {
        return new VertexPair(this.v2, this.v1);
    }

    /**
     * Returns a string representation consisting of vertex1Name - vertex2Name.
     * @return the string representation.
     */
    public String toString() {
        return this.v1 + " - " + this.v2;
    }

}
