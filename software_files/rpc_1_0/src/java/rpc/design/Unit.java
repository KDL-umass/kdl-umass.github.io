/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 5, 2009
 * Time: 3:11:09 PM
 */
package rpc.design;

import rpc.schema.SchemaItem;
import rpc.schema.Schema;
import rpc.model.util.Variable;
import rpc.model.util.Vertex;
import rpc.model.util.StructureVariable;
import rpc.model.util.AttributeVariable;
import rpc.util.DesignUtil;

/**
 * The unit class consists of a pair of treatment and outcome paths with a common base item.
 * A unit corresponds to a potential dependency in the causal model.
 */
public class Unit {

    /**
     * The common base schema item of the unit.
     */
    public SchemaItem baseItem;

    /**
     * The treatment path of the unit.
     */
    public Path treatmentPath;
    /**
     * The outcome path of the unit.
     */
    public Path outcomePath;

    /**
     * The treatment variable of the unit.
     */
    public Variable treatment;
    /**
     * The outcome variable of the unit.
     */
    public Variable outcome;

    private String stringRep = null;
    private int hashCached = -1;

    /**
     * Construct a unit given its component base item and treatment and outcome paths.
     * @param baseItem the base schema item.
     * @param treatmentPath the treatment path.
     * @param outcomePath the outcome path.
     */
    public Unit(SchemaItem baseItem, Path treatmentPath, Path outcomePath) {
        this.baseItem = baseItem;
        this.treatmentPath = treatmentPath;
        this.outcomePath = outcomePath;

        this.treatment = this.treatmentPath.getVariable();
        this.outcome = this.outcomePath.getVariable();
    }

    /**
     * Constructs an empty unit with only treatment and outcome variables corresponding to
     * abstract treatment and outcome vertex.
     * @param t the treatment vertex.
     * @param o the outcome vertex.
     */
    public Unit(Vertex t, Vertex o) {
        this.baseItem = null;
        this.treatmentPath = null;
        this.outcomePath = null;

        if (t.isExistence()) {
            this.treatment = new StructureVariable(Schema.getRelationship(t.name));
        }
        else {
            this.treatment = new AttributeVariable(Schema.getSchemaItem(t.getBaseTable()).getAttribute(t.getAttribute()));
        }

        if (o.isExistence()) {
            this.outcome= new StructureVariable(Schema.getRelationship(o.name));
        }
        else {
            this.outcome = new AttributeVariable(Schema.getSchemaItem(o.getBaseTable()).getAttribute(o.getAttribute()));
        }
    }

    /**
     * Constructs a shell of a unit.  The base item, treatment and outcome paths, and treatment and outcome
     * variables are all set to null.
     */
    public Unit() {
        this.baseItem = null;
        this.treatmentPath = null;
        this.outcomePath = null;        
    }

    /**
     * Construct a new unit by reversing the treatment and outcome paths and standardizing
     * the result.  Will return null if the standardized reverse does not exist.
     * If the unit consists of null components, then there is no reverse.
     * @return a new Unit: outcome -> treatment.
     */
    public Unit reverse() {
        if (this.treatmentPath == null || this.outcomePath == null) {
            Unit reverseUnit = new Unit();
            reverseUnit.treatment = this.outcome;
            reverseUnit.outcome = this.treatment;
            return reverseUnit;
        }
        else {
            Unit reverseUnit = new Unit(this.baseItem, this.outcomePath, this.treatmentPath);
            try {
                reverseUnit = DesignUtil.standardize(reverseUnit);
            }
            catch (NullPointerException npe) {
                return null;
            }
            return reverseUnit;
        }
    }

    /**
     * Compares this unit to the specified object. The result is true if and only if the unit
     * objects have the same base items and treatment and outcome paths.
     * @param o the object to compare this unit against.
     * @return true if the units are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Unit) {
            Unit other = (Unit) o;            
            //everything must be the same!
            if (this.baseItem == null && other.baseItem == null && this.treatmentPath == null && other.treatmentPath == null
                    && this.outcomePath == null && other.outcomePath == null) {
                return this.treatment.equals(other.treatment) && this.outcome.equals(other.outcome);
            }
            else {
                return this.baseItem != null && other.baseItem != null && this.baseItem.equals(other.baseItem) &&
                    this.treatmentPath.equals(other.treatmentPath) &&
                    this.outcomePath.equals(other.outcomePath);
            }
        }
        else {
            return false;
        }
    }

    private int getHashCode() {
        return this.stringRep.hashCode();
    }

    /**
     * Returns a hash code for this unit.  The hash code is equal to the hash code of the string representaiton
     * of the unit.
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

    private String getStringRep() {
      String s = "(";
      s += "Base: " + this.baseItem + ", ";
      s += "Treatment Path <" + this.treatmentPath + ">, ";
      s += "Outcome Path <" + this.outcomePath + ">)";
      return s;
    }

    /**
     * Returns a string representation of the unit.
     * @return the string representation.
     */
    public String toString() {
        if (this.stringRep == null) {
            this.stringRep = getStringRep();
        }
        return this.stringRep;
    }

    /**
     * Returns a string representation of the unit used in visualizations as edge annotations
     * of the dependencies in the causal model.
     * @return the string representation.
     */
    public String visualize() {
        //e.g., [b, ab, a].x1 --> [b].y1
        if (this.treatmentPath == null || this.outcomePath == null) {
            return this.treatment.name() + " --> " + this.outcome.name();
        }
        else {
            return this.treatmentPath.getPath() + "." + this.treatment.name() + " --> " +
                    this.outcomePath.getPath() + "." + this.outcome.name();
        }
    }
}
