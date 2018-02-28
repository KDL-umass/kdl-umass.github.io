/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 12, 2009
 * Time: 11:28:26 AM
 */
package rpc.util;

import rpc.schema.SchemaItem;
import rpc.schema.Schema;
import rpc.design.*;
import rpc.model.util.AttributeVariable;

import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import jpl.Query;
import jpl.Term;

/**
 * Utility class that provides methods to compare, standardize, and build paths and units.
 */
public class DesignUtil {

    /**
     * Checks whether or not two paths (lists of schema items) are the same.
     * @param path1 first list of schema items.
     * @param path2 second list of schema items.
     * @return true if the paths are equal; false otherwise.
     */
    public static boolean comparePaths(List<SchemaItem> path1, List<SchemaItem> path2) {
            boolean isEqual = path1.size() == path2.size();
            if (! isEqual) {
                return false;
            }

            //now check through the paths
            for (int i=0; i<path1.size(); i++) {
                if (! path1.get(i).equals(path2.get(i))) {
                    return false;
                }
            }
            return true;
    }

    /**
     * Creates a new unit based on the standardized version (i.e., having singleton outcomes) of a given unit.
     * @param u the unit to standardize.
     * @return the standardized version of the unit. Will return null if no standardized version exists.
     */
    public static Unit standardize(Unit u) {
        SchemaItem baseItem = u.outcomePath.getTarget();
        //first truncate outcome path to its source, becomes new base item
        //base item can be either entity or relationship
        List<SchemaItem> canonPath = new ArrayList<SchemaItem>();
        canonPath.add(baseItem);

        SchemaItem treatmentItem = u.treatmentPath.getTarget();

        String treatment = u.treatmentPath instanceof StructurePath ? treatmentItem.name : u.treatment.name();
        String outcome = u.outcomePath instanceof StructurePath ? baseItem.name : u.outcome.name();

        int minThresh;
        int maxThresh;

        int overlapSize = findOverlapSize(u.treatmentPath.getPath(), u.outcomePath.getPath());

        List<SchemaItem> newTreatmentPath = buildNewPath(u.treatmentPath.getPath(), u.outcomePath.getPath(), overlapSize);

        minThresh = u.treatmentPath.getPath().size() + u.outcomePath.getPath().size() - 2*overlapSize;

        maxThresh = minThresh;

        try {
            return getUnitUnique(baseItem, treatmentItem, newTreatmentPath, treatment,
                baseItem, canonPath, Cardinality.ONE, outcome, minThresh, maxThresh);
        }
        catch(NullPointerException npe) {
            return null;      
        }
    }

    /**
     * Gets the unique unit (having singleton outcomes) from Prolog based on having most details of the
     * unit already known.
     * @param baseItem the base item of the unit.
     * @param target1 the target of the treatment path.
     * @param path1 the treatment path.
     * @param treatment the name of the treatment variable.
     * @param target2 the target of the outcome path.
     * @param path2 the outcome path.
     * @param card2 the cardinality of the outcome path (ONE).
     * @param outcome the name of the outcome variable.
     * @param minThresh the minimum number of hops in the treatment path.
     * @param maxThresh the maximum number of hops in the treatment path.
     * @return the unique unit with the passed in details.
     */
    public static Unit getUnitUnique(SchemaItem baseItem, SchemaItem target1, List<SchemaItem> path1,
                                String treatment, SchemaItem target2, List<SchemaItem> path2, Cardinality card2,
                                String outcome, int minThresh, int maxThresh) {

        String path1Str = buildStringFromPath(path1);
        String path2Str = buildStringFromPath(path2);

        String s = "unitUnique(" + baseItem.name + ", " + target1.name + ", " + path1Str + ", Card1, " + treatment + ", ";
        s += target2.name + ", " + path2Str + ", " + card2.toString().toLowerCase() + ", " + outcome + ", " + minThresh + ", " + maxThresh + ")";

        Hashtable solution = Query.oneSolution(s);
        Cardinality card1 = Cardinality.valueOf(((Term) solution.get("Card1")).name().toUpperCase());

        Path treatmentPath;
        Path outcomePath;

        if (Schema.isRelationship(treatment)) {
            treatmentPath = new StructurePath(baseItem, target1, path1, card1);
        }
        else {
            treatmentPath = new AttributePath(baseItem, target1, path1, card1, target1.getAttribute(treatment));
        }

        if (Schema.isRelationship(outcome)) {
            outcomePath = new StructurePath(baseItem, target2, path2, card2);    
        }
        else {
            outcomePath = new AttributePath(baseItem, target2, path2, card2, target2.getAttribute(outcome));
        }

        return new Unit(baseItem, treatmentPath, outcomePath);
    }

    /**
     * Gets the number of elements that overlap (at the beginning) in the two paths of schema items.
     * @param path1 the first list of schema items.
     * @param path2 the second list of schema items.
     * @return the number of elements in the starting overlap.
     */
    public static int findOverlapSize(List<SchemaItem> path1, List<SchemaItem> path2) {
        if (path1.size() <= path2.size() ) {
            for (int i=0; i<path1.size(); i++) {
                if (! path1.get(i).equals(path2.get(i))) {
                    return i;
                }
            }
            return path1.size();
        }
        else {
            for (int i=0; i<path2.size(); i++) {
                if (! path2.get(i).equals(path1.get(i))) {
                    return i;
                }
            }
            return path2.size();
        }
    }

    /**
     * Builds a new path list of schema items based on two paths and the number of elements that overlap.
     * @param path1 the first list of schema items.
     * @param path2 the second list of schema items.
     * @param overlapIdx the index of the final element that overlaps in the paths.
     * @return the new path, without overlap, constructed from the two given paths.
     */
    public static List<SchemaItem> buildNewPath(List<SchemaItem> path1, List<SchemaItem> path2, int overlapIdx) {
        List<SchemaItem> newPath = new ArrayList<SchemaItem>();
        for (int i=path2.size()-1; i>=overlapIdx; i--) {
            newPath.add(path2.get(i));
        }
        for (int i=overlapIdx-1; i<path1.size(); i++) {
            newPath.add(path1.get(i));
        }        
        return newPath;
    }

    private static String buildStringFromPath(List<SchemaItem> path) {
        String pathStr = "[";
        for (SchemaItem si : path) {
            pathStr += si.name + ", ";
        }
        pathStr = pathStr.substring(0, pathStr.length()-2);
        pathStr += "]";

        return pathStr;
    }

    /**
     * Constructs a new path by removing the starting relationship in the given path.
     * @param oldPath the old path to rewrite.
     * @return a new path that starts with the first entity in the given path.
     */
    public static Path rewriteFromEntity(Path oldPath) {
        //assumes oldPath begins with a relationship and has more than one element on the path
        if (! Schema.isRelationship(oldPath.getPath().get(0).name) || oldPath.getPath().size() <= 1) {
            return null;            
        }
        Path newPath;
        SchemaItem newBase = oldPath.getPath().get(1);
        SchemaItem newTarget = oldPath.getTarget();
        List<SchemaItem> choppedPath = new ArrayList<SchemaItem>(oldPath.getPath().subList(1, oldPath.getPath().size()));
        Cardinality newCard = oldPath.getCardinality();
        if (oldPath instanceof AttributePath) {
            newPath = new AttributePath(newBase, newTarget, choppedPath, newCard,
                    ((AttributeVariable) oldPath.getVariable()).getAttribute());
        }
        else {
            newPath = new StructurePath(newBase, newTarget, choppedPath, newCard);

        }
        return newPath;
    }
}