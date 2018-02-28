/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 2, 2010
 * Time: 12:38:00 PM
 */
package rpc.model.edgeorientation;

import rpc.model.util.Model;
import rpc.model.util.VertexPair;
import rpc.model.util.Vertex;
import rpc.model.util.VertexTriple;
import rpc.model.scoring.ModelScoring;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * The Known Non-Colliders edge orientation rule. If X-->Y-Z and < X,Y,Z > is not a collider, then orient as X-->Y-->Z. 
 */
public class KnownNonColliders extends EdgeOrientation {

    private static Logger log = Logger.getLogger(KnownNonColliders.class);

    /**
     * Initializes the Known Non-Colliders edge orientation rule.
     * @param vertices the set of all vertices in the model.
     * @param model the model containing all dependencies among pairs of vertices.
     * @param sepsets the set of vertices that render pairs of variables conditionally independent.
     */
    public KnownNonColliders(Set<Vertex> vertices, Model model, Map<VertexPair, Set<Vertex>> sepsets) {
        super(vertices, model, sepsets);
    }

    /**
     * Finds and orients all known non-collider triples.
     * @return true if any edges were oriented; false otherwise.
     */
    public boolean orient() {
        // find the set of (v1-->v2--v3) triples within the model
        Set<VertexTriple> triples = new HashSet<VertexTriple>();
        for (Vertex v1 : this.vertices) {
            Set<Vertex> neighbors = this.model.getNeighbors(v1);
            for (Vertex v2 : neighbors) {
                if (this.model.hasDependence(new VertexPair(v2, v1)) ||
                        !this.model.hasNonTrivialDependence(new VertexPair(v1, v2))) {
                    //We only want directed edges v1-->v2
                    //But, those directed edges must be supported by a non-trivial dependence
                    continue;
                }

                Set<Vertex> nextNeighbors = this.model.getNeighbors(v2);
                for (Vertex v3 : nextNeighbors) {
                    //acceptable triple if 3 different vertices with no edge between v1 and v3
                    //and an undirected edge between v2 and v3
                    //and skip cases in which collider detection has NOT occurred yet (i.e., v2 and v3 have a common
                    //effect of an existence variable)
                    if (!v3.equals(v1) && !this.model.hasNonTrivialDependence(new VertexPair(v1, v3)) &&
                            !this.model.hasNonTrivialDependence(new VertexPair(v3, v1)) &&
                            this.model.hasDependence(new VertexPair(v2, v3)) &&
                            this.model.hasDependence(new VertexPair(v3, v2)) &&
                            !this.model.commonExistsEffect(v2, v3)) {
                        triples.add(new VertexTriple(v1, v2, v3));
                    }
                }
            }
        }

        boolean oriented = false;
        // iterate through the triples and orient second edge
        // (direction must be v2-->v3 because we know it's not a collider)
        for (VertexTriple vt : triples) {
            //orient as a known non-collider v1-->v2-->v3 by removing reverse direction
            VertexPair v1v3 = new VertexPair(vt.v1,  vt.v3);
            //make sure that collider detection would have passed it up (v2 d-separates v1 and v3)
            if (this.sepsets.containsKey(v1v3) && this.sepsets.get(v1v3).contains(vt.v2)) {
                if (this.model.hasDependence(new VertexPair(vt.v3, vt.v2))) {
                    this.model.removeDependence(new VertexPair(vt.v3, vt.v2));
                    oriented = true;
                    log.info("orienting (Known Non-Colliders) " + vt + " : " + vt.v2 + "-->" + vt.v3);
                    ModelScoring.usedRule("KNC");
                }
            }
        }

        return oriented;
    }
}
