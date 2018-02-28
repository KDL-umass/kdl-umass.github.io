/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 */
package rpc.design;

import java.util.*;

/**
 * A design object consists of the unit to test for dependence and the set of design elements that will
 * control for various aspects of potential confounders.
 */
public class Design {

    public Unit unit;
    private List<DesignElement> designElements;
    private String stringRep = null;

    /**
     * Instantiate a design based on its underlying unit and model support data structure.
     * @param u
     */
    public Design(Unit u) {
        this.unit = u;
        this.designElements = new ArrayList<DesignElement>();
    }

    /**
     * Add a design element to the design.
     * @param de the design element to add.
     */
    public void addDesignElement(DesignElement de) {
        this.designElements.add(de);
    }

    /**
     * Get the list of all design elements being used in the design.
     * @return the list of all design elements used in the design.
     */
    public List<DesignElement> getDesignElements() {
        return this.designElements;
    }

    /**
     * Compares this design to the specified object. The result is true if and only if the
     * designs have the same underlying unit and design elements.
     * @param o the object to compare this design against.
     * @return true if the designs are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Design) {
            Design other = (Design) o;

            if (! this.unit.equals(other.unit)) {
                return false;
            }
            else {
                boolean isEqual = this.designElements.size() == other.designElements.size();

                if (! isEqual) {
                    return false;
                }

                //now check each design element
                for (int i=0; i<this.designElements.size(); i++) {
                    if (! this.designElements.get(i).equals(other.designElements.get(i))) {
                        return false;
                    }
                }

            }
        }
        return false;
    }

    private String getStringRep() {
        String ret = "UNIT:\n" + this.unit.toString() + "\n";
        ret += "DESIGN ELEMENTS:\n";
        for (DesignElement de : this.designElements) {
            ret += de.toString() + "\n";
        }
        return ret;
    }

    /**
     * Returns a string representation consisting of the underlying unit and the design elements
     * of the design.
     * @return the string representation.
     */
    public String toString(){
        if (this.stringRep == null) {
            this.stringRep = getStringRep();
        }
        return this.stringRep;
    }
	
}