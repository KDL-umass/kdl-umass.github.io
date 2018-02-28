/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import edu.cmu.tetrad.data.DiscreteVariable;
import edu.cmu.tetrad.graph.*;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.search.csp.Assignment;
import org.apache.log4j.Logger;

import java.util.*;

public class GraphUtil {


    protected static Logger log = Logger.getLogger(GraphUtil.class);


    public static boolean[][] copyGraph(boolean[][] graph) {
        boolean[][] copy = new boolean[graph.length][graph.length];

        int numVars = graph.length;
        for (int i = 0; i < numVars; i++) {
            for (int j = 0; j < numVars; j++) {
                copy[i][j] = graph[i][j];
            }
        }
        return copy;
    }

    public static Graph copyGraph(Graph graph) {
        Graph copy = new EdgeListGraph(graph.getNodes());

        for (Edge edge : graph.getEdges()) {
            copy.addEdge(new Edge(edge));
        }

        return copy;
    }

    /**
     * Conversion from int representation to boolean representation.
     * Note: Some information is lost as bidirected are converted to undirected edges.
     *
     * @param intGraph
     * @return
     */
    public static boolean[][] convert(int[][] intGraph) {
        int l = intGraph.length;

        boolean[][] boolGraph = new boolean[l][l];

        for (int i = 0; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                //Undirected or bidirected edges look the same.
                if ((intGraph[i][j] == 2 && intGraph[j][i] == 2) || (intGraph[i][j] == 1 && intGraph[j][i] == 1)) {
                    boolGraph[i][j] = true;
                    boolGraph[j][i] = true;
                }

                if (intGraph[i][j] == 1 && intGraph[j][i] == 0) {
                    boolGraph[i][j] = true;
                    boolGraph[j][i] = false;
                }

                if (intGraph[i][j] == 0 && intGraph[j][i] == 1) {
                    boolGraph[j][i] = true;
                    boolGraph[i][j] = false;
                }

            }
        }

        return boolGraph;

    }

    public static int[][] convert(boolean[][] boolGraph) {
        int l = boolGraph.length;
        int[][] intGraph = new int[l][l];

        for (int i = 0; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                if (boolGraph[i][j] && boolGraph[j][i]) {
                    intGraph[i][j] = 1;
                    intGraph[j][i] = 1;
                }

                if (boolGraph[i][j] && !boolGraph[j][i]) {
                    intGraph[i][j] = 1;
                    intGraph[j][i] = 0;
                }

                if (!boolGraph[i][j] && boolGraph[j][i]) {
                    intGraph[i][j] = 0;
                    intGraph[j][i] = 1;
                }

                if (!boolGraph[i][j] && !boolGraph[j][i]) {
                    intGraph[i][j] = 0;
                    intGraph[j][i] = 0;
                }
            }
        }
        return intGraph;
    }


    public static boolean[][] getSkeleton(boolean[][] dag) {
        boolean[][] skeleton = new boolean[dag.length][dag.length];

        for (int i = 0; i < dag.length; i++) {
            for (int j = i + 1; j < dag.length; j++) {
                if (dag[i][j] || dag[j][i]) {
                    skeleton[i][j] = true;
                    skeleton[j][i] = true;
                }

            }
        }
        return skeleton;
    }

    public static String dagToString(boolean[][] dag) {
        StringBuffer sb = new StringBuffer("{");
        for (int i = 0; i < dag.length; i++) {
            sb.append("{");
            for (int j = 0; j < dag[i].length; j++) {
                sb.append(dag[i][j]);
                if (j < dag[i].length - 1) {
                    sb.append(",");
                }
            }
            sb.append("}");
            if (i < dag.length - 1) {
                sb.append(",");
            }
        }
        sb.append("}");

        return sb.toString();
    }

    public static String dagToString(int[][] dag) {
        StringBuffer sb = new StringBuffer("{");
        for (int i = 0; i < dag.length; i++) {
            int[] row = dag[i];
            sb.append(Arrays.toString(row));
            sb.append("\t");
        }
        sb.append("}");

        return sb.toString();
    }

    public static String dagToTetradString(boolean[][] dag) {
        StringBuffer sb = new StringBuffer("\n");

        int row = 0;

        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag.length; j++) {

                if (dag[i][j] && !dag[j][i]) {
                    sb.append(row++);
                    sb.append(". ");
                    sb.append(i);
                    sb.append(" -> ");
                    sb.append(j);
                    sb.append("\n");
                }

                if (dag[i][j] && dag[j][i] && i < j) {
                    sb.append(row++);
                    sb.append(". ");
                    sb.append(i);
                    sb.append(" -- ");
                    sb.append(j);
                    sb.append("\n");
                }

            }
        }
        return sb.toString();
    }

    public static boolean equalDag(boolean[][] dag1, boolean[][] dag2) {
        if (dag1.length != dag2.length) {
            return false;
        }
        int numVars = dag1.length;
        for (int i = 0; i < numVars; i++) {
            for (int j = i + 1; j < numVars; j++) {
                if (dag1[i][j] != dag2[i][j] || dag1[j][i] != dag2[j][i]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * C-separation is a conditional independence criterion for chain graph (mixed directed and undirected graphs)
     * If the pdag is actually a fully directed DAG then c-separation is equivalent to the more common c-separation.
     * <p/>
     * This algorithm finds Y such that (X _||_ Y | Z) holds in the given pdag.
     * <p/>
     * This is the implementation described in:
     * <p/>
     * Bayesian Networks from the Point of View of Chain Graphs
     * Milan Studeny
     * UAI 1998
     *
     * @param pdag
     * @param xIdx
     * @param C
     * @return set of nodes that a c-separated from x given z.
     */
    public static Set<Integer> findCSeparatedNodes(boolean[][] pdag, int xIdx, Set<Integer> C) {
        Set<Integer> U = new HashSet<Integer>();
        U.add(xIdx);
        Set<Integer> V = new HashSet<Integer>();
        Set<Integer> W = new HashSet<Integer>();
        Set<Integer> Z = new HashSet<Integer>();

        boolean madeChange = true;

        while (madeChange) {
            madeChange = false;
            Set<Integer> uCopy = new HashSet<Integer>(U);
            for (Integer u : uCopy) {
                for (int v = 0; v < pdag.length; v++) {
                    //Rule 1: u \in U. u - v and v not in C  --> v \in U
                    if (pdag[u][v] && pdag[v][u] && !C.contains(v) && !U.contains(v)) {
                        U.add(v);
                        madeChange = true;
                    }
                    //Rule 2 : u \in U, u <- v and v not in C --> v \in U
                    if (pdag[v][u] && !pdag[u][v] && !C.contains(v) && !U.contains(v)) {
                        U.add(v);
                        madeChange = true;
                    }

                    //Rule 3: u \in U or V. u -> v and v not in C --> v \in V
                    if (pdag[u][v] && !pdag[v][u] && !C.contains(v) && !V.contains(v)) {
                        V.add(v);
                        madeChange = true;
                    }

                    //Rule 5: u in \U or V. u -> v --> v \in W
                    if (pdag[u][v] && !pdag[v][u] && !W.contains(v)) {
                        W.add(v);
                        madeChange = true;
                    }
                }
            }
            Set<Integer> vCopy = new HashSet<Integer>(V);
            for (Integer u : vCopy) {
                for (int v = 0; v < pdag.length; v++) {

                    //Rule 3: u \in U or V. u -> v and v not in C --> v \in V
                    if (pdag[u][v] && !pdag[v][u] && !C.contains(v) && !V.contains(v)) {
                        V.add(v);
                        madeChange = true;
                    }

                    //Rule 4: u \in V. u - v and v not in C --> v \in V
                    if (pdag[u][v] && pdag[v][u] && !C.contains(v) && !V.contains(v)) {
                        V.add(v);
                        madeChange = true;
                    }

                    //Rule 5: u in \U or V. u -> v --> v \in W
                    if (pdag[u][v] && !pdag[v][u] && !W.contains(v)) {
                        W.add(v);
                        madeChange = true;
                    }
                }
            }

            Set<Integer> wCopy = new HashSet<Integer>(W);
            for (Integer u : wCopy) {

                //Rule 7: u \in W and u \in C --> v \in Z
                if (C.contains(u) && !Z.contains(u)) {
                    Z.add(u);
                    madeChange = true;
                }


                for (int v = 0; v < pdag.length; v++) {
                    //Rule 6: u \in W. u - v --> v \in W
                    if (pdag[u][v] && pdag[v][u] && !W.contains(v)) {
                        W.add(v);
                        madeChange = true;
                    }
                }
            }
            Set<Integer> zCopy = new HashSet<Integer>(Z);
            for (Integer u : zCopy) {
                for (int v = 0; v < pdag.length; v++) {
                    //Rule 8: u - v --> v \in Z
                    if (pdag[u][v] && pdag[v][u] && !Z.contains(v)) {
                        Z.add(v);
                        madeChange = true;
                    }
                    //Rule 9: u <- v and v not in C --> v \in U
                    if (pdag[v][u] && !pdag[u][v] && !C.contains(v) && !U.contains(v)) {
                        U.add(v);
                        madeChange = true;
                    }
                }
            }
        }

        Set<Integer> cSeparated = new HashSet<Integer>();
        for (int i = 0; i < pdag.length; i++) {
            if (!U.contains(i) && !V.contains(i) && !C.contains(i)) {
                cSeparated.add(i);
            }
        }
        return cSeparated;
    }


    /**
     * Find DSeparated nodes uses a modified BFS to follow all paths out of X while checking the evidence for blocking
     * This is Algorithm 2 from:
     * <p/>
     * d-Separation: From Theorems to Algorithms
     * Geiger, Verma, and Pearl
     * Uncertainty in Artifical Intelligence 5 (1990)
     *
     * @param dag
     * @param xIdx
     * @param evidence
     * @return
     */

    public static Set<Integer> findDSeparatedNodes(boolean[][] dag, boolean[][] skeleton, int xIdx, Set<Integer> zIdxs, boolean[] descendent) {
        Set<Integer> reachableSet = new HashSet<Integer>();

        //Create a data structure for edge labels.
        boolean[][] visited = new boolean[skeleton.length][skeleton.length];

//        for (int i = 0; i < visited.length; i++) {
//            for (int j = 0; j < visited.length; j++) {
//                visited[i][j] = false;
//            }
//        }

        //Initialize the queue with edges out of X
        Queue<Integer> q = new LinkedList<Integer>();
        for (int i = 0; i < skeleton.length; i++) {
            if (skeleton[xIdx][i]) {
                reachableSet.add(i);
                q.offer((xIdx * skeleton.length) + i);
            }

        }

        //log.debug("Z: " + Arrays.deepToString(zIdxs.toArray(new Integer[zIdxs.size()])));

        while (q.peek() != null) {
            int edge = q.poll();
            int u = edge / skeleton.length;
            int v = edge % skeleton.length;

            if (!visited[u][v]) {
                //Find all links adjacent to v
                for (int w = 0; w < skeleton.length; w++) {
                    if (skeleton[v][w] && u != w) {  //Edge exists out of v and is not a loop
                        if ((dag[u][v] && dag[w][v] && descendent[v]) || // 1) v is a head-to-head node and is a descendent of Z
                                (!(dag[u][v] && dag[w][v]) && !zIdxs.contains(v)))    // 2) v is not a head-to-head node and is not in Z
                        {
                            reachableSet.add(w);
                            q.offer(v * skeleton.length + w);

                            visited[u][v] = true;
                        }
                    }
                }
            }
        }
        // dSeparated = V
        // dSep = V - (reachable + X + Z)
        Set<Integer> dSeparated = new HashSet<Integer>();
        for (int i = 0; i < skeleton.length; i++) {
            if (!reachableSet.contains(i) && i != xIdx && !zIdxs.contains(i)) {
                dSeparated.add(i);
            }
        }

        //log.info(Arrays.deepToString(dSeparated.toArray()));
        return dSeparated;
    }

    public static Set<Integer> findDSeparatedNodes(boolean[][] dag, int xIdx, Set<Integer> zIdxs) {

        boolean[] descendent = GraphUtil.zBlocked(dag, zIdxs);

        boolean[][] skeleton = GraphUtil.getSkeleton(dag);

        return findDSeparatedNodes(dag, skeleton, xIdx, zIdxs, descendent);


    }

    /**
     * Returns a minimal set of variables, Z, that is sufficent to d-separate X from Y.
     * Uses algorithm 4 of Tian, Paz and Pearl (R-254,1998)
     *
     * @param dag
     * @param xIdx
     * @param yIdx
     * @return
     */
    public static Set<Integer> findMinimalDSeparators(boolean[][] dag, int xIdx, int yIdx) {
        boolean[][] subgraph = getAncestorSubgraph(dag, xIdx, yIdx);
        List<Set<Integer>> parentSets = getParentSets(subgraph);

        Set<Integer> z = new HashSet<Integer>(parentSets.get(xIdx));
        z.addAll(parentSets.get(yIdx));
        z.remove(xIdx);
        z.remove(yIdx);

        boolean zChanged = true;

        while (zChanged) {
            zChanged = false;
            Set<Integer> zPrime = new HashSet<Integer>(z);
            for (Integer node : z) {
                zPrime.remove(node);
                if (findDSeparatedNodes(subgraph, xIdx, zPrime).contains(yIdx)) {
                    //zPrime is a dSeparator
                    zChanged = true;
                    break;
                }
                //If node is not a separator then it is in the minimal set so add back in.
                zPrime.add(node);
            }
            z = zPrime;
        }

        return z;
    }

    /**
     * Return a graph containing only the ancestors of the seed nodes. Due to our representation, the nodes are
     * still present in the graph, but the edges are removed.
     *
     * @param dag
     * @param subset
     * @return
     */
    public static boolean[][] getAncestorSubgraph(boolean[][] dag, Set<Integer> seedNodes) {
        Set<Integer> ancestorNodes = new HashSet<Integer>();
        for (Integer node : seedNodes) {
            ancestorNodes.addAll(getAncestors(dag, node));
        }

        boolean[][] ancestorGraph = copyGraph(dag);

        Set<Integer> nonAncestors = new HashSet<Integer>();
        for (int i = 0; i < dag.length; i++) {
            nonAncestors.add(i);
        }
        nonAncestors.removeAll(ancestorNodes);

        for (Integer nonAncestor : nonAncestors) {
            for (int j = 0; j < dag.length; j++) {
                if (dag[nonAncestor][j] || dag[j][nonAncestor]) {
                    ancestorGraph[j][nonAncestor] = false;
                    ancestorGraph[nonAncestor][j] = false;
                }
            }
        }
        return ancestorGraph;
    }

    /**
     * Convenience method for common use case (determining the ancestor subgraph for a pair of nodes.)
     *
     * @param dag
     * @param x
     * @param y
     * @return
     */
    public static boolean[][] getAncestorSubgraph(boolean[][] dag, int x, int y) {
        Set<Integer> seeds = new HashSet<Integer>();
        seeds.add(x);
        seeds.add(y);
        return getAncestorSubgraph(dag, seeds);
    }

    /**
     * Returns the set of nodes that are ancestors of X.
     *
     * @param dag
     * @param xIdx
     * @return
     */
    public static Set<Integer> getAncestors(boolean[][] dag, int xIdx) {
        List<Set<Integer>> parentSets = getParentSets(dag);

        Set<Integer> ancestors = new HashSet<Integer>();
        ancestors.add(xIdx);

        boolean added = true;
        while (added) {
            Set<Integer> ancestorsToAdd = new HashSet<Integer>();
            for (Integer ancestor : ancestors) {
                ancestorsToAdd.addAll(parentSets.get(ancestor));
            }
            int sizeBefore = ancestors.size();
            ancestors.addAll(ancestorsToAdd);
            int sizeAfter = ancestors.size();

            if (ancestorsToAdd.size() == 0 || sizeAfter == sizeBefore) {
                added = false;
            }
        }
        return ancestors;

    }


    /**
     * Returns a set of Integer List of the form x,y,z where Z is the collider
     * X --> Z <-- Y
     *
     * @param dag
     * @return
     */
    public static Set<List<Integer>> findColliders(boolean[][] dag) {

        Set<List<Integer>> colliders = new HashSet<List<Integer>>();

        List<Set<Integer>> parentSets = getParentSets(dag);
        for (int i = 0; i < dag.length; i++) {
            Set<Integer> parents = parentSets.get(i);

            for (Integer parent1 : parents) {
                for (Integer parent2 : parents) {

                    //If edges are directed p1 --> i <-- p2 and p1 and p2 are not connected
                    // then this is a collider
                    if (dag[parent1][i] && dag[parent2][i]
                            && !dag[i][parent1] && !dag[i][parent2]
                            && !dag[parent1][parent2] && !dag[parent2][parent1]) {

                        if (parent1 < parent2) { //to avoid duplicates
                            List<Integer> collider = new ArrayList<Integer>();
                            collider.add(parent1);
                            collider.add(parent2);
                            collider.add(i);
                            colliders.add(collider);
                        }
                    }
                }
            }
        }
        return colliders;
    }

    public static boolean[][] getCompletedPDAG(boolean[][] dag) {
        List<Integer> unknownEdges = orderEdges(dag);
        List<Set<Integer>> parentSets = getParentSets(dag);

        Set<Integer> compelledEdges = new HashSet<Integer>();
        Set<Integer> reversibleEdges = new HashSet<Integer>();

        while (!unknownEdges.isEmpty()) {
            int xy = unknownEdges.remove(0); //Lowest unknown edge.
            int x = xy / dag.length;
            int y = xy % dag.length;

            Set<Integer> xParents = parentSets.get(x);
            Set<Integer> yParents = parentSets.get(y);

            boolean foundBreak = false;
            for (Integer w : xParents) {
                int wx = w * dag.length + x;
                if (compelledEdges.contains(wx)) {
                    if (!yParents.contains(w)) {
                        compelledEdges.add(xy);
                        unknownEdges.remove((Integer) xy);
                        for (Integer yParent : yParents) {
                            int zy = yParent * dag.length + y;
                            compelledEdges.add(zy);
                            unknownEdges.remove((Integer) zy);

                        }
                        foundBreak = true;
                    } else {
                        int wy = w * dag.length + y;
                        compelledEdges.add(wy);
                        unknownEdges.remove((Integer) wy);
                    }
                }
            }

            if (!foundBreak) {
                boolean foundZ = false;
                for (Integer z : yParents) {
                    if (z != x && !xParents.contains(z)) {
                        foundZ = true;
                        break;
                    }
                }

                Set<Integer> setToAdd = reversibleEdges;
                if (foundZ) {
                    setToAdd = compelledEdges;
                }

                setToAdd.add(xy);
                for (Integer yParent : yParents) {
                    int zy = yParent * dag.length + y;
                    if (unknownEdges.contains(zy)) {
                        setToAdd.add(zy);
                        unknownEdges.remove((Integer) zy);
                    }
                }
            }
        }

        boolean[][] pdag = new boolean[dag.length][dag.length];

        for (Integer compelledEdge : compelledEdges) {
            int x = compelledEdge / dag.length;
            int y = compelledEdge % dag.length;
            pdag[x][y] = true;
        }

        for (Integer reversibleEdge : reversibleEdges) {
            int x = reversibleEdge / dag.length;
            int y = reversibleEdge % dag.length;
            pdag[x][y] = true;
            pdag[y][x] = true;
        }
        return pdag;
    }

    /**
     * Use algorithm of Dor and Tarsi to create a consistent Dag from this PDAG, if one exists.
     *
     * @param pdag
     * @return null if no consistent extension exists, otherwise the extension itself is returned.
     */
    public static boolean[][] getConsistentDag(boolean[][] pdag) {
        boolean[][] gPrime = copyGraph(pdag);
        Graph A = getTetradGraph(pdag);

        List<Node> aNodes = A.getNodes();

        while (!aNodes.isEmpty()) {

            Node x = null;

            for (Node node : aNodes) {
                // find x where:
                // a. x is a sink no edges directed outward.
                List<Node> children = A.getNodesOutTo(node, Endpoint.ARROW);
                if (!children.isEmpty()) {
                    continue;
                }
                //b. any y connected to x by an undirected edge is adjacent to all other nodes connected to x.
                //Any nodes into node are neighbors as if node has children we skipped it.
                List<Node> neighbors = A.getNodesInTo(node, Endpoint.TAIL);
                List<Node> adjacentNodes = A.getAdjacentNodes(node);

                boolean hasAllEdges = true;
                for (Node y : neighbors) {
                    for (Node adj : adjacentNodes) {
                        if (y == adj) {
                            continue;
                        }

                        Edge hasEdge = A.getEdge(y, adj);
                        if (hasEdge == null) {
                            hasAllEdges = false;
                            break;
                        }

                    }
                    if (!hasAllEdges) {
                        break;
                    }
                }

                if (hasAllEdges) {
                    x = node;
                    break;
                }
            }

            if (x == null) {
                return null;
            }

            // Direct all incident edges of x towards x;
            List<Node> neighbors = A.getNodesInTo(x, Endpoint.TAIL);

            for (Node neighbor : neighbors) {
                int i = Integer.parseInt(x.getName());
                int j = Integer.parseInt(neighbor.getName());

                gPrime[j][i] = true;
                gPrime[i][j] = false;
            }

            Graph aCopy = GraphUtil.copyGraph(A);
            List<Edge> incidentEdges = aCopy.getEdges(x);

            for (Edge incidentEdge : incidentEdges) {
                A.removeEdge(incidentEdge);
            }

            A.removeNode(x);

            aNodes = A.getNodes();
        }

        return gPrime;
    }

    /**
     * Translates an adjacency matrix into a Tetrad graph.
     * Allows for both directed and undirected edges.
     *
     * @param graph
     * @return
     */
    public static Graph getTetradGraph(boolean[][] graph) {
        List<Node> nodeList = new ArrayList<Node>();
        int numVars = graph.length;
        for (int i = 0; i < numVars; i++) {
            nodeList.add(new DiscreteVariable(i + "", 2));
        }

        Graph tetradGraph = new EdgeListGraph(nodeList);

        for (int i = 0; i < numVars; i++) {
            for (int j = 0; j < numVars; j++) {
                if (graph[i][j] && graph[j][i] && i < j) {
                    tetradGraph.addEdge(new Edge(nodeList.get(i), nodeList.get(j), Endpoint.TAIL, Endpoint.TAIL));
                }

                if (graph[i][j] && !graph[j][i]) {
                    tetradGraph.addEdge(new Edge(nodeList.get(i), nodeList.get(j), Endpoint.TAIL, Endpoint.ARROW));
                }
            }
        }

        return tetradGraph;
    }


    public static List<Integer> getEdges(boolean[][] dag) {
        List<Integer> edges = new ArrayList<Integer>();

        for (int i = 0; i < dag.length; i++) {
            for (int j = 0; j < dag.length; j++) {
                if (dag[i][j] && !dag[j][i]) {
                    int edge = i * dag.length + j;
                    edges.add(edge);
                }
            }
        }
        return edges;
    }

    public static List<Set<Integer>> getParentSets(boolean[][] dag) {
        List<Set<Integer>> parentSets = new ArrayList<Set<Integer>>();

        for (int j = 0; j < dag.length; j++) {
            parentSets.add(new HashSet<Integer>());
            for (int i = 0; i < dag.length; i++) {
                if (dag[i][j]) {
                    parentSets.get(j).add(i);
                }

            }
        }
        return parentSets;
    }

    public static List<Integer> orderEdges(boolean[][] dag) {
        //log.info("Order Edges: " + dagToString(dag));
        List<Integer> topSort = topologicalSort(dag);
        List<Integer> unorderedEdges = getEdges(dag);
        List<Set<Integer>> parentSets = getParentSets(dag);

        List<Integer> edgeOrder = new ArrayList<Integer>();

        int i = 1;
        int startJ = 0;
        while (!unorderedEdges.isEmpty()) {
            //log.info(Arrays.deepToString(unorderedEdges.toArray()));

            int minY = -1;
            int maxX = -1;
            for (int j = startJ; j < topSort.size(); j++) {
                int y = topSort.get(j);
                Set<Integer> parentSet = parentSets.get(y);
                int maxIndex = -1;

                for (Integer x : parentSet) {

                    int edge = x * dag.length + y;
                    int xSortIdx = topSort.indexOf((Integer) x);

                    if (xSortIdx > maxIndex && unorderedEdges.contains(edge)) {
                        maxIndex = xSortIdx;
                    }
                }

                if (maxIndex > -1) {
                    maxX = topSort.get(maxIndex);
                    minY = y;
                }

                if (maxX > -1 && minY > -1) {
                    int edge = maxX * dag.length + minY;
                    edgeOrder.add(edge);
                    unorderedEdges.remove((Integer) edge);
                    startJ = topSort.indexOf(minY);
                    i++;
                    break;
                }
            }
        }
        return edgeOrder;
    }

    public static List<Integer> topologicalSort(boolean[][] dag) {
        List<Integer> visited = new ArrayList<Integer>();
        List<Integer> sortList = new ArrayList<Integer>();

        for (int i = 0; i < dag.length; i++) {

            if (visited.contains(i)) {
                continue;
            }
            Stack<Integer> nodeStack = new Stack<Integer>();
            nodeStack.push(i);

            while (!nodeStack.isEmpty()) {
                int node = nodeStack.peek();
                boolean hasChild = false;

                for (int j = 0; j < dag.length; j++) {
                    if (dag[node][j] && !visited.contains(j)) {
                        nodeStack.remove((Integer) j); //Remove any earlier occurances of j
                        nodeStack.push(j);
                        hasChild = true;
                    }
                }

                visited.add(node);

                if (!hasChild) {
                    while (!nodeStack.isEmpty() && visited.contains(nodeStack.peek())) {
                        int leaf = nodeStack.pop();
                        sortList.add(0, leaf);
                    }
                }

            }
        }

        return sortList;

    }


    public static Assignment getAssignmentForDag(boolean[][] dag) {
        Assignment assignment = new Assignment();

        int n = dag.length;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int var = i * n + j;
                if (dag[i][j] && !dag[j][i]) {
                    assignment.setVar(var, "R");
                }

                if (!dag[i][j] && dag[j][i]) {
                    assignment.setVar(var, "L");
                }

                if (!dag[i][j] && !dag[j][i]) {
                    assignment.setVar(var, "E");
                }
                //Otherwise ignore, don't assign the variable.
                //This is a bi-directed edge.
            }
        }
        return assignment;
    }

    public static boolean[][] convertTetradGraphToDag(Graph graph) {
        int n = graph.getNumNodes();
        boolean[][] dag = new boolean[n][n];
        List<Node> nodes = graph.getNodes();
        int i = 0;
        for (Node node1 : nodes) {
            int j = 0;
            for (Node node2 : nodes) {
                if (graph.isChildOf(node2, node1)) {
                    //i -> j
                    if (!dag[j][i]) { //Indicates i <-> j so we just add
                        dag[i][j] = true;
                    }
                }
                j++;
            }
            i++;
        }
        return dag;
    }

    /**
     * This works for both directed and mixed graphs, and currently slow but effective
     *
     * @param dag
     * @return
     */
    public static boolean hasDirectedCycle(int[][] dag) {
        int n = dag.length;

        for (int i = 0; i < n; i++) { //Start DFS from every node.
            boolean[] visited = new boolean[n];
            Stack<Integer> nodeQueue = new Stack<Integer>();

            visited[i] = true;
            for (int j = 0; j < n; j++) {
                if (dag[i][j] == 1) {
                    nodeQueue.push(j);
                }
            }

            while (!nodeQueue.isEmpty()) {
                int node = nodeQueue.pop();
                if (node == i) {
                    return true;
                }
                if (!visited[node]) {
                    visited[node] = true;
                    for (int j = 0; j < n; j++) {
                        if (dag[node][j] == 1) {
                            nodeQueue.push(j);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * This works for both directed and mixed graphs, and currently slow but effective
     *
     * @param dag
     * @return
     */
    public static boolean hasDirectedCycle(boolean[][] dag) {
        int n = dag.length;

        for (int i = 0; i < n; i++) { //Start DFS from every node.
            boolean[] visited = new boolean[n];
            Stack<Integer> nodeQueue = new Stack<Integer>();

            visited[i] = true;
            for (int j = 0; j < n; j++) {
                if (dag[i][j]) {
                    nodeQueue.push(j);
                }
            }

            while (!nodeQueue.isEmpty()) {
                int node = nodeQueue.pop();
                if (node == i) {
                    return true;
                }
                if (!visited[node]) {
                    visited[node] = true;
                    for (int j = 0; j < n; j++) {
                        if (dag[node][j]) {
                            nodeQueue.push(j);
                        }
                    }
                }
            }
        }
        return false;
    }


    /**
     * Block Z returns a boolean[] indicating whether a node is in Z or has
     * a descendent in Z
     *
     * @param dag
     * @return
     */
    public static boolean[] zBlocked(boolean[][] dag, Set<Integer> z) {
        boolean[] desc = new boolean[dag.length];

        Queue<Integer> zQueue = new LinkedList<Integer>(z);

        while (zQueue.peek() != null) {
            int node = zQueue.poll();
            desc[node] = true;
            for (int i = 0; i < dag.length; i++) {
                if (dag[i][node]) {
                    if (!desc[i]) {
                        zQueue.offer(i);
                    }
                }
            }
        }
        return desc;
    }

    public static boolean[] zBlocked(PowerBayesNet bn, Set<Integer> z) {
        boolean[] desc = new boolean[bn.getNrOfNodes()];

        Queue<Integer> zQueue = new LinkedList<Integer>(z);

        while (zQueue.peek() != null) {
            int node = zQueue.poll();
            desc[node] = true;

            int[] parents = bn.getParentSet(node).getParents();
            for (int i = 0; i < parents.length; i++) {
                int parent = parents[i];
                if (!desc[parent]) {
                    zQueue.offer(parent);
                }

            }
        }
        return desc;
    }
}
