/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 9, 2009
 * Time: 2:56:48 PM
 */
package rpc.datagen;

import rpc.design.*;
import rpc.schema.*;
import rpc.util.UnitUtil;
import rpc.util.PrologUtil;
import rpc.util.CausalModelUtil;
import rpc.util.PythonUtil;
import rpc.model.util.Model;
import rpc.model.util.VertexPair;

import java.util.*;

import jpl.Query;
import jpl.Term;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.dag.Vertex;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.apache.log4j.Logger;
import org.python.core.PyDictionary;

/**
 * The Causal Model Generator generates a random causal model over the current schema with a pre-specified
 * number of dependencies to add.  The generator ensures that the model will be a valid causal model
 * (e.g., it will not contain cyclic dependencies).
 */
public class CausalModelGenerator {

    private static Logger log = Logger.getLogger(CausalModelGenerator.class);

    private int hop_threshold;
    private int max_indegree;
    private Set<Unit> dependencies;
    private Map<String, List<Unit>> vertexToParents;
    private DAG dag;

    private List<Vertex> vertices;
    private List<Vertex> visited;

    /**
     * Create a random causal model with a given number of dependencies.
     * @param numDependencies the number of dependencies to choose for the causal model.
     * @param hop_threshold the hop threshold over the schema governs the set of possible dependencies.
     * @param max_indegree the maximum in-degree (i.e., number of parents) for any variable.
     */
    public CausalModelGenerator(int numDependencies, int hop_threshold, int max_indegree) {
        this.hop_threshold = hop_threshold;
        this.max_indegree = max_indegree;

        int numAttempts = 0;
        boolean notGenerated = true;
        while (notGenerated && numAttempts < 20) {
            //initialize dag with all vertices (each possible variable)
            log.debug("Building causal model with " + numDependencies + " dependencies");
            this.dependencies = new HashSet<Unit>();
            this.vertexToParents = new HashMap<String, List<Unit>>();            
            this.dag = new DAG();
            List<Unit> units = UnitUtil.getUniqueUnits(this.hop_threshold);
            for (Unit u : units) {
                this.dag.addVertex(CausalModelUtil.variableToVertex(u.outcome));
            }

            addTrivialDependencies();

            //find random dependencies to fill out DAG
            int ctrPerTry = 0;
            while (this.dependencies.size() < numDependencies) {
                addDependence(getRandomUnit());
                ctrPerTry++;
                if (ctrPerTry >= 500) {
                    numAttempts++;
                    break;
                }
            }

            if (this.dependencies.size() == numDependencies) {
                notGenerated = false;
            }
        }

        if (notGenerated) {
            //tried too many times
            log.error("Attempted to generate causal model " + numAttempts + " times without success. " +
                    "Try reducing number of dependencies.");
            throw new NullPointerException();
        }

        setVertices();
        for (Vertex vertex : this.vertices) {
            log.debug(vertex);
            log.debug("\tParents: " + vertex.getParents());
            log.debug("\tChildren: " + vertex.getChildren());
            log.debug("");
        }

    }

    private void addTrivialDependencies() {
        //add trivial dependencies from relationships to attributes on that relationship in order to prevent
        //cycles that would require the attribute to come before the relationship
        for (Relationship r : Schema.getAllRelationships()) {
            Set<Attribute> relAttrs = r.getAllAttributes();
            for (Attribute relAttr : relAttrs) {
                if (relAttr.isForeignKey() || relAttr.isPrimaryKey()) {
                    continue;
                }
                String attrVertexName = r.name + "." + relAttr.name;
                try {

                    String intermediateVertex = "trivial:" + r.name + ":" + attrVertexName;
                    this.dag.addVertex(intermediateVertex);
                    this.dag.addEdge(r.name, intermediateVertex);
                    this.dag.addEdge(intermediateVertex, attrVertexName);
                    log.debug("Added edge from " + r.name + " to " + attrVertexName);
                }
                catch (CycleDetectedException e) {
                    log.warn("Cycle detected while adding trivial dependence from relationship " +
                            r.name + " to attribute " + attrVertexName);
                }
            }
        }
    }

    private void setVertices() {
        this.vertices = new ArrayList<Vertex>();
        this.visited = new ArrayList<Vertex>();

        List vertices = this.dag.getVerticies();
        for (Object v : vertices) {
            Vertex vertex = (Vertex) v;
            this.vertices.add(vertex);
        }
    }

    private boolean addDependence(Unit u) {
        //skip units that are known to be constant (e.g., treatment structure paths that have a one cardinality)
        if (u.treatmentPath.getCardinality().equals(Cardinality.ONE) && u.treatmentPath instanceof StructurePath){
            return false;
        }

        //skip units that are known to be constant (e.g., causing the structure of a relationship
        //in direction of a one cardinality)
        if (u.outcomePath instanceof StructurePath) {
            Entity e = (Entity) u.treatmentPath.getPath().get(1);
            Relationship r = (Relationship) u.outcomePath.getBaseItem();
            Cardinality cardDir = e.equals(r.entity1) ? r.entity2Card : r.entity1Card;
            if (cardDir.equals(Cardinality.ONE)) {
                return false;
            }
        }

        if (u.outcomePath instanceof StructurePath && relationshipUsedAncestrally(u)) {
            log.debug("Skipping dependence since relationship used ancestrally...");
            return false;
        }

        //skip units that would make a relationship in a path be required for to create itself (different kind of cycle)
        if (createStructureConflict(u)) {
            log.debug("Skipping dependence that creates a conflict...");
            return false;
        }

        if (! this.dependencies.contains(u)) {
            String treatmentVertex = CausalModelUtil.variableToVertex(u.treatment);
            String intermediateVertex = variableToHelperVertex(u.treatmentPath);
            String outcomeVertex = CausalModelUtil.variableToVertex(u.outcome);

            //limit the fan-in of each variable
            if (Schema.isRelationship(u.outcomePath.getBaseItem().name) && u.outcomePath instanceof AttributePath) {
                if (this.dag.getParentLabels(outcomeVertex).size() >= max_indegree + 1) {
                    return false;
                }
            }
            else {
                if (this.dag.getParentLabels(outcomeVertex).size() >= max_indegree) {
                    return false;
                }
            }

            try {
                this.dag.addVertex(intermediateVertex);
                this.dag.addEdge(treatmentVertex, intermediateVertex);
                this.dag.addEdge(intermediateVertex, outcomeVertex);
                this.dependencies.add(u);
                if (! this.vertexToParents.containsKey(outcomeVertex)) {
                    this.vertexToParents.put(outcomeVertex, new ArrayList<Unit>());
                }
                this.vertexToParents.get(outcomeVertex).add(u);
                return true;
            }
            catch (CycleDetectedException e) {
                log.debug("Skipping dependence that creates a cycle...");
                return false;
            }
        }
        else {
            return false;
        }
    }

    private Unit getRandomUnit() {
        String query = String.format("getRandomUnit(BaseItem, Target1, Path1, Card1, Var1, Target2, Path2, " +
                "Card2, Var2, 0, %s)", this.hop_threshold*2);
        Hashtable solution = Query.oneSolution(query);
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

            return new Unit(baseItem, treatmentPath, outcomePath);
    }

    /**
     * Checks whether or not there is any remaining dependencies to return for the iterator.
     * @return true if there are dependencies/vertices that have not yet been returned; false if done.
     */
    public boolean hasNextDependency() {
        return this.vertices.size() > 0;
    }

    /**
     * If there is another dependency remaining, return the next one for which all parents have already been
     * returned.  This provides a partial ordering over the dependencies.
     * @return a vertex data object that contains all necessary information to generate that particular variable.
     */
    public VertexData getNextDependency() {
        String curVertex = "";
        List<Unit> curParents = new ArrayList<Unit>();
        for (Vertex vertex : this.vertices) {
            List parents = vertex.getParents();
            if (this.visited.containsAll(parents) && !necessaryStructureRemaining(vertex.getLabel())) {
                this.vertices.remove(vertex);
                this.vertices.removeAll(vertex.getChildren());
                this.visited.add(vertex);
                for (Object child : vertex.getChildren()) {
                    this.visited.add((Vertex) child);
                }
                curVertex = vertex.getLabel();
                curParents = this.vertexToParents.get(vertex.getLabel());
                break;
            }
        }
        return new VertexData(new rpc.model.util.Vertex(curVertex), curParents);
    }

    /**
     * Reset the iterator to start from the beginning.
     */
    public void resetIterator() {
        setVertices();        
    }

    /**
     * Get the number of dependencies in the causal model.
     * @return the number of dependencies in the causal model.
     */
    public int getNumDependencies() {
        return this.dependencies.size();
    }

    private String variableToHelperVertex(Path p) {
        return p.getPathNames().toString() + "." + p.getVariable().name();
    }

    private boolean createStructureConflict(Unit u) {
        //System.out.println("Checking conflict for " + u + "...");
        //check each relationship in treatment path, outcome path and through ancestors
        //and make sure that adding this edge to the DAG won't put any of the relationships as an ancestor of itself
        //just need to check if the outcome vertex is an ancestor of any of the relationships
        Vertex outcomeVertex = this.dag.getVertex(CausalModelUtil.variableToVertex(u.outcome));

        Set<String> relationships = new HashSet<String>();
        for (SchemaItem si : u.treatmentPath.getPath()) {
            if (Schema.isRelationship(si.name)) {
                relationships.add(si.name);
            }
        }
        for (SchemaItem si : u.outcomePath.getPath()) {
            if (Schema.isRelationship(si.name)) {
                relationships.add(si.name);
            }
        }

        Set<Vertex> treatmentAncestors = getAncestors(dag.getVertex(CausalModelUtil.variableToVertex(u.treatment)));
        treatmentAncestors.add(dag.getVertex(CausalModelUtil.variableToVertex(u.treatment)));
        //log.debug("\tTreatment ancestors: " + treatmentAncestors);
        for (Vertex ancestor : treatmentAncestors) {
            List<Unit> depUnits = this.vertexToParents.get(ancestor.getLabel());
            if (depUnits != null) {
                for (Unit depUnit : depUnits) {
                    for (SchemaItem si : depUnit.treatmentPath.getPath()) {
                        if (Schema.isRelationship(si.name)) {
                            relationships.add(si.name);
                        }
                    }
                    for (SchemaItem si : depUnit.outcomePath.getPath()) {
                        if (Schema.isRelationship(si.name)) {
                            relationships.add(si.name);
                        }
                    }
                }
            }
        }        

        //log.debug("\tRelationships: " + relationships);

        for (String relationship : relationships) {
            Vertex v = this.dag.getVertex(relationship);
            Set<Vertex> ancestors = getAncestors(v);
           // log.debug("Ancestors of " + relationship + ": " + ancestors);
            if (ancestors.contains(outcomeVertex)) {
                return true;
            }
        }
        return false;
    }

    private Set<Vertex> getAncestors(Vertex v) {
        Set<Vertex> ancestors = new HashSet<Vertex>();
        List parents = v.getParents();
        for (Object parent : parents) {
            ancestors.add((Vertex) parent);
            ancestors.addAll(getAncestors((Vertex) parent));
        }        
        return ancestors;
    }

    private boolean relationshipUsedAncestrally(Unit u) {
        //log.debug("Checking if relationship is used ancestrally for unit " + u);
        String relName = CausalModelUtil.variableToVertex(u.outcome);
        Set<Vertex> treatmentAncestors = getAncestors(dag.getVertex(CausalModelUtil.variableToVertex(u.treatment)));
        treatmentAncestors.add(dag.getVertex(CausalModelUtil.variableToVertex(u.treatment)));
        //log.debug("\tTreatment ancestors: " + treatmentAncestors);
        for (Vertex ancestor : treatmentAncestors) {
            List<Unit> depUnits = this.vertexToParents.get(ancestor.getLabel());
            if (depUnits != null) {
                for (Unit depUnit : depUnits) {
                    if (isRelationshipUsed(relName, depUnit)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isRelationshipUsed(String relationshipName, Unit u) {
        return u.treatmentPath.getPathNames().contains(relationshipName) ||
                u.outcomePath.getPathNames().contains(relationshipName);
    }

    private boolean necessaryStructureRemaining(String vertex) {
        //checks the remaining vertices that have yet to be returned
        //to see if the current vertex requires any of them to be generated first in terms of structure
        List<Unit> parents = this.vertexToParents.get(vertex);
        if (parents == null) {
            return false;
        }
        //get all the relationships involved in the paths to generate current vertex
        Set<String> relationships = new HashSet<String>();
        for (Unit u : parents) {
            for (SchemaItem si : u.treatmentPath.getPath()) {
                if (Schema.isRelationship(si.name)) {
                    relationships.add(si.name);
                }
            }
            for (SchemaItem si : u.outcomePath.getPath()) {
                if (Schema.isRelationship(si.name)) {
                    relationships.add(si.name);
                }
            }
        }
        if (relationships.size() <= 0) {
            return false;
        }
        relationships.remove(vertex); //remove current vertex if we're trying to generate it
        //if any remaining relationship hasn't been generated yet, then we need to
        for (String relationship : relationships) {
            if (this.vertices.contains(this.dag.getVertex(relationship))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the model object that has been generated.
     * @return the generated causal model.
     */
    public Model getModel() {
        Model model = new Model();
        for (Unit u : this.dependencies) {
            rpc.model.util.Vertex v1 = new rpc.model.util.Vertex(CausalModelUtil.variableToVertex(u.treatment));
            rpc.model.util.Vertex v2 = new rpc.model.util.Vertex(CausalModelUtil.variableToVertex(u.outcome));
            VertexPair vp = new VertexPair(v1, v2);
            model.addDependence(vp, u);
        }

        return model;
    }

    /**
     * Jython overload: Get a random parameterization over the causal model structure.
     * @param ratios Python dictionary containing the ratios of entity table sizes for each pair of entities
     * involved in all relationships.
     * @return a parameterization over the causal model structure.
     */
    public Parameterization parameterize(PyDictionary ratios) {
        return parameterize(PythonUtil.mapFromStringDoublePyDict(ratios));
    }

    /**
     * Get a random parameterization over the causal model structure.
     * @param ratios the ratios of entity table sizes for each pair of entities involved in all relationships.
     * @return a parameterization over the causal model structure.
     */
    public Parameterization parameterize(HashMap<String, Double> ratios) {
        Parameterization p = new Parameterization();

        resetIterator();
        while (hasNextDependency()) {
            VertexData vData = getNextDependency();
            if (vData.getParents() == null) {
                if (vData.isAttribute()) {
                    log.debug("Generating parameters for " + vData.getBaseTable()  + "." + vData.getAttribute());
                    HashMap<Integer, Double> coinFlip = new HashMap<Integer, Double>();
                    //attributes with no parents have a 50-50 prior
                    coinFlip.put(0, 0.5);
                    coinFlip.put(1, 0.5);
                    CPT cpt = new CPTPrior(coinFlip);
                    log.debug(cpt);
                    p.setCPT(vData, cpt);
                }
                else {
                    log.debug("Generating parameters for " + vData.getBaseTable() + "." + vData.getAttribute());
                    //structure with no parents are generated from a Poisson distribution with mean based on the ratio
                    //of its connected entities
                    p.setConnector(vData, vData.getConnector(ratios.get(vData.getBaseTable())));
                }
            }
            else { //has parents
                if (vData.isAttribute()) {
                    log.debug("Generating parameters for " + vData.getBaseTable() + "." + vData.getAttribute());

                    //get the list of values for each parent
                    //need to duplicate lists with null value to support missing parent values
                    List<List<Object>> parentDomains = new ArrayList<List<Object>>();
                    List<List<Object>> parentDomainsWithNull = new ArrayList<List<Object>>();
                    for (Unit parent : vData.getParents()) {
                        List<Object> dom = vData.getParentDomainDefaults(parent);
                        parentDomains.add(dom);
                        List<Object> copyDom = new ArrayList<Object>(dom);
                        copyDom.add(null);
                        parentDomainsWithNull.add(copyDom);
                    }

                    //parent configurations to probability tables
                    HashMap<List<Object>, HashMap<Integer, Double>> cptParentTable =
                            new HashMap<List<Object>, HashMap<Integer, Double>>();

                    //generate all possible parent configurations
                    List<List<Object>> parentConfigs = CausalModelUtil.crossList(parentDomainsWithNull);

                    //Construct probability distributions for each setting of parents without missing values
                    for (List<Object> parentConfig : parentConfigs) {
                        if (!parentConfig.contains(null)) {
                            double[] probs = CausalModelUtil.uniformDirichlet(2);
                            //the following is used to try to limit extreme probabilities
                            while ((probs[0] > 0.48 && probs[0] < 0.52) || probs[0] > 0.92 || probs[0] < 0.08) {
                                probs = CausalModelUtil.uniformDirichlet(2);
                            }
                            HashMap<Integer, Double> table = new HashMap<Integer, Double>();
                            table.put(0, probs[0]);
                            table.put(1, probs[1]);
                            cptParentTable.put(parentConfig, table);
                        }
                    }

                    //Construct marginalized/averaged rows for entries that have a null value
                    for (List<Object> parentConfig : parentConfigs) {
                        if (parentConfig.contains(null)) {
                            List<List<Object>> nullParentDomains = new ArrayList<List<Object>>();
                            for (int i=0; i<parentConfig.size(); i++) {
                                if (parentConfig.get(i) == null) {
                                    nullParentDomains.add(parentDomains.get(i));
                                }
                                else {
                                    List<Object> nonNullSingleton = new ArrayList<Object>();
                                    nonNullSingleton.add(parentConfig.get(i));
                                    nullParentDomains.add(nonNullSingleton);
                                }
                            }

                            
                            HashMap<Integer, Double> valToTotalProbs = new HashMap<Integer, Double>();
                            List<List<Object>> nullParentConfigs = CausalModelUtil.crossList(nullParentDomains);
                            for (List<Object> nullParentConfig : nullParentConfigs) {
                                HashMap<Integer, Double> valToProbs = cptParentTable.get(nullParentConfig);
                                for (Integer val : valToProbs.keySet()) {
                                    if (!valToTotalProbs.containsKey(val)) {
                                        valToTotalProbs.put(val, 0.0);
                                    }
                                    valToTotalProbs.put(val, valToTotalProbs.get(val) + valToProbs.get(val));
                                }
                            }

                            HashMap<Integer, Double> valToAverageProbs = new HashMap<Integer, Double>();

                            for (Integer val : valToTotalProbs.keySet()) {
                                valToAverageProbs.put(val, 1.0*valToTotalProbs.get(val)/nullParentConfigs.size());
                            }

                            cptParentTable.put(parentConfig, valToAverageProbs);
                        }
                    }

                    //parent configurations to CPTPrior
                    HashMap<List<Object>, CPTPrior> parentsToCPTs = new HashMap<List<Object>, CPTPrior>();
                    for (List<Object> parentConfig : cptParentTable.keySet()) {
                        parentsToCPTs.put(parentConfig, new CPTPrior(cptParentTable.get(parentConfig)));
                    }

                    CPTParent cptParent = new CPTParent(parentsToCPTs);
                    log.debug(cptParent);
                    p.setCPT(vData, cptParent);
                }
                else {
                    log.debug("Generating parameters for " + vData.getBaseTable() + "." + vData.getAttribute());
                    //get the list of values for each parent
                    //need to duplicate lists with null value to support missing parent values
                    List<List<Object>> parentDomains = new ArrayList<List<Object>>();
                    List<List<Object>> parentDomainsWithNull = new ArrayList<List<Object>>();
                    for (Unit parent : vData.getParents()) {
                        List<Object> dom = vData.getParentDomainDefaults(parent);
                        parentDomains.add(dom);
                        List<Object> copyDom = new ArrayList<Object>(dom);
                        copyDom.add(null);
                        parentDomainsWithNull.add(copyDom);
                    }

                    //parent configurations to probability tables
                    Map<List<Object>, Double> parentLinkMeans =
                            new HashMap<List<Object>, Double>();

                    //generate all possible parent configurations
                    List<List<Object>> parentConfigs = CausalModelUtil.crossList(parentDomainsWithNull);

                    //Construct probability distributions for each setting of parents without missing values
                    for (List<Object> parentConfig : parentConfigs) {
                        if (!parentConfig.contains(null)) {
							double paretoLinks = CausalModelUtil.boundedPareto();
                            parentLinkMeans.put(parentConfig, paretoLinks);
                        }
                    }


                    //Construct marginalized/averaged rows for entries that have a null value
                    for (List<Object> parentConfig : parentConfigs) {
                        if (parentConfig.contains(null)) {
                            List<List<Object>> nullParentDomains = new ArrayList<List<Object>>();
                            for (int i=0; i<parentConfig.size(); i++) {
                                if (parentConfig.get(i) == null) {
                                    nullParentDomains.add(parentDomains.get(i));
                                }
                                else {
                                    List<Object> nonNullSingleton = new ArrayList<Object>();
                                    nonNullSingleton.add(parentConfig.get(i));
                                    nullParentDomains.add(nonNullSingleton);
                                }
                            }

                            double totalLinkMean = 0.0;
                            List<List<Object>> nullParentConfigs = CausalModelUtil.crossList(nullParentDomains);
                            for (List<Object> nullParentConfig : nullParentConfigs) {
                                totalLinkMean += parentLinkMeans.get(nullParentConfig);
                            }

                            double averageLinkMean = totalLinkMean/nullParentConfigs.size();

                            parentLinkMeans.put(parentConfig, averageLinkMean);
                        }
                    }
                    p.setLinkMean(vData, parentLinkMeans);
                }
            }
        }

        return p;
    }
}