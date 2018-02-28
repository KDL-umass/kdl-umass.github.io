/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 4, 2010
 * Time: 1:50:30 PM
 */
package rpc.model.util;

import rpc.design.Unit;
import rpc.util.CausalModelUtil;

/**
 * A Dependency object holds information about a dependency in the model, including which unit
 * supports the dependency and which Vertex objects the dependency connects.
 */
public class Dependency {

    /**
     * The unit that supports the dependency.
     */
    public Unit unit;
    /**
     * The parent Vertex.
     */
    public Vertex from;
    /**
     * The child Vertex.
     */
    public Vertex to;

    /**
     * Constructs a Dependency given a unit that supports it.
     * @param u the unit for which a dependence exists.
     */
    public Dependency(Unit u){
        this.unit = u;
        this.from = new Vertex(CausalModelUtil.variableToVertex(u.treatment));
        this.to = new Vertex(CausalModelUtil.variableToVertex(u.outcome));
    }

    /**
     * Constructs a Dependency in which there is no unit that supports it.
     * This type of Dependency is referred to as "trivial" since it connects
     * two vertices, but is not supported by an underlying unit.
     * @param from the parent vertex.
     * @param to the child vertex.
     */
    public Dependency(Vertex from, Vertex to) {
        this.unit = null;
        this.from = from;
        this.to = to;
    }

    /**
     * Checks if the Dependency is trivial.
     * @return true if the underlying unit is null (no unit supports the Dependency); false otherwise.
     */
    public boolean isTrivial() {
        return this.unit == null;
    }

    /**
     * Compares this Dependency to the specified object. The result is true if and only if the Dependency
     * objects have the same from and to Vertex objects and the same unit supporting them.
     * @param o the object to compare this Dependency against.
     * @return true if the Dependency objects are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Dependency) {
            Dependency other = (Dependency) o;
            return this.from.equals(other.from) && this.to.equals(other.to) &&
                    ((this.unit == null && other.unit == null) ||
                        ((this.unit != null && other.unit != null) && (this.unit.equals(other.unit))));
        }
        return false;
    }

    /**
     * Returns a hash code for this Dependency.  The hash code is equal to the hash code of the string representaiton
     * of the Dependency.
     * @return a hash code value for this object.
     */
    public int hashCode(){
        return this.toString().hashCode();
    }

    /**
     * Construct a new Dependency by reversing the from and to vertices and the underlying unit.
     * If the Dependency is trivial (has no supporting unit), then there is no reverse.
     * @return a new Dependency: to -> from.
     */
    public Dependency reverse() {
        if (this.unit == null) {
            return null;
        }
        else if (this.unit.treatmentPath == null) {

            Unit reverseUnit = new Unit();
            reverseUnit.treatment = this.unit.outcome;
            reverseUnit.outcome = this.unit.treatment;
            return new Dependency(reverseUnit);
        }
        else {
            Unit reverseUnit = this.unit.reverse();
            if (reverseUnit == null) {
                return null;
            }
            return new Dependency(reverseUnit);
        }
    }

    /**
     * Returns a string representation consisting of from --> to (unit).
     * @return the string representation.
     */
    public String toString() {
        return this.from + " --> " + this.to + " (" + this.unit + ")";
    }
}
