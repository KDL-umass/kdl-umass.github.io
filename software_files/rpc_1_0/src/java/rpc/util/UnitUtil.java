/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 6, 2009
 * Time: 10:47:15 AM
 */
package rpc.util;

import rpc.design.*;
import rpc.schema.SchemaItem;
import rpc.schema.Schema;
import rpc.schema.Attribute;
import rpc.schema.Relationship;
import rpc.model.util.Vertex;

import java.util.*;

import jpl.Query;
import jpl.Term;

/**
 * Utility class that provides methods to process and retrieve units.
 */
public class UnitUtil {

    /**
     * Get a list of unique names of tables (with suffixes for multiple appearances) from the
     * paths in a unit to be used as the FROM clause in a SQL query.
     * @param unit the unit from which to process the list of schema items.
     * @return a list of unique names of tables.
     */
    public static List<String> mapItemNames(Unit unit){
        List<String> mappedPathNames = new ArrayList<String>();
        List<String> pathNames = new ArrayList<String>();

        List<String> pathNames1 = unit.treatmentPath.getPathNames();
        List<String> pathNames2 = unit.outcomePath.getPathNames();
        for (int i=pathNames1.size()-1; i>=0; i--) {
            //go through first path backwards
            pathNames.add(pathNames1.get(i));
        }
        for (int i=1; i<pathNames2.size(); i++) {
            pathNames.add(pathNames2.get(i));
        }

        //total appearance counts of items occurring in pathNames
        HashMap<String, Integer> itemCounts = new HashMap<String, Integer>();

        //traverse pathNames and increment appearances
        for (String item : pathNames) {
            if (! itemCounts.containsKey(item)) {
                itemCounts.put(item, 0);
            }
            //increment appearance of item
            itemCounts.put(item, itemCounts.get(item) + 1);
        }

        //stores most recently used item name increment
        HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();

        for (String item : pathNames) {
			if ( itemCounts.get(item) > 1 ) {
				if (! nameCounts.containsKey(item)) {
					nameCounts.put(item, 0);
                }
                nameCounts.put(item, nameCounts.get(item) + 1);
                String mappedName = item + nameCounts.get(item);
                mappedPathNames.add(mappedName);
            }
            else {
				mappedPathNames.add(item);
            }
        }

        return mappedPathNames;
    }

    /**
     * Get the list of all units given a specific hop threshold to use in the schema.
     * @param HOP_THRESHOLD the maximum number of hops in each path.
     * @return the list of all units.
     */
    public static List<Unit> getUnits(int HOP_THRESHOLD) {
        return getUnitResults(String.format("unitAll(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, Card2, " +
                "Var2, %s, %s)", HOP_THRESHOLD, HOP_THRESHOLD));
	}

    /**
     * Get the list of all unique units corresponding to dependencies (having a singleton outcome)
     * given a specific hop threshold to use in the schema.
     * @param HOP_THRESHOLD the maximum number of hops in each path.
     * @return the list of all unique units.
     */
    public static List<Unit> getUniqueUnits(int HOP_THRESHOLD) {
        return getUnitResults(String.format("unitUnique(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, " +
                "Card2, Var2, %s, %s)", 0, HOP_THRESHOLD*2));
    }

    private static List<Unit> getUnitResults(String s) {
        List<Unit> units = new ArrayList<Unit>();

        Hashtable[] solutions = Query.allSolutions(s);
        for (Hashtable solution : solutions) {
            String baseItemStr     = ((Term) solution.get("BaseItem")).name();
            String target1Str      = ((Term) solution.get("Target1")).name();
            List<SchemaItem> path1 = PrologUtil.termToList((Term) solution.get("Path1"));
            Cardinality card1      = Cardinality.valueOf(((Term) solution.get("Card1")).name().toUpperCase());
            String var1 	       = ((Term) solution.get("Var1")).name();
            String target2Str      = ((Term) solution.get("Target2")).name();
            List<SchemaItem> path2 = PrologUtil.termToList((Term) solution.get("Path2"));
            Cardinality card2      = Cardinality.valueOf(((Term) solution.get("Card2")).name().toUpperCase());
            String var2 	       = ((Term) solution.get("Var2")).name();

            SchemaItem baseItem = Schema.getSchemaItem(baseItemStr);
            SchemaItem target1  = Schema.getSchemaItem(target1Str);
            SchemaItem target2  = Schema.getSchemaItem(target2Str);
            Attribute treatment = target1.getAttribute(var1);
            Attribute outcome   = target2.getAttribute(var2);

            Path treatmentPath;
            Path outcomePath;

            if (Schema.isRelationship(var1)) {
                treatmentPath = new StructurePath(baseItem, target1, path1, card1);
            }
            else {
                treatmentPath = new AttributePath(baseItem, target1, path1, card1, treatment);
            }

            if (Schema.isRelationship(var2)) {
                outcomePath = new StructurePath(baseItem, target2, path2, card2);
            }
            else {
                outcomePath = new AttributePath(baseItem, target2, path2, card2, outcome);
            }

            Unit u = new Unit(baseItem, treatmentPath, outcomePath);

            units.add(u);
        }
        return units;
    }

    /**
     * Get all relationships as vertices (excluding the base and treatment if structural)
     * that appear on the treatment path of a unit.
     * @param u the unit.
     * @return the set of vertices corresponding to the relationships that appear along the treatment path
     * in a unit.
     */
    public static Set<Vertex> getRelationshipsOnUnit(Unit u) {
        Set<Vertex> rels = new HashSet<Vertex>();
        List<SchemaItem> path = new ArrayList<SchemaItem>(u.treatmentPath.getPath());

        //remove instances of base and target (if treatment is structural, then remove all instances of target)
        path.remove(0);
        if (u.treatmentPath instanceof StructurePath) {
            while (path.contains(u.treatmentPath.getTarget())) {
                path.remove(u.treatmentPath.getTarget());
            }
        }
        else {
            if (path.size() > 0) {            
                path.remove(path.size()-1);
            }
        }

        for (SchemaItem si : path) {
            if (si instanceof Relationship) {
                rels.add(new Vertex(CausalModelUtil.relationshipToVertex((Relationship) si)));
            }
        }

        return rels;
    }

    /**
     * Get all relationships as singleton path objects (excluding the base and treatment if structural)
     * that appear on the treatment path of a unit.
     * @param u the unit.
     * @return the set of singleton relationship paths that appear along the treatment path in a unit.
     */
    public static Set<Path> getRelationshipPathsOnUnit(Unit u) {
        Set<Path> rels = new HashSet<Path>();
        List<SchemaItem> path = new ArrayList<SchemaItem>(u.treatmentPath.getPath());

        //remove instances of base and target (if treatment is structural, then remove all instances of target)
        path.remove(0);
        if (u.treatmentPath instanceof StructurePath) {
            while (path.contains(u.treatmentPath.getTarget())) {
                path.remove(u.treatmentPath.getTarget());
            }
        }
        else {
            if (path.size() > 0) {
                path.remove(path.size()-1);
            }
        }

        for (SchemaItem si : path) {
            if (si instanceof Relationship) {
                List<SchemaItem> singletonPath = new ArrayList<SchemaItem>();
                singletonPath.add(si);
                rels.add(new StructurePath(si, si, singletonPath, Cardinality.ONE));
            }
        }

        return rels;
    }

}