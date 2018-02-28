/**
 * $
 *
 * Part of the open-source Proximity system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.rules;

import kdl.bayes.skeleton.SkeletonFinder;
import kdl.bayes.skeleton.util.PowerSetIterator;
import kdl.bayes.util.GraphUtil;
import kdl.bayes.util.PermutationIterator;
import org.apache.log4j.Logger;

import java.util.*;

public class EdgeOrientation {

    protected static Logger log = Logger.getLogger(EdgeOrientation.class);

    SkeletonFinder skeleton;

    boolean[][] pdag;

    Set<List<Integer>> bidirectedEdges;

    public EdgeOrientation(SkeletonFinder skeleton) {
        this.skeleton = skeleton;
        pdag = new boolean[skeleton.getNumVariables()][skeleton.getNumVariables()];
        bidirectedEdges = new HashSet<List<Integer>>();
    }

    public Set<List<Integer>> getBidirectedEdges() {
        return bidirectedEdges;
    }

    public int[][] getIntPdag() {
        int[][] intGraph = GraphUtil.convert(pdag);
        for (List<Integer> bidirectedEdge : bidirectedEdges) {
            int x = bidirectedEdge.get(0);
            int y = bidirectedEdge.get(1);

            intGraph[x][y] = 2;
            intGraph[y][x] = 2;
        }
        return intGraph;
    }

    public boolean[][] getPDag() {
        return pdag;
    }

    public void orientEdges() {
        orientColliders();

        while (meekRule1() || meekRule2() || meekRule3()) ;

        //Confirm all edges in the skeleton appear in the pdag

        for (int i = 0; i < pdag.length; i++) {
            for (int j = i + 1; j < pdag.length; j++) {
                if (skeleton.hasEdge(i, j)) {
                    if (!pdag[i][j] && !pdag[j][i]) { //If neither edge exists we need to add an edge.
                        pdag[i][j] = true;
                        pdag[j][i] = true;
                    }
                }
            }
        }

    }

    public void orientColliders() {
        Set<List<Integer>> colliders = skeleton.findColliders();
        for (List<Integer> collider : colliders) {
            int xIdx = collider.get(0);
            int zIdx = collider.get(1);
            int yIdx = collider.get(2);

            List<String> varNames = skeleton.getNames();
            Set<Integer> pair = new HashSet<Integer>();
            pair.add(xIdx);
            pair.add(zIdx);
            Set<Integer> sepSet = skeleton.getSepset(pair);

            StringBuffer sepSetBuffer = new StringBuffer();
            boolean first = true;
            for (Integer varIdx : sepSet) {
                if (!first) {
                    sepSetBuffer.append(",");
                }
                sepSetBuffer.append(varNames.get(varIdx));
                first = false;
            }

            log.debug(varNames.get(xIdx) + "*->" + varNames.get(yIdx) + "<-*" + varNames.get(zIdx) + " sepset = [" + sepSetBuffer.toString() + "]");
            if (pdag[yIdx][xIdx]) {
                List<Integer> edge = new ArrayList<Integer>();
                edge.add(yIdx);
                edge.add(xIdx);
                bidirectedEdges.add(edge);
            }

            if (pdag[yIdx][zIdx]) {
                List<Integer> edge = new ArrayList<Integer>();
                edge.add(yIdx);
                edge.add(zIdx);
                bidirectedEdges.add(edge);
            }

            pdag[xIdx][yIdx] = true;
            pdag[zIdx][yIdx] = true;

        }
    }


    /**
     * If A->B and B-C and A !- C then add B -> C
     *
     * @return
     */
    public boolean meekRule1() {
        //Current implementation is highly inefficient.

        List<String> varNames = skeleton.getNames();

        for (int i = 0; i < pdag.length; i++) {
            List<Integer> iNeighbors = skeleton.getNeighbors(i);
            for (int j = 0; j < pdag[0].length; j++) {
                if (pdag[i][j]) {
                    List<Integer> neighbors = skeleton.getNeighbors(j);
                    for (Integer neighbor : neighbors) {

                        if (!iNeighbors.contains(neighbor) && !pdag[j][neighbor] && !pdag[neighbor][j]) {
                            pdag[j][neighbor] = true;
                            log.debug("Meek R1: " + varNames.get(j) + "-->" + varNames.get(neighbor));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean orientRule1() {
        return meekRule1();
    }

    /**
     * If A -> B -> C and A - C then orient A -> C.
     *
     * @return
     */
    public boolean meekRule2() {

        //Implementation may be inefficient
        List<String> varNames = skeleton.getNames();

        for (int i = 0; i < pdag.length; i++) {
            List<Integer> neighbors = skeleton.getNeighbors(i);

            for (Integer n : neighbors) {
                List<Integer> neighbors2d = skeleton.getNeighbors(n);
                for (Integer n2d : neighbors2d) {
                    // If A -> B -> C  and edge not already oriented and A - C then orient the edge.
                    if (pdag[i][n] && pdag[n][n2d] && !(pdag[n2d][i] || pdag[i][n2d]) && skeleton.hasEdge(i, n2d) && n2d != i) {
                        pdag[i][n2d] = true;
                        log.debug("Meek R2: " + varNames.get(i) + "-->" + varNames.get(n2d));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * If A - B, A-C, A-D, B->D and C->D then orient A->D.
     *
     * @return
     */
    public boolean meekRule3() {
        List<String> varNames = skeleton.getNames();

        for (int a = 0; a < pdag.length; a++) {
            List<Integer> neighbors = skeleton.getNeighbors(a);

            if (neighbors.size() < 3) {
                continue;
            }

            Iterator iter = new PowerSetIterator(neighbors, 3);
            while (iter.hasNext()) {
                List<Integer> subset = new ArrayList<Integer>((Set<Integer>) iter.next());

                Iterator permutations = new PermutationIterator(subset);
                while (permutations.hasNext()) {
                    List<Integer> permutation = (List<Integer>) permutations.next();
                    int b = permutation.get(0);
                    int c = permutation.get(1);
                    int d = permutation.get(2);

                    if (pdag[b][d] && pdag[c][d] && !pdag[d][a] && !pdag[a][d] && !(pdag[a][b] || pdag[b][a]) && !(pdag[a][c] && pdag[c][a])) {
                        pdag[a][d] = true;
                        log.debug("Meek R3: " + varNames.get(a) + "-->" + varNames.get(d));
                        return true;
                    }
                }

            }
        }
        return false;
    }


    public boolean isDirectedPath(boolean[][] dag, int source, int sink) {
        List<Integer> expandList = new ArrayList<Integer>();
        expandList.add(source);

        boolean[] visitedList = new boolean[dag.length];

        while (expandList.size() > 0) {
            int node = expandList.remove(0);
            visitedList[node] = true;

            boolean[] children = dag[node];
            for (int i = 0; i < children.length; i++) {
                if (children[i] && !visitedList[i]) {
                    if (i == sink) {
                        return true;
                    }
                    expandList.add(i);
                }
            }

        }

        return false;
    }


}
