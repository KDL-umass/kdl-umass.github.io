/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 29, 2009
 * Time: 2:15:34 PM
 */
package rpc.model.edgeorientation;

import rpc.model.util.Model;
import rpc.model.util.VertexPair;
import rpc.model.util.Vertex;
import rpc.model.util.VertexTriple;
import rpc.model.scoring.ModelScoring;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * The Collider Detection edge orientation rule. If X-Y-Z and Y is not in sepset(X, Z), then orient as X-->Y<--Z.
 */
public class ColliderDetection extends EdgeOrientation {

    private static Logger log = Logger.getLogger(ColliderDetection.class);

    /**
     * Initializes the Collider Detection edge orientation rule.
     * @param vertices the set of all vertices in the model.
     * @param model the model containing all dependencies among pairs of vertices.
     * @param sepsets the set of vertices that render pairs of variables conditionally independent.
     */
    public ColliderDetection(Set<Vertex> vertices, Model model, Map<VertexPair, Set<Vertex>> sepsets) {
        super(vertices, model, sepsets);
    }

    /**
     * Finds and orients all collider triples.
     * @return true if any edges were oriented; false otherwise.
     */
    public boolean orient() {
        // find the set of (v1--v2--v3) triples within the model
        Set<VertexTriple> triples = new HashSet<VertexTriple>();
        for (Vertex v1 : this.vertices) {
            Set<Vertex> neighbors = this.model.getNeighbors(v1);
            for (Vertex v2 : neighbors) {
                if (this.model.commonExistsEffect(v1, v2)) {                    
                    //Also, skip cases in which v1 and v2 could have a possible common effect of an existence variable
                    continue;
                }

                Set<Vertex> nextNeighbors = this.model.getNeighbors(v2);

                for (Vertex v3 : nextNeighbors) {

                    //only accept triple if there are supporting dependencies (with actual units, not trivial)
                    //for both v1---v2 and v2---v3, AND it has to be undirected or directed as v1-->v2 or v3-->v2
                    if (    (this.model.hasDependence(new VertexPair(v1, v2))
                                &&
                            (this.model.hasNonTrivialDependence(new VertexPair(v1, v2)) ||
                                this.model.hasNonTrivialDependence(new VertexPair(v2, v1))))
                            &&
                            (  this.model.hasDependence(new VertexPair(v3, v2))
                                &&
                            (this.model.hasNonTrivialDependence(new VertexPair(v2, v3)) ||
                                this.model.hasNonTrivialDependence(new VertexPair(v3, v2))))                             
                            ) {

                        //acceptable triple if 3 different vertices with no edge between v1 and v3
                        //Also, skip cases in which v2 and v3 could have a possible common effect of an existence variable
                        if (!v3.equals(v1) && !this.model.hasNonTrivialDependence(new VertexPair(v1, v3)) &&
                                !this.model.hasNonTrivialDependence(new VertexPair(v3, v1)) && !this.model.commonExistsEffect(v2, v3)) {
                            VertexTriple vt = new VertexTriple(v1, v2, v3);
                            //add to triples set only once (i.e., v3--v2--v1 isn't already there)
                            if (!triples.contains(vt.reverse()))
                                triples.add(vt);
                        }
                    }
                }
            }
        }

        boolean oriented = false;
        // iterate through the triples and identify colliders
        for (VertexTriple vt : triples) {
            VertexPair v1v3 = new VertexPair(vt.v1, vt.v3);
            if (this.sepsets.containsKey(v1v3) && !this.sepsets.get(v1v3).contains(vt.v2)
                    && this.model.hasDependence(new VertexPair(vt.v1, vt.v2))
                    && this.model.hasDependence(new VertexPair(vt.v3, vt.v2))
                ) {
                //orient as a collider v1-->v2<--v3 by removing reverse directions
                if (this.model.hasDependence(new VertexPair(vt.v2, vt.v1))) {
                    this.model.removeDependence(new VertexPair(vt.v2, vt.v1));
                    oriented = true;
                    log.info("orienting (Collider Detection) " + vt + " : " + vt.v1 + "-->" + vt.v2);
                    ModelScoring.usedRule("CD");
                }
                if (this.model.hasDependence(new VertexPair(vt.v2, vt.v3))) {
                    this.model.removeDependence(new VertexPair(vt.v2, vt.v3));
                    oriented = true;
                    log.info("orienting (Collider Detection) " + vt + " : " + vt.v3 + "-->" + vt.v2);
                    ModelScoring.usedRule("CD");
                }                                
            }
        }

        return oriented;
    }

}