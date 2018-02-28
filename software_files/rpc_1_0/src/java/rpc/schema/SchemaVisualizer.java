/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 25, 2010
 * Time: 2:05:18 PM
 */
package rpc.schema;

import java.util.*;

import rpc.design.Cardinality;

/**
 * This class translates the schema into the dot visualization language.
 */
public class SchemaVisualizer {

/*  EXAMPLE FOR test-schema.pl:

	//Entity, Relationship, and Attribute defintions
	entity_a [shape=box, style=bold, label="a"];
	x1;
	x2;

	relationship_ab [shape=diamond, style=bold, label="ab"];
	xy1;
	xy2;
	ab_exists [shape=point];

	entity_b [shape=box, style=bold, label="b"];
	y1;

	relationship_bc [shape=diamond, style=bold, label="bc"];
	yz1;
	bc_exists [shape=point];

	entity_c [shape=box, style=bold, label="c"];
	z1;
	z2;
	z3;

	relationship_da [shape=diamond, style=bold, label="da"];
	wx1;
	da_exists [shape=point];

	entity_d [shape=box, style=bold, label="d"];
	w1;

	//Rank entity and relationship groups the same, as well as attributes on same item
	{rank=same; entity_a; relationship_ab; entity_b};
	{rank=same; entity_b; relationship_bc; entity_c};
	{rank=same; entity_d; relationship_da; entity_a};
	{rank=same; x1; x2};
	{rank=same; xy1; xy2; ab_exists};
	{rank=same; y1};
	{rank=same; yz1; bc_exists};
	{rank=same; z1; z2; z3};
	{rank=same; wx1; da_exists};
	{rank=same; w1};

	//Connect attributes to their items (try to keep them as straight edges)
	entity_a -> x1 [style=dashed, dir=none, weight=8];
	entity_a -> x2 [style=dashed, dir=none, weight=8];
	relationship_ab -> xy1 [style=dashed, dir=none, weight=8];
	relationship_ab -> xy2 [style=dashed, dir=none, weight=8];
	relationship_ab -> ab_exists [style=dashed, dir=none, weight=8];
	entity_b -> y1 [style=dashed, dir=none, weight=8];
	relationship_bc -> yz1 [style=dashed, dir=none, weight=8];
	relationship_bc -> bc_exists [style=dashed, dir=none, weight=8];
	entity_c -> z1 [style=dashed, dir=none, weight=8];
	entity_c -> z2 [style=dashed, dir=none, weight=8];
	entity_c -> z3 [style=dashed, dir=none, weight=8];
	entity_c -> z4 [style=dashed, dir=none, weight=8];
	relationship_da -> wx1 [style=dashed, dir=none, weight=8];
	relationship_da -> da_exists [style=dashed, dir=none, weight=8];
	entity_d -> w1 [style=dashed, dir=none, weight=8];

	//Connect entities and relationships with cardinalities
	entity_a -> relationship_ab [style=dashed, dir=none, minlen=2];
	relationship_ab -> entity_b [style=dashed, dir=forward, arrowhead=crow, minlen=2];
	entity_b -> relationship_bc [style=dashed, dir=back, arrowtail=crow, minlen=2];
	relationship_bc -> entity_c [style=dashed, dir=forward, arrowhead=crow, minlen=2];
	entity_d -> relationship_da [style=dashed, dir=none, minlen=2];
	relationship_da -> entity_a [style=dashed, dir=none, minlen=2];
*/

    /**
     * Get the list of lines of the translation of the schema into the dot language.
     * @return a list of lines in the dot language.
     */
    public static List<String> getDot() {
        List<String> dotLines = new ArrayList<String>();
        dotLines.add("//Entity, Relationship, and Attribute defintions");
        for (SchemaItem si : Schema.getAllSchemaItems()) {
            dotLines.addAll(getItemNode(si));
            dotLines.addAll(getAttributeNodes(si));
            dotLines.add("\n");
        }

        dotLines.add("//Rank entity and relationship groups the same, as well as attributes on same item");
        dotLines.addAll(getRanksRelationships());
        dotLines.addAll(getRanksAttributes());
        dotLines.add("\n");

        dotLines.add("//Connect attributes to their items (try to keep them as straight edges)");
        dotLines.addAll(getItemToAttrConnections());
        dotLines.add("\n");

        dotLines.add("//Connect entities and relationships with cardinalities");
        dotLines.addAll(getEntityToRelationshipConnections());

        return dotLines;
    }

    private static List<String> getItemNode(SchemaItem si) {
        List<String> itemDotLines = new ArrayList<String>();
        if (Schema.isEntity(si.name)) {
            itemDotLines.add("entity_" + si.name + " [shape=box, style=bold, label=\"" + si.name + "\"];");

        }
        else {
            itemDotLines.add("relationship_" + si.name + " [shape=diamond, style=bold, label=\"" + si.name + "\"];");
        }
        return itemDotLines;
    }

    private static List<String> getAttributeNodes(SchemaItem si) {
        List<String> attrDotLines = new ArrayList<String>();
        for (Attribute attr : si.getAllAttributes()) {
            if (!attr.isPrimaryKey() && !attr.isForeignKey()) {
                attrDotLines.add(attr.name + ";");
            }
        }
        if (Schema.isRelationship(si.name)) {
            attrDotLines.add(si.name + "_exists [shape=point];");
        }
        return attrDotLines;
    }

    private static List<String> getRanksRelationships() {
        List<String> relRankLines = new ArrayList<String>();
        for (Relationship rel : Schema.getAllRelationships()) {
                relRankLines.add("{rank=same; entity_" + rel.entity1.name + "; relationship_" + rel.name + "; entity_" + rel.entity2.name + "};");
        }
        return relRankLines;
    }

    private static List<String> getRanksAttributes() {
        List<String> attrRankLines = new ArrayList<String>();
        for (SchemaItem si : Schema.getAllSchemaItems()) {
            boolean hasAttr = false;
            String attrLine = "{rank=same";
            List<Attribute> attrs = new ArrayList<Attribute>(si.getAllAttributes());
            Collections.sort(attrs);
            for (Attribute attr : attrs) {
                if (!attr.isPrimaryKey() && !attr.isForeignKey()) {
                    hasAttr = true;
                    attrLine += "; " + attr.name;
                }
            }
            if (Schema.isRelationship(si.name)) {
                attrLine += "; " + si.name + "_exists";
                hasAttr = true;
            }
            if (hasAttr) {
                attrLine += "};";
                attrRankLines.add(attrLine);
            }
        }
        return attrRankLines;
    }

    private static List<String> getItemToAttrConnections() {
        List<String> itemToAttrLines = new ArrayList<String>();
        for (SchemaItem si : Schema.getAllSchemaItems()) {
            String prefix = Schema.isEntity(si.name) ? "entity_" : "relationship_";
            for (Attribute attr : si.getAllAttributes()) {
                if (!attr.isPrimaryKey() && !attr.isForeignKey()) {
                    itemToAttrLines.add(prefix + si.name + " -> " + attr.name + " [style=dashed, dir=none, weight=8];");
                }
            }
            if (Schema.isRelationship(si.name)) {
                itemToAttrLines.add(prefix + si.name + " -> " + si.name + "_exists [style=dashed, dir=none, weight=8];");
            }
        }
        return itemToAttrLines;
    }

    private static List<String> getEntityToRelationshipConnections() {
        List<String> entRelLines = new ArrayList<String>();
        //entity_a -> relationship_ab [style=dashed, dir=none, minlen=2];
        for (Relationship rel : Schema.getAllRelationships()) {
                String conn1 = "entity_" + rel.entity1.name + " -> " + "relationship_" + rel.name + " [style=dashed, ";
                conn1 += rel.entity1Card.equals(Cardinality.ONE)
                        ? "dir=none, minlen=2];" : "dir=back, arrowtail=crow, minlen=2];";
                entRelLines.add(conn1);

                String conn2 = "relationship_" + rel.name + " -> " + "entity_" + rel.entity2.name + " [style=dashed, ";
                conn2 += rel.entity2Card.equals(Cardinality.ONE)
                        ? "dir=none, minlen=2];" : "dir=forward, arrowhead=crow, minlen=2];";
                entRelLines.add(conn2);
        }

        return entRelLines;
    }
}
