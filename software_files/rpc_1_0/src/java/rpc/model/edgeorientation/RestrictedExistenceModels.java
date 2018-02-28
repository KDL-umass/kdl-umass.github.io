/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 2, 2010
 * Time: 2:55:25 PM
 */
package rpc.model.edgeorientation;

import rpc.model.util.*;
import rpc.model.scoring.ModelScoring;
import rpc.util.UnitUtil;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The Restricted Existence Models edge orientation rule.  Existence uncertainty over relationships imposes
 * constraints on the space of possible causal models.  These rules are derived from acyclicity (atemporal causal
 * models contain no cycles), existence precondition (X can only have an effect on Y if a relational connection
 * between them exists), and common effects (existence uncertainty can induce a spurious correlation between X and Y).
 */
public class RestrictedExistenceModels extends EdgeOrientation {

    private static Logger log = Logger.getLogger(RestrictedExistenceModels.class);

    /**
     * Initializes the Restricted Existence Models edge orientation rule.
     * @param vertices the set of all vertices in the model.
     * @param model the model containing all dependencies among pairs of vertices.
     * @param sepsets the set of vertices that render pairs of variables conditionally independent.
     */
    public RestrictedExistenceModels(Set<Vertex> vertices, Model model, Map<VertexPair, Set<Vertex>> sepsets) {
        super(vertices, model, sepsets);
    }

    /**
     * Finds and orients all cases where relationship existence leads to constraints on the space of causal models.
     * @return true if any edges were oriented; false otherwise.
     */
    public boolean orient() {
        // find the set of (v1--E--v3) triples within the model
        Set<VertexTriple> triples = new HashSet<VertexTriple>();
        for (Vertex v1 : this.vertices) {
            Set<Vertex> neighbors = this.model.getNeighbors(v1);
            for (Vertex v2 : neighbors) {

                //Need to find all relationships along all paths in the units for
                //each dependency for the given pair of vertices to use as the existence variables
                Set<Dependency> deps = new HashSet<Dependency>();
                Set<Dependency> deps1 = this.model.getDependencies(new VertexPair(v1, v2));
                if (deps1 != null) {
                    deps.addAll(deps1);
                }
                Set<Dependency> deps2 = this.model.getDependencies(new VertexPair(v2, v1));
                if (deps2 != null) {
                    deps.addAll(deps2);
                }

                Set<Vertex> relsAlongPaths = new HashSet<Vertex>();
                for (Dependency dep : deps) {
                    if (!dep.isTrivial() && dep.unit.treatmentPath != null) {
                        relsAlongPaths.addAll(UnitUtil.getRelationshipsOnUnit(dep.unit));
                    }
                }

                for (Vertex existence : relsAlongPaths) {
                    VertexTriple vt = new VertexTriple(v1, existence, v2);
                    //skip triple if all edges are directed
                    if (    this.model.hasDirectedEdge(new VertexPair(v1, existence))
                            &&
                            this.model.hasDirectedEdge(new VertexPair(v2, existence))
                            &&
                            this.model.hasDirectedEdge(new VertexPair(v1, v2))
                        ) {
                        continue;
                    }
                    //add to triples set only once (i.e., v2--existence--v1 isn't already there)
                    if (!triples.contains(vt.reverse())) {
                        triples.add(vt);
                    }
                }
            }
        }

        boolean oriented = false;
        // iterate through the triples and identify restricted models for given structure
        for (VertexTriple vt : triples) {
            log.debug("Checking triple " + vt);
            if (!this.model.hasEdge(new VertexPair(vt.v1, vt.v2)) && !this.model.hasEdge(new VertexPair(vt.v2, vt.v3))) {
                //CASE: v1-----v3
                //
                //
                //         E
                if (this.model.hasDependence(new VertexPair(vt.v1, vt.v3)) &&
                        !this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1---->v3
                    //So, add trivial dependence E--->v3
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v3));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 1 " + vt + " : " + vt.v2 + "-->" + vt.v3 + " (trivial)");
                    ModelScoring.usedRule("REM-TRIV");
                }
                else if (!this.model.hasDependence(new VertexPair(vt.v1, vt.v3)) &&
                        this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1<---v3
                    //So, add trivial dependence E--->v1
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v1));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 2 " + vt + " : " + vt.v2 + "-->" + vt.v1 + " (trivial)");
                    ModelScoring.usedRule("REM-TRIV");
                }
            }
            else if (!this.model.hasEdge(new VertexPair(vt.v1, vt.v2)) && this.model.hasEdge(new VertexPair(vt.v2, vt.v3))) {
                //CASE: v1-----v3
                //             /
                //           /
                //         E
                if (this.model.hasDependence(new VertexPair(vt.v1, vt.v3)) &&
                        !this.model.hasDependence(new VertexPair(vt.v3, vt.v1))) {
                    //Have, v1---->v3
                    if (this.model.hasDependence(new VertexPair(vt.v3, vt.v2))) {
                        //Not yet oriented, so direct from E--->v3
                        this.model.removeDependence(new VertexPair(vt.v3, vt.v2));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 3 " + vt + " : " + vt.v2 + "-->" + vt.v3);
                        ModelScoring.usedRule("REM");
                    }
                }
                else if (!this.model.hasDependence(new VertexPair(vt.v1, vt.v3)) &&
                            this.model.hasDependence(new VertexPair(vt.v3, vt.v1))) {
                    //Have, v1<---v3
                    //So, add trivial dependence E--->v1
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v1));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 4 " + vt + " : " + vt.v2 + "-->" + vt.v1 + " (trivial)");
                    ModelScoring.usedRule("REM-TRIV");

                }
                else if ( !this.model.hasDependence(new VertexPair(vt.v2, vt.v3)) &&
                            this.model.hasDependence(new VertexPair(vt.v3, vt.v2))
                        ) {
                    //Have, either E<---v3
                    //We know, v1---v3 and no edge between E and v1, so orient as v1<---v3
                    //and add trivial dependence E---->v1
                    this.model.removeDependence(new VertexPair(vt.v1, vt.v3));
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v1));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 5 " + vt + " : " + vt.v3 + "-->" + vt.v1 +
                            ", " + vt.v2 + "-->" + vt.v1 + " (trivial)");
                    ModelScoring.usedRule("REM");
                    ModelScoring.usedRule("REM-TRIV");
                }

            }
            else if (this.model.hasEdge(new VertexPair(vt.v1, vt.v2)) && !this.model.hasEdge(new VertexPair(vt.v2, vt.v3))) {
                //CASE: v1-----v3
                //       \
                //        \
                //         E
                if (this.model.hasDependence(new VertexPair(vt.v3, vt.v1)) &&
                        !this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1<---v3
                    if (this.model.hasDependence(new VertexPair(vt.v1, vt.v2))) {
                        //Not yet oriented, so direct from E--->v1
                        this.model.removeDependence(new VertexPair(vt.v1, vt.v2));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 6 " + vt + " : " + vt.v2 + "-->" + vt.v1);
                        ModelScoring.usedRule("REM");
                    }
                }
                else if (!this.model.hasDependence(new VertexPair(vt.v3, vt.v1)) &&
                            this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1--->v3
                    //So, add trivial dependence E--->v3
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v3));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 7 " + vt + " : " + vt.v2 + "-->" + vt.v3 + " (trivial)");
                    ModelScoring.usedRule("REM-TRIV");
                }
                else if ( !this.model.hasDependence(new VertexPair(vt.v2, vt.v1)) &&
                            this.model.hasDependence(new VertexPair(vt.v1, vt.v2))
                        ) {
                    //Have, either E<---v1
                    //We know, v1---v3 and no edge between E and v3, so orient as v1--->v3
                    //and add trivial dependence E---->v3
                    this.model.removeDependence(new VertexPair(vt.v3, vt.v1));
                    this.model.addTrivialDependence(new VertexPair(vt.v2, vt.v3));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 8 " + vt + " : " + vt.v1 + "-->" + vt.v3 +
                            ", " + vt.v2 + "-->" + vt.v3 + " (trivial)");
                    ModelScoring.usedRule("REM");
                    ModelScoring.usedRule("REM-TRIV");
                }
            }
            else {
                //CASE: v1-----v3
                //       \     /
                //        \  /
                //         E
                if (!this.model.hasDependence(new VertexPair(vt.v2, vt.v1)) &&
                        !this.model.hasDependence(new VertexPair(vt.v2, vt.v3))) {
                    //Have v1--->E<---v3
                    //Remove edge between v1 and v3
                    this.model.removeDependence(new VertexPair(vt.v1, vt.v3));
                    this.model.removeDependence(new VertexPair(vt.v3, vt.v1));
                    oriented = true;
                    log.info("orienting (Restricted Existence Models) 9 " + vt + " : " + vt.v1 + "-x-" + vt.v3);
                    ModelScoring.usedRule("REM-TRIV");
                }
                else if (!this.model.hasDependence(new VertexPair(vt.v3, vt.v1)) &&
                            this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1--->v3 and edge was not oriented with precedence rules
                    if (this.model.hasDependence(new VertexPair(vt.v3, vt.v2))) {
                        //Not yet oriented, so direct from E--->v3
                        this.model.removeDependence(new VertexPair(vt.v3, vt.v2));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 10 " + vt + " : " + vt.v2 + "-->" + vt.v3);
                        ModelScoring.usedRule("REM");
                    }
                }
                else if (this.model.hasDependence(new VertexPair(vt.v3, vt.v1)) &&
                            !this.model.hasDependence(new VertexPair(vt.v1, vt.v3))) {
                    //Have, v1<---v3, and edge was not oriented with precedence rules
                    if (this.model.hasDependence(new VertexPair(vt.v1, vt.v2))) {
                        //Not yet oriented, so direct from E--->v1
                        this.model.removeDependence(new VertexPair(vt.v1, vt.v2));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 11 " + vt + " : " + vt.v2 + "-->" + vt.v1);
                        ModelScoring.usedRule("REM");
                    }
                }
                else if (this.model.hasDependence(new VertexPair(vt.v2, vt.v1)) &&
                            !this.model.hasDependence(new VertexPair(vt.v1, vt.v2))) {
                    //Have, E--->v1
                    if (!this.model.hasDependence(new VertexPair(vt.v2, vt.v3)) &&
                            this.model.hasDependence(new VertexPair(vt.v3, vt.v2))) {
                        //Also, have E<---v3
                        //We know, v1---v3, so orient as v1<---v3
                        this.model.removeDependence(new VertexPair(vt.v1, vt.v3));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 12 " + vt + " : " + vt.v3 + "-->" + vt.v1);
                        ModelScoring.usedRule("REM");
                    }
                }
                else if (this.model.hasDependence(new VertexPair(vt.v2, vt.v3)) &&
                            !this.model.hasDependence(new VertexPair(vt.v3, vt.v2))) {
                    //Have, E--->v3
                    if (!this.model.hasDependence(new VertexPair(vt.v2, vt.v1)) &&
                            this.model.hasDependence(new VertexPair(vt.v1, vt.v2))) {
                        //Also, have E<---v1
                        //We know, v1---v3, so orient as v1--->v3
                        this.model.removeDependence(new VertexPair(vt.v3, vt.v1));
                        oriented = true;
                        log.info("orienting (Restricted Existence Models) 13 " + vt + " : " + vt.v1 + "-->" + vt.v3);
                        ModelScoring.usedRule("REM");
                    }
                }                
            }
        }

        return oriented;
    }

}
