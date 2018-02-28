/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 12, 2009
 * Time: 10:54:04 AM
 */
package rpc.datagen;

import rpc.design.*;
import rpc.schema.*;
import rpc.querygen.PathTranslator;
import rpc.querygen.DistinctTranslator;
import rpc.dataretrieval.*;
import rpc.util.DesignUtil;
import rpc.model.util.Vertex;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.python.core.PyDictionary;
import org.apache.log4j.Logger;

/**
 * Vertex data contains the necessary information in order to generate the variable (attribute or structure)
 * for which the vertex corresponds to in the causal model.
 */
public class VertexData {

    private static Logger log = Logger.getLogger(VertexData.class);

    private Vertex vertex;
    private List<Unit> parents;

    /**
     * Construct a vertex data object by setting the parents of the given vertex.
     * @param vertex the vertex about which the vertex data holds information.
     * @param parents a list of units that correspond to the precise way in which variables are parents
     * of the given vertex.
     */
    public VertexData(Vertex vertex, List<Unit> parents) {
        this.vertex = vertex;

        if (isAttribute()) {
            this.parents = parents;
        }
        else {
            Relationship baseRel = Schema.getRelationship(getBaseTable());

            List<Unit> entity1Units = new ArrayList<Unit>();
            List<Unit> entity2Units = new ArrayList<Unit>();
            this.parents = new ArrayList<Unit>();

            if (parents != null) {
                for (Unit parentUnit : parents) {
                    Path parentPath = parentUnit.treatmentPath;
                    Entity parentEntity = (Entity) parentPath.getPath().get(1);
                    if (parentEntity.equals(baseRel.entity1)) {
                        entity1Units.add(parentUnit);
                    }
                    else {
                        entity2Units.add(parentUnit);
                    }
                }
                this.parents.addAll(entity1Units);
                this.parents.addAll(entity2Units);
            }
            else {
                this.parents = parents; //will be null
            }
        }
        
    }

    /**
     * Check whether or not the vertex corresponds to an attribute.
     * @return true if the vertex corresponds to an attribute; false otherwise.
     */
    public boolean isAttribute() {
        return this.vertex.isAttribute();
    }

    /**
     * Check whether or not the vertex corresponds to structure.
     * @return true if the vertex corresponds to structure; false otherwise.
     */
    public boolean isStructure() {
        return this.vertex.isStructure();
    }

    /**
     * Get the list of parents for the vertex.
     * @return a list of units that correspond to the precise way in which variables are parents
     * of the given vertex.
     */
    public List<Unit> getParents() {
        return this.parents;
    }

    /**
     * Get the vertex that the vertex data object describes.
     * @return the vertex.
     */
    public Vertex getVertex() {
        return this.vertex;
    }

    /**
     * Get the base table (entity or relationship) that is the source of the vertex.
     * @return the name of the base table.
     */
    public String getBaseTable() {
        return this.vertex.getBaseTable();
    }

    /**
     * Get the name of the variable corresponding to the vertex.
     * @return the name of the variable corresponding to the vertex.
     */
    public String getAttribute() {
        return this.vertex.getAttribute();
    }

    /**
     * Check whether or not a given parent variable is structural.
     * @param u the parent unit to check.
     * @return true if the treatment on the parent unit is structural; false otherwise.
     */
    public boolean isParentStructure(Unit u) {
        return u.treatmentPath instanceof StructurePath;
    }

    /**
     * Get the domain values for a given parent.
     * @param u the parent unit.
     * @return a list of integers corresponding to the domain values for the parent.
     */
    public List<Integer> getParentDomain(Unit u) {
        PathTranslator pt = getParentQuery(u);
        DistinctTranslator dt = new DistinctTranslator(pt);
        log.debug(dt.getQuery());
        ResultSet distinctVals = Database.executeQuery(dt.getQuery());
        List<Integer> domainValues = new ArrayList<Integer>();
        try {
            distinctVals.last();
            if (distinctVals.getRow() <= Database.NUM_BINS) {
                    distinctVals.beforeFirst();
                    while(distinctVals.next()) {
                        domainValues.add(distinctVals.getInt(1));
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

                for (int i=0; i<Database.NUM_BINS; i++) {
                    domainValues.add(i);
                }
            }
        }
        catch (SQLException e) {
            log.error("Error while discretizing: " + e);
        }
        log.debug("domain values: " + domainValues);
        return domainValues;
    }

    /**
     * Get the default domain values for a given parent unit.  The default values correspond to the mapped
     * values if discretized.
     * @param u the parent unit.
     * @return a list of objects (integers) that correspond to the default domain values.
     */
    public List<Object> getParentDomainDefaults(Unit u) {
        List<Object> domainValues = new ArrayList<Object>();
        if (u.treatmentPath instanceof StructurePath) {
            for (int i=0; i<Database.NUM_BINS; i++) {
                domainValues.add(i);
            }
        }
        else {
            domainValues.add(0);
            domainValues.add(1);
        }
        return domainValues;
    }

    /**
     * Get a connector for the vertex if it corresponds to a structural variable.
     * @param mean the mean number of links for any given entity.
     * @return null if the vertex is an attribute; otherwise, depends on the cardinality of the structural
     * relationship.  Will be a constant connector if one-to-one or a Poisson connector if a many is involved.
     * If the relationship is many-to-many, replacement is used with the connector.
     */
    public Connector getConnector(double mean) {
        if (isAttribute()) {
            return null;
        }
        Relationship rel = Schema.getRelationship(this.vertex.toString());
        if (rel.entity1Card == Cardinality.ONE && rel.entity2Card == Cardinality.ONE) {
            String query = String.format("SELECT %s FROM %s", rel.entity2.getPrimaryKey().name, rel.entity2.name);
            return new ConstantConnector(query, 1, false);
        }
        else if (rel.entity1Card == Cardinality.ONE && rel.entity2Card == Cardinality.MANY) {
            String query = String.format("SELECT %s FROM %s", rel.entity2.getPrimaryKey().name, rel.entity2.name);
            return new PoissonConnector(query, (mean-1), false);

        }
        else if (rel.entity1Card == Cardinality.MANY && rel.entity2Card == Cardinality.ONE) {
            String query = String.format("SELECT %s FROM %s", rel.entity1.getPrimaryKey().name, rel.entity1.name);
            return new PoissonConnector(query, (mean-1), false);
        }
        else {
            String query = String.format("SELECT %s FROM %s", rel.entity2.getPrimaryKey().name, rel.entity2.name);
            return new PoissonConnector(query, (mean-1), true);
        }
    }

    /**
     * Get a CPT connector for the vertex. Only call if the vertex is structural.
     * @param parentsToCPTs a Python dictionary of parent values to CPTs.
     * @return a CPT connector with the given CPTs.
     */
    public CPTConnector getConnectorWithParents(PyDictionary parentsToCPTs) {
        CPTParent cpt = new CPTParent(parentsToCPTs);
        Relationship rel = Schema.getRelationship(this.vertex.toString());
        //if many-to-many relationship, connect with replacement to get backlinks
        boolean withReplacement = rel.entity1Card == Cardinality.MANY && rel.entity2Card == Cardinality.MANY;

        SchemaItem targetEnt = this.parents.get(0).outcomePath.getTarget();
        String query = String.format("SELECT %s FROM %s", targetEnt.getPrimaryKey().name, targetEnt.name);
        return new CPTConnector(query, cpt, withReplacement);
    }

    /**
     * Get the list of SQL queries that will retrieve the parent variables.
     * @return a list of SQL queries that will retrieve the parent variables.
     */
    public List<String> getParentQueries() {
        List<String> parentQueries = new ArrayList<String>();
        for (Unit parentUnit : this.parents) {
            parentQueries.add(getParentQuery(parentUnit).getQuery());
        }
        return parentQueries;
    }

    private PathTranslator getParentQuery(Unit u) {
            Aggregator treatmentAgg;
            if (u.treatmentPath instanceof AttributePath){
                treatmentAgg = new ModeAggregator(new NopAggregator(u.treatment));
            }
            else {
                treatmentAgg = new CountAggregator(new NopAggregator(u.treatment));
            }

            if (isStructure()) {
                return new PathTranslator(DesignUtil.rewriteFromEntity(u.treatmentPath), treatmentAgg, "parent");
            }
            else {
                return new PathTranslator(u.treatmentPath, treatmentAgg, "parent");
            }
    }

    /**
     * If the vertex is structural, gets the number of rows in the entity tables associated with the relationship.
     * @return null if the vertex is an attribute; otherwise, the number of rows in the two entity tables
     * associated with the relationship. 
     */
    public int[] getNumEntities() {
        if (isAttribute()) {
            return null;
        }
        else {
            int[] numEntities = new int[2];
            Relationship baseRel = Schema.getRelationship(getBaseTable());

            numEntities[0] =  Metadata.getItemCount(baseRel.entity1);
            numEntities[1] =  Metadata.getItemCount(baseRel.entity2);
            
            return numEntities;
        }
    }

    /**
     * Returns a string representation of the vertex data.
     * @return the string representation.
     */
    public String toString() {
        return "Vertex: " + this.vertex + "\nParents: " + this.parents;
    }

}