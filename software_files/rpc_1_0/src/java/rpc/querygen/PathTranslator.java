/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 5, 2009
 * Time: 2:21:56 PM
 */
package rpc.querygen;

import rpc.schema.Schema;
import rpc.schema.Attribute;
import rpc.design.Path;
import rpc.dataretrieval.Aggregator;
import rpc.dataretrieval.NopAggregator;
import rpc.dataretrieval.ModeAggregator;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * PathTranslator provides a SQL query for a given path and aggregator.
 */
public class PathTranslator {

    private static Logger log = Logger.getLogger(PathTranslator.class);

    private Aggregator aggregator;

    private String baseItem;
    private String mappedBaseItem;

    private List<String> pathNames;
    private List<String> mappedPathNames;

    private String target;
    private String mappedTarget;

    private String var;
    private String newVarName;

    /**
     * Initializes a PathTranslator object and necessary SQL mappings for names along the path
     * if the same table used multiple times.
     * @param path the path to translate.
     * @param agg the aggregator over the path.
     * @param newVarName the name to use to rename the variable in the query.
     */
    public PathTranslator(Path path, Aggregator agg, String newVarName) {
        this.aggregator = agg;

        this.baseItem = path.getBaseItem().name;
        this.target = path.getTarget().name;
        this.var = path.getVariable().name();
        this.newVarName = newVarName;

        this.pathNames = path.getPathNames();

        mapItemNames();
    }

    /**
     * Gets the base table name of the path and query.
     * @return the name of the base table.
     */
    public String getBaseItem() {
        return this.baseItem;
    }

    /**
     * Get the name used to rename the variable in the query.
     * @return the new name for the variable in the query.
     */
    public String getNewVarName() {
        return this.newVarName;
    }

    //Mode aggregator implemented using a windowing function
    //SELECT id, val
    //FROM (
    //  SELECT id, val, row_number() OVER (PARTITION BY id) AS rn
    //  FROM (
    //      SELECT id, val
    //      FROM table
    //      GROUP BY id, val
    //      ORDER BY id, COUNT(*) DESC, RANDOM()
    //  ) temp1
    //) temp2
    //WHERE rn = 1

    /**
     * Builds the SQL query for the path.
     * @return the SQL query.
     */
    public String getQuery() {
        StringBuffer sb = new StringBuffer();

        if (this.aggregator instanceof ModeAggregator) { //specific mode implementation
            String baseId = Schema.getSchemaItem(this.baseItem).getPrimaryKey().name;
            String newId = "";
            if (! this.baseItem.equals(this.mappedBaseItem)) {
                newId += String.format("%s.%s", this.mappedBaseItem, baseId);
            }
            else {
                newId += String.format("%s.%s", this.baseItem, baseId);
            }

            String newVal;
            if (! this.target.equals(this.mappedTarget)) {
                this.aggregator.setTableName(this.mappedTarget);
                newVal = this.mappedTarget + "." + this.var;
            }
            else {
                newVal = this.target + "." + this.var;
            }

            sb.append(String.format("SELECT %s, %s AS %s", baseId, this.var, this.newVarName));
            sb.append("\nFROM (");
                sb.append(String.format("\nSELECT %s, %s, row_number() OVER (PARTITION BY %s ORDER BY ct DESC, RANDOM()) AS rn",
                        baseId, this.var, baseId));
//                sb.append(String.format("\nSELECT %s, %s, row_number() OVER (PARTITION BY %s ORDER BY ct DESC) AS rn",
//                        baseId, this.var, baseId));
                sb.append("\nFROM (");
                    sb.append("\nSELECT ");
                    sb.append(newId);
                    sb.append(", ");
                    sb.append(newVal);
                    sb.append(", COUNT(*) AS ct");
                    sb.append(getFrom());                    
                    sb.append(getWhere());
                    sb.append(getGroupBy());
                    sb.append(", ");
                    sb.append(newVal);
                sb.append(") ");
                sb.append(newVarName);        //make it a really unique table name
                sb.append("temp1");
            sb.append(") ");
            sb.append(newVarName);      //make it a really unique table name
            sb.append("temp2");
            sb.append("\nWHERE rn = 1 AND ");
            sb.append(this.aggregator.getVarName());
            sb.append(" IS NOT NULL");
        }
        else { //all other standard aggregates
          sb.append(getSelect());
            sb.append(getFrom());
            sb.append(getWhere());
            sb.append(getGroupBy());
        }
        return sb.toString();
    }

    private String getSelect() {
        String query = "SELECT ";
        if (! this.baseItem.equals(this.mappedBaseItem)) {
            query += String.format("%s.%s", this.mappedBaseItem, Schema.getSchemaItem(this.baseItem).getPrimaryKey().name);
        }
        else {
            query += String.format("%s.%s", this.baseItem, Schema.getSchemaItem(this.baseItem).getPrimaryKey().name);
        }

        if (! this.target.equals(this.mappedTarget)) {
            this.aggregator.setTableName(this.mappedTarget);
        }
        query += String.format(", %s AS %s", this.aggregator, this.newVarName);

        return query;
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

        if (!where_clause) {
            query += "\nWHERE " + this.aggregator.getTableName() + "." +
                    this.aggregator.getVarName() + " IS NOT NULL";
        }
        else {
            query += " AND " + this.aggregator.getTableName() + "."  +
                    this.aggregator.getVarName() + " IS NOT NULL";
        }

        return query;
    }

    private String getGroupBy() {
        if (this.aggregator instanceof NopAggregator) {
            //don't do a group by if the aggregator is no op, since SQL will fail
            return "";
        }
        else {
            return String.format("\nGROUP BY %s.%s", this.mappedBaseItem, Schema.getSchemaItem(this.baseItem).getPrimaryKey().name);
        }
    }

    private void mapItemNames(){
        //total appearance counts of items occurring in pathNames
        HashMap<String, Integer> itemCounts = new HashMap<String, Integer>();

        //traverse pathNames and increment appearances
        for (String item : this.pathNames) {
            if (! itemCounts.containsKey(item)) {
                itemCounts.put(item, 0);
            }
            //increment appearance of item
            itemCounts.put(item, itemCounts.get(item) + 1);
        }

        //stores most recently used item name increment
        HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();

        this.mappedPathNames = new ArrayList<String>();

        for (String item : this.pathNames) {
			if ( itemCounts.get(item) > 1 ) {
				if (! nameCounts.containsKey(item)) {
					nameCounts.put(item, 0);
                }
                nameCounts.put(item, nameCounts.get(item) + 1);
				this.mappedPathNames.add(item + nameCounts.get(item));
            }
            else {
				this.mappedPathNames.add(item);
            }
        }

        this.mappedBaseItem = this.mappedPathNames.get(0);
        this.mappedTarget = this.mappedPathNames.get(this.mappedPathNames.size()-1);
    }
}