/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 11, 2010
 * Time: 3:42:28 PM
 */
package rpc.model;

import rpc.model.util.*;
import rpc.model.edgeorientation.*;
import rpc.design.Unit;
import rpc.util.UnitUtil;
import rpc.util.CausalModelUtil;
import rpc.schema.Schema;
import rpc.schema.Relationship;
import rpc.schema.Attribute;

import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * The Oracle PC algorithm for learning partially directed causal models of relational domains.
 * The oracle consists of two separate phases: skeleton identification and edge orientation.  However,
 * the oracle is given the true model so that perfect information about the dependencies and
 * conditional independencies are given to the edge orientation phase.  This is useful to ascertain
 * upper bounds on recall for orienting edges.  The oracle does not access the database.
 */
public class OraclePC {

    private static Logger log = Logger.getLogger(OraclePC.class);

    //Needs access to schema, all units, unique units, causal model (possible causes and priors)
    private ModelSupport modelSupport;
    private Set<Vertex> vertices;

    private Model model;
    private Model trueModel;

    /**
     * List of edge orientation rules to use if testing with attached order
     */
    private List<String> rules;

    /**
     * Initializes the Oracle PC object.
     * @param modelSupport data structure containing set of all units, dependencies, and potential causes.
     * @param trueModel the true model containing the true dependencies.
     */
    public OraclePC(ModelSupport modelSupport, Model trueModel) {
        this.modelSupport = modelSupport;
        this.model = new Model();
        List<Unit> units = modelSupport.getAllUnits();
        int numPossibleDependencies = units.size();
        this.model.setNumPossibleDependencies(numPossibleDependencies);
        this.trueModel = trueModel;

        this.rules = new ArrayList<String>();

        //get the set of all vertices in the abstracted model
        this.vertices = new HashSet<Vertex>();
        for (Unit u : units) {
            this.vertices.add(new Vertex(CausalModelUtil.variableToVertex(u.treatment)));
            this.vertices.add(new Vertex(CausalModelUtil.variableToVertex(u.outcome)));
        }
    }

    /**
     * The first phase identifies the skeleton by taking the true causal model and rendering each
     * edge as undirected.
     */
    public void identifySkeleton() {
        Map<VertexPair, Set<Dependency>> trueDependencies = this.trueModel.getAllDependencies();
        for (VertexPair vp : trueDependencies.keySet()) {
            for (Dependency dep : this.trueModel.getDependencies(vp)){
                this.model.addDependence(vp, dep);

                //and reverse since undirected (might be repeating efforts...)
                Dependency reverseDep = dep.reverse();
                if (reverseDep != null) {
                    this.model.addDependence(vp.reverse(), reverseDep);
                }
            }
        }

        //add in extra edge when two variables have a common effect of link existence
        for (Vertex v1 : this.vertices) {

            Set<Vertex> neighbors = this.trueModel.getChildren(v1);
            for (Vertex v2 : neighbors) {

                if (! v2.isExistence()) {
                    continue;
                }

                Set<Vertex> nextNeighbors = this.trueModel.getParents(v2);
                for (Vertex v3 : nextNeighbors) {
                    if (!v3.equals(v1) ){
                        VertexPair v1v3 = new VertexPair(v1, v3);

                        if (this.model.hasDependence(v1v3)) {
                            continue;
                        }

                        Unit nonNullUnit = new Unit(v1, v3);
                        Dependency dep = new Dependency(v1, v3);
                        dep.unit = nonNullUnit;
                        this.model.addDependence(v1v3, dep);

                        VertexPair v3v1 = new VertexPair(v3, v1);
                        Unit nonNullUnitRev = new Unit(v3, v1);
                        Dependency depRev = new Dependency(v3, v1);
                        depRev.unit = nonNullUnitRev;
                        this.model.addDependence(v3v1, depRev);
                    }
                }
            }
        }
    }

    /**
     * Get the current version of the learned model.
     * @return the currently learned model.
     */
    public Model getModel() {
        return this.model;
    }

    /**
     * Run Phase II of Oracle PC to orient edges that are consistent with the true patterns of association
     * and conditional independencies encoded by the true model.
     */
    public void orientEdges(boolean withREM) {
        log.info("Orienting edges...");
        Map<VertexPair, Set<Vertex>> sepsets = createSepsets();

        //Add trivial dependencies from each relationship to its attributes
        for (Relationship r : Schema.getAllRelationships()) {
            if (this.vertices.contains(new Vertex(CausalModelUtil.relationshipToVertex(r)))) {
                Set<Attribute> relAttrs = r.getAllAttributes();
                for (Attribute relAttr : relAttrs) {
                    if (relAttr.isForeignKey() || relAttr.isPrimaryKey()) {
                        continue;
                    }
                    Vertex attrVertex = new Vertex(r.name + "." + relAttr.name);
                    if (this.vertices.contains(attrVertex)) {
                        this.model.addTrivialDependence(new VertexPair(
                                new Vertex(CausalModelUtil.relationshipToVertex(r)), attrVertex));
                    }
                }
            }
        }

        boolean changed = true;
        //while changes in model
        while (changed) {
            //1. run collider detection
            ColliderDetection cd = new ColliderDetection(this.vertices, this.model, sepsets);
            boolean cdChanged = cd.orient();

            boolean remChanged = false;
            if (withREM) {
                //2. run restricted existence models
                RestrictedExistenceModels rem = new RestrictedExistenceModels(this.vertices, this.model, sepsets);
                remChanged = rem.orient();
            }
            
            //3. run known non-collider detection
            KnownNonColliders knc = new KnownNonColliders(this.vertices, this.model, sepsets);
            boolean kncChanged = knc.orient();

            //4. run cycle avoidance
            CycleAvoidance ca = new CycleAvoidance(this.vertices, this.model, sepsets);
            boolean caChanged = ca.orient();

            changed = cdChanged || kncChanged || caChanged || remChanged;
        }
    }

    /**
     * Runs the edge orientation rules in a pre-specified order to orient edges that are consistent with the
     * true patterns of association and conditional independencies encoded by the true model.
     * Call attachRule() method in the order desired before running orientEdgesOrder().
     */
    public void orientEdgesOrder() {
        log.info("Orienting edges with order...");
        Map<VertexPair, Set<Vertex>> sepsets = createSepsets();

        //Add trivial dependencies from each relationship to its attributes
        for (Relationship r : Schema.getAllRelationships()) {
            if (this.vertices.contains(new Vertex(CausalModelUtil.relationshipToVertex(r)))) {
                Set<Attribute> relAttrs = r.getAllAttributes();
                for (Attribute relAttr : relAttrs) {
                    if (relAttr.isForeignKey() || relAttr.isPrimaryKey()) {
                        continue;
                    }
                    Vertex attrVertex = new Vertex(r.name + "." + relAttr.name);
                    if (this.vertices.contains(attrVertex)) {
                        this.model.addTrivialDependence(new VertexPair(
                                new Vertex(CausalModelUtil.relationshipToVertex(r)), attrVertex));
                    }
                }
            }
        }

        //Iterates through a pre-specified set of class names corresponding to
        //an ordered list of different edge orientation rules
        boolean changed = true;
        Class[] eoClassParams = new Class[]{Set.class, Model.class, Map.class};
        //while changes in model
        while (changed) {

            changed = false;
            for (String eoName : this.rules) {
                try {
                    Class eoClass = Class.forName(eoName);
                    Constructor eoConstructor = eoClass.getConstructor(eoClassParams);
                    Object eo = eoConstructor.newInstance(this.vertices, this.model, sepsets);
                    Method eoOrient = eoClass.getMethod("orient");
                    Object didOrient = eoOrient.invoke(eo);
                    changed = changed || (Boolean) didOrient;
                }
                catch (Exception e) {
                    log.error("edge orientation rule reflection error: " + e);
                }
            }
        }
    }

    /**
     * Attaches an edge orientation rule to be used to orient the model in the specified attaching order.
     * @param eoClassName the class name for an edge orientation rule
     * (e.g., aiq.model.edgeorientation.ColliderDetection).
     */
    public void attachRule(String eoClassName) {
        this.rules.add(eoClassName);
    }

    private Map<VertexPair, Set<Vertex>> createSepsets() {
        Map<VertexPair, Set<Vertex>> sepsets = new HashMap<VertexPair, Set<Vertex>>();

        //first add all implicitly conditioned relationship existence variables from all units
        for (Unit u : this.modelSupport.getAllUnits()) {
            VertexPair vp = new VertexPair(new Vertex(CausalModelUtil.variableToVertex(u.treatment)),
                    new Vertex(CausalModelUtil.variableToVertex(u.outcome)));

            VertexPair reverseVP = vp.reverse();

            if (this.model.hasDependence(vp) || this.model.hasDependence(reverseVP)) {
                continue;
            }

            if (!sepsets.containsKey(vp)) {
                sepsets.put(vp, new HashSet<Vertex>());
            }
            if (!sepsets.containsKey(reverseVP)) {
                sepsets.put(reverseVP, new HashSet<Vertex>());
            }


            for (Vertex v : UnitUtil.getRelationshipsOnUnit(u)) {
                sepsets.get(vp).add(v);
                sepsets.get(reverseVP).add(v);
            }
        }

        //next, identify all triples and if it's not a collider add middle vertex to sepset
        for (Vertex v1 : this.vertices) {

            Set<Vertex> neighbors = this.model.getNeighbors(v1);
            for (Vertex v2 : neighbors) {

                Set<Vertex> nextNeighbors = this.model.getNeighbors(v2);
                for (Vertex v3 : nextNeighbors) {

                    //check if it is a NON-collider in the true model
                    if ( !this.trueModel.hasDependence(new VertexPair(v1, v2)) ||
                         !this.trueModel.hasDependence(new VertexPair(v3, v2))) {

                        //acceptable triple if 3 different vertices with no edge between v1 and v3
                        //Also, skip cases in which v2 and v3 could have a possible common effect of an existence variable
                        if (!v3.equals(v1) && !this.trueModel.hasDependence(new VertexPair(v1, v3)) &&
                                !this.trueModel.hasDependence(new VertexPair(v3, v1))) {
                            VertexPair v1v3 = new VertexPair(v1, v3);
                            if (!sepsets.containsKey(v1v3)) {
                                sepsets.put(v1v3, new HashSet<Vertex>());
                            }
                            if (!sepsets.containsKey(v1v3.reverse())) {
                                sepsets.put(v1v3.reverse(), new HashSet<Vertex>());
                            }

                            sepsets.get(v1v3).add(v2);
                            sepsets.get(v1v3.reverse()).add(v2);
                        }
                    }
                    else {
                        //it IS a collider, so add an empty list as its sepset
                        VertexPair v1v3 = new VertexPair(v1, v3);
                        if (!sepsets.containsKey(v1v3)) {
                            sepsets.put(v1v3, new HashSet<Vertex>());
                        }
                        if (!sepsets.containsKey(v1v3.reverse())) {
                            sepsets.put(v1v3.reverse(), new HashSet<Vertex>());
                        }
                    }
                }
            }
        }

        return sepsets;
    }
}