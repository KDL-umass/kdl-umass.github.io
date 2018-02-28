/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 2:27:52 PM
 */
package rpc.model.util;

import rpc.schema.Attribute;

/**
 * A variable based on an attribute of a schema item.
 */
public class AttributeVariable extends Variable {

    private Attribute attr;

    /**
     * Constructs an AttributeVariable by storing the attribute and the attribute's source table.
     * @param attr the attribute.
     */
    public AttributeVariable(Attribute attr) {
        super(attr.getBaseTable());
        this.attr = attr;
        this.name = attr.name;
    }

    /**
     * Get the attribute associated with this AttributeVariable.
     * @return the attribute.
     */
    public Attribute getAttribute() {
        return this.attr;
    }
}
