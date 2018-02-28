/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 2, 2010
 * Time: 1:20:49 PM
 */
package rpc.model.edgeorientation;

import rpc.model.util.Vertex;
import rpc.model.util.VertexPair;
import rpc.model.util.Model;
import rpc.model.scoring.ModelScoring;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * The Cycle Avoidance edge orientation rule. If X-Y and X-->V1...-->Vk-->Y, then orient as X-->Y.
 */
public class CycleAvoidance extends EdgeOrientation {

    private static Logger log = Logger.getLogger(CycleAvoidance.class);

    /**
     * Initializes the Cycle Avoidance edge orientation rule.
     * @param vertices the set of all vertices in the model.
     * @param model the model containing all dependencies among pairs of vertices.
     * @param sepsets the set of vertices that render pairs of variables conditionally independent.
     */
    public CycleAvoidance(Set<Vertex> vertices, Model model, Map<VertexPair, Set<Vertex>> sepsets) {
        super(vertices, model, sepsets);
    }

    /**
     * Finds and orients all instances that will avoid cycles.
     * @return true if any edges were oriented; false otherwise.
     */
    public boolean orient() {
        // find the set of (v1--v2) pairs within the model
        Set<VertexPair> pairs = new HashSet<VertexPair>();
        for (Vertex v1 : this.vertices) {
            Set<Vertex> neighbors = this.model.getNeighbors(v1);
            for (Vertex v2 : neighbors) {

                VertexPair pair = new VertexPair(v1, v2);
                if (this.model.hasDependence(pair) &&
                        this.model.hasDependence(pair.reverse())) {
                    pairs.add(pair);
                }
            }
        }

        boolean oriented = false;
        //orient v1-->v2 if there is a separate directed path from v1 to v2
        for (VertexPair pair : pairs) {
            if (! this.model.hasDependence(pair) || ! this.model.hasDependence(pair.reverse())) {
                //needs to be undirected to start (pair could have been oriented by its reverse already)
                continue;
            }

            //breadth-first search to find v2 through a separate directed path
            if (isDirectedPath(pair.v1, pair.v2)) {
                //orient as a cycle avoidance v1-->v2 by removing reverse direction
                this.model.removeDependence(pair.reverse());
                oriented = true;
                log.info("orienting (Cycle Avoidance) " + pair + " : " + pair.v1 + "-->" + pair.v2);                 
                ModelScoring.usedRule("CA");
            }
        }

        return oriented;
    }

    private boolean isDirectedPath(Vertex v1, Vertex v2) {
        Set<Vertex> source = new HashSet<Vertex>();
        source.add(v1);
        Set<Vertex> frontier = getDirectedNeighbors(source);
        frontier.remove(v2);

        Set<Vertex> expanded = new HashSet<Vertex>();
        expanded.addAll(source);
        while (true) {
            Set<Vertex> frontierNeighbors = new HashSet<Vertex>(getDirectedNeighbors(frontier));
            expanded.addAll(frontier);

            frontier = frontierNeighbors;

            if (frontier.size() == 0) {
                return false;
            }

            if (frontier.contains(v2)) {
                return true;
            }

            frontier.removeAll(expanded);
        }
    }

    private Set<Vertex> getDirectedNeighbors(Set<Vertex> vs) {
        Set<Vertex> neighbors = new HashSet<Vertex>();
        for (Vertex v : vs) {
            Set<Vertex> potentialNeighbors = this.model.getNeighbors(v);
            Set<Vertex> neighborsToRemove = new HashSet<Vertex>();
            for (Vertex potentialNeighbor : potentialNeighbors) {
                if (this.model.hasDependence(new VertexPair(potentialNeighbor, v))) {
                    //only want directed neighbors
                    neighborsToRemove.add(potentialNeighbor);
                }
            }
            potentialNeighbors.removeAll(neighborsToRemove);
            neighbors.addAll(potentialNeighbors);
        }
        return neighbors;
    }

}
