/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 7:43:46 PM
 */
package rpc.dataretrieval;

import rpc.design.*;
import rpc.querygen.DesignTranslator;
import rpc.querygen.QueryFactory;
import rpc.querygen.MinMaxTranslator;
import rpc.querygen.PathTranslator;
import rpc.model.util.AttributeVariable;
import rpc.schema.Metadata;
import rpc.schema.SchemaItem;
import rpc.schema.Relationship;
import rpc.schema.Entity;
import rpc.util.DesignUtil;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * The DataRetrieval class accepts a design that corresponds to a set of variables and their aggregators
 * and retrieves the data from the database that holds observations about the design.  Theses results can be
 * organized into multi-dimensional contingency tables that can be used as input for statistical tests of independence.
 */
public class DataRetrieval {

    private static Logger log = Logger.getLogger(DataRetrieval.class);

    private int numControl;

    private String designQuery;

    /**
     * The design that contains information about the unit and design elements (e.g., conditioning variables)
     * over which to retrieve data from the database.
     */
    public Design design;

    /**
     * The aggregator used on the treatment variable of the unit.
     */
    public Aggregator treatmentAggregator;

    /**
     * The aggregator used on the outcome variable of the unit.
     */
    public Aggregator outcomeAggregator;

    /**
     * Constructs a DataRetrieval object, creates the corresponding database query and discretizes paths involving
     * non-categorical variables. 
     * @param design the design that contains information about the unit and design elements (e.g., conditioning variables).
     * @param treatmentAgg the aggregator used on the treatment variable of the unit.
     * @param outcomeAgg the aggregator used on the outcome variable of the unit.
     */
    public DataRetrieval(Design design, Aggregator treatmentAgg, Aggregator outcomeAgg) {
        this.design = design;
        this.treatmentAggregator = treatmentAgg;
        this.outcomeAggregator = outcomeAgg;

        this.numControl = 0;
        for (DesignElement de : design.getDesignElements()) {
            if (de instanceof ConditioningSet) {
                this.numControl += ((ConditioningSet) de).getConditioningSet().size();
            }
        }

        DesignTranslator dt = new DesignTranslator(design, treatmentAgg, outcomeAgg);
        this.designQuery = dt.getQuery();

        //Check discretization of treatment, outcome, and all conditioning variables
        discretize(this.design.unit.treatmentPath, this.treatmentAggregator);
        discretize(this.design.unit.outcomePath, this.outcomeAggregator);
        for (DesignElement de : design.getDesignElements()) {
            if (de instanceof ConditioningSet) {
                ConditioningSet cs = (ConditioningSet) de;
                for (Path cp : cs.getConditioningSet()) {
                    discretize(cp, cs.getConditioningAggregate(cp));
                }
            }
        }
    }

    /**
     * Get the 2D contingency table filled in with the results of the query.  These queries have only two columns and
     * correspond to the queries of a single unit (treatment and outcome pair).
     * @return the contingency table of results.
     */
    public ContingencyTable getContingencyTable() {
        ContingencyTable contTable = new ContingencyTable();
        ResultSet results = Database.executeQuery(this.designQuery);

        try {
            results.beforeFirst();
            while(results.next()) {
                int v1 = lookupBin(this.design.unit.treatmentPath, results.getInt(1));
                int v2 = lookupBin(this.design.unit.outcomePath, results.getInt(2));
                contTable.addValues(v1, v2, 1);
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through result set: " + e);
        }

        if (this.design.unit.outcomePath instanceof StructurePath) { //existence uncertainty
            //we must compute all potential counts and subtract the existence counts that were already computed above
            Relationship baseRel = (Relationship) this.design.unit.baseItem;

            SchemaItem relatedEntity = this.design.unit.treatmentPath.getPath().get(1).equals(baseRel.entity1)
                    ? baseRel.entity2 : baseRel.entity1;

            //Create the query to compute the counts for each configuration
            List<Path> treatmentPathWrapper = new ArrayList<Path>();
            Path newTreatmentPath = DesignUtil.rewriteFromEntity(this.design.unit.treatmentPath);
            treatmentPathWrapper.add(newTreatmentPath);
            List<Aggregator> treamentAggWrapper = new ArrayList<Aggregator>();
            this.treatmentAggregator.setTableName(newTreatmentPath.getTarget().name);
            treamentAggWrapper.add(this.treatmentAggregator);

            String joinQuery1 = QueryFactory.joinPathsGrouped(treatmentPathWrapper, treamentAggWrapper, "ct2");
            String joinQuery2 = String.format("SELECT COUNT(*) AS ct1\nFROM %s", relatedEntity.name);

            String joinQuery = QueryFactory.potentialLinkCounts(joinQuery1, joinQuery2);

            Map<Double, Integer> treatmentToLinkCounts = new HashMap<Double, Integer>();

            //Get the results and add them to the contingency table
            ResultSet joinRS = Database.executeQuery(joinQuery);
            try {
                joinRS.beforeFirst();
                while(joinRS.next()) {
                    try {
                        double treatment = lookupBin(this.design.unit.treatmentPath, joinRS.getInt(1));
                        int potentialLinkCount = joinRS.getInt(4);

                        if (! treatmentToLinkCounts.containsKey(treatment)) {
                            treatmentToLinkCounts.put(treatment, 0);
                        }
                        treatmentToLinkCounts.put(treatment,
                                potentialLinkCount + treatmentToLinkCounts.get(treatment));
                                            }
                    catch(SQLException e) {
                        log.error("Error while retrieving values from potential links result set: " + e);
                    }
                }
            }
            catch (SQLException e) {
                log.error("Error while iterating through potential links result set: " + e);
            }

            //Compute potentialLinkCount - existingCount for each set of configuration/treatment values
            HashMap<Double, HashMap<Double, Integer>> existingCounts = contTable.getCounts();
            for (Double treatment : treatmentToLinkCounts.keySet()) {
                int potentialLinks = treatmentToLinkCounts.get(treatment);

                int existingCount;
                if (existingCounts.containsKey(treatment)) {
                    existingCount = existingCounts.get(treatment).get(1.0);
                }
                else {
                    existingCount = 0;
                }

                //log.debug(treatment + " - " + potentialLinks);
                int nonExistentLinks = Math.max(0, potentialLinks - existingCount);
                contTable.addValues(treatment, 0, nonExistentLinks);
            }
        }

        return contTable;
    }

    /**
     * Get the 3D contingency table filled in with the results of the query.  These queries have multiple columns and
     * correspond to the queries of a unit (treatment and outcome pair) and all configurations
     * of conditioning variables.
     * @return the 3-dimensional contingency table of results.
     */
    public ContingencyTable3D getContingencyTable3D() {
        ContingencyTable3D contTable = new ContingencyTable3D();
        ResultSet results = Database.executeQuery(this.designQuery);

        ConditioningSet cs =(ConditioningSet) this.design.getDesignElements().get(0);
        List<Path> Paths = cs.getConditioningSet();

        try {
            results.beforeFirst();
            while(results.next()) {
                try {
                    int v1 = lookupBin(this.design.unit.treatmentPath, results.getInt(1));
                    int v2 = lookupBin(this.design.unit.outcomePath, results.getInt(2));

                    List<Object> config = new ArrayList<Object>();
                    for (int i=0; i<this.numControl; i++) {
                        config.add(lookupBin(Paths.get(i), results.getInt(i+3)));
                    }
                    contTable.addValues(v1, v2, config, 1);
                }
                catch(SQLException e) {
                    log.error("Error while retrieving values from result set: " + e);
                }
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through result set: " + e);
        }

        if (this.design.unit.outcomePath instanceof StructurePath) { //existence uncertainty
            //we must compute all potential counts and subtract the existence counts that were already computed above
            Relationship baseRel = (Relationship) this.design.unit.baseItem;

            //Separate all paths (treatment and control) into two sets, one for each entity of the relationship
            //This will allow us to determine the number of entities on each side of the relationship for each
            //configuration of values so that we can compute the potential number of links for each configuration.
            List<Path> entity1Paths = new ArrayList<Path>();
            List<Aggregator> entity1Aggs = new ArrayList<Aggregator>();
            List<Path> entity2Paths = new ArrayList<Path>();
            List<Aggregator> entity2Aggs = new ArrayList<Aggregator>();

            //Store the mappings from the original paths to the paths that begin with the connected entity
            Map<Path, Path> mappedPaths = new HashMap<Path, Path>();

            Entity treatmentEntity = (Entity) this.design.unit.treatmentPath.getPath().get(1);
            Path mappedTreatmentPath = DesignUtil.rewriteFromEntity(this.design.unit.treatmentPath);
            mappedPaths.put(mappedTreatmentPath, this.design.unit.treatmentPath);

            this.treatmentAggregator.setTableName(mappedTreatmentPath.getTarget().name);
            if (treatmentEntity.equals(baseRel.entity1)) {
                entity1Paths.add(mappedTreatmentPath);
                entity1Aggs.add(this.treatmentAggregator);
            }
            else {
                entity2Paths.add(mappedTreatmentPath);
                entity2Aggs.add(this.treatmentAggregator);
            }

            for (Path controlPath : Paths) {
                Entity controlEntity = (Entity) controlPath.getPath().get(1);
                Path mappedControlPath = DesignUtil.rewriteFromEntity(controlPath);
                mappedPaths.put(mappedControlPath, controlPath);
                cs.getConditioningAggregate(controlPath).setTableName(mappedControlPath.getTarget().name);
                if (controlEntity.equals(baseRel.entity1)) {
                    entity1Paths.add(mappedControlPath);
                    entity1Aggs.add(cs.getConditioningAggregate(controlPath));
                }
                else {
                    entity2Paths.add(mappedControlPath);
                    entity2Aggs.add(cs.getConditioningAggregate(controlPath));
                }
            }

            //Create a map of the indices the configuration to the indices of the query result
            Map<Integer, Integer> idxMap = new HashMap<Integer, Integer>();
            //set the index of the treatment variable in the query result
            int treatmentIdx;
            if (entity1Paths.size() > 0 && entity1Paths.get(0).equals(mappedTreatmentPath)) {
                treatmentIdx = 1;
                for (int i=1; i<entity1Paths.size(); i++) {
                    idxMap.put(Paths.indexOf(mappedPaths.get(entity1Paths.get(i))), i+1);
                }
                for (int i=0; i<entity2Paths.size(); i++) {
                    idxMap.put(Paths.indexOf(mappedPaths.get(entity2Paths.get(i))), i+entity1Paths.size()+2);
                }
            }
            else {
                treatmentIdx = entity1Paths.size() + 2;
                for (int i=0; i<entity1Paths.size(); i++) {
                    idxMap.put(Paths.indexOf(mappedPaths.get(entity1Paths.get(i))), i+1);
                }
                for (int i=1; i<entity2Paths.size(); i++) {
                    idxMap.put(Paths.indexOf(mappedPaths.get(entity2Paths.get(i))), i+entity1Paths.size()+2);
                }
            }

            //Create the query to compute the counts for each configuration
            String joinQuery1;
            if (entity1Paths.size() <= 0) {
                joinQuery1 = String.format("SELECT COUNT(*) AS ct1\nFROM %s", baseRel.entity1.name);
            }
            else {
                joinQuery1 = QueryFactory.joinPathsGrouped(entity1Paths, entity1Aggs, "ct1");
            }

            String joinQuery2;
            if (entity2Paths.size() <= 0) {
                joinQuery2 = String.format("SELECT COUNT(*) AS ct2\nFROM %s", baseRel.entity2.name);
            }
            else {
                joinQuery2 = QueryFactory.joinPathsGrouped(entity2Paths, entity2Aggs, "ct2");
            }

            String joinQuery = QueryFactory.potentialLinkCounts(joinQuery1, joinQuery2);

            Map<List<Object>, Map<Double, Integer>> potentialLinkCounts =
                    new HashMap<List<Object>, Map<Double, Integer>>();

            //Get the results and add them to the contingency table
            ResultSet joinRS = Database.executeQuery(joinQuery);
            int columnCount = entity1Paths.size() + entity2Paths.size() + 3;
            try {
                joinRS.beforeFirst();
                while(joinRS.next()) {
                    try {
                        double treatment = lookupBin(this.design.unit.treatmentPath, joinRS.getInt(treatmentIdx));

                        List<Object> config = new ArrayList<Object>();
                        for (int i=0; i<this.numControl; i++) {
                            config.add(lookupBin(Paths.get(i), joinRS.getInt(idxMap.get(i))));
                        }
                        int potentialLinkCount = joinRS.getInt(columnCount);

                        if (! potentialLinkCounts.containsKey(config)) {
                            potentialLinkCounts.put(config, new HashMap<Double, Integer>());
                        }
                        if (! potentialLinkCounts.get(config).containsKey(treatment)) {
                            potentialLinkCounts.get(config).put(treatment, 0);
                        }
                        potentialLinkCounts.get(config).put(treatment,
                                potentialLinkCount + potentialLinkCounts.get(config).get(treatment));

                    }
                    catch(SQLException e) {
                        log.error("Error while retrieving values from potential links result set: " + e);
                    }
                }
            }
            catch (SQLException e) {
                log.error("Error while iterating through potential links result set: " + e);
            }

            //Compute potentialLinkCount - existingCount for each set of configuration/treatment values
            Map<List<Object>, ContingencyTable> existingConfigs = contTable.getCounts();

            for (List<Object> config : potentialLinkCounts.keySet()) {
                for (Double treatment : potentialLinkCounts.get(config).keySet()) {
                    int potentialLinks = potentialLinkCounts.get(config).get(treatment);
                    int existingCount;
                    if (existingConfigs.containsKey(config)) {
                        if (existingConfigs.get(config).getCounts().containsKey(treatment)) {
                            existingCount = existingConfigs.get(config).getCounts().get(treatment).get(1.0);
                        }
                        else {
                            existingCount = 0;
                        }
                    }
                    else {
                        existingCount = 0;
                    }

                    int nonExistentLinks = Math.max(0, potentialLinks - existingCount);
                    contTable.addValues(treatment, 0, config, nonExistentLinks);
                }
            }
        }
        return contTable;
    }

    private void discretize(Path p, Aggregator agg) {
        if (Database.categoricalPaths.contains(p)) {
            return;
        }

        if (p.getVariable() instanceof AttributeVariable) {
            if (((AttributeVariable) p.getVariable()).getAttribute().isCategorical()) {
                Database.categoricalPaths.add(p);
                return;
            }
        }

        if (! Database.discretizeCache.containsKey(p)) {
            Database.discretizeCache.put(p, null);
            int distinctVals = Metadata.getDistinctValues(p, agg);
            log.debug("Number of distinct values for (" + p + "): " + distinctVals);

            //Check the number of rows first
            //if less than number of bins parameter, continue, leave null in cache
            //else get min and max and calculate the bin size = b
            //then create new list that has upper bounds for each bin (min+b), (min+2b), ...
            if (distinctVals > Database.NUM_BINS) {
                MinMaxTranslator mmt = new MinMaxTranslator(new PathTranslator(p, agg, "tempVar"));
                ResultSet rs = Database.executeQuery(mmt.getQuery());
                try {
                    rs.beforeFirst();
                    rs.next();
                    int min = rs.getInt(1);
                    int max = rs.getInt(2);
                    int binSize = (max - min)/ Database.NUM_BINS;
                    List<Integer> thresholds = new ArrayList<Integer>();
                    for (int i=0; i< Database.NUM_BINS; i++) {
                        thresholds.add(min+(i+1)*binSize);
                    }
                    Database.discretizeCache.put(p, thresholds);
                }
                catch (SQLException e) {
                    log.error("Error while iterating through distinct values result set: " + e);
                }
            }
        }
    }

    private int lookupBin(Path p, int value) {
        if (Database.categoricalPaths.contains(p) || ! Database.discretizeCache.containsKey(p)
                || Database.discretizeCache.get(p) == null) {
            return value;
        }
        else {
            List<Integer> bins = Database.discretizeCache.get(p);
            for (int i=0; i<bins.size(); i++) {
                if (value <= bins.get(i)) {
                    return i;
                }
            }
            return bins.size()-1;
        }
    }
}
