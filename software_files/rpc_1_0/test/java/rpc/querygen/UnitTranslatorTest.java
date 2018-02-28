/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 7, 2009
 * Time: 12:51:42 PM
 */
package rpc.querygen;

import junit.framework.TestCase;
import rpc.TestUtil;
import rpc.design.AttributePath;
import rpc.design.Unit;
import rpc.design.StructurePath;
import rpc.util.LogUtil;

public class UnitTranslatorTest extends TestCase {

    public static final String SCHEMA_FILE = "./test/test-schema.pl";

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

    public void testUnitsNoAggs() {
        //[b ab a].x1 --> [b].y1
        AttributePath tPath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        AttributePath oPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit unit = TestUtil.getUnit("b", tPath, oPath);

        String expectedQuery = "SELECT a.a_id, ab.ab_id, b.b_id, a.x1 AS v1, b.y1 AS v2";
        expectedQuery       += "\nFROM a, ab, b";
        expectedQuery       += "\nWHERE a.a_id = ab.a_id AND ab.b_id = b.b_id";

        UnitTranslator ut = new UnitTranslator(unit, null, null);
        String actualQuery = ut.getQuery();
        assertEquals(expectedQuery, actualQuery);

        //[b bc c bc b ab a].x2 --> [b ab a da d].w1
        tPath = TestUtil.getAttributePath("b", "a", "b,bc,c,bc,b,ab,a", "many", "x2");
        oPath = TestUtil.getAttributePath("b", "d", "b,ab,a,da,d", "one", "w1");
        unit = TestUtil.getUnit("b", tPath, oPath);

        expectedQuery  = "SELECT a1.a_id AS a_id1, ab1.ab_id AS ab_id1, b1.b_id AS b_id1, ";
        expectedQuery += "bc1.bc_id AS bc_id1, c.c_id, bc2.bc_id AS bc_id2, b2.b_id AS b_id2, ab2.ab_id AS ab_id2, ";
        expectedQuery += "a2.a_id AS a_id2, da.da_id, d.d_id, a1.x2 AS v1, d.w1 AS v2";
        expectedQuery += "\nFROM a a1, ab ab1, b b1, bc bc1, c, bc bc2, b b2, ab ab2, a a2, da, d";
        expectedQuery += "\nWHERE a1.a_id = ab1.a_id AND ab1.b_id = b1.b_id AND b1.b_id = bc1.b_id";
        expectedQuery += " AND bc1.c_id = c.c_id AND c.c_id = bc2.c_id AND bc2.b_id = b2.b_id";
        expectedQuery += " AND b2.b_id = ab2.b_id AND ab2.a_id = a2.a_id AND a2.a_id = da.a_id AND da.d_id = d.d_id";
        expectedQuery += " AND ab1.ab_id <> ab2.ab_id AND bc1.bc_id <> bc2.bc_id AND a1.a_id <> a2.a_id AND b1.b_id <> b2.b_id";

        ut = new UnitTranslator(unit, null, null);
        actualQuery = ut.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }

    public void testUnitTreatmentAgg(){
        //[a ab b].b --> [a].x1
        StructurePath tPath = TestUtil.getStructurePath("a", "ab", "a,ab", "many");
        AttributePath oPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Unit unit = TestUtil.getUnit("a", tPath, oPath);

        String treatmentQuery = "SELECT a.a_id, count(ab.ab_id) AS v1";
        treatmentQuery += "\nFROM a, ab";
        treatmentQuery += "\nWHERE a.a_id = ab.a_id AND ab.ab_id IS NOT NULL";
        treatmentQuery += "\nGROUP BY a.a_id";

        String outcomeQuery = "SELECT a.a_id, a.x1 AS v2";
        outcomeQuery += "\nFROM a";
        outcomeQuery += "\nWHERE a.x1 IS NOT NULL";

        String expectedQuery = "SELECT outcome.a_id, v1, v2";
        expectedQuery += "\nFROM\n(" + treatmentQuery + ") treatment,\n(" + outcomeQuery + ") outcome";
        expectedQuery += "\nWHERE treatment.a_id = outcome.a_id";


        UnitTranslator ut = new UnitTranslator(unit, tPath.getAggregators().get(0), null);
        String actualQuery = ut.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }

    public void testUnitOutcomeAgg(){
        //[b].y1 --> [b bc c].c
        AttributePath tPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        StructurePath oPath = TestUtil.getStructurePath("b", "bc", "b,bc", "many");
        Unit unit = TestUtil.getUnit("b", tPath, oPath);

        String treatmentQuery = "SELECT b.b_id, b.y1 AS v1";
        treatmentQuery += "\nFROM b";
        treatmentQuery += "\nWHERE b.y1 IS NOT NULL";

        String outcomeQuery = "SELECT b.b_id, count(bc.bc_id) AS v2";
        outcomeQuery += "\nFROM b, bc";
        outcomeQuery += "\nWHERE b.b_id = bc.b_id AND bc.bc_id IS NOT NULL";
        outcomeQuery += "\nGROUP BY b.b_id";

        String expectedQuery = "SELECT outcome.b_id, v1, v2";
        expectedQuery += "\nFROM\n(" + treatmentQuery + ") treatment,\n(" + outcomeQuery + ") outcome";
        expectedQuery += "\nWHERE treatment.b_id = outcome.b_id";


        UnitTranslator ut = new UnitTranslator(unit, null, oPath.getAggregators().get(0));
        String actualQuery = ut.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }

    public void testUnitWithAggs(){
        //[bc b ab].ab --> [bc].bc
        StructurePath tPath = TestUtil.getStructurePath("bc", "ab", "bc,b,ab", "one");
        StructurePath oPath = TestUtil.getStructurePath("bc", "bc", "bc", "one");
        Unit unit = TestUtil.getUnit("bc", tPath, oPath);

        String treatmentQuery = "SELECT bc.bc_id, count(ab.ab_id) AS v1";
        treatmentQuery += "\nFROM bc, b, ab";
        treatmentQuery += "\nWHERE bc.b_id = b.b_id AND b.b_id = ab.b_id AND ab.ab_id IS NOT NULL";
        treatmentQuery += "\nGROUP BY bc.bc_id";

        String outcomeQuery = "SELECT bc_id, 1 AS v2";
        outcomeQuery += "\nFROM bc";

        String expectedQuery = "SELECT outcome.bc_id, v1, v2";
        expectedQuery += "\nFROM\n(" + treatmentQuery + ") treatment,\n(" + outcomeQuery + ") outcome";
        expectedQuery += "\nWHERE treatment.bc_id = outcome.bc_id";


        UnitTranslator ut = new UnitTranslator(unit, tPath.getAggregators().get(0), oPath.getAggregators().get(0));
        String actualQuery = ut.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }
}
