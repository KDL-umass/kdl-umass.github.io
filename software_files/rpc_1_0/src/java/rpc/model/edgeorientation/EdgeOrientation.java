/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Jan 2, 2010
 * Time: 2:56:18 PM
 */
package rpc.model.edgeorientation;

import rpc.model.util.Model;
import rpc.model.util.VertexPair;
import rpc.model.util.Vertex;

import java.util.Set;
import java.util.Map;

/**
 * The abstract class for Edge Orientation rules, specifying that all extensions must implement the orient() method.
 */
public abstract class EdgeOrientation {

    /**
     * The model containing all dependencies among pairs of vertices.
     */
    protected Model model;

    /**
     * The set of vertices that render pairs of variables conditionally independent.
     */
    protected Map<VertexPair, Set<Vertex>> sepsets;

    /**
     * The set of all vertices in the model.
     */
    protected Set<Vertex> vertices;

    /**
     * Initializes an Edge Orientation rule.
     * @param vertices the set of all vertices in the model.
     * @param model the model containing all dependencies among pairs of vertices.
     * @param sepsets the set of vertices that render pairs of variables conditionally independent.
     */
    public EdgeOrientation(Set<Vertex> vertices, Model model, Map<VertexPair, Set<Vertex>> sepsets) {
        this.model = model;
        this.sepsets = sepsets;
        this.vertices = vertices;
    }

    /**
     * Interface method for orienting a model.
     * @return true if the rule was able to orient any edges; false otherwise.
     */
    public abstract boolean orient();
}
