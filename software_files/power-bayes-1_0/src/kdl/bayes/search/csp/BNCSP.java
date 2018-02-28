/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.search.csp;

import kdl.bayes.util.constraint.Acyclicity;
import kdl.bayes.util.constraint.Constraint;
import kdl.bayes.util.constraint.Dependence;
import kdl.bayes.util.constraint.dSeparation;
import org.apache.log4j.Logger;

import java.util.*;

public class BNCSP implements CSP {

    protected static Logger log = Logger.getLogger(BNCSP.class);

    List<String> varNames;

    List<Integer> variables;
    Map<Object, List<Object>> possibleValues;

    Set<Constraint> constraints;

    int[][] dag;
    boolean[][] pdag;
    boolean[][] committedGraph;
    boolean[][] committedSkeleton;

    int numNodes = -1;

    public BNCSP(List<String> varNames, Set<Constraint> dSeparations, List<Object> indepValues, List<Object> depValues) {
        List<Integer> tmpVars = new ArrayList<Integer>();
        this.varNames = varNames;
        numNodes = varNames.size();
        for (int i = 0; i < varNames.size(); i++) {
            for (int j = i + 1; j < varNames.size(); j++) {
                int possibleEdge = (i * numNodes) + j;
                tmpVars.add(possibleEdge);
            }
        }

        possibleValues = new HashMap<Object, List<Object>>();


        this.constraints = dSeparations;
        this.constraints.add(new Acyclicity()); //Acyclicity constraint

        List<Integer> indepVars = getIndependentVars();
        List<Integer> depVars = new ArrayList<Integer>(tmpVars);
        depVars.removeAll(indepVars);

        //Add dependence constraint for all depVars
        for (Integer depVar : depVars) {
            Dependence depConstraint = new Dependence(depVar, numNodes);
            constraints.add(depConstraint);
        }

        //Add different orderings for the two kinds of variables
        for (Integer indepVar : indepVars) {
            possibleValues.put(indepVar, indepValues);
        }

        for (Integer depVar : depVars) {
            possibleValues.put(depVar, depValues);
        }

        //Always put indep vars in first, followed by dep vars.
        //More ways to violate indep (2/3) than to violate dep (1/3)
        //Leads to faster pruning.
        variables = new ArrayList<Integer>();
        variables.addAll(indepVars);
        variables.addAll(depVars);
    }

    public BNCSP(List<String> varNames, Set<Constraint> dSeparations) {
        List<Integer> tmpVars = new ArrayList<Integer>();
        this.varNames = varNames;
        numNodes = varNames.size();
        for (int i = 0; i < varNames.size(); i++) {
            for (int j = i + 1; j < varNames.size(); j++) {
                int possibleEdge = (i * numNodes) + j;
                tmpVars.add(possibleEdge);
            }
        }

        possibleValues = new HashMap<Object, List<Object>>();


        this.constraints = dSeparations;
        this.constraints.add(new Acyclicity()); //Acyclicity constraint

        List<Integer> indepVars = getIndependentVars();
        List<Integer> depVars = new ArrayList<Integer>(tmpVars);
        depVars.removeAll(indepVars);

        //Add dependence constraint for all depVars
        for (Integer depVar : depVars) {
            Dependence depConstraint = new Dependence(depVar, numNodes);
            constraints.add(depConstraint);
        }


        List<Object> indepValues = new ArrayList<Object>();
        indepValues.add("R");
        indepValues.add("L");
        indepValues.add("E");
        List<Object> depValues = new ArrayList<Object>();
        depValues.add("E");
        depValues.add("L");
        depValues.add("R");

        //Add different orderings for the two kinds of variables
        for (Integer indepVar : indepVars) {
            possibleValues.put(indepVar, indepValues);
        }

        for (Integer depVar : depVars) {
            possibleValues.put(depVar, depValues);
        }

        variables = new ArrayList<Integer>();
        variables.addAll(indepVars);
        variables.addAll(depVars);
    }

    public Set<Constraint> getConstraints() {
        return constraints;
    }

    public Object getUnassignedVariable(Assignment varAssign) {
        if (varAssign.size() == variables.size()) {
            return null;
        }
        return variables.get(varAssign.size());
    }

    public Object getRandomVariable() {
        Random rand = new Random();
        int varIdx = rand.nextInt(variables.size());
        return variables.get(varIdx);
    }

    public boolean isConsistent(Assignment varAssign) {
        reset();
        for (Constraint constraint : constraints) {
            //log.info("\tChecking constraint: " + constraint);
            if (!constraint.isValid(varAssign, this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts the number of independence constraints that are violated.
     * Bayes nets must be acyclic so if that constraint fails then it is worth MAX_VALUE;
     *
     * @param varAssign
     * @return
     */
    public int numConflicts(Assignment varAssign) {
        reset();
        int numViolated = 0;
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(varAssign, this)) {
                //log.info(constraint);
                if (constraint instanceof Acyclicity) {
                    return Integer.MAX_VALUE;
                }
                numViolated++;
            }
        }
        return numViolated;
    }

    public double getConflictWeight(Assignment varAssign) {
        reset();
        double numViolated = 0.0;
        for (Constraint constraint : constraints) {
            if (!constraint.isValid(varAssign, this)) {
                numViolated += constraint.getWeight();
            }
        }
        return numViolated;
    }

    public int numVars() {
        return variables.size();
    }

    /**
     * Chooses an intial BN structure, initializes all edges but must not violate the acyclicity constraint.
     *
     * @return
     */
    public Assignment getCompleteAssignment() {
        BNCSP assignCSP = new BNCSP(varNames, new HashSet<Constraint>());
        //Acyclicity is included in the constraints by default.
        Backtracking bt = new Backtracking(assignCSP);
        return bt.search();
    }

    public List getOrderedValues(Object var) {
        return possibleValues.get(var);
    }

    public int[][] getDag(Assignment currAssign) {
        if (dag != null) {
            return dag;
        }

        dag = new int[numNodes][numNodes];

        for (Integer edge : variables) {
            String value = (String) currAssign.getValue(edge);

            int i = edge / numNodes;
            int j = edge % numNodes;

            if (value == null) {  //This edge is uncommitted
                dag[i][j] = -1;
                dag[j][i] = -1;
            } else if (value.equalsIgnoreCase("R")) {    //This edge runs i -> j
                dag[i][j] = 1;
                dag[j][i] = 0;
            } else if (value.equalsIgnoreCase("L")) {    //This edge runs j -> i
                dag[j][i] = 1;
                dag[i][j] = 0;
            } else if (value.equalsIgnoreCase("E")) {    //This edge does not exist
                dag[i][j] = 0;
                dag[j][i] = 0;
            } else {
                log.warn("Undefined value in Assignment");
            }

        }

        return dag;

    }

    public boolean[][] getPdag(Assignment currAssign) {
        if (pdag != null) {
            return pdag;
        }

        pdag = new boolean[numNodes][numNodes];

        for (Integer edge : variables) {
            String value = (String) currAssign.getValue(edge);

            int i = edge / numNodes;
            int j = edge % numNodes;

            if (value == null) {  //This edge is uncommitted
                pdag[i][j] = true;
                pdag[j][i] = true;
            } else if (value.equalsIgnoreCase("R")) {    //This edge runs i -> j
                pdag[i][j] = true;
                pdag[j][i] = false;
            } else if (value.equalsIgnoreCase("L")) {    //This edge runs j -> i
                pdag[j][i] = true;
                pdag[i][j] = false;
            } else if (value.equalsIgnoreCase("E")) {    //This edge does not exist
                pdag[i][j] = false;
                pdag[j][i] = false;
            } else {
                log.warn("Undefined value in Assignment");
            }

        }

        return pdag;

    }


    public boolean[][] getCommittedGraph(Assignment assignment) {
        if (committedGraph != null) {
            return committedGraph;
        }


        committedGraph = new boolean[numNodes][numNodes];

        if (assignment == null) {
            return committedGraph;
        }

        for (Integer edge : variables) {
            String value = (String) assignment.getValue(edge);

            int i = edge / numNodes;
            int j = edge % numNodes;

            if (value == null || value.equalsIgnoreCase("E")) {  //This edge is uncommitted
                committedGraph[i][j] = false;
                committedGraph[j][i] = false;
            } else if (value.equalsIgnoreCase("R")) {    //This edge runs i -> j
                committedGraph[i][j] = true;
                committedGraph[j][i] = false;
            } else if (value.equalsIgnoreCase("L")) {    //This edge runs j -> i
                committedGraph[j][i] = true;
                committedGraph[i][j] = false;
            } else {
                log.warn("Undefined value in Assignment");
            }

        }

        return committedGraph;

    }

    public boolean[][] getCommittedSkeleton(Assignment assignment) {
        if (committedSkeleton != null) {
            return committedSkeleton;
        }

        committedSkeleton = new boolean[numNodes][numNodes];

        for (Integer edge : variables) {
            String value = (String) assignment.getValue(edge);

            int i = edge / numNodes;
            int j = edge % numNodes;

            if (value == null || value.equalsIgnoreCase("E")) {  //This edge is uncommitted  or committed to E
                committedSkeleton[i][j] = false;
                committedSkeleton[j][i] = false;
            } else if (value.equalsIgnoreCase("R") || value.equalsIgnoreCase("L")) {    //There is an edge between i and j
                committedSkeleton[i][j] = true;
                committedSkeleton[j][i] = true;
            } else {
                log.warn("Undefined value in Assignment");
            }

        }

        return committedSkeleton;
    }

    public void reset() {
        dag = null;
        pdag = null;
        committedGraph = null;
        committedSkeleton = null;
    }

    protected List<Integer> getIndependentVars() {
        List<Integer> independentVars = new ArrayList<Integer>();
        for (Constraint generic : constraints) {
            if (generic instanceof dSeparation) {
                dSeparation constraint = (dSeparation) generic;

                int x = (constraint).getX();
                int y = (constraint).getY();

                int edgeVar = -1;
                if (x < y) {
                    edgeVar = x * numNodes + y;
                } else {
                    edgeVar = y * numNodes + x;
                }

                independentVars.add(edgeVar);
            }
        }

        return independentVars;

    }

}
