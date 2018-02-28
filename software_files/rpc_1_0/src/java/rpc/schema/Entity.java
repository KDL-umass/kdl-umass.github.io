/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 2:10:15 PM
 */
package rpc.schema;

import java.util.HashMap;

/**
 * Entity schema item.
 */
public class Entity extends SchemaItem {

    /**
     * Construct an entity object and create its primary key.
     * @param name the name of the entity to create.
     */
    public Entity(String name) {
        this.name = name;
        this.attributes = new HashMap<String, Attribute>();

        this.primaryKey = new Attribute(this.getPrimaryKeyName(), this);
        this.primaryKey.setIsPrimaryKey(true);
        addAttribute(this.primaryKey);
    }

}
