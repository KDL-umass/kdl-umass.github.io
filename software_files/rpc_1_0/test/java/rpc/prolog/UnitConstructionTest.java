/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Aug 11, 2009
 * Time: 1:40:24 PM
 */
package rpc.prolog;

import jpl.Term;
import jpl.Query;
import junit.framework.TestCase;

import java.util.*;

import rpc.TestUtil;
import rpc.design.Unit;
import rpc.design.AttributePath;
import rpc.design.Path;
import rpc.util.PrologUtil;
import rpc.util.LogUtil;
import rpc.schema.Schema;
import rpc.schema.SchemaItem;
import rpc.schema.Attribute;
import org.apache.log4j.Logger;

public class UnitConstructionTest extends TestCase {

    private static Logger log = Logger.getLogger(UnitConstructionTest.class);

    public static final String SCHEMA_FILE = "./test/test-schema.pl";
    public static final int HOP_THRESHOLD = 7;

    /****************************************TEST SCHEMA************************************************

          ______        / \        ______        / \       ______      / \       ______
         | D    |      /DA \      | A    |      /AB \     | B    |    /BC \     | C    |
         | (w1) |-----/(wx1)\-----| (x1) |-----/(xy1)\---<| (y1) |>--/(yz1)\---<| (z1) |
         |      |     \     /     | (x2) |     \(xy2)/    |      |   \     /    | (z2) |
         ------       \   /      |      |      \   /      ------     \   /     | (z3) |
                       \ /        -----         \ /                   \ /      | (z4) |
                                                                                ------
     ***************************************************************************************************/

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testUnitAllHopZero() {
        // this will test all ESS (all units in U0 = T0 X O0)
        Query q = new Query("unitAll(BaseItem, BaseItem, Path1, Card1, Var1, BaseItem, Path2, Card2, Var2, 0, 0)");
		Hashtable[] solutions = q.allSolutions();
        Set<Unit> results = new HashSet<Unit>();
        for (Hashtable solution : solutions) {
            String baseItemStr    = solution.get("BaseItem").toString();
            List<SchemaItem> path1Str = PrologUtil.termToList((Term) solution.get("Path1"));
            String card1Str 	  = solution.get("Card1").toString();
            String var1Str 		  = solution.get("Var1").toString();
            List<SchemaItem> path2Str = PrologUtil.termToList((Term) solution.get("Path2"));
            String card2Str 	  = solution.get("Card2").toString();
            String var2Str 		  = solution.get("Var2").toString();

            AttributePath tPath = TestUtil.getAttributePath(baseItemStr, baseItemStr, path1Str, card1Str, var1Str);
            AttributePath oPath = TestUtil.getAttributePath(baseItemStr, baseItemStr, path2Str, card2Str, var2Str);

            Unit u = TestUtil.getUnit(baseItemStr, tPath, oPath);
            results.add(u);
        }
        q.close();

        Set<Unit> expected = new HashSet<Unit>();
        for (SchemaItem si : Schema.getAllSchemaItems()) {
            for (Attribute attr1 : si.getAllAttributes()) {
                if (!attr1.isPrimaryKey() && !attr1.isForeignKey()) {
                    for (Attribute attr2 : si.getAllAttributes()) {
                        if (!attr2.isPrimaryKey() && !attr2.isForeignKey()) {
                            if (!attr1.equals(attr2)) {
                                AttributePath tPath = TestUtil.getAttributePath(si.name, si.name, si.name, "one", attr1.name);
                                AttributePath oPath = TestUtil.getAttributePath(si.name, si.name, si.name, "one", attr2.name);
                                expected.add(TestUtil.getUnit(si.name, tPath, oPath));
                            }
                        }
                    }
                }
            }
        }

        printUnits("Expected units:", expected);
        printUnits("\nActual units:", results);

        TestUtil.verifySets("Testing units with zero hops - ", expected, results);
    }

    public void testUnitAttributeHopZero() {
        // this will test all ESS (all units in U0 = T0 X O0)
        Query q = new Query("unitUnique(BaseItem, BaseItem, Path1, Card1, Var1, BaseItem, Path2, Card2, Var2, 0, 0)");
		Hashtable[] solutions = q.allSolutions();
        Set<Unit> results = new HashSet<Unit>();
        for (Hashtable solution : solutions) {
            String baseItemStr    = solution.get("BaseItem").toString();
            List<SchemaItem> path1Str = PrologUtil.termToList((Term) solution.get("Path1"));
            String card1Str 	  = solution.get("Card1").toString();
            String var1Str 		  = solution.get("Var1").toString();
            List<SchemaItem> path2Str = PrologUtil.termToList((Term) solution.get("Path2"));
            String card2Str 	  = solution.get("Card2").toString();
            String var2Str 		  = solution.get("Var2").toString();

            AttributePath tPath = TestUtil.getAttributePath(baseItemStr, baseItemStr, path1Str, card1Str, var1Str);
            AttributePath oPath = TestUtil.getAttributePath(baseItemStr, baseItemStr, path2Str, card2Str, var2Str);

            Unit u = TestUtil.getUnit(baseItemStr, tPath, oPath);
            results.add(u);
        }
        q.close();

        Set<Unit> expected = new HashSet<Unit>();
        for (SchemaItem si : Schema.getAllSchemaItems()) {
            for (Attribute attr1 : si.getAllAttributes()) {
                if (!attr1.isPrimaryKey() && !attr1.isForeignKey()) {
                    for (Attribute attr2 : si.getAllAttributes()) {
                        if (!attr2.isPrimaryKey() && !attr2.isForeignKey()) {
                            if (!attr1.equals(attr2)) {
                                AttributePath tPath = TestUtil.getAttributePath(si.name, si.name, si.name, "one", attr1.name);
                                AttributePath oPath = TestUtil.getAttributePath(si.name, si.name, si.name, "one", attr2.name);
                                expected.add(TestUtil.getUnit(si.name, tPath, oPath));
                            }
                        }
                    }
                }
            }
        }

//        printUnits("Expected units:", expected);
//        printUnits("\nActual units:", results);

        TestUtil.verifySets("Testing units with zero hops - ", expected, results);
    }

    public void testUnitAllWithPathUpToTwoHops() {
        // this will test ESE from base entity a
        Query q = new Query("unitAll(a, a, Path1, Card1, Var1, Target2, Path2, Card2, Var2, 0, 2)");
		Hashtable[] solutions = q.allSolutions();
        Set<Unit> results = getResultUnitsSingletonTreatment("a", solutions);
        q.close();
        Set<Unit> expected = new HashSet<Unit>();

        List<Path> treatments = new ArrayList<Path>();
        treatments.add(TestUtil.getAttributePath("a", "a", "a", "one", "x1"));
        treatments.add(TestUtil.getAttributePath("a", "a", "a", "one", "x2"));

        List<Path> outcomes = new ArrayList<Path>();
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy1"));
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy2"));
        outcomes.add(TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1"));
        outcomes.add(TestUtil.getStructurePath("a", "ab", "a,ab", "many"));
        outcomes.add(TestUtil.getAttributePath("a", "d", "a,da,d", "one", "w1"));
        outcomes.add(TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1"));
        outcomes.add(TestUtil.getStructurePath("a", "da", "a,da", "one"));
        for (Path treatment : treatments) {
            outcomes.add(treatment);
        }

        for (Path treatment : treatments) {
            for (Path outcome : outcomes) {
                if (!treatment.equals(outcome)) {
                    expected.add(TestUtil.getUnit("a", treatment, outcome));
                }
            }
        }

//        printUnits("Expected units:", expected);
//        printUnits("\nActual units:", results);

        TestUtil.verifySets("Testing up to two hops from base entity a - ", expected, results);
    }

    public void testUnitAllWithPathUpToSevenHops() {
        // this will test ESE from base entity a
        Query q = new Query("unitAll(a, a, Path1, Card1, Var1, Target2, Path2, Card2, Var2, 0, 7)");
		Hashtable[] solutions = q.allSolutions();
        Set<Unit> results = getResultUnitsSingletonTreatment("a", solutions);
        q.close();
        Set<Unit> expected = new HashSet<Unit>();

        List<Path> treatments = new ArrayList<Path>();
        treatments.add(TestUtil.getAttributePath("a", "a", "a", "one", "x1"));
        treatments.add(TestUtil.getAttributePath("a", "a", "a", "one", "x2"));

        List<Path> outcomes = new ArrayList<Path>();
        outcomes.add(TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1")); //1 hop
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy1"));
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy2"));
        outcomes.add(TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1")); //2 hops
        outcomes.add(TestUtil.getStructurePath("a", "ab", "a,ab", "many"));
        outcomes.add(TestUtil.getAttributePath("a", "d", "a,da,d", "one", "w1"));
        outcomes.add(TestUtil.getStructurePath("a", "da", "a,da", "one"));
        outcomes.add(TestUtil.getAttributePath("a", "bc", "a,ab,b,bc", "many", "yz1")); //3 hops
        outcomes.add(TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z1")); //4 hops
        outcomes.add(TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z2"));
        outcomes.add(TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z3"));
        outcomes.add(TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z4"));
        outcomes.add(TestUtil.getStructurePath("a", "bc", "a,ab,b,bc", "many"));
        outcomes.add(TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc", "many", "yz1")); //5 hops
        outcomes.add(TestUtil.getAttributePath("a", "b", "a,ab,b,bc,c,bc,b", "many", "y1")); //6 hops
        //removed when cycles exist in structure paths
        //outcomes.add(TestUtil.getStructurePath("a", "b", "a,ab,b,bc,c,bc,b", "many")); //6 hops
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab,b,bc,c,bc,b,ab", "many", "xy1")); //7 hops
        outcomes.add(TestUtil.getAttributePath("a", "ab", "a,ab,b,bc,c,bc,b,ab", "many", "xy2"));
        outcomes.add(TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc,b,bc", "many", "yz1"));
        for (Path treatment : treatments) {
            outcomes.add(treatment);
        }

        for (Path treatment : treatments) {
            for (Path outcome : outcomes) {
                if (!treatment.equals(outcome)) {
                    expected.add(TestUtil.getUnit("a", treatment, outcome));
                }
            }
        }

//        printUnits("Expected units:", expected);
//        printUnits("\nActual units:", results);

        TestUtil.verifySets("Testing with up to 7 hops from base entity a - ", expected, results);
    }

    /***************************
      PRIVATE HELPER FUNCTIONS
     ***************************/
    private void printUnits(String header, Set<Unit> units) {
        log.debug(header);
        for (Unit u : units) {
            log.debug(u);
        }
    }

    private Set<Unit> getResultUnitsSingletonTreatment(String baseItem, Hashtable[] solutions) {
        Set<Unit> results = new HashSet<Unit>();
        for (Hashtable solution : solutions) {
            List<SchemaItem> path1Str = PrologUtil.termToList((Term) solution.get("Path1"));
            String card1Str 	  = solution.get("Card1").toString();
            String var1Str 		  = solution.get("Var1").toString();
            String target2Str     = solution.get("Target2").toString();
            List<SchemaItem> path2Str = PrologUtil.termToList((Term) solution.get("Path2"));
            String card2Str 	  = solution.get("Card2").toString();
            String var2Str 		  = solution.get("Var2").toString();

            AttributePath tPath = TestUtil.getAttributePath(baseItem, baseItem, path1Str, card1Str, var1Str);
            Path oPath;
            if (var2Str.equals(target2Str)) {
                oPath = TestUtil.getStructurePath(baseItem, target2Str, path2Str, card2Str);
            }
            else {
                oPath = TestUtil.getAttributePath(baseItem, target2Str, path2Str, card2Str, var2Str);
            }

            Unit u = TestUtil.getUnit(baseItem, tPath, oPath);
            results.add(u);
        }
        return results;
    }
}
