/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 23, 2009
 * Time: 11:38:21 AM
 */
package rpc.model.util;

import rpc.design.Unit;
import rpc.schema.SchemaVisualizer;

import java.util.*;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * Model object holds the set of known dependencies. Undirected dependencies are stored as
 * dependencies in each direction for a pair of vertices.
 */
public class Model {

    private static Logger log = Logger.getLogger(Model.class);

    private Map<VertexPair, Set<Dependency>> dependencies;
    private int numPossibleDependencies;

    /**
     * Initialize a model with an empty set of dependencies.
     */
    public Model() {
        this.dependencies = new HashMap<VertexPair, Set<Dependency>>();
    }

    /**
     * Set the total number of possible dependencies (i.e., the total number of unique units to test).
     * @param numPossibleDependencies the total number of dependencies.
     */
    public void setNumPossibleDependencies(int numPossibleDependencies) {
        this.numPossibleDependencies = numPossibleDependencies;
    }

    /**
     * Get the total number of possible dependencies (i.e., the total number of unique units to test).
     * @return the total number of possible dependencies.
     */
    public int getTotalDependencies() {
        return this.numPossibleDependencies;
    }

    /**
     * Adds a known dependency to the model. Constructs the Dependency object.
     * @param vp the pair of vertices for which the dependency holds.
     * @param u the unit that supports the dependency.
     */
    public void addDependence(VertexPair vp, Unit u) {
        if (! this.dependencies.containsKey(vp)) {
            this.dependencies.put(vp, new HashSet<Dependency>());
        }
        this.dependencies.get(vp).add(new Dependency(u));
    }

    /**
     * Adds a known dependency to the model.
     * @param vp the pair of vertices for which the dependency holds.
     * @param dep the Dependency object to add.
     */
    public void addDependence(VertexPair vp, Dependency dep) {
        if (! this.dependencies.containsKey(vp)) {
            this.dependencies.put(vp, new HashSet<Dependency>());
        }
        this.dependencies.get(vp).add(dep);
    }

    /**
     * Adds a known trivial dependency to the model. The dependency will have a null underlying unit.
     * @param vp the pair of vertices for which the trivial dependency holds.
     */
    public void addTrivialDependence(VertexPair vp) {
        if (! this.dependencies.containsKey(vp)) {
            this.dependencies.put(vp, new HashSet<Dependency>());
        }
        this.dependencies.get(vp).add(new Dependency(vp.v1, vp.v2));
    }

    /**
     * Removes a dependency from the model.
     * @param vp the pair of vertices for which the dependency is to be removed.
     * @param u the unit that supports the dependency to remove.
     */
    public void removeDependence(VertexPair vp, Unit u) {
        if (this.dependencies.containsKey(vp)) {
            this.dependencies.get(vp).remove(new Dependency(u));
            if (this.dependencies.get(vp).size() <= 0) {
                this.dependencies.remove(vp);
            }
        }
    }

    /**
     * Removes all dependencies from the model for a given pair of vertices.
     * @param vp the pair of vertices for which the dependencies are to be removed.
     */
    public void removeDependence(VertexPair vp) {
        if (this.dependencies.containsKey(vp)) {
            this.dependencies.remove(vp);
        }
    }

    /**
     * Retrieve all the dependencies that the model holds.
     * @return a map from each pair of vertices to the set of Dependency objects that hold for each.
     */
    public Map<VertexPair, Set<Dependency>> getAllDependencies() {
        Map<VertexPair, Set<Dependency>> copyDependencies = new HashMap<VertexPair, Set<Dependency>>();
        for (Map.Entry<VertexPair, Set<Dependency>> me : this.dependencies.entrySet()) {
            VertexPair vp = new VertexPair(me.getKey());
            Set<Dependency> deps = new HashSet<Dependency>(me.getValue());
            copyDependencies.put(vp, deps);
        }

        return copyDependencies;
    }

    /**
     * Get all the dependencies that hold for a given pair of vertices.
     * @param vp the given pair of vertices.
     * @return the set of Dependency objects that hold for the pair of vertices.
     */
    public Set<Dependency> getDependencies(VertexPair vp) {
        if (this.dependencies.containsKey(vp)) {
            return this.dependencies.get(vp);
        }
        else {
            return null;
        }
    }

    /**
     * Checks whether or not the given vertex pair has a non-trivial dependency
     * @param vp the vertex pair in question.
     * @return true if the vertex pair has a Dependency with a non-null unit that supports it; false otherwise.
     */
    public boolean hasNonTrivialDependence(VertexPair vp) {
        if (this.dependencies.containsKey(vp)) {
            Set<Dependency> deps = this.dependencies.get(vp);
            for (Dependency dep : deps){
                if (!dep.isTrivial()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the set of neighbors of a given vertex, ignoring the direction of dependencies.
     * @param v the source vertex.
     * @return the set of neighboring vertices.
     */
    public Set<Vertex> getNeighbors(Vertex v) {
        Set<Vertex> neighbors = new HashSet<Vertex>();
        for (VertexPair vp : this.dependencies.keySet()) {
            if (v.equals(vp.v1)) {
                neighbors.add(new Vertex(vp.v2));
            }
            if (v.equals(vp.v2)) {
                neighbors.add(new Vertex(vp.v1));
            }
        }
        return neighbors;
    }

    /**
     * Get the set of children of a given vertex (i.e., the vertices for which this vertex is a parent).
     * @param v the parent vertex.
     * @return the set of children vertices.
     */
    public Set<Vertex> getChildren(Vertex v) {
        Set<Vertex> children = new HashSet<Vertex>();
        for (VertexPair vp : this.dependencies.keySet()) {
            if (v.equals(vp.v1)) {
                children.add(new Vertex(vp.v2));
            }
        }
        return children;
    }

    /**
     * Get the set of parents of a given vertex (i.e., the vertices for which this vertex is a child).
     * @param v the child vertex.
     * @return the set of parent vertices.
     */
    public Set<Vertex> getParents(Vertex v) {
        Set<Vertex> parents = new HashSet<Vertex>();
        for (VertexPair vp : this.dependencies.keySet()) {
            if (v.equals(vp.v2)) {
                parents.add(new Vertex(vp.v1));
            }
        }
        return parents;
    }

    /**
     * Checks whether or not there exists a dependency for the given pair of vertices.
     * @param vp the vertex pair in question.
     * @return true if there is a dependency from the first vertex to the second vertex (regardless if there
     * is a dependency in the opposite direction); false otherwise.
     */
    public boolean hasDependence(VertexPair vp) {
        return this.dependencies.containsKey(vp);        
    }

    /**
     * Checks whether or not there exists a dependency between the given pair of vertices, ignoring direction.
     * @param vp the vertex pair in question.
     * @return true if there is either a dependency from the first vertex to the second or vice versa;
     * false otherwise.
     */
    public boolean hasEdge(VertexPair vp) {
        return this.dependencies.containsKey(vp) || this.dependencies.containsKey(vp.reverse());
    }

    /**
     * Checks whether or not there is a directed edge between the given pair of vertices, regardless of direction.
     * @param vp the vertex pair in question.
     * @return true if there is either a dependency from the first vertex to the second or vice versa, but
     * not in both directions; false otherwise.
     */
    public boolean hasDirectedEdge(VertexPair vp) {
        return (this.dependencies.containsKey(vp) && !this.dependencies.containsKey(vp.reverse())) ||
                (this.dependencies.containsKey(vp.reverse()) && !this.dependencies.containsKey(vp));
    }

    /**
     * Checks whether or not the model has the specified dependency for the given vertex pair.
     * @param vp the pair of vertices.
     * @param dep the dependency.
     * @return true if the pair of vertices has the specified dependency between them; false otherwise.
     */
    public boolean checkDependence(VertexPair vp, Dependency dep) {
        return dep != null && this.dependencies.containsKey(vp) && this.dependencies.get(vp).contains(dep);
    }

    /**
     * Checks whether or not the model has the specified dependency for the given vertex pair and that the
     * reverse dependency does not exist in the model.
     * @param vp the pair of vertices.
     * @param dep the dependency.
     * @return true if the pair of vertices has the specified dependency between them and the reverse
     * does not exist in the model; false otherwise.
     */
    public boolean checkDirectedDependence(VertexPair vp, Dependency dep) {
        return dep != null && this.dependencies.containsKey(vp) && this.dependencies.get(vp).contains(dep)
                && !checkReverseDependence(vp, dep);
    }

    /**
     * Checks whether or not the model stores the reverse of the specified dependency for the reverse
     * of the given pair of vertices.
     * @param vp the pair of vertices.
     * @param dep the dependency.
     * @return true if the reverse of the given pair of vertices has the reverse of the specified
     * dependency between them in the model; false otherwise.
     */
    public boolean checkReverseDependence(VertexPair vp, Dependency dep) {
        return this.dependencies.containsKey(vp.reverse()) &&
                dep.reverse() != null && this.dependencies.get(vp.reverse()).contains(dep.reverse());
    }

    /**
     * Checks whether or not the model currently has an existence (or structural) variable as a potential common
     * effect of two given vertices.  The potential dependencies must be non-trivial.
     * @param v1 vertex 1.
     * @param v2 veretex 2.
     * @return true if the two vertices have the potential for common effect of an existence variable;
     * false otherwise.
     */
    public boolean commonExistsEffect(Vertex v1, Vertex v2) {
        Set<Vertex> v1Neighbors = getNeighbors(v1);
        Set<Vertex> v2Neighbors = getNeighbors(v2);

        for (Vertex neighbor : v1Neighbors) {
            if (neighbor.isExistence() && v2Neighbors.contains(neighbor)
                    && hasNonTrivialDependence(new VertexPair(v1, neighbor))
                    && hasNonTrivialDependence(new VertexPair(v2, neighbor))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a string representation, which is just a dump of the map containing all dependencies for
     * each pair of vertices.
     * @return the string representation.
     */
    public String toString() {
        return this.dependencies.toString();        
    }

    /**
     * Gets the dot file that can be used to visualize all dependency information in the model.
     * @param filename the name of the file to write out the dot.
     * @param withEdgeLabels flag denoting whether or not to also write out annotations about the
     * path information for each dependency.
     */
    public void getDotFile(String filename, boolean withEdgeLabels) {
        try {
            File file = new File(filename);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write("digraph G {\n");
            bw.write("\trankdir=TB;\n");
            bw.write("\n");
            for (String line : SchemaVisualizer.getDot()) {
                bw.write("\t" + line + "\n");
            }

            bw.write("\n");
            for (String line : ModelVisualizer.getDot(this, withEdgeLabels)) {
                bw.write("\t" + line + "\n");
            }
            
            bw.write("}");
            bw.close();
        }
        catch (IOException e) {
            log.error("Failed writing dot file, " + e);
        }
    }    

}