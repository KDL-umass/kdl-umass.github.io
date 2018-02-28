/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 23, 2009
 * Time: 11:25:38 AM
 */

package rpc.util;

import rpc.model.util.Variable;
import rpc.model.util.StructureVariable;
import rpc.schema.Relationship;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import cern.jet.random.engine.RandomEngine;
import cern.jet.random.Uniform;

/**
 * Utility class that defines methods used in the causal model generator and mapping for consistent
 * naming conventions of variables and vertices. 
 */
public class CausalModelUtil {

    private final static Random rand = new Random();

    /**
     * Converts the given variable to the standard name used for vertices.
     * @param var the variable to translate.
     * @return the name used for the vertex representation of the variable.
     */
    public static String variableToVertex(Variable var) {
        String vertex;
        if (var instanceof StructureVariable) {
            vertex = var.getSource().name;
        }
        else {
            vertex = var.getSource().name;
            vertex += ".";
            vertex += var.name();
        }
        return vertex;
    }

    /**
     * Converts the given relationship to the standard name used for vertices.
     * @param rel the relationship.
     * @return the name used for the vertex representation of the relationship.
     */
    public static String relationshipToVertex(Relationship rel) {
        return rel.name;
    }

    /**
     * Computes the cross product of elements in each list.
     * @param lists a list of lists of objects from which to take the cross product.
     * @return a list of list of objects consisting of the cross product of all elements
     * within each of the input lists.
     */
    public static List<List<Object>> crossList(List<List<Object>> lists) {
        return _cartesianProduct(0, lists);
    }

    private static List<List<Object>> _cartesianProduct(int index, List<List<Object>> lists) {
        List<List<Object>> ret = new ArrayList<List<Object>>();
        if (index == lists.size()) {
            ret.add(new ArrayList<Object>());
        }
        else {
            for (Object obj : lists.get(index) ) {
                for (List<Object> sublist : _cartesianProduct(index+1, lists)) {
                    List<Object> orderedSubList = new ArrayList<Object>();
                    orderedSubList.add(obj);
                    orderedSubList.addAll(sublist);
                    ret.add(orderedSubList);
                }
            }
        }
        return ret;
    }

    /**
     * Sample from a uniform dirichlet, assuming alpha_1, ..., alpha_k are all equal to 1.
     * Uses the special case of sampling from k gamma distributions.
     * @param k the number of values to sample.
     * @return a uniform dirichlet probability distribution of k values.
     */
    public static double[] uniformDirichlet(int k) {
        double[] probs = new double[k];

        Random r = new Random();
        double sum = 0.0;
        for (int i=0; i<k; i++) {
            double val = -1.0*Math.log(r.nextDouble());
            sum += val;
            probs[i] = val;
        }
        for (int i=0; i<k; i++) {
            probs[i] /= sum;
        }
        return probs;
    }

    /**
     * Sample from a bounded Pareto distribution with hard-coded lower and upper bounds of 3 and 20.
     * Useful for generating expected number of links between entities.
     * @return a value sampled from the bounded Pareto distribution with lower and upper
     * bounds of 3 and 20.
     */
    public static double boundedPareto() {

        double L = 3.0; //lower bound (expected 3 links as lower bound)
        double H = 20.0; //upper bound (expected 20 links as lower bound)
        double alpha = 0.9; //shape parameter

        RandomEngine randEng = new RandomEngine() {
            public int nextInt() {
                return rand.nextInt();
            }
        };

        Uniform uniformDistr = new Uniform(0, 1, randEng);

        double u = uniformDistr.nextDouble();

        //(-(UH^alpha - UL^alpha - H^alpha)/(H^alpha*L^alpha))^ (-1/alpha)
        //Taken from Wikipedia: http://en.wikipedia.org/wiki/Pareto_distribution
        return Math.pow(-1.0*((u*Math.pow(H, alpha) - u*Math.pow(L, alpha) - Math.pow(H, alpha))/
                (Math.pow(H, alpha)*Math.pow(L, alpha))), -1.0/alpha);
    }

}