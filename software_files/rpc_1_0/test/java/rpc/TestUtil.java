/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 */ 
package rpc;

import junit.framework.Assert;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;

import rpc.design.*;
import rpc.schema.SchemaItem;
import rpc.schema.Attribute;
import rpc.schema.Schema;
import rpc.util.LogUtil;
import jpl.Query;

/**
 * Helper class that defines static methods useful to testing.
 */
public class TestUtil {

    private static boolean isSchemaLoaded = false;
    private static boolean isRPCLoaded = false;
    private static final String RPC_HOME = System.getenv().get("RPC_HOME");

    private TestUtil() {
    }

    public static void initLog() {        
        LogUtil.initApp();
    }

    public static void loadSchemaOncePerTest(String schemaFile) {
        if (! isSchemaLoaded) {
            Schema.loadSchema(schemaFile);
            isSchemaLoaded = true;
        }
    }

    public static void loadRPCOncePerTest() {
        if (!isRPCLoaded) {
            String s = String.format("consult('%s/src/prolog/rpc-modules.pl')", RPC_HOME);
            Query.oneSolution(s);
            isRPCLoaded = true;
        }
    }

    /**
     * Checks a list of strings, order counts
     *
     * @param correct
     * @param suspect
     */
    public static void verifyStringList(String message, List<String> correct, List<String> suspect) {
        Assert.assertEquals(message, correct.size(), suspect.size());
        for (int i = 0; i < correct.size(); i++) {
            Assert.assertEquals(message, correct.get(i), suspect.get(i));
        }
    }

    public static void verifyStringInList(String message, List<String> correct, String suspect) {
        boolean found = false;
        for (String el : correct) {
            if (el.equals(suspect))
                found = true;
        }

        // we didn't find a match
        Assert.assertTrue(message + "- Unmatched: " + suspect + " in list: " + correct, found);
    }

    /**
     * Checks a string is not in a list
     *
     * @param correct
     * @param suspect
     */
    public static void verifyStringNotInList(String message, List<String> correct, String suspect) {
        for (String el : correct) {
            Assert.assertFalse(message, el.equals(suspect));
        }
    }

    /**
     * Checks a set of objects, order doesn't count
     *
     * @param correct
     * @param suspect
     */
    public static void verifySets(String message, Set correct, Set suspect) {
        Assert.assertEquals(message, correct.size(), suspect.size());
        for (Object el : correct) {
            Assert.assertTrue(message + " suspect set is missing " + el, suspect.contains(el));
        }
    }

    public static void unitEquals(Unit correct, Unit suspect) {
        String message = "\nExpected: " + correct;
        message += "\nActual: " + suspect;
        Assert.assertEquals(message, correct, suspect);
    }

    public static void verifyLists(List correct, List suspect) {
        Assert.assertEquals(correct.size(), suspect.size());
        for (Object o : correct) {
            Assert.assertTrue(suspect.contains(o));
        }            
        for (Object o : suspect) {
            Assert.assertTrue(correct.contains(o));
        }
    }

    public static void verifyArrays(Object[] correct, Object[] suspect) {
        Assert.assertEquals(correct.length, suspect.length);
        for (int i=0; i<correct.length; i++) {
            Assert.assertEquals(correct[i], suspect[i]);
        }
    }
    
    public static void verifyMaps(Map correct, Map suspect) {
        Assert.assertEquals(correct.size(), suspect.size());
        for (Object o : correct.keySet()) {
            Assert.assertTrue(suspect.containsKey(o) && correct.get(o).equals(suspect.get(o)));
        }
    }

    /***************************************************
      PRIVATE HELPER FUNCTIONS -- modularize unit tests
     ***************************************************/
    public static List<SchemaItem> buildPath(String pathItemStr) {
        List<SchemaItem> path = new ArrayList<SchemaItem>();
        String[] pathItems = pathItemStr.split(",");
        for (String pathItem : pathItems) {
            pathItem = pathItem.trim();
            path.add(Schema.getSchemaItem(pathItem.toLowerCase()));
        }
        return path;
    }

    public static AttributePath getAttributePath(String base, String target, String pathItemStr, String cardStr, String attrName) {
        SchemaItem baseItem = Schema.getSchemaItem(base.toLowerCase());
        SchemaItem targetItem = Schema.getSchemaItem(target.toLowerCase());
        List<SchemaItem> path = buildPath(pathItemStr);
        Cardinality card;
        if (cardStr.equalsIgnoreCase("one")) {
            card = Cardinality.ONE;
        }
        else {
            card = Cardinality.MANY;
        }
        Attribute attr = targetItem.getAttribute(attrName.toLowerCase());

        return new AttributePath(baseItem, targetItem, path, card, attr);
    }

    public static StructurePath getStructurePath(String base, String target, String pathItemStr, String cardStr) {
        SchemaItem baseItem = Schema.getSchemaItem(base.toLowerCase());
        SchemaItem targetItem = Schema.getSchemaItem(target.toLowerCase());
        List<SchemaItem> path = buildPath(pathItemStr);
        Cardinality card;
        if (cardStr.equalsIgnoreCase("one")) {
            card = Cardinality.ONE;
        }
        else {
            card = Cardinality.MANY;
        }

        return new StructurePath(baseItem, targetItem, path, card);
    }

    public static AttributePath getAttributePath(String base, String target, List<SchemaItem> path, String cardStr, String attrName) {
        SchemaItem baseItem = Schema.getSchemaItem(base.toLowerCase());
        SchemaItem targetItem = Schema.getSchemaItem(target.toLowerCase());
        Cardinality card;
        if (cardStr.equalsIgnoreCase("one")) {
            card = Cardinality.ONE;
        }
        else {
            card = Cardinality.MANY;
        }
        Attribute attr = targetItem.getAttribute(attrName.toLowerCase());

        return new AttributePath(baseItem, targetItem, path, card, attr);
    }

    public static StructurePath getStructurePath(String base, String target, List<SchemaItem> path, String cardStr) {
        SchemaItem baseItem = Schema.getSchemaItem(base.toLowerCase());
        SchemaItem targetItem = Schema.getSchemaItem(target.toLowerCase());
        Cardinality card;
        if (cardStr.equalsIgnoreCase("one")) {
            card = Cardinality.ONE;
        }
        else {
            card = Cardinality.MANY;
        }

        return new StructurePath(baseItem, targetItem, path, card);
    }

    public static Unit getUnit(String base, Path tPath, Path oPath) {
        return new Unit(Schema.getSchemaItem(base.toLowerCase()), tPath, oPath);
    }

}