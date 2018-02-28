/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 2:09:14 PM
 */
package rpc.schema;

import jpl.Query;
import jpl.Term;
import jpl.Atom;

import java.util.*;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import rpc.design.Cardinality;
import org.apache.log4j.Logger;

/**
 * Schema object holds information about the structure of the database (the entities, relationships, and attributes).
 */
public class Schema {

    private static Logger log = Logger.getLogger(Schema.class);

    private static HashMap<String, Entity> entities = new HashMap<String, Entity>();
    private static HashMap<String, Relationship> relationships = new HashMap<String, Relationship>();

    /**
     * Private constructor ensures no one creates Schema objects.  The Schema class is used as a collection
     * of static methods to get entity and relationship items.
     */
    private Schema() {
    }

    /**
     * Get the entity object with the given name.
     * @param name the name of the entity.
     * @return the entity object with the given name.
     */
    public static Entity getEntity(String name) {
        if (!isEntity(name)) {
            log.warn("Not an entity: " + name);
        }
        return entities.get(name);
    }

    /**
     * Get the relationship object with the given name.
     * @param name the name of the relationship.
     * @return the relationship object with the given name.
     */
    public static Relationship getRelationship(String name) {
        if (!isRelationship(name)) {
            log.warn("Not a relationship: " + name);
        }
        return relationships.get(name);
    }

    /**
     * Get the schema item (entity or relationship) object with the given name.
     * @param name the name of the schema item.
     * @return the schema item object with the given name.
     */
    public static SchemaItem getSchemaItem(String name) {
        if (isEntity(name)) {
            return getEntity(name);
        }
        else if (isRelationship(name)) {
            return getRelationship(name);
        }
        else {
            log.warn("Not a schema item: " + name);
            return null;
        }
    }

    /**
     * Get the set of all entity objects in the schema.
     * @return the set of all entity objects in the schema.
     */
    public static Set<Entity> getAllEntities() {
        return new HashSet<Entity>(entities.values());
    }

    /**
     * Get the set of all relationship objects in the schema.
     * @return the set of all relationship objects in the schema.
     */
    public static Set<Relationship> getAllRelationships() {
        return new HashSet<Relationship>(relationships.values());
    }

    /**
     * Get the set of all schema item objects in the schema.
     * @return a list of all schema item objects in the schema.
     */
    public static List<SchemaItem> getAllSchemaItems() {
        List<SchemaItem> allItems = new ArrayList<SchemaItem>();
        allItems.addAll(entities.values());
        allItems.addAll(relationships.values());
        return allItems;
    }

    /**
     * Cheecks if the given name corresponds to an entity in the schema.
     * @param name possible name of an entity.
     * @return true if the name does correspond to an entity; false otherwise.
     */
    public static boolean isEntity(String name) {
        return entities.containsKey(name);
    }

    /**
     * Cheecks if the given name corresponds to a relationship in the schema.
     * @param name possible name of an relationship.
     * @return true if the name does correspond to an relationship; false otherwise.
     */
    public static boolean isRelationship(String name) {
        return relationships.containsKey(name);
    }

    /**
     * Loads the schema and populates the set of entities and relationships given a schema
     * file speciication written in Prolog.
     * @param schemaFile the Prolog schema file specification.
     */
    public static void loadSchema(String schemaFile) {
        String s = "consult(\'" + schemaFile + "\')";
        Query.oneSolution(s);

        //create schema object, fill in entities, relationships, attributes
        loadEntities();
        loadRelationships();
        loadAttributes();
    }

    private static void loadEntities() {
        String s = "entity(Ent)";

        for (Hashtable solution : Query.allSolutions(s)) {
            Term ent = (Atom) solution.get("Ent");
            addEntity(new Entity(ent.name()));
        }
    }

    private static void addEntity(Entity e) {
        entities.put(e.name, e);
    }

    private static void loadRelationships() {
        String s = "relationship(Rel)";

        for (Hashtable solution : Query.allSolutions(s)) {
            Term rel = (Atom) solution.get("Rel");

            String s2 = String.format("cardinality(%s, Ent, Card)", rel);
            Entity[] entities = new Entity[2];
            Hashtable[] subSolutions = Query.allSolutions(s2);
            entities[0] = new Entity(((Term) subSolutions[0].get("Ent")).name());
            Cardinality card1 = Cardinality.valueOf(((Term) subSolutions[0].get("Card")).name().toUpperCase());
            entities[1] = new Entity(((Term) subSolutions[1].get("Ent")).name());
            Cardinality card2 = Cardinality.valueOf(((Term) subSolutions[1].get("Card")).name().toUpperCase());

            Relationship relationship = new Relationship(rel.toString(), entities[0], entities[1]);
            relationship.assignCardinality(entities[0], card1);
            relationship.assignCardinality(entities[1], card2);
            addRelationship(relationship);
        }
    }

    private static void addRelationship(Relationship r) {
        relationships.put(r.name, r);
    }

    private static void loadAttributes() {

        String s = "attr(Attr, Base)";

        for (Hashtable solution : Query.allSolutions(s)) {
            Term attr = (Atom) solution.get("Attr");
            Term base = (Atom) solution.get("Base");

            if (isEntity(base.toString())) {
                getEntity(base.toString()).addAttribute(new Attribute(attr.name(), getEntity(base.name())));
            }
            else {
                getRelationship(base.name()).addAttribute(new Attribute(attr.name(), getRelationship(base.name())));
            }
        }
    }

    /**
     * Get a list of SQL statements that will instantiate all entity and relationship tables with the
     * appropriate attributes in a relational database.
     * @return the list of SQL statements to create a relational database.
     */
    public static List<String> getSQL() {

        List<String> sqlStmts = new ArrayList<String>();
        
        // entity tables with static attributes and lifespan
        for (Entity e : entities.values()) {

            String sql = "CREATE TABLE " + e.name + " (";

            // need to also store the order the attributes are created for later reference
            List<String> attrList = new ArrayList<String>();

            // enforce that the primary key is first
            Attribute primaryKey = e.getPrimaryKey();
            sql += primaryKey.name + " int PRIMARY KEY";
            attrList.add(primaryKey.name);

            for (Attribute attr : e.getAllAttributes()) {
                if (!attr.isPrimaryKey()) {
                    sql += ", " + attr.name + " int";
                    attrList.add(attr.name);
                }
            }

            sql += ")";
            
            sqlStmts.add(sql);

            // ensure this entity knows the order of its attributes
            e.setSQLAttrs(attrList);
        }

        // relationship tables with static attributes and lifespan
        for (Relationship r : relationships.values()) {

            String sql = "CREATE TABLE " + r.name + " (";

            // need to also store the order the attributes are created for later reference
            List<String> attrList = new ArrayList<String>();

            // enforce the primary key and foreign keys are first
            Attribute primaryKey = r.getPrimaryKey();
            sql += primaryKey.name + " int PRIMARY KEY, ";
            Attribute[] foreignKeys = r.getForeignKeys();
            sql += foreignKeys[0].name + " int, ";
            sql += foreignKeys[1].name + " int";

            attrList.add(primaryKey.name);
            attrList.add(foreignKeys[0].name);
            attrList.add(foreignKeys[1].name);

            for (Attribute attr : r.getAllAttributes()) {
                if (!attr.isPrimaryKey() && !attr.isForeignKey()) {
                    sql += ", " + attr.name + " int";
                    attrList.add(attr.name);
                }
            }

            for (Attribute attr : r.getAllAttributes()) {
                if (attr.isForeignKey()) {
                    sql += ", FOREIGN KEY (" + attr.name + ") REFERENCES " + attr.getRefTable().name;
                }
            }
            sql += ")";

            r.setSQLAttrs(attrList);
            sqlStmts.add(sql);
        }

        for (SchemaItem si : getAllSchemaItems()) {
            if (isEntity(si.name)) {
                sqlStmts.add(String.format("CREATE INDEX %s_%s on %s USING HASH(%s)",
                    si.name, si.getPrimaryKey().name, si.name, si.getPrimaryKey().name));
            }
            else {
                sqlStmts.add(String.format("CREATE INDEX %s_%s on %s USING HASH(%s)",
                        si.name, si.getPrimaryKey().name, si.name, si.getPrimaryKey().name));

                Attribute[] foreignKeys = ((Relationship)si).getForeignKeys();
                sqlStmts.add(String.format("CREATE INDEX %s_%s on %s USING HASH(%s)",
                        si.name, foreignKeys[0].name, si.name, foreignKeys[0].name));
                sqlStmts.add(String.format("CREATE INDEX %s_%s on %s USING HASH(%s)",
                        si.name, foreignKeys[1].name, si.name, foreignKeys[1].name));
            }
        }

        return sqlStmts;

    }

    /**
     * Get a string representation of the schema.
     * NB: cannot override the toString() object method from a static context.
     * @return a string that denotes all the entities, relationships, and their attributes that exist
     * in the schema.
     */
    public static String stringRep() {
        String ret = "";
        ret += "Entities: ";

        for (Entity entity : entities.values()) {
            ret += "\n\t" + entity;
        }

        ret += "\nRelationships: ";

        for (Relationship rel : relationships.values()) {
            ret += "\n\t" + rel;
        }

        ret += "\nEntity Attributes:";
        for (Entity e : entities.values()) {
            ret += "\n\t" + e + " ->";
            for (Attribute attr : e.getAllAttributes()) {
                ret += " " + attr.name;
            }
        }

        ret += "\nRelationship Attributes:";
        for (Relationship r : relationships.values()) {
            ret += "\n\t" + r.name + " ->";
            for (Attribute attr : r.getAllAttributes()) {
                ret += " " + attr.name;
            }
        }

        return ret;
    }

    /**
     * Gets the dot file that can be used to visualize the schema.
     * @param filename the name of the file to write out the dot.
     */
    public static void getDotFile(String filename) {
        try {
            File file = new File(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("digraph G {\n");
            bw.write("\trankdir=TB;\n");
            bw.write("\n");
            for (String line : SchemaVisualizer.getDot()) {
                bw.write("\t" + line + "\n");
            }
            bw.write("}");
            bw.close();
        }
        catch (IOException e) {
            log.error("Failed writing dot file, " + e);
        }
    }

}