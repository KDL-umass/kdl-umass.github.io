/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Sep 23, 2009
 * Time: 2:25:39 PM
 */
package rpc.querygen;

import rpc.schema.Attribute;
import rpc.schema.Schema;
import rpc.design.Unit;
import rpc.util.UnitUtil;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * SourceTranslator provides a SQL query for a given unit with replication and no aggregates on
 * the treatment and outcome paths.
 */
public class SourceTranslator {

    private static Logger log = Logger.getLogger(SourceTranslator.class);

    private List<String> pathNames;
    private List<String> mappedPathNames;

    private String target1;
    private String mappedTarget1;
    private String target2;
    private String mappedTarget2;

    private String var1;
    private String var2;

    /**
     * Initializes a SourceTranslator object and constructs mappings for each item on
     * both treatment and outcome paths for the cases in which items appear multiple times.
     * @param unit the unit to translate.
     */
    public SourceTranslator(Unit unit) {

        this.target1 = unit.treatmentPath.getTarget().name;
        this.var1 = unit.treatmentPath.getVariable().name();
        this.target2 = unit.outcomePath.getTarget().name;
        this.var2 = unit.outcomePath.getVariable().name();

        this.pathNames = new ArrayList<String>();
        List<String> pathNames1 = unit.treatmentPath.getPathNames();
        List<String> pathNames2 = unit.outcomePath.getPathNames();

        for (int i=pathNames1.size()-1; i>=0; i--) {
            //go through first path backwards
            this.pathNames.add(pathNames1.get(i));
        }
        for (int i=1; i<pathNames2.size(); i++) {
            this.pathNames.add(pathNames2.get(i));
        }

        this.mappedPathNames = UnitUtil.mapItemNames(unit);
        //target1 is the first item on the list
        this.mappedTarget1 = this.mappedPathNames.get(0);
        //target 2 is the last item on the list
        this.mappedTarget2 = this.mappedPathNames.get(this.mappedPathNames.size()-1);
    }
    
    /**
     * Builds the SQL query for the unit by combining the queries for the
     * treatment and outcome paths in the unit.
     * @return the SQL query.
     */
    public String getQuery() {
        StringBuffer sb = new StringBuffer();
        sb.append(getSelect());
        sb.append(getFrom());
        sb.append(getWhere());
        return sb.toString();
    }

    private String getSelect() {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");

        //get all item ids along path
        for (int i=0; i<this.pathNames.size(); i++) {
            String curItem = this.pathNames.get(i);
            String curItemMapped = this.mappedPathNames.get(i);
            if (! curItem.equals(curItemMapped)) {
                sb.append(curItemMapped);
            }
            else {
                sb.append(curItem);
            }
            sb.append(".");
            sb.append(Schema.getSchemaItem(curItem).getPrimaryKey().name);
            if (! curItem.equals(curItemMapped)) {
                sb.append(" AS ");
                sb.append(Schema.getSchemaItem(curItem).getPrimaryKey().name);
                sb.append(curItemMapped.substring(curItemMapped.length()-1,curItemMapped.length()));
            }
            sb.append(", ");
        }

        //get first variable
        if (! this.target1.equals(this.mappedTarget1)) {
            sb.append(this.mappedTarget1);
        }
        else {
            sb.append(this.target1);
        }
        sb.append(".");
        sb.append(this.var1);
        sb.append(" AS v1, ");

        //get second variable
        if (! this.target2.equals(this.mappedTarget2)) {
            sb.append(this.mappedTarget2);
        }
        else {
            sb.append(this.target2);
        }
        sb.append(".");
        sb.append(this.var2);
        sb.append(" AS v2");

        return sb.toString();
    }

	private String getFrom() {
        String query = "\nFROM ";

        //iterate through pathNames and check if items need to have aliases
        for (int i=0; i<this.pathNames.size(); i++) {
            if (this.pathNames.get(i).equals(this.mappedPathNames.get(i))) {
                query += String.format("%s, ", this.pathNames.get(i));
            }
            else {
                query += String.format("%s %s, ", this.pathNames.get(i), this.mappedPathNames.get(i));
            }
        }

        //remove trailing whitespace and comma
        query = query.substring(0, query.length()-2);

        return query;
    }

	private String getWhere() {
		String query = "";
        boolean where_clause = false;
        if (this.pathNames.size() > 1) {
            where_clause = true;
            query += "\nWHERE ";
        }

        //First add in all equality conditions to follow pathNames
		for (int i=0; i < this.pathNames.size()-1; i++) {
            String item1 = this.pathNames.get(i);
            String item2 = this.pathNames.get(i+1);
            String mappedItem1 = this.mappedPathNames.get(i);
            String mappedItem2 = this.mappedPathNames.get(i+1);

            if (Schema.isEntity(item1) && Schema.isRelationship(item2)) {
                query += String.format("%s.%s = ", mappedItem1, Schema.getEntity(item1).getPrimaryKey().name);

				Attribute[] foreignKeys = Schema.getRelationship(item2).getForeignKeys();
				if ( foreignKeys[0].getRefTable().name.equals(item1) ) {
					query += String.format("%s.%s AND ", mappedItem2, foreignKeys[0].name);
                }
                else if (foreignKeys[1].getRefTable().name.equals(item1) ) {
                    query += String.format("%s.%s AND ", mappedItem2, foreignKeys[1].name);
                }
                else {
                    log.warn("Error: Path information incorrect, cannot match entity with relationship foreign key");
                }
            }
            else if (Schema.isEntity(item2) && Schema.isRelationship(item1)) {
				Attribute[] foreignKeys = Schema.getRelationship(item1).getForeignKeys();

                if ( foreignKeys[0].getRefTable().name.equals(item2) ) {
					query += String.format("%s.%s = ", mappedItem1, foreignKeys[0].name);
                    query += String.format("%s.%s AND ", mappedItem2, Schema.getEntity(item2).getPrimaryKey().name);
                }
                else if (foreignKeys[1].getRefTable().name.equals(item2) ) {
                    query += String.format("%s.%s = ", mappedItem1, foreignKeys[1].name);
                    query += String.format("%s.%s AND ", mappedItem2, Schema.getEntity(item2).getPrimaryKey().name);
                }
                else {
                    log.warn("Error: Path information incorrect, cannot match entity with relationship foreign key");
                }
            }
            else {
                log.warn("Error: Path information incorrect, two consecutive entities or relationships");
            }
        }

        if (where_clause) { //remove trailing " AND "
            query = query.substring(0, (query.length()-5));
        }

        //for each item name, keep track of all mapped names used for that item
        //these will need to be ensured to be not equal
        HashMap<String, List<String>> itemToMappedNames = new HashMap<String, List<String>>();
		for (int i=0; i<this.pathNames.size(); i++) {
			String item = this.pathNames.get(i);
			if (! itemToMappedNames.containsKey(item)) {
				itemToMappedNames.put(item, new ArrayList<String>());
            }
            itemToMappedNames.get(item).add(this.mappedPathNames.get(i));
        }

        for (Map.Entry<String, List<String>> entry : itemToMappedNames.entrySet()) {
            String key = entry.getKey(); // item name
            List<String> value = entry.getValue(); // mapped names
            for (int i=0; i<value.size()-1; i++) {
                String primary_key;
                if (Schema.isEntity(key)) {
					primary_key = Schema.getEntity(key).getPrimaryKey().name;
                }
                else {
					primary_key = Schema.getRelationship(key).getPrimaryKey().name;
                }
                query += String.format(" AND %s.%s <> %s.%s", value.get(i), primary_key, value.get(i+1), primary_key);
            }
        }

        return query;
    }
}