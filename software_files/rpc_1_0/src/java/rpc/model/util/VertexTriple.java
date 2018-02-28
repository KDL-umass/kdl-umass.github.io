/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 30, 2009
 * Time: 12:55:11 PM
 */
package rpc.model.util;

/**
 * A wrapper object that holds three underlying Vertex objects with a given order.
 */
public class VertexTriple {

    /**
     * Vertex 1.
     */
    public Vertex v1;
    /**
     * Vertex 2.
     */
    public Vertex v2;
    /**
     * Vertex 3.
     */
    public Vertex v3;

    /**
     * Constructs a new VertexTriple given three underlying Vertex objects.
     * @param v1 Vertex 1.
     * @param v2 Vertex 2.
     * @param v3 Vertex 3.
     */
    public VertexTriple(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    /**
     * Constructs a new VertexTriple as a copy of the given VertexTriple.
     * @param oldVT the VertexTriple to copy.
     */
    public VertexTriple(VertexTriple oldVT) {
        this.v1 = oldVT.v1;
        this.v2 = oldVT.v2;
        this.v3 = oldVT.v3;
    }

    /**
     * Returns a hash code for this VertexTriple.  The hash code is equal to the hash code of the string representaiton
     * of the VertexTriple.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Compares this VertexTriple to the specified object. The result is true if and only if the VertexTriples
     * consist of the same three underlying Vertex objects.
     * @param o the object to compare this VertexTriple against.
     * @return true if the VertexTriples are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof VertexTriple) {
            VertexTriple other = (VertexTriple) o;
            return this.v1.equals(other.v1) && this.v2.equals(other.v2)
                     && this.v3.equals(other.v3);
        }
        return false;
    }

    /**
     * Construct a new VertexTriple by reversing the positions of the three underlying Vertex objects.
     * @return a new VertexTriple consisting of (v3, v2, v1).
     */
    public VertexTriple reverse() {
        return new VertexTriple(this.v3, this.v2, this.v1);
    }

    /**
     * Returns a string representation consisting of vertex1Name - vertex2Name - vertex3Name.
     * @return the string representation.
     */
    public String toString() {
        return this.v1 + " - " + this.v2 + " - " + this.v3;
    }

}
