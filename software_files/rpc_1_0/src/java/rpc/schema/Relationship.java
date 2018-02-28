/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 2:10:55 PM
 */
package rpc.schema;

import rpc.design.Cardinality;

import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import jpl.Query;
import jpl.Term;

/**
 * Relationship schema item. A relationship connects two entities, each with a foreign key.
 */
public class Relationship extends SchemaItem {

    private static Logger log = Logger.getLogger(Relationship.class);

    /**
     * Enity 1.
     */
    public Entity entity1;
    /**
     * Entity 2.
     */
    public Entity entity2;

    /**
     * Cardinality associated with entity 1.
     */
    public Cardinality entity1Card;
    /**
     * Cardinality associated with entity 2.
     */
    public Cardinality entity2Card;

    private Attribute foreignKey1;
    private Attribute foreignKey2;

    /**
     * Construct a relationship object, its primary key, and its foreign keys.
     * @param name the name of the relationship to create.
     * @param entity1 the first entity involved in the relationship.
     * @param entity2 the second entity involved in the relationship.
     */
    public Relationship(String name, Entity entity1, Entity entity2) {
        this.name = name;
        this.entity1 = entity1;
        this.entity2 = entity2;

        this.attributes = new HashMap<String, Attribute>();

        this.primaryKey = new Attribute(this.getPrimaryKeyName(), this);        
        this.primaryKey.setIsPrimaryKey(true);
        addAttribute(this.primaryKey);

        this.foreignKey1 = new Attribute(this.getForeignKeyName(this.entity1), this);
        this.foreignKey1.setIsForeignKey(true);
        this.foreignKey1.setRefTable(this.entity1);
        addAttribute(this.foreignKey1);

        this.foreignKey2 = new Attribute(this.getForeignKeyName(this.entity2), this);
        this.foreignKey2.setIsForeignKey(true);
        this.foreignKey2.setRefTable(this.entity2);
        addAttribute(this.foreignKey2);
    }

    private String getForeignKeyName(Entity e) {
        String s = String.format("foreignKey(FKey, %s, %s)", this.name, e.name);
        Hashtable solution = Query.oneSolution(s);
        String foreignKeyName;
        if (solution == null) {
            foreignKeyName = e.primaryKey.name;
        }
        else {
            foreignKeyName = ((Term) solution.get("FKey")).name();
        }
        return foreignKeyName;
    }

    /**
     * Compares this relationship to the specified object. The result is true if and only if the
     * relationships have the same name and connect the same two entities.
     * @param o the object to compare this relationship against.
     * @return true if the relationships are equal; false otherwise.
     */
    public boolean equals(Object o) {
        if (o instanceof Relationship) {
            Relationship other = (Relationship) o;

            return this.name.equals(other.name)
                    && this.entity1.equals(other.entity1)
                    && this.entity2.equals(other.entity2);
        }
        else {
            return false;
        }
    }

    /**
     * Get the two foreign keys associated with this relationship.
     * @return a two-element array of attributes that hold the two foreign keys.
     */
    public Attribute[] getForeignKeys() {
        Attribute[] foreignKeys = new Attribute[2];
        foreignKeys[0] = this.foreignKey1;
        foreignKeys[1] = this.foreignKey2;
        return foreignKeys;
    }

    /**
     * Assign the cardinality of the relationship associated with a specific entity.
     * @param entity the entity.
     * @param card the cardinality.
     */
    public void assignCardinality(Entity entity, Cardinality card) {
        if (entity.equals(entity1)) {
            this.entity1Card = card;
        }
        else if (entity.equals(entity2)) {
            this.entity2Card = card;
        }
        else {
            log.error("Attempted to assign cardinality for non-existent entity: " + entity);
        }
    }

}
