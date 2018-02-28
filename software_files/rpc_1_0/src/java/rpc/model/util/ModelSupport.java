/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 5, 2009
 * Time: 3:09:23 PM
 */
package rpc.model.util;

import rpc.design.*;
import rpc.util.DesignUtil;
import rpc.schema.Schema;
import rpc.schema.Attribute;
import rpc.schema.SchemaItem;
import rpc.schema.Relationship;

import java.util.*;

/**
 * ModelSupport is a data structure that consists of all units, all possible dependencies in that space of units,
 * information about potential causes, caches that store the standardization of units, and which units have
 * been marked as dependent or independent.
 */
public class ModelSupport {

    /**
     * Cache that maps each non-unique unit to its standardized version.
     */
    public Map<Unit, Unit> standardizedCache = new HashMap<Unit, Unit>();
    /**
     * Cache that stores for each unique unit, the set of non-unique units that
     * represent the same dependence.
     */
    public Map<Unit, Set<Unit>> equivalentCache = new HashMap<Unit, Set<Unit>>();
    /**
     * Cache that stores the set of all inplausible units (e.g., units with the same treatment and outcome).
     */
    public Set<Unit> inadmissibleUnits = new HashSet<Unit>();

    private HashMap<Unit, Double> prior;
    private Set<Unit> trivialDependencies = new HashSet<Unit>();

    private HashMap<Path, List<Path>> potentialCauses;

    /**
     * The HOP THRESHOLD is the maximum number of hops in the schema used to generate the set of units.
     */
    public int HOP_THRESHOLD;
    private final Double BASEPRIOR = 0.5;

    /**
     * Initialize the ModelSupport data structure by filling in the appropriate caches.
     * @param allUnits the set of all possible units given the hop threshold.
     * @param uniqueUnits the set of unique units corresponding to testable dependencies.
     * @param hopThreshold the number of hops in the schema used to generate the set of units.
     */
    public ModelSupport(List<Unit> allUnits, List<Unit> uniqueUnits, int hopThreshold) {
        this.HOP_THRESHOLD = hopThreshold;

        //initialize data structures
        this.potentialCauses = new HashMap<Path, List<Path>>();
        this.prior = new HashMap<Unit, Double>();

        for (Unit u : allUnits) {
            if (! this.potentialCauses.containsKey(u.outcomePath)) {
                this.potentialCauses.put(u.outcomePath, new ArrayList<Path>());
            }
            this.potentialCauses.get(u.outcomePath).add(u.treatmentPath);
        }

        for (Unit u : uniqueUnits) {
            if (! this.potentialCauses.containsKey(u.outcomePath)) {
                this.potentialCauses.put(u.outcomePath, new ArrayList<Path>());
            }
            this.potentialCauses.get(u.outcomePath).add(u.treatmentPath);            
        }

        //Add in trivial causal dependencies from relationship existence to its attributes
        for (Relationship r : Schema.getAllRelationships()) {
                List<SchemaItem> singleton = new ArrayList<SchemaItem>();
                singleton.add(r);
                StructurePath relExist = new StructurePath(r, r, singleton, Cardinality.ONE);
                Set<Attribute> relAttrs = r.getAllAttributes();
                for (Attribute relAttr : relAttrs) {
                    if (relAttr.isForeignKey() || relAttr.isPrimaryKey()) {
                        continue;
                    }
                    AttributePath attrPath = new AttributePath(r, r, singleton, Cardinality.ONE, relAttr);
                    this.trivialDependencies.add(new Unit(r, relExist, attrPath));
            }
        }

        //Fill unit caches
        //populate map from units to standardized versions and reverse map
        for (Unit u : allUnits) {
            if (this.hasUnit(u)) {
                if (! this.equivalentCache.containsKey(u)) {
                    this.equivalentCache.put(u, new HashSet<Unit>());
                }
                this.equivalentCache.get(u).add(u);
                this.standardizedCache.put(u, u);
            }
            else {
                Unit standardUnit = DesignUtil.standardize(u);
                this.standardizedCache.put(u, standardUnit);
                if (! this.equivalentCache.containsKey(standardUnit)) {
                    this.equivalentCache.put(standardUnit, new HashSet<Unit>());
                }
                this.equivalentCache.get(standardUnit).add(u);
            }
        }

        //add in remaining units to the cache that are already standard versions
        for (Unit u : uniqueUnits) {
            if (! this.standardizedCache.containsKey(u)) {
                this.standardizedCache.put(u, u);
            }
            if (! this.equivalentCache.containsKey(u)) {
                this.equivalentCache.put(u, new HashSet<Unit>());
            }
            this.equivalentCache.get(u).add(u);
        }

        //initialize priors
        for (Unit u : uniqueUnits) {
            this.prior.put(u, BASEPRIOR);
        }

    }

    /**
     * Retrieve the list of all unique units that correspond to testable dependencies.
     * @return the list of units.
     */
    public List<Unit> getAllUnits() {
        return new ArrayList<Unit>(this.prior.keySet());
    }

    /**
     * Get all the potential causes of a given path (i.e., relational variable).
     * @param p the path.
     * @return the list of other paths (i.e., other relational variables) that are potential causes.
     */
    public List<Path> getPotentialCauses(Path p) {
        return this.potentialCauses.get(p);
    }

    /**
     * Checks if a given unit exists in the model support data structure.
     * @param u the unit in question.
     * @return true if the unit exists in the data structure; false otherwise.
     */
    public boolean hasUnit(Unit u) {
        return this.prior.containsKey(u);
    }

    /**
     * Checks if the given unit is trivially dependent (e.g., relationship attributes are trivially
     * dependent on the existence of the relationship).
     * @param u the unit in question.
     * @return true if the unit is a trivial dependence; false otherwise.
     */
    public boolean isTrivialDependence(Unit u) {
        return this.trivialDependencies.contains(u);
    }

    /**
     * Gets the probability that the unit corresponds to a dependency.
     * @param u the unit in question.
     * @return the current prior probability.
     */
    public double getPrior(Unit u) {
        return this.prior.get(u);
    }

    /**
     * Sets the probability that a unit is dependent (i.e., update with a posterior probability).
     * @param u the unit.
     * @param prob the updated probability.
     */
    public void updatePrior(Unit u, Double prob) {
        this.prior.put(u, prob);
    }
}