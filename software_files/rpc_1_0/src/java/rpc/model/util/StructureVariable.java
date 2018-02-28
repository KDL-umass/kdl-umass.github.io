/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 11, 2009
 * Time: 3:44:40 PM
 */
package rpc.model.util;

import rpc.schema.Relationship;

/**
 * A variable based on the structure of the schema.  A StructureVariable consists of
 * a relationship in the schema and can be used for existence and cardinality variables.
 */
public class StructureVariable extends Variable {

    private Relationship relationship;

    /**
     * Constructs a StructureVariable from a relationship in the schema.
     * @param rel the relationship.
     */
    public StructureVariable(Relationship rel) {
        super(rel);
        this.relationship = rel;
        this.name = rel.getPrimaryKey().name;
    }

    /**
     * Get the relationship associated with this StructureVariable.
     * @return the relationship.
     */
    public Relationship getRelationship() {
        return this.relationship;
    }
}

