/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 25, 2010
 * Time: 2:02:50 PM
 */
package rpc.model.util;

import java.util.*;

/**
 * This class translates the dependencies within a model into the dot visualization language.
 */
public class ModelVisualizer {

    /* Example
        //Add in dependencies
        x1 -> y1 [label="[b, ab, a].x1 --> [b].y1\n [a, ab, b].y1 --> [a].x1", dir=none];
        y1 -> z2 [label="[c, bc, b].y1 --> [c].z2"];
        y1 -> z2 [label="[c, bc, b, bc, c, bc, b].y1 --> [c].z2"];
        w1 -> z1 [label="[c, bc, b, ab, a, da, d].w1 --> [c].z1"];
        x2 -> bc_exists [label="[bc, b, ab, a].x2 --> [bc].bc_id"];\
        ab_exists -> z3 [label="[c, bc, b, ab].ab_id --> [c].z3"];
    */

    /**
     * Get the list of lines of the translation of the model into the dot language.
     * @param model the model to translate into dot.
     * @param withEdgeLabels flag to control whether or not the dependencies will be annotated with path information.
     * @return a list of lines in the dot language.
     */
    public static List<String> getDot(Model model, boolean withEdgeLabels) {
        List<String> dotLines = new ArrayList<String>();
        dotLines.add("//Add in dependencies");

        Set<Dependency> undirectedAdded = new HashSet<Dependency>();

        Map<VertexPair, Set<Dependency>> allDependencies = model.getAllDependencies();
        for (VertexPair vp : allDependencies.keySet()) {
            //only draw in non-trivial dependencies
            if (model.hasNonTrivialDependence(vp)) {
                Set<Dependency> dependencies = allDependencies.get(vp);
                for (Dependency dep : dependencies) {
                    if (dep.isTrivial()) {
                        continue;
                    }

                    if (!model.checkReverseDependence(vp, dep)) {
                        //dependence is directed

                        String v1Name = vp.v1.getAttribute() + (vp.v1.isStructure() ? "_exists" : "");
                        String v2Name = vp.v2.getAttribute() + (vp.v2.isStructure() ? "_exists" : "");

                        if (withEdgeLabels) {
                            dotLines.add(v1Name + " -> " + v2Name +
                                    " [label=\"" + dep.unit.visualize() + "\"];");
                        }
                        else {
                            dotLines.add(v1Name + " -> " + v2Name + ";");
                        }
                    }
                    else {
                        //dependence is undirected
                        if (undirectedAdded.contains(dep.reverse())) {
                            //already added
                            continue;
                        }

                        String v1Name = vp.v1.getAttribute() + (vp.v1.isStructure() ? "_exists" : "");
                        String v2Name = vp.v2.getAttribute() + (vp.v2.isStructure() ? "_exists" : "");

                        if (v1Name.compareToIgnoreCase(v2Name) < 0) { //always add in lexicographic order
                            if (withEdgeLabels) {
                                dotLines.add(v1Name + " -> " + v2Name +
                                    " [label=\"" + dep.unit.visualize() + "\\n" + dep.unit.reverse().visualize() + "\", dir=none];");
                            }
                            else {
                                dotLines.add(v1Name + " -> " + v2Name + " [dir=none];");                                
                            }
                            undirectedAdded.add(dep);
                        }
                    }
                }
            }
        }

        return dotLines;
    }

}