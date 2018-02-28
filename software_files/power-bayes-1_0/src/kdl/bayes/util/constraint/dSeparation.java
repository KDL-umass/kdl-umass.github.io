/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util.constraint;

import kdl.bayes.search.csp.Assignment;
import kdl.bayes.search.csp.BNCSP;
import kdl.bayes.search.csp.CSP;
import kdl.bayes.util.GraphUtil;

import java.text.DecimalFormat;
import java.util.*;

public class dSeparation implements Constraint {

    int xIdx;
    int yIdx;

    Set<Integer> zIdxs;

    double weight;

    String weightStr = "";

    public volatile int hashCode;

    public dSeparation(int x, int y, Set<Integer> z) {
        xIdx = x;
        yIdx = y;
        zIdxs = new HashSet<Integer>(z);

        double weight = 1.0;
        weightStr = "1.0";

        int hashX, hashY;

        if (x < y) {
            hashX = xIdx;
            hashY = yIdx;
        } else {
            hashX = yIdx;
            hashY = xIdx;
        }

        final int multiplier = 37;
        int code = 13;
        code = multiplier * code + hashX;
        code = multiplier * code + hashY;
        hashCode = code;

    }

    public dSeparation(int x, int y, Set<Integer> z, double weight) {
        this(x, y, z);
        this.weight = weight;

        DecimalFormat formatter = new DecimalFormat("#.###");
        weightStr = formatter.format(weight);

    }

    public double getWeight() {
        return weight;
    }

    /**
     * A dSeparation constraint is valid unless:
     * 1) There exists a directed path that is not blocked by Z (Z is not
     * sufficient for d-separation).
     * 2) a subset of Z is sufficient for d-separation implying the full
     * set is not necessary for d-separation.
     *
     * @param assignment
     * @param csp
     * @return
     */

    public boolean isValid(Assignment assignment, CSP csp) {
        if (!(csp instanceof BNCSP)) {
            return false;
        }

        boolean[][] committedGraph = ((BNCSP) csp).getCommittedGraph(assignment);
        boolean valid = GraphUtil.findDSeparatedNodes(committedGraph, xIdx, zIdxs).contains(yIdx);
        if (valid && zIdxs.size() > 0) {
            valid = findSufficientPaths(((BNCSP) csp).getDag(assignment), xIdx, yIdx, zIdxs);
            //boolean[][] pdag = ((BNCSP) csp).getPdag(assignment);
            //for(int size = 0; size < zIdxs.size() ; size++){
            //    PowerSetIterator iter = new PowerSetIterator(zIdxs, size);
            //    while(iter.hasNext()){
            //        Set<Integer> subset = (Set<Integer>) iter.next();
            //        //A subset was sufficient to dSeparation x from y so the constraint fails.
            //        if(GraphUtil.findCSeparatedNodes(pdag, xIdx, subset).contains(yIdx)){
            //            return false;
            //        }
            //    }
            //}
        }
        return valid;
    }


    /**
     * Determines whether there are more than limit *undirected* paths from s to t
     * This is used determine whether the full d-separation set is needed.
     *
     * @param graph
     * @param s
     * @param t
     * @param limit
     * @return
     */
    public boolean hasSufficientPaths(int[][] graph, int s, int t, int limit) {
        int pathCount = 0;
        boolean[] visited = new boolean[graph.length];

        Queue<Integer> nodeQueue = new LinkedList<Integer>();
        nodeQueue.offer(s);

        while (!nodeQueue.isEmpty()) {
            int node = nodeQueue.poll();

            for (int j = 0; j < graph.length; j++) {
                if ((graph[node][j] != 0 || graph[j][node] == 1) && !visited[node]) {
                    if (j == t) {
                        pathCount++;
                        if (pathCount == limit) {
                            return true;
                        }
                    } else {
                        nodeQueue.offer(j);
                    }
                }
            }
            visited[node] = true;
        }
        return false;
    }

    public boolean findSufficientPaths(int[][] graph, int s, int t, Set<Integer> zIdxs) {

        Set<Integer> usedZs = new HashSet<Integer>();
        List<Integer> newPath = new ArrayList<Integer>();
        newPath.add(s);
        Set<List<Integer>> foundPaths = findPath(graph, s, t, newPath);

        for (List<Integer> path : foundPaths) {
            boolean blocked = false;
            for (Integer zIdx : zIdxs) {
                if (path.contains(zIdx)) {
                    blocked = true;
                    usedZs.add(zIdx);
                    break;
                }

            }

            if (!blocked && path.size() > 1) {   //If we find an unblocked path, then we fail.
                return false;
            }
        }

        //Found too few paths to require every Z.
        if (foundPaths.size() < zIdxs.size()) {
            return false;
        }

        //If all paths are blocked but didn't need all of the z's.
        if (usedZs.size() < zIdxs.size()) {
            return false;
        }
        return true;
    }


    public Set<List<Integer>> findPath(int[][] graph, int s, int t, List<Integer> path) {

        Set<List<Integer>> foundPaths = new HashSet<List<Integer>>();

        int l = s;
        if (path.size() > 1) {
            l = path.get(path.size() - 2);
        }

        for (int j = 0; j < graph.length; j++) {
            if ((graph[s][j] != 0 || graph[j][s] == 1) && !(graph[l][s] == 1 && graph[j][s] == 1)) {
                if (j == t) {
                    foundPaths.add(path);
                } else if (!path.contains(j)) {
                    List<Integer> newPath = new ArrayList<Integer>(path);
                    newPath.add(j);
                    foundPaths.addAll(findPath(graph, j, t, newPath));
                }
            }

        }
        return foundPaths;
    }

    public int getX() {
        return xIdx;
    }

    public int getY() {
        return yIdx;
    }

    public Set<Integer> getZ() {
        return zIdxs;
    }

    public int size() {
        return zIdxs.size();
    }


    public String toString() {
        return "[" + weightStr + "](" + xIdx + " _||_ " + yIdx + " | " + (zIdxs.size() > 0 ? Arrays.deepToString(zIdxs.toArray()) : "") + ")";
    }
}
