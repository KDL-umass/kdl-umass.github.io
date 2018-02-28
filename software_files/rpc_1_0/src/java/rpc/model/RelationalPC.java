/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Sep 25, 2009
 * Time: 1:07:16 PM
 */
package rpc.model;

import rpc.design.*;
import rpc.dataretrieval.*;
import rpc.model.util.*;
import rpc.model.edgeorientation.*;
import rpc.model.scoring.ModelScoring;
import rpc.util.DesignUtil;
import rpc.util.CausalModelUtil;
import rpc.util.UnitUtil;
import rpc.querygen.DesignTranslator;
import rpc.statistics.ChiSquareTest;
import rpc.statistics.CMHTest;
import rpc.schema.SchemaItem;
import rpc.schema.Attribute;
import rpc.schema.Schema;
import rpc.schema.Relationship;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The Relational PC algorithm for learning partially directed causal models of relational domains.
 * RPC consists of two separate phases: skeleton identification and edge orientation.  It is a relational
 * extension to the PC algorithm (Spirtes, Glymour, & Scheines 2001).
 */
public class RelationalPC {
    private static Logger log = Logger.getLogger(RelationalPC.class);

    //Needs access to schema, all units, unique units, causal model (possible causes and priors)
    private ModelSupport modelSupport;

    //Needs to remember sepsets for each pair of variables
    private Map<Unit, List<Path>> sepsets;

    private List<String> constraints;
    private Set<Unit> dependentUnits;

    private Model model;

    private double defaultSignficanceThreshold = 0.01;
    /**
     * The significance threshold used for statistical tests.
     */
    public double signficanceThreshold;

    private double defaultStrengthEffectThreshold = 0.1;
    /**
     * The minimum strength of effect threshold used for determining substantive dependencies in statistical tests.
     */
    public double strengthOfEffectThreshold;

    /**
     * Initializes the Relational PC object with default settings.
     * @param modelSupport data structure containing set of all units, dependencies, and potential causes.
     */
    public RelationalPC(ModelSupport modelSupport) {
        this.modelSupport = modelSupport;
        this.model = new Model();
        this.constraints = new ArrayList<String>();
        this.sepsets = new HashMap<Unit, List<Path>>();
        this.dependentUnits = new HashSet<Unit>();
        this.setSignficanceThresholdAdjust(this.defaultSignficanceThreshold);
        this.setStrengthOfEffectThreshold(this.defaultStrengthEffectThreshold);
    }

    /**
     * Set the alpha significance level threshold used for statistical tests.
     * @param alpha signficance threshold.
     */
    public void setSignficanceThreshold(double alpha) {
        this.signficanceThreshold = alpha;
    }

    /**
     * Set the base alpha significance level threshold used for statistical tests. This threshold will be
     * adjusted using the Bonferonni correction based on the total number of possible dependencies used in Phase I.
     * @param alpha base significance threshold.
     */
    public void setSignficanceThresholdAdjust(double alpha) {
        int numPossibleDependencies = modelSupport.getAllUnits().size();
        this.model.setNumPossibleDependencies(numPossibleDependencies);
        this.signficanceThreshold = alpha/numPossibleDependencies;
        log.info("Running RPC with alpha=" + this.signficanceThreshold);
    }

    /**
     * Set the minimum strength of effect threshold used for determining substantive dependencies in statistical tests.
     * @param phi strength of effect threshold.
     */
    public void setStrengthOfEffectThreshold(double phi) {
        this.strengthOfEffectThreshold = phi;
    }

    /**
     * Get the significance level threshold used for statistical tests.
     * @return alpha, the significance level threshold.
     */
    public double getSignificanceThreshold() {
        return this.signficanceThreshold;
    }

    /**
     * Get the minimum strength of effect threshold used for determining substantive dependencies in statistical tests.
     * @return phi, the strength of effect threshold.
     */
    public double getStrengthOfEffectThreshold() {
        return this.strengthOfEffectThreshold;
    }

    /**
     * Get the current version of the learned model.
     * @return the currently learned model.
     */
    public Model getModel() {
        return this.model;
    }

    /**
     * Get the list of conditional independencies learned in Phase I of RPC.
     * @return a list of conditional independence statements.
     */
    public List<String> getConstraints() {
        return this.constraints;
    }

    /**
     * Get the set of units that correspond to the identified dependencies from Phase I of RPC.
     * @return the set of units that are statistically dependent.
     */
    public Set<Unit> getDependencies() {
        return this.dependentUnits;
    }
    
    /**
     * Run Phase I of Relational PC to identify the undirected skeleton that encodes the set of
     * conditional independencies in the data.
     * @param depth the maximum number of parent variables for each conditional test of independence.
     */
    public void identifySkeleton(int depth) {
        int d = 0;
        while (d <= depth) {
            log.info("depth " + d);
            List<Unit> units = this.modelSupport.getAllUnits();
            while (units.size() > 0) {
                log.debug(units.size() + " remaining units for current iteration (depth " + d + ")");
                Unit unit = units.get(0);

                log.debug("Testing unit: " + unit);
                if (this.modelSupport.getPrior(unit) <= 0) {
                    units.remove(unit);
                    log.debug("\tskipped, independent");
                    continue;
                }

                if (this.modelSupport.getPrior(unit) >= 1) {
                    units.remove(unit);
                    log.debug("\tskipped, known dependency");
                    continue;
                }

                //First, check sepsets of size d for treatment -> outcome

                //Get neighbors of the treatment variable
                Set<Path> potentialCauses = getNeighbors(unit);
                log.debug("\t" + potentialCauses.size() + " potential causes");

                if (potentialCauses.size() < d) {
                    units.remove(unit);
                    log.debug("\tskipped, no sepsets of size " + d);
                    this.dependentUnits.add(unit);
                    continue;
                }

                Set<Set<Path>> sepsets = getPotentialSepsets(potentialCauses, d);
                Set<ConditioningSet> conditioningSets = getConditioningSets(sepsets);

                log.debug("\t" + sepsets.size() + " sepsets of size " + d);
                log.debug("\t" + conditioningSets.size() + " conditioning sets with aggs of size " + d);

                boolean skipToNext = false;
                for (ConditioningSet conditioningSet : conditioningSets) {
                    if (skipToNext) {
                        break;
                    }
                    for (Aggregator treatmentAgg : unit.treatmentPath.getAggregators()) {
                        if (skipToNext) {
                            break;
                        }
                        for (Aggregator outcomeAgg : unit.outcomePath.getAggregators()) {
                            if (skipToNext) {
                                break;
                            }
                            StatisticResults sr = testPair(unit, treatmentAgg, outcomeAgg, conditioningSet);
                            log.debug("\tTest result: " + sr);
                            if (sr == null) {
                                //Variables in sepset are constant, remove them as possible causes
                                for (Path sep : conditioningSet.getConditioningSet()) {
                                    if (potentialCauses.contains(sep)) {
                                        this.modelSupport.getPotentialCauses(unit.treatmentPath).remove(sep);
                                        this.modelSupport.getPotentialCauses(unit.outcomePath).remove(sep);
                                    }
                                }
                                if (d==0) {
                                    ModelScoring.setMarginalTestPVal(unit, 1.0);
                                    ModelScoring.setMarginalTestStrength(unit, -1.0);
                                }
                                else {
                                    ModelScoring.setConditionalTestPVal(unit, 1.0);
                                    ModelScoring.setConditionalTestStrength(unit, -1.0);
                                }
                                continue;
                            }
                            //check if test failed from insufficient data (e.g., CMH with stratum size < 2)
                            if (sr.isFailed()) {
                                log.debug(sr.testName);
                                if (d==0) {
                                    ModelScoring.setMarginalTestPVal(unit, 1.0);
                                    ModelScoring.setMarginalTestStrength(unit, -1.0);
                                }
                                else {
                                    ModelScoring.setConditionalTestPVal(unit, 1.0);
                                    ModelScoring.setConditionalTestStrength(unit, -1.0);
                                }
                                continue;
                            }

                            //update model scoring with p-value and strength of marginal or conditional tests
                            if (d==0) {
                                ModelScoring.setMarginalTestPVal(unit, sr.pval);
                                ModelScoring.setMarginalTestStrength(unit, sr.phisquare);
                            }
                            else {
                                ModelScoring.setConditionalTestPVal(unit, sr.pval);
                                ModelScoring.setConditionalTestStrength(unit, sr.phisquare);
                            }

                            if (sr.pval > this.signficanceThreshold || sr.phisquare <= this.strengthOfEffectThreshold) {
                                //independent OR strength of effect too small
                                skipToNext = true;
                                this.constraints.add(unit.treatmentPath + " _||_ " + unit.outcomePath + " | " + conditioningSet);
                                this.sepsets.put(unit, conditioningSet.getConditioningSet());
                                this.sepsets.get(unit).addAll(UnitUtil.getRelationshipPathsOnUnit(unit));
                                this.modelSupport.updatePrior(unit, 0.0);
                                List<Path> curPotentialCauses = this.modelSupport.getPotentialCauses(unit.outcomePath);
                                if (curPotentialCauses != null) {
                                    curPotentialCauses.remove(unit.treatmentPath);
                                }

                                //Can only update the reverse unit if the cardinality is 1:1
                                if (unit.treatmentPath.getCardinality() == Cardinality.ONE) {
                                    Unit reverseUnit = unit.reverse();
                                    if (reverseUnit == null) {                                    
                                        break;
                                    }

                                    if (reverseUnit.treatmentPath.getCardinality() == Cardinality.ONE) {
                                        this.modelSupport.updatePrior(reverseUnit, 0.0);
                                        this.modelSupport.getPotentialCauses(reverseUnit.outcomePath).remove(reverseUnit.treatmentPath);
                                        if (d==0) {
                                            ModelScoring.setMarginalTestPVal(reverseUnit, sr.pval);
                                            ModelScoring.setMarginalTestStrength(reverseUnit, sr.phisquare);
                                        }
                                        else {
                                            ModelScoring.setConditionalTestPVal(reverseUnit, sr.pval);
                                            ModelScoring.setConditionalTestStrength(reverseUnit, sr.phisquare);
                                        }
                                        break;
                                    }                                        
                                }
                            }
                        }
                    }
                }
                units.remove(unit);
            }
            d++;
        }
        //Add the remaining dependent units
        List<Unit> units = this.modelSupport.getAllUnits();
        for (Unit u : units) {
            if (this.modelSupport.getPrior(u) > 0) {
                this.dependentUnits.add(u);
            }
        }

        //Build undirected skeleton
        createUndirectedModel();

        //Check all triples of variables to check for additional conditional independencies which may
        //have been missed due to having more hops than the hop threshold
        outOfRangeConditionalIndependencies();

    }

    private Set<Set<Path>> getPotentialSepsets(Set<Path> potentialCauses, int depth) {
        Set<Set<Path>> sepsets = new HashSet<Set<Path>>();
        sepsets.add(new HashSet<Path>());
        sepsets = createSubsets(sepsets, potentialCauses, depth);
        List<Set<Path>> sepsetsList = new ArrayList<Set<Path>>(sepsets);
        sepsets.clear();
        //need to copy to list and add back to avoid concurrent modification exception
        for (Set<Path> sepset : sepsetsList) {
            if (sepset.size() == depth) {
                sepsets.add(sepset);
            }
        }
        return sepsets;
    }

    /**
    def createSubsetsDepth(subsets, elements, maxSize):
	if elements == None:
		return None
	elif len(elements) == 0:
		return subsets
	else:
		el = elements[0]
		curSubsToAdd = []
		for subs in subsets:
			subset = subs[:]
			if len(subset) == maxSize:
				continue
			subset.append(el)
			curSubsToAdd.append(subset)
		subsets.extend(curSubsToAdd)
		return createSubsetsDepth(subsets, elements[1:], maxSize)
    */
    private Set<Set<Path>> createSubsets(Set<Set<Path>> subsets, Set<Path> elements, int maxSize) {
        if (elements == null || elements.size() == 0) {
            return subsets;
        }
        else {
            Path element = elements.iterator().next();
            Set<Set<Path>> curSubsToAdd = new HashSet<Set<Path>>();
            for (Set<Path> subs : subsets) {
                Set<Path> subset = new HashSet<Path>(subs);
                if (subset.size() == maxSize) {  //don't add any subsets greater than maxSize
                    continue;
                }
                subset.add(element);
                curSubsToAdd.add(subset);
            }
            subsets.addAll(curSubsToAdd);
            elements.remove(element);
            return createSubsets(subsets, elements, maxSize);
        }
    }

    private Set<ConditioningSet> getConditioningSets(Set<Set<Path>> sepsets) {
        Set<ConditioningSet> conditioningSets = new HashSet<ConditioningSet>();
        for (Set<Path> sepset : sepsets) {
            ConditioningSet cs = new ConditioningSet(new ArrayList<Path>(sepset));
            for (Path control : cs.getConditioningSet()) {
                NopAggregator nopAgg = new NopAggregator(control.getVariable());
                if (control instanceof AttributePath) {
                    cs.setConditioningAggregate(control, new ModeAggregator(nopAgg));
                }
                else { //structural
                    cs.setConditioningAggregate(control, new CountAggregator(nopAgg));
                }
            }
            conditioningSets.add(cs);
        }
        return conditioningSets;
    }

    private Set<Path> getNeighbors(Unit unit) {
        Set<Path> potentialCauses = new HashSet<Path>();

        List<Path> currentCauses = this.modelSupport.getPotentialCauses(unit.treatmentPath);

        if (currentCauses != null) {
            for (Path currentCause : currentCauses) {
                if (this.modelSupport.isTrivialDependence(new Unit(unit.baseItem, unit.outcomePath, currentCause))) {
                    //outcome is a known cause of the variable
                    continue;
                }

                if (! unit.outcomePath.equals(currentCause)) {//remove potential cause if it is the outcome
                        potentialCauses.add(currentCause);
                }
            }
        }

        currentCauses = this.modelSupport.getPotentialCauses(unit.outcomePath);

        if (currentCauses != null) {
            for (Path currentCause : currentCauses) {
                if (! unit.treatmentPath.equals(currentCause)) {//remove potential cause if it is the treatment
                        potentialCauses.add(currentCause);
                }
            }
        }

        return potentialCauses;
    }

    private StatisticResults testPair(Unit u, Aggregator treatmentAgg, Aggregator outcomeAgg, ConditioningSet conditioningSet) {
        Design design = new Design(u);
        if (conditioningSet.getConditioningSet().size() > 0) {
            design.addDesignElement(conditioningSet);
        }
        log.debug(new DesignTranslator(design, treatmentAgg, outcomeAgg).getQuery());

        DataRetrieval dr = new DataRetrieval(design, treatmentAgg, outcomeAgg);

        if (conditioningSet.getConditioningSet().size() <= 0) {
            ChiSquareTest cst = new ChiSquareTest(dr);
            StatisticResults sr = cst.getStatistic();
            log.debug(sr.statistic);
            return sr;
        }
        else {
            CMHTest cmhTest = new CMHTest(dr);
            return cmhTest.getStatistic();
        }
    }

    private void createUndirectedModel() {
        for (Unit u : this.dependentUnits) {
            Vertex v1 = new Vertex(CausalModelUtil.variableToVertex(u.treatment));
            Vertex v2 = new Vertex(CausalModelUtil.variableToVertex(u.outcome));
            VertexPair vp = new VertexPair(v1, v2);
            model.addDependence(vp, u);

            //and reverse since undirected (might be repeating efforts...)
            Unit reverseUnit = u.reverse();
            if (reverseUnit != null) {
                VertexPair revVP = new VertexPair(v2, v1);
                model.addDependence(revVP, reverseUnit);
            }
        }

    }

    private void outOfRangeConditionalIndependencies() {
        //Check all triples to see if we need to run additional conditional independence test that has
        //more hops than the hop threshold

        /*
            1. Identify possible v1--v2--v3 triples where v2 not in sepset(v1, v3)
            2. Test only the pairs that have valid units (check with Prolog) and would not have been
                tested in the first phase (i.e., have hop threshold too large)
            3. For each potential dependency, if marginally dependent, run a single conditional independence test
                with v2 as conditioning variable
            4. If conditionally independent, add to sepset of v1v3 and stop
         */
        log.debug("Checking v1-->v2-->v3 cases");
        for (Unit u1 : this.dependentUnits) {
            Variable v1 = u1.treatment;
            Variable v2 = u1.outcome;
            int u1Hops = u1.treatmentPath.getHopCount();
            for (Unit u2 : this.dependentUnits) {
                Variable v3 = u2.outcome;
                if (! u2.treatment.equals(v2) || v1.equals(v3)) {
                    continue;
                }

                //v1, v2, v3 are distinct variables at this point
                log.debug("Checking u1 " + u1 + ", u2 " + u2);

                Vertex vertex1 = new Vertex(CausalModelUtil.variableToVertex(u1.treatment));
                Vertex vertex3 = new Vertex(CausalModelUtil.variableToVertex(u2.outcome));
                VertexPair v1v3 = new VertexPair(vertex1, vertex3);
                if (this.model.hasEdge(v1v3) && !this.model.commonExistsEffect(vertex1, vertex3)) {
                    //skip if there is an edge between v1 and v3 (not a collider anyway)
                    log.debug("\thas v1v3 edge");
                    continue;
                }

                int u2Hops = u2.treatmentPath.getHopCount();
                if (u1Hops + u2Hops <= 2*this.modelSupport.HOP_THRESHOLD) {
                    //if less than the hop threshold, we previously tested above
                    log.debug("\t" + (u1Hops + u2Hops) + " hops <= " + (2*this.modelSupport.HOP_THRESHOLD));
                    continue;
                }

                //check if a different unit with v1, v3 lead to v2 separating them
                if (inSepset(v1, v2, v3)) {
                    log.debug("\talready has " + v2 + " in sepset");
                    continue;
                }

                // v1-v3 has not been tested yet, must run new conditional independence test with v2 as conditioning variable

                //New unit will be v1-->v3 conditioned on v2
                int minThresh;
                int maxThresh;

                List<SchemaItem> newTreatmentPath = new ArrayList<SchemaItem>();
                //check if base is relationship with the first entity on u1 treatment path
                //same as the last entity on u2 treatment path
                if (u1.baseItem instanceof Relationship && u1.treatmentPath.getHopCount() > 0 && u2.treatmentPath.getHopCount() > 0
                        && u1.treatmentPath.getPath().get(1).equals(u2.treatmentPath.getPath().get(u2.treatmentPath.getPath().size()-2))) {
                    newTreatmentPath.addAll(u2.treatmentPath.getPath().subList(0, u2.treatmentPath.getPath().size()-1));
                    newTreatmentPath.addAll(u1.treatmentPath.getPath().subList(2, u1.treatmentPath.getPath().size()));
                    log.debug("\thad to modify path because of relationship base");
                }
                else {
                    newTreatmentPath.addAll(u2.treatmentPath.getPath());
                    newTreatmentPath.addAll(u1.treatmentPath.getPath().subList(1, u1.treatmentPath.getPath().size()));
                }
                log.debug("\tnew treatment path " + newTreatmentPath);
                List<SchemaItem> newOutcomePath = u2.outcomePath.getPath();
                SchemaItem baseItem = v3.getSource();
                SchemaItem treatmentItem = v1.getSource();
                String treatment = u1.treatmentPath instanceof StructurePath ? treatmentItem.name : v1.name();
                String outcome = u2.outcomePath instanceof StructurePath ? baseItem.name : v3.name();

                minThresh = newTreatmentPath.size()-1;
                maxThresh = minThresh;
                Unit constructedUnit;
                try {
                    constructedUnit = DesignUtil.getUnitUnique(baseItem, treatmentItem, newTreatmentPath,
                            treatment, baseItem, newOutcomePath, Cardinality.ONE, outcome, minThresh, maxThresh);
                    log.debug("\tconstructed unit " + constructedUnit);
                }
                catch(NullPointerException npe) {
                    log.debug("invalid unit for u1 " + u1 + " and u2 " + u2);
                    continue;
                }

                ConditioningSet emptyCS = new ConditioningSet(new ArrayList<Path>());
                //First test marginal dependence
                Aggregator treatmentAgg = u1.treatmentPath.getAggregators().get(0);
                treatmentAgg.resetTableName();
                Aggregator outcomeAgg = u2.outcomePath.getAggregators().get(0);
                outcomeAgg.resetTableName();
                StatisticResults sr = testPair(constructedUnit, treatmentAgg, outcomeAgg, emptyCS);
                log.debug("\tstatistics result " + sr);

                if (sr == null || sr.isFailed()) {
                    continue;
                }

                if (sr.pval > this.signficanceThreshold || sr.phisquare <= this.strengthOfEffectThreshold) {
                    log.debug("\tmarginally independent, do not test conditional");
                    continue;
                }

                //If marginally independent, v2 should not be added to sepset
                //So, only conduct conditional independence test if marginally dependent
                //construct conditioning set from v2
                List<Path> conditioning = new ArrayList<Path>();
                Path control = u2.treatmentPath;
                conditioning.add(control);
                ConditioningSet cs = new ConditioningSet(conditioning);
                NopAggregator nopAgg = new NopAggregator(control.getVariable());
                if (control instanceof AttributePath) {
                    cs.setConditioningAggregate(control, new ModeAggregator(nopAgg));
                }
                else { //structural
                    cs.setConditioningAggregate(control, new CountAggregator(nopAgg));
                }
                log.debug("\tconditioning set " + cs);

                sr = testPair(constructedUnit, treatmentAgg, outcomeAgg, cs);
                log.debug("\tstatistics result " + sr);

                if (sr == null || sr.isFailed()) {
                    continue;
                }

                if (sr.pval > this.signficanceThreshold || sr.phisquare <= this.strengthOfEffectThreshold) {
                    this.constraints.add(constructedUnit.treatmentPath + " _||_ " + constructedUnit.outcomePath + " | " + cs);
                    this.sepsets.put(constructedUnit, conditioning);
                    this.sepsets.get(constructedUnit).addAll(UnitUtil.getRelationshipPathsOnUnit(constructedUnit));
                    log.debug("Found missed conditional independence for " + constructedUnit + ", with " + this.sepsets.get(constructedUnit));
                }
            }
        }

        log.debug("Checking v1<--v2-->v3 cases");
        //Now check potential v1<--v2-->v3 cases
        for (Unit u1 : this.dependentUnits) {
            Variable v2 = u1.treatment;
            Variable v1 = u1.outcome;
            int u1Hops = u1.treatmentPath.getHopCount();
            for (Unit u2 : this.dependentUnits) {
                Variable v3 = u2.outcome;
                if (! u2.treatment.equals(v2) || v1.equals(v3)) {
                    continue;
                }

                //v1, v2, v3 are distinct variables at this point
                log.debug("Checking u1 " + u1 + ", u2 " + u2);

                Vertex vertex1 = new Vertex(CausalModelUtil.variableToVertex(u1.outcome));
                Vertex vertex3 = new Vertex(CausalModelUtil.variableToVertex(u2.outcome));
                VertexPair v1v3 = new VertexPair(vertex1, vertex3);
                if (this.model.hasEdge(v1v3) && !this.model.commonExistsEffect(vertex1, vertex3)) {
                    //skip if there is an edge between v1 and v3 (not a collider anyway)
                    log.debug("\thas v1v3 edge");
                    continue;
                }

                int u2Hops = u2.treatmentPath.getHopCount();
                if (u1Hops + u2Hops <= 2*this.modelSupport.HOP_THRESHOLD) {
                    //if less than the hop threshold, we previously tested above
                    log.debug("\t" + (u1Hops + u2Hops) + " hops <= " + (2*this.modelSupport.HOP_THRESHOLD));
                    continue;
                }

                //check if a different unit with v1, v3 lead to v2 separating them
                if (inSepset(v1, v2, v3)) {
                    log.debug("\talready has " + v2 + " in sepset");
                    continue;
                }

                // v1-v3 has not been tested yet, must run new conditional independence test with v2 as conditioning variable

                //New unit will be v1-->v3 conditioned on v2
                Unit constructedUnit;
                try {
                    Path reversePath1;
                    SchemaItem baseItem = v2.getSource();
                    SchemaItem targetItem1 = v1.getSource();
                    List<SchemaItem> path1 = new ArrayList<SchemaItem>(u1.treatmentPath.getPath());
                    Collections.reverse(path1);
                    if (u1.outcomePath instanceof StructurePath) {
                        reversePath1 = new StructurePath(baseItem, targetItem1, path1, u1.treatmentPath.getCardinality());
                    }
                    else {
                        reversePath1 = new AttributePath(baseItem, targetItem1, path1,
                                u1.treatmentPath.getCardinality(), ((AttributeVariable) u1.outcome).getAttribute());
                    }
                    Path reversePath2;
                    SchemaItem targetItem2 = v3.getSource();
                    List<SchemaItem> path2 = new ArrayList<SchemaItem>(u2.treatmentPath.getPath());
                    Collections.reverse(path2);
                    if (u2.outcomePath instanceof StructurePath) {
                        reversePath2 = new StructurePath(baseItem, targetItem2, path2, u2.treatmentPath.getCardinality());
                    }
                    else {
                        reversePath2 = new AttributePath(baseItem, targetItem2, path2,
                                u2.treatmentPath.getCardinality(), ((AttributeVariable) u2.outcome).getAttribute());
                    }
                    Unit mergedUnit = new Unit(baseItem, reversePath1, reversePath2);
                    log.debug("\tmerged unit " + mergedUnit);
                    constructedUnit = DesignUtil.standardize(mergedUnit);
                    log.debug("\tconstructed unit " + constructedUnit);
                    if (constructedUnit == null) {
                        throw new NullPointerException();
                    }
                }
                catch(NullPointerException npe) {
                    log.debug("invalid unit for u1 " + u1 + " and u2 " + u2);
                    continue;
                }

                ConditioningSet emptyCS = new ConditioningSet(new ArrayList<Path>());
                //First test marginal dependence
                Aggregator treatmentAgg = constructedUnit.treatmentPath.getAggregators().get(0);
                treatmentAgg.resetTableName();
                Aggregator outcomeAgg = constructedUnit.outcomePath.getAggregators().get(0);
                outcomeAgg.resetTableName();
                StatisticResults sr = testPair(constructedUnit, treatmentAgg, outcomeAgg, emptyCS);
                log.debug("\tstatistics result " + sr);

                if (sr == null || sr.isFailed()) {
                    continue;
                }

                if (sr.pval > this.signficanceThreshold || sr.phisquare <= this.strengthOfEffectThreshold) {
                    log.debug("\tmarginally independent, do not test conditional");
                    continue;
                }

                //If marginally independent, v2 should not be added to sepset
                //So, only conduct conditional independence test if marginally dependent
                //construct conditioning set from v2
                List<Path> conditioning = new ArrayList<Path>();
                Path control = u2.treatmentPath;
                conditioning.add(control);
                ConditioningSet cs = new ConditioningSet(conditioning);
                NopAggregator nopAgg = new NopAggregator(control.getVariable());
                if (control instanceof AttributePath) {
                    cs.setConditioningAggregate(control, new ModeAggregator(nopAgg));
                }
                else { //structural
                    cs.setConditioningAggregate(control, new CountAggregator(nopAgg));
                }
                log.debug("\tconditioning set " + cs);

                sr = testPair(constructedUnit, treatmentAgg, outcomeAgg, cs);
                log.debug("\tstatistics result " + sr);

                if (sr == null || sr.isFailed()) {
                    continue;
                }

                if (sr.pval > this.signficanceThreshold || sr.phisquare <= this.strengthOfEffectThreshold) {
                    this.constraints.add(constructedUnit.treatmentPath + " _||_ " + constructedUnit.outcomePath + " | " + cs);
                    this.sepsets.put(constructedUnit, conditioning);
                    this.sepsets.get(constructedUnit).addAll(UnitUtil.getRelationshipPathsOnUnit(constructedUnit));
                    log.debug("Found missed conditional independence for " + constructedUnit + ", with " + this.sepsets.get(constructedUnit));
                }
            }
        }


    }    

    private boolean inSepset(Variable v1, Variable v2, Variable v3) {
        //checks if v2 in sepset(v1, v3)
        boolean foundSep = false;
        for (Unit u3 : this.sepsets.keySet()) {
            if ((u3.treatment.equals(v1) && u3.outcome.equals(v3)) ||
                (u3.treatment.equals(v3) && u3.outcome.equals(v1)) ) {

                for (Path p: this.sepsets.get(u3)) {
                    if (p.getVariable().equals(v2)) {
                        foundSep = true;
                        break;
                    }
                }

                if (foundSep) {
                    break;
                }
            }
        }
        return foundSep;
    }

    /**
     * Run Phase II of Relational PC to orient edges that are consistent with the patterns of association
     * and conditional independencies found in Phase I.
     */
    public void orientEdges() {
        log.info("Orienting edges...");
        Map<VertexPair, Set<Vertex>> flatSepsets = flattenSepsets();

        //get the set of all vertices in the abstracted model
        Set<Vertex> vertices = new HashSet<Vertex>();
        for (VertexPair vp : this.model.getAllDependencies().keySet()) {
            vertices.add(vp.v1);
            vertices.add(vp.v2);
        }
        for (VertexPair vp : flatSepsets.keySet()) {
            vertices.add(vp.v1);
            vertices.add(vp.v2);
        }

        //Add trivial dependencies from each relationship to its attributes
        for (Relationship r : Schema.getAllRelationships()) {
            if (vertices.contains(new Vertex(CausalModelUtil.relationshipToVertex(r)))) {
                Set<Attribute> relAttrs = r.getAllAttributes();
                for (Attribute relAttr : relAttrs) {
                    if (relAttr.isForeignKey() || relAttr.isPrimaryKey()) {
                        continue;
                    }
                    Vertex attrVertex = new Vertex(r.name + "." + relAttr.name);
                    if (vertices.contains(attrVertex)) {
                        this.model.addTrivialDependence(new VertexPair(
                                new Vertex(CausalModelUtil.relationshipToVertex(r)), attrVertex));
                    }
                }
            }
        }

        log.debug(this.model);

        boolean changed = true;
        //while changes in model
        while (changed) {
            //1. run collider detection
            ColliderDetection cd = new ColliderDetection(vertices, this.model, flatSepsets);
            boolean cdChanged = cd.orient();

            //2. run restricted existence models
            RestrictedExistenceModels rem = new RestrictedExistenceModels(vertices, this.model, flatSepsets);
            boolean remChanged = rem.orient();

            //3. run known non-collider detection
            KnownNonColliders knc = new KnownNonColliders(vertices, this.model, flatSepsets);
            boolean kncChanged = knc.orient();

            //4. run cycle avoidance
            CycleAvoidance ca = new CycleAvoidance(vertices, this.model, flatSepsets);
            boolean caChanged = ca.orient();

            changed = cdChanged || kncChanged || caChanged || remChanged;
        }
    }

    private Map<VertexPair, Set<Vertex>> flattenSepsets() {
        Map<VertexPair, Set<Vertex>> flatSepsets = new HashMap<VertexPair, Set<Vertex>>();

        //go through found independencies (marginal and conditional)
        //and uses canonical versions of treatments, outcomes, and conditioning variables in the sepsets
        //
        //NB: If a pair is found to be dependent (i.e., the vertex pair exists in the current
        //    undirected model, then that takes precedence it should not appear in the list of independencies
        //    Similarly, conditional independence takes precedence over marginal independence to ensure
        //    the strongest guarantees about causality
        for (Unit u : this.sepsets.keySet()) {
            VertexPair vp = new VertexPair(new Vertex(CausalModelUtil.variableToVertex(u.treatment)),
                    new Vertex(CausalModelUtil.variableToVertex(u.outcome)));
            VertexPair reverseVP = vp.reverse();

            if (this.model.hasDependence(vp) || this.model.hasDependence(reverseVP)) {
                continue;
            }
            Set<Vertex> vertexSepset = new HashSet<Vertex>();
            for (Path p : this.sepsets.get(u)) {
                vertexSepset.add(new Vertex(CausalModelUtil.variableToVertex(p.getVariable())));
            }

            if (! flatSepsets.containsKey(vp)) {
                flatSepsets.put(vp, new HashSet<Vertex>());
            }

            if (! flatSepsets.containsKey(reverseVP)) {
                flatSepsets.put(reverseVP, new HashSet<Vertex>());
            }

            flatSepsets.get(vp).addAll(vertexSepset);
            flatSepsets.get(reverseVP).addAll(vertexSepset);
        }

        return flatSepsets;
    }
}