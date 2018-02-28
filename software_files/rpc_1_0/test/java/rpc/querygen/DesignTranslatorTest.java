/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 7, 2009
 * Time: 2:12:01 PM
 */
package rpc.querygen;

import junit.framework.TestCase;
import rpc.design.*;
import rpc.TestUtil;
import rpc.dataretrieval.NopAggregator;
import rpc.util.UnitUtil;
import rpc.util.LogUtil;
import rpc.model.util.ModelSupport;

import java.util.*;

public class DesignTranslatorTest extends TestCase {

    public static final String SCHEMA_FILE = "./test/test-schema.pl";
    public static final int HOP_THRESHOLD = 4;
    private ModelSupport modelSupport;

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

        //get all units to load causal model
        List<Unit> units = UnitUtil.getUnits(HOP_THRESHOLD);
        List<Unit> uniqueUnits = UnitUtil.getUniqueUnits(HOP_THRESHOLD);
        //initialize causal model with list of all units
        this.modelSupport = new ModelSupport(units, uniqueUnits, HOP_THRESHOLD);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDesignNoControl() {
        //[b ab a].x1 --> [b].y1
        AttributePath tPath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        AttributePath oPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit unit = TestUtil.getUnit("b", tPath, oPath);
        Design design = new Design(unit);

        String unitQuery = "SELECT a.a_id, ab.ab_id, b.b_id, a.x1 AS v1, b.y1 AS v2";
        unitQuery       += "\nFROM a, ab, b";
        unitQuery       += "\nWHERE a.a_id = ab.a_id AND ab.b_id = b.b_id";

        String expectedQuery = "SELECT v1, v2";
        expectedQuery += "\nFROM (" + unitQuery + ") design";

        DesignTranslator dt = new DesignTranslator(design, null, null);
        String actualQuery = dt.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }

    public void testDesignOneControl() {
        //[b ab a].x1 --> [b].y1
        //control for [b ab a da d].w1
        AttributePath tPath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        AttributePath oPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit unit = TestUtil.getUnit("b", tPath, oPath);
        Design design = new Design(unit);
        List<Path> controlPaths = new ArrayList<Path>();
        AttributePath control = TestUtil.getAttributePath("b", "d", "b,ab,a,da,d", "one", "w1");
        controlPaths.add(control);
        ConditioningSet cs = new ConditioningSet(controlPaths);
        cs.setConditioningAggregate(control,new NopAggregator(control.getVariable()));
        design.addDesignElement(cs);

        String unitQuery = "SELECT a.a_id, ab.ab_id, b.b_id, a.x1 AS v1, b.y1 AS v2";
        unitQuery       += "\nFROM a, ab, b";
        unitQuery       += "\nWHERE a.a_id = ab.a_id AND ab.b_id = b.b_id";

        String controlQuery = "SELECT b.b_id, d.w1 AS control0";
        controlQuery += "\nFROM b, ab, a, da, d";
        controlQuery += "\nWHERE b.b_id = ab.b_id AND ab.a_id = a.a_id AND a.a_id = da.a_id AND da.d_id = d.d_id";
        controlQuery += " AND d.w1 IS NOT NULL";

        String designQuery = "SELECT designSub0.*, control0";
        designQuery += "\nFROM\n(" + unitQuery + ") designSub0,\n(" + controlQuery + ") controlTable0";
        designQuery += "\nWHERE designSub0.b_id = controlTable0.b_id";

        String expectedQuery = "SELECT v1, v2, control0";
        expectedQuery += "\nFROM (" + designQuery + ") design";

        DesignTranslator dt = new DesignTranslator(design, null, null);
        String actualQuery = dt.getQuery();
        assertEquals(expectedQuery, actualQuery);

        //[b bc c bc b ab a].x1 --> [b].y1
        //control for [b ab a].x2
        tPath = TestUtil.getAttributePath("b", "a", "b,bc,c,bc,b,ab,a", "many", "x1");
        oPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        unit = TestUtil.getUnit("b", tPath, oPath);

        design = new Design(unit);
        controlPaths = new ArrayList<Path>();
        control = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x2");
        controlPaths.add(control);
        cs = new ConditioningSet(controlPaths);
        cs.setConditioningAggregate(control,new NopAggregator(control.getVariable()));
        design.addDesignElement(cs);

        unitQuery  = "SELECT a.a_id, ab.ab_id, b1.b_id AS b_id1, bc1.bc_id AS bc_id1, c.c_id, " +
                        "bc2.bc_id AS bc_id2, b2.b_id AS b_id2, a.x1 AS v1, b2.y1 AS v2";
        unitQuery += "\nFROM a, ab, b b1, bc bc1, c, bc bc2, b b2";
        unitQuery += "\nWHERE a.a_id = ab.a_id AND ab.b_id = b1.b_id AND b1.b_id = bc1.b_id AND " +
                        "bc1.c_id = c.c_id AND c.c_id = bc2.c_id AND bc2.b_id = b2.b_id AND " +
                        "bc1.bc_id <> bc2.bc_id AND b1.b_id <> b2.b_id";

        controlQuery  = "SELECT b.b_id, a.x2 AS control0";
        controlQuery += "\nFROM b, ab, a";
        controlQuery += "\nWHERE b.b_id = ab.b_id AND ab.a_id = a.a_id";
        controlQuery += " AND a.x2 IS NOT NULL";

        designQuery  = "SELECT designSub0.*, control0";
        designQuery += "\nFROM\n(" + unitQuery + ") designSub0,\n(" + controlQuery + ") controlTable0";
        designQuery += "\nWHERE designSub0.b_id = controlTable0.b_id";

        expectedQuery  = "SELECT v1, v2, control0";
        expectedQuery += "\nFROM (" + designQuery + ") design";

        dt = new DesignTranslator(design, null, null);
        actualQuery = dt.getQuery();
        assertEquals(expectedQuery, actualQuery);
    }

}
