/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: May 9, 2009
 * Time: 9:34:03 PM
 */
package rpc.datagen;

import rpc.dataretrieval.*;
import rpc.schema.*;
import rpc.design.*;
import rpc.util.DesignUtil;
import rpc.util.PythonUtil;
import rpc.querygen.QueryFactory;
import rpc.querygen.PathTranslator;
import rpc.querygen.DistinctTranslator;
import rpc.model.util.AttributeVariable;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.python.core.PyDictionary;
import org.apache.log4j.Logger;

/**
 * Data Generator provides methods for instantiating a relational database based on a generative causal model
 * and its parameterization.
 */
public class DataGenerator {

    private static Logger log = Logger.getLogger(DataGenerator.class);

    /**
     * Cache that stores query strings for variables and their list of discretized thresholds.
     */
    public static Map<String, List<Integer>> discretizeCache = new HashMap<String, List<Integer>>();
    /**
     * Cache that stores paths (i.e., variables) and their list of discretized thresholds.
     */
    public static Map<Path, List<Integer>> discretizeCachePath = new HashMap<Path, List<Integer>>();
    /**
     * Cache that stores which paths (i.e., variables) are categorical and should not be discretized.
     */
    public static Set<Path> categoricalPaths = new HashSet<Path>();

    /**
     * Instantiate the data generator and create the tables necessary to populate the relational database for the
     * current schema.
     * @param dbname the name of the relational database.
     */
    public DataGenerator(String dbname) {

        Database.open(dbname);
        Database.clearDB();

        // create the entities and relationships
        List<String> tableStmts = Schema.getSQL();
        for (String stmt: tableStmts) {
            log.debug(stmt);
            Database.executeQuery(stmt);
        }
    }

    /**
     * Jython overload: Generates data and populates a relational database given a causal model structure, a
     * parameterization over that structure, and a map of ratios of the size of entity tables for each relationship.
     * @param cmg the causal model structure governing the data to generate.
     * @param p the parameterization over the causal structure.
     * @param entitySizes a Python dictionary of ratios of the size of entity tables for each relationship.
     */
    public void generate(CausalModelGenerator cmg, Parameterization p, PyDictionary entitySizes) {
        generate(cmg, p, PythonUtil.mapFromStringIntegerPyDict(entitySizes));
    }

    /**
     * Generates data and populates a relational database given a causal model structure, a parameterization
     * over that structure, and a map of ratios of the size of entity tables for each relationship.
     * @param cmg the causal model structure governing the data to generate.
     * @param p the parameterization over the causal structure.
     * @param entitySizes a map of ratios of the size of entity tables for each relationship.
     */
    public void generate(CausalModelGenerator cmg, Parameterization p, HashMap<String, Integer> entitySizes) {
        //initialize and populate entity tables
        for (String e : entitySizes.keySet()) {
            populateEntityTable(e, entitySizes.get(e));
        }

        cmg.resetIterator();
        while (cmg.hasNextDependency()) {
            VertexData vData = cmg.getNextDependency();
            if (vData.getParents() == null) {
                if (vData.isAttribute()) {
                    log.debug("Generating data for " + vData.getBaseTable()  + "." + vData.getAttribute());
                    generateAttribute(vData.getBaseTable(), vData.getAttribute(), (CPTPrior) p.getCPT(vData));
                }
                else {
                    log.debug("Generating relationships for " + vData.getBaseTable() + "." + vData.getAttribute());
                    populateRelationshipTable(vData.getAttribute(), p.getConnector(vData), null);
                }
            }
            else { //has parents
                if (vData.isAttribute()) {
                    log.debug("Generating data for " + vData.getBaseTable() + "." + vData.getAttribute());
                    log.debug(vData.getParents());
                    generateAttributeWithParents(vData.getBaseTable(), vData.getAttribute(),
                            (CPTParent) p.getCPT(vData), vData.getParents());
                }
                else {
                    log.debug("Generating relationships for " + vData.getBaseTable() + "." + vData.getAttribute());
                    //scale link means to get link probabilities based on actual number of entities 
                    String ent1 = Schema.getRelationship(vData.getBaseTable()).entity1.name;
                    String ent2 = Schema.getRelationship(vData.getBaseTable()).entity2.name;
                    int numEnt1 = entitySizes.get(ent1);
                    int numEnt2 = entitySizes.get(ent2);
                    double scale = 1.0*Math.max(numEnt1, numEnt2)/(numEnt1*numEnt2);

                    HashMap<List<Object>, CPTPrior> parentsToCPTs = new HashMap<List<Object>, CPTPrior>();
                    Map<List<Object>, Double> linkMeans = p.getLinkMeans(vData);
                    for (List<Object> parents : linkMeans.keySet()) {
                        HashMap<Integer, Double> table = new HashMap<Integer, Double>();
                        table.put(1, linkMeans.get(parents)*scale);
                        table.put(0, 1-linkMeans.get(parents)*scale);
                        CPTPrior cpt = new CPTPrior(table);
                        parentsToCPTs.put(parents, cpt);
                    }
                    CPTParent cptParent = new CPTParent(parentsToCPTs);

                    generateRelationshipsWithParents(vData.getParents(), cptParent);
                }
            }
        }
    }

    /**
     * Get the list of unique ids from an entity or relationship table
     * @param item the schema item to get ids for.
     * @return the set of unique ids for the table.
     */
    public Set<Integer> getKeys(SchemaItem item) {
        String query = "SELECT " + item.getPrimaryKey().name + " FROM " + item.name;
        ResultSet results = Database.executeQuery(query);

        // convert this into a set
        Set<Integer> idSet = new HashSet<Integer>();
        try {
            results.beforeFirst();
            while(results.next()) {
                idSet.add(results.getInt(1));
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through result set: " + e);
        }

        return idSet;
    }

    /**
     * Join a list of strings together with a specified delimiter between each string.
     * @param coll a list of strings to join.
     * @param delimiter the delimiter used between strings.
     * @return the joined string.
     */
    public static String join(List<String> coll, String delimiter) {
        if (coll.isEmpty())
	        return "";

        StringBuilder sb = new StringBuilder();

        for (String x : coll) {
            String temp = x + delimiter;
            sb.append(temp);
        }

        sb.delete(sb.length()-delimiter.length(), sb.length());

        return sb.toString();
    }

    /**
     * Populate an entity table by instantiating entity ids and leaving all other columns NULL.
     * @param tableName the name of the entity table to populate.
     * @param numRows the number of rows to create.
     */
    public void populateEntityTable(String tableName, int numRows) {
        Entity e = Schema.getEntity(tableName);
        log.debug("filling table " + tableName);

        // get all the attributes for this entity in the correct order
        List<String> attrNames = e.getSQLAttrs();

        String insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";

        boolean addingMore = false;
        for (int i = 0; i < numRows; i++) {
            addingMore = true;

            insertStmt += "(";
            for (String attr: attrNames) {

                if (e.getAttribute(attr).isPrimaryKey())
                    insertStmt += i;
                else
                    insertStmt += "NULL";

                insertStmt += ", ";
            }

            insertStmt = insertStmt.substring(0, insertStmt.length()-2);
            insertStmt += "),";

            if (i % 1000 == 0) {
                // remove the extra comma
                insertStmt = insertStmt.substring(0, insertStmt.length()-1) + ";";
                Database.executeQuery(insertStmt);
                insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";
                addingMore = false;
            }

        }

        if (addingMore) {
            insertStmt = insertStmt.substring(0, insertStmt.length()-1) + ";";
            Database.executeQuery(insertStmt);
        }
    }

    /**
     * Populate a relationship table by instantiating relationship ids and foreign keys and
     * leaving all other columns NULL. The relationships connect entities based on the provided Connector
     * object.
     * @param tableName the name of the relationship table to populate.
     * @param connector the Connector object specifying how to link together entities.
     * @param parentQueries a list of SQL queries that retrieve parent values if the relationship is
     * contingent on a set of parent variables.
     */
    public void populateRelationshipTable(String tableName, Connector connector, List<String> parentQueries) {
        Relationship r = Schema.getRelationship(tableName);
        log.debug("filling table " + tableName);

        //set up connector by resetting ids
        connector.resetIds();

        // get all the attributes for this relationship in the correct order
        List<String> attrNames = r.getSQLAttrs();

        // get the set of ids representing the two entities involved in this relationship
        // i.e., relationship (AB), we need all the a_ids and b_ids
        Attribute[] foreignKeys = r.getForeignKeys();
        Set<Integer> outer = this.getKeys(foreignKeys[0].getRefTable());
        //Set<Integer> inner = this.getKeys(foreignKeys[1].getRefTable());

        HashMap<Integer, Object[]> keyToParentVals = new HashMap<Integer, Object[]>();

        if (parentQueries != null) {

            // retrieve data for parent values for each record in the entity table
            for (int i = 0; i < parentQueries.size(); i++) {
                String parentQuery = parentQueries.get(i);
                ResultSet results = Database.executeQuery(parentQuery);

                try {
                    results.beforeFirst();
                    while(results.next()) {
                        Integer key = results.getInt(1);
                        Integer val = lookupBin(parentQuery, results.getInt(2));
                        if (!keyToParentVals.containsKey(key)) {
                            keyToParentVals.put(key, new Integer[parentQueries.size()]);
                        }
                        keyToParentVals.get(key)[i] = val;
                    }
                }
                catch (SQLException e) {
                    log.error("Error while iterating through result set: " + e);
                }

            }
        }

        String insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";
        
        int ctr = 0;
        boolean addingMore = false;
        for (Integer x : outer) {

            List<Integer> relatedEnts;
            if (parentQueries != null) {
                List<Object> parents = Arrays.asList(keyToParentVals.get(x));
                relatedEnts = ((CPTConnector)connector).sample(parents);
            }
            else {
                relatedEnts = connector.sample();
            }

            for (Integer relatedEnt : relatedEnts) {
                addingMore = true;

                // fill in primary and foreign keys
                insertStmt += "(" + ctr + ", " + x + ", " + relatedEnt;

                // fill in NULL for relationship attributes
                for (String attr : attrNames) {
                    if (!r.getAttribute(attr).isPrimaryKey() &&
                        !r.getAttribute(attr).isForeignKey())
                        insertStmt += ", NULL";
                }

                insertStmt += "), ";
                ctr++;

                if (ctr % 1000 == 0) {
                    // remove the extra comma
                    insertStmt = insertStmt.substring(0, insertStmt.length()-2);
                    Database.executeQuery(insertStmt);
                    insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";
                    addingMore = false;
                }
            }

        }

        if (addingMore) {
            // remove the extra comma
            insertStmt = insertStmt.substring(0, insertStmt.length()-2);
            Database.executeQuery(insertStmt);
        }
    }

    /**
     * Jython overload: Generate attribute values based on a conditional probability distribution
     * without parent variables and insert them into the appropriate base table.
     * @param itemName the name of the attribute's base table.
     * @param attrName the name of the attribute.
     * @param probs Python dictionary of values to probabilities.
     */
    public void generateAttribute(String itemName, String attrName, PyDictionary probs) {
        CPTPrior cpt = new CPTPrior(probs);
        generateAttribute(itemName, attrName, cpt);
    }

    /**
     * Generate attribute values based on a conditional probability distribution without parent variables
     * and insert them into the appropriate base table.
     * @param itemName the name of the attribute's base table.
     * @param attrName the name of the attribute.
     * @param cpt the conditional probability table to be sampled.
     */
    public void generateAttribute(String itemName, String attrName, CPTPrior cpt) {
        SchemaItem si = Schema.getSchemaItem(itemName);
        Set<Integer> keys = getKeys(si);
        for (Integer key : keys) {
            double val = cpt.sample();
            String updateStmt = "UPDATE " + itemName + " SET " + attrName + "=" + val + " WHERE "
                    + si.getPrimaryKey().name + "=" + key;
            Database.executeQuery(updateStmt);
        }
    }

    /**
     * Jython overload: Generate attribute values based on a conditional probability distribution with parent variables
     * and insert them into the appropriate base table.
     * @param itemName the name of the attribute's base table.
     * @param attrName the name of the attribute.
     * @param parentsToCPTs Python dictionary specifying the conditional probability tables for each configuration
     * of parent values.
     * @param parentUnits the units that correspond to the precise way in which the parent variables affect this
     * attribute.
     */
    public void generateAttributeWithParents(String itemName, String attrName, PyDictionary parentsToCPTs, List<Unit> parentUnits) {
        CPTParent cpt = new CPTParent(parentsToCPTs);
        generateAttributeWithParents(itemName, attrName, cpt, parentUnits);
    }

    /**
     * Generate attribute values based on a conditional probability distribution with parent variables
     * and insert them into the appropriate base table.
     * @param itemName the name of the attribute's base table.
     * @param attrName the name of the attribute.
     * @param cpt the conditional probability table to be sampled.
     * @param parentUnits the units that correspond to the precise way in which the parent variables affect this
     * attribute.
     */
    public void generateAttributeWithParents(String itemName, String attrName, CPTParent cpt, List<Unit> parentUnits) {
        SchemaItem item = Schema.getSchemaItem(itemName);
        HashMap<Integer, Object[]> keyToParentVals = new HashMap<Integer, Object[]>();

        // retrieve data for parent values for each record in the item table
        for (int i = 0; i < parentUnits.size(); i++) {
            discretize(parentUnits.get(i));
            String parentQuery = getParentQuery(parentUnits.get(i)).getQuery();
            ResultSet results = Database.executeQuery(parentQuery);

            try {
                results.beforeFirst();
                while(results.next()) {
                    Integer key = results.getInt(1);
                    Integer val = lookupBin(parentQuery, results.getInt(2));
                    if (!keyToParentVals.containsKey(key)) {
                        keyToParentVals.put(key, new Integer[parentUnits.size()]);
                    }
                    keyToParentVals.get(key)[i] = val;
                }
            }
            catch (SQLException e) {
                log.error("Error while iterating through result set: " + e);
            }
        }

        // assign attribute values to each record by sampling CPT given parent values
        for (Integer key: keyToParentVals.keySet()) {
            List<Object> parents = Arrays.asList(keyToParentVals.get(key));
            double val;
            try {
                val = cpt.sample(parents);
            }
            catch (NullPointerException npe) {
                log.debug("whoops null pointer! parents are: " + parents);
                throw new NullPointerException();
            }
            String updateStmt = "UPDATE " + itemName + " SET " + attrName + "=" + val + " WHERE " + item.getPrimaryKey().name + "=" + key;
            Database.executeQuery(updateStmt);
        }
    }

    /**
     * Jython overload: Generate relationships governed by parent variables and populate the
     * corresponding relationship table.
     * @param parentUnits the list of units that correspond to parent variables.
     * @param parentsToCPTs Python dictionary specifying the link probabilities
     * for each configuration of parent values.
     */
    public void generateRelationshipsWithParents(List<Unit> parentUnits, PyDictionary parentsToCPTs) {
        CPTParent cpt = new CPTParent(parentsToCPTs);
        generateRelationshipsWithParents(parentUnits, cpt);
    }

    /**
     * Generate relationships governed by parent variables and populate the corresponding relationship table.
     * @param parentUnits the list of units that correspond to parent variables.
     * @param cpt the conditional probability table that specifies link probabilities
     * for each configuration of parent values.
     */
    public void generateRelationshipsWithParents(List<Unit> parentUnits, CPTParent cpt) {
        for (Unit parentUnit : parentUnits) {
            discretize(parentUnit);
        }        

        //Should be called with a list of units in which all outcome paths are relationship existences
        Relationship baseRel = Schema.getRelationship(parentUnits.get(0).baseItem.name);

        List<Path> entity1Paths = new ArrayList<Path>();
        List<Aggregator> entity1Aggs = new ArrayList<Aggregator>();
        List<Path> entity2Paths = new ArrayList<Path>();
        List<Aggregator> entity2Aggs = new ArrayList<Aggregator>();

        //Store the mappings from the new rewritten-from-entity paths to the original paths
        Map<Path, Path> mappedPaths = new HashMap<Path, Path>();

        for (Unit parentUnit : parentUnits) {
            Path parentPath = parentUnit.treatmentPath;
            Entity parentEntity = (Entity) parentPath.getPath().get(1);
            Path mappedParentPath = DesignUtil.rewriteFromEntity(parentPath);
            mappedPaths.put(mappedParentPath, parentPath);
            Aggregator parentAgg = mappedParentPath.getAggregators().get(0);

            if (parentEntity.equals(baseRel.entity1)) {
                entity1Paths.add(mappedParentPath);
                entity1Aggs.add(parentAgg);
            }
            else {
                entity2Paths.add(mappedParentPath);
                entity2Aggs.add(parentAgg);
            }
        }

        String joinQuery1;
        if (entity1Paths.size() <= 0) {
            joinQuery1 = String.format("SELECT %s\nFROM %s", baseRel.entity1.getPrimaryKey().name, baseRel.entity1.name);
        }
        else {
            joinQuery1 = QueryFactory.joinPaths(entity1Paths, entity1Aggs);
        }

        String joinQuery2;
        if (entity2Paths.size() <= 0) {
            joinQuery2 = String.format("SELECT %s\nFROM %s", baseRel.entity2.getPrimaryKey().name, baseRel.entity2.name);
        }
        else {
            joinQuery2 = QueryFactory.joinPaths(entity2Paths, entity2Aggs);
        }

        //store the values of the parent variables for each entity
        Map<Integer, List<Object>> entity1IdMap = new HashMap<Integer, List<Object>>();
        Map<Integer, List<Object>> entity2IdMap = new HashMap<Integer, List<Object>>();

        ResultSet join1RS = Database.executeQuery(joinQuery1);
        int columnCount = entity1Paths.size() + 1;
        try {
            join1RS.beforeFirst();
            while(join1RS.next()) {
                try {
                    int entity1Id = join1RS.getInt(1);

                    List<Object> config = new ArrayList<Object>();
                    for (int i=1; i<columnCount; i++) {
                        config.add(lookupBin(mappedPaths.get(entity1Paths.get(i-1)), join1RS.getInt(i+1)));
                    }
                    entity1IdMap.put(entity1Id, config);
                                        }
                catch(SQLException e) {
                    log.error("Error while retrieving values from entity1 parent result set: " + e);
                }
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through entity1 parent result set: " + e);
        }

        ResultSet join2RS = Database.executeQuery(joinQuery2);
        columnCount = entity2Paths.size() + 1;
        try {
            join2RS.beforeFirst();
            while(join2RS.next()) {
                try {
                    int entity2Id = join2RS.getInt(1);

                    List<Object> config = new ArrayList<Object>();
                    for (int i=1; i<columnCount; i++) {
                        config.add(lookupBin(mappedPaths.get(entity2Paths.get(i-1)), join2RS.getInt(i+1)));
                    }
                    entity2IdMap.put(entity2Id, config);
                                        }
                catch(SQLException e) {
                    log.error("Error while retrieving values from entity2 parent result set: " + e);
                }
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through entity2 parent result set: " + e);
        }


        List<int[]> relsToAdd = new ArrayList<int[]>();

        //ONE-TO-ONE relationships will never happen in this function because constant from both directions
        if (baseRel.entity1Card.equals(Cardinality.ONE)) {
            //Iterate through every potential relationship (quadratic!)
            Set<Integer> entity2Ids = entity2IdMap.keySet(); 
            for (Integer entity1Id : entity1IdMap.keySet()) {

                //keep track of entity2s that get linked, and remove them from consideration for remaining entity1s
                Set<Integer> chosenIds = new HashSet<Integer>();
                for (Integer entity2Id : entity2Ids) {
                    List<Object> entity1Config = entity1IdMap.get(entity1Id);
                    List<Object> entity2Config = entity2IdMap.get(entity2Id);

                    entity1Config.addAll(entity2Config);
                    //System.out.println("\tChecking " + entity1Id + ", " + entity2Id);

                    if (cpt.sample(entity1Config) == 1) {
                        //System.out.println("\t\tADDING " + entity1Id + ", " + entity2Id + "!");
                        //link should exist, add to relationship set
                        relsToAdd.add(new int[]{entity1Id, entity2Id});
                        chosenIds.add(entity2Id);
                    }
                }
                entity2Ids.removeAll(chosenIds);
            }
        }
        else if(baseRel.entity2Card.equals(Cardinality.ONE)) {
            //Iterate through every potential relationship (quadratic!)
            Set<Integer> entity1Ids = entity1IdMap.keySet();
            for (Integer entity2Id : entity2IdMap.keySet()) {

                //keep track of entity1s that get linked, and remove them from consideration for remaining entity2s
                Set<Integer> chosenIds = new HashSet<Integer>();
                for (Integer entity1Id : entity1Ids) {
                    List<Object> entity1Config = entity1IdMap.get(entity1Id);
                    List<Object> entity2Config = entity2IdMap.get(entity2Id);

                    entity1Config.addAll(entity2Config);
                    //System.out.println("\tChecking " + entity1Id + ", " + entity2Id);

                    if (cpt.sample(entity1Config) == 1) {
                        //System.out.println("\t\tADDING " + entity1Id + ", " + entity2Id + "!");
                        //link should exist, add to relationship set
                        relsToAdd.add(new int[]{entity1Id, entity2Id});
                        chosenIds.add(entity1Id);
                    }
                }
                entity1Ids.removeAll(chosenIds);
            }
        }
        else {
            //Iterate through every potential relationship (quadratic!)
            for (Integer entity1Id : entity1IdMap.keySet()) {
                for (Integer entity2Id : entity2IdMap.keySet()) {
                    List<Object> entity1Config = entity1IdMap.get(entity1Id);
                    List<Object> entity2Config = entity2IdMap.get(entity2Id);
                    
                    List<Object> parentConfig = new ArrayList<Object>();
                    parentConfig.addAll(entity1Config);
                    parentConfig.addAll(entity2Config);
                    //System.out.println("\tChecking " + entity1Id + ", " + entity2Id + " (with " + parentConfig + ")");

                    if (cpt.sample(parentConfig) == 1) {
                        //System.out.println("\t\tADDING " + entity1Id + ", " + entity2Id + "!");
                        relsToAdd.add(new int[]{entity1Id, entity2Id});
                    }
                }
            }
        }

        //finally, add rows into relationship table
        populateRelationshipTable(baseRel.name, relsToAdd);

    }

    private void populateRelationshipTable(String tableName, List<int[]> foreignKeys) {
        Relationship r = Schema.getRelationship(tableName);
        log.debug("filling table " + tableName);

        // get all the attributes for this relationship in the correct order
        List<String> attrNames = r.getSQLAttrs();

        String insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";

        int ctr = 0;
        boolean addingMore = true;
        for (int[] idPair : foreignKeys) {
            addingMore = true;

            // fill in primary and foreign keys
            insertStmt += "(" + ctr + ", " + idPair[0] + ", " + idPair[1];

            // fill in NULL for relationship attributes
            for (String attr : attrNames) {
                if (!r.getAttribute(attr).isPrimaryKey() &&
                    !r.getAttribute(attr).isForeignKey())
                    insertStmt += ", NULL";
            }

            insertStmt += "), ";
            ctr++;

            if (ctr % 1000 == 0) {
                // remove the extra comma
                insertStmt = insertStmt.substring(0, insertStmt.length()-2);
                Database.executeQuery(insertStmt);
                insertStmt = "INSERT INTO " + tableName + "(" + join(attrNames, ",") + ") VALUES ";
                addingMore = false;
            }

        }

        if (addingMore) {
            // remove the extra comma
            insertStmt = insertStmt.substring(0, insertStmt.length()-2);
            Database.executeQuery(insertStmt);
        }
    }

    /**
     * Close the data generator object and the database.
     */
    public void close() {
        Database.close();
    }

    /**
     * Determine which bin the value falls in if discretization is used.
     * @param query the query that would retrieve the given value.
     * @param value the value to discretize.
     * @return the discretized bin for the given value if its variable is discretized; the value otherwise.
     */
    public int lookupBin(String query, int value) {
        if (! DataGenerator.discretizeCache.containsKey(query) || DataGenerator.discretizeCache.get(query) == null) {
            return value;
        }
        else {
            List<Integer> bins = DataGenerator.discretizeCache.get(query);
            for (int i=0; i<bins.size(); i++) {
                if (value <= bins.get(i)) {
                    return i;
                }
            }
            return bins.size()-1;
        }
    }

    /**
     * Determine which bin the value falls in if discretization is used.
     * @param path the path corresponding to the variable that would retrieve the given value.
     * @param value the value to discretize.
     * @return the discretized bin for the given value if its variable is discretized; the value otherwise.
     */
    public int lookupBin(Path path, int value) {
        if (Database.categoricalPaths.contains(path) || ! DataGenerator.discretizeCachePath.containsKey(path) 
                || DataGenerator.discretizeCachePath.get(path) == null) {
            return value;
        }
        else {
            List<Integer> bins = DataGenerator.discretizeCachePath.get(path);
            for (int i=0; i<bins.size(); i++) {
                if (value <= bins.get(i)) {
                    return i;
                }
            }
            return bins.size()-1;
        }
    }

    /**
     * Discretize the given unit by finding the set of disctinct values and determining appropriate thresholds
     * given the default number of bins used.
     * @param u the unit to discretize.
     */
    public void discretize(Unit u) {
        if (Database.categoricalPaths.contains(u.treatmentPath)) {
            return;
        }

        if (u.treatment instanceof AttributeVariable) {
            if (((AttributeVariable) u.treatment).getAttribute().isCategorical()) {
                Database.categoricalPaths.add(u.treatmentPath);
                return;
            }
        }

        PathTranslator pt = getParentQuery(u);
        DistinctTranslator dt = new DistinctTranslator(pt);
        log.debug(dt.getQuery());
        ResultSet distinctVals = Database.executeQuery(dt.getQuery());
        try {
            distinctVals.last();
            if (distinctVals.getRow() <= Database.NUM_BINS) {
                if (u.treatmentPath instanceof StructurePath) {
                    //for purposes of data generator, we discretize anyway if degree
                    List<Integer> thresholds = new ArrayList<Integer>();
                    distinctVals.beforeFirst();
                    while(distinctVals.next()) {
                        thresholds.add(distinctVals.getInt(1));
                    }
                    log.debug("thresholds: " + thresholds);
                    DataGenerator.discretizeCache.put(pt.getQuery(), thresholds);
                    DataGenerator.discretizeCachePath.put(u.treatmentPath, thresholds);
                }
            }
            else {
                distinctVals.beforeFirst();
                distinctVals.next();
                int min = distinctVals.getInt(1);

                distinctVals.last();
                int max = distinctVals.getInt(1);

                int binSize = (max - min)/ Database.NUM_BINS;
                List<Integer> thresholds = new ArrayList<Integer>();
                for (int i=0; i< Database.NUM_BINS; i++) {
                    thresholds.add(min+(i+1)*binSize);
                }
                log.debug("thresholds: " + thresholds);
                DataGenerator.discretizeCache.put(pt.getQuery(), thresholds);
                DataGenerator.discretizeCachePath.put(u.treatmentPath, thresholds);
            }
        }
        catch (SQLException e) {
            log.error("Error while discretizing: " + e);
        }
    }

    private PathTranslator getParentQuery(Unit u) {
            Aggregator treatmentAgg;
            if (u.treatmentPath instanceof AttributePath){
                treatmentAgg = new ModeAggregator(new NopAggregator(u.treatment));
            }
            else {
                treatmentAgg = new CountAggregator(new NopAggregator(u.treatment));
            }

            if (u.outcomePath instanceof StructurePath) {
                return new PathTranslator(DesignUtil.rewriteFromEntity(u.treatmentPath), treatmentAgg, "parent");
            }
            else {
                return new PathTranslator(u.treatmentPath, treatmentAgg, "parent");
            }
    }

    /**
     * Empties the caches that store the thresholds for discretization and which variable paths are
     * restricted as categorical.
     */
    public static void resetCache(){
        discretizeCache = new HashMap<String, List<Integer>>();
        discretizeCachePath = new HashMap<Path, List<Integer>>();
        categoricalPaths = new HashSet<Path>();
    }

}