/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Aug 13, 2009
 * Time: 9:28:05 PM
 */
package rpc.util;

import junit.framework.TestCase;
import rpc.TestUtil;
import rpc.design.AttributePath;
import rpc.design.Unit;
import rpc.design.StructurePath;
import rpc.design.Path;

import java.util.List;
import java.util.ArrayList;

public class DesignUtilTest extends TestCase {

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

    /* Units to test standardize (only those that don't follow form of singleton outcomes):
        1. ESE
        2. EEE
        3. ERE
        4. ESR
        5. EER
        6. ERR
        7. RSE
        8. REE
        9. RRE
       10. RSR
       11. RER
       12. RRR

       Variants: cardinalities in either path, cycles in either path
                 long vs short paths, different overlaps
                 structural treatments/outcomes
    */

    public void testESE() {
        //test: [A].x1 --> [A AB B].y1 == [B AB A].x1 --> [B].y1
        // ESE == EES
        // ONE to MANY == ONE to ONE
        // 0,2 == 2,0
        AttributePath testTPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        AttributePath testOPath = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Unit testUnit           = TestUtil.getUnit("a", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        AttributePath expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit expectedUnit           = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A].x1 --> [A AB B BC C BC B].y1 == [B BC C BC B AB A].x1 --> [B].y1
        // ESE == EES
        // ONE to MANY == MANY to ONE
        // 0,6 == 6,0
        testTPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        testOPath = TestUtil.getAttributePath("a", "b", "a,ab,b,bc,c,bc,b", "many", "y1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("b", "a", "b,bc,c,bc,b,ab,a", "many", "x1");
        expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        expectedUnit  = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B].y1 --> [B AB A DA D].w1 == [D DA A AB B].y1 --> [D].w1
        // ESE == EES
        // ONE to ONE == MANY to ONE
        // 0,4 == 4,0
        testTPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        testOPath = TestUtil.getAttributePath("b", "d", "b,ab,a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "b", "d,da,a,ab,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B].y1 --> [B BC C BC B AB A].x1 == [A AB B BC C BC B].y1 --> [A].x1
        // ESE == EES
        // ONE to MANY == MANY to ONE
        // 0,6 == 6,0
        testTPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        testOPath = TestUtil.getAttributePath("b", "a", "b,bc,c,bc,b,ab,a", "many", "x1");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("a", "b", "a,ab,b,bc,c,bc,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        expectedUnit  = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testEEE() {
        //test: [A AB B BC C].z1 --> [A AB B].y1 == [B BC C].z1 --> [B].y1
        // EEE == EES
        // MANY to MANY == MANY to ONE
        // 4,2 == 2,0
        AttributePath testTPath = TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z1");
        AttributePath testOPath = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Unit testUnit           = TestUtil.getUnit("a", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        AttributePath expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit expectedUnit           = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A AB B BC C BC B].y1 --> [A AB B BC C].z1 == [C BC B].y1 --> [C].z1
        // EEE == EES
        // MANY to MANY == MANY to ONE
        // 6,4 == 2,0
        testTPath = TestUtil.getAttributePath("a", "b", "a,ab,b,bc,c,bc,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "b", "c,bc,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B AB A AB B BC C].z1 --> [B BC C BC B AB A].x1 ==
        //                  [A AB B BC C BC B AB A AB B BC C].z1 --> [A].x1
        // EEE == EES
        // MANY to MANY == MANY to ONE
        // 6,6 == 12,0
        testTPath = TestUtil.getAttributePath("b", "c", "b,ab,a,ab,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("b", "a", "b,bc,c,bc,b,ab,a", "many", "x1");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c,bc,b,ab,a,ab,b,bc,c", "many", "z1");
        expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        expectedUnit  = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B AB A AB B BC C].z1 --> [B AB A AB B BC C].z2 == [C].z1 --> [C].z2
        // EEE == ESS
        // MANY to MANY == ONE to ONE
        // 6,6 == 0,0
        testTPath = TestUtil.getAttributePath("b", "c", "b,ab,a,ab,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("b", "c", "b,ab,a,ab,b,bc,c", "many", "z2");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B AB A AB B].y1 --> [B BC C].z1 == [C BC B AB A AB B].y1 --> [C].z1
        // EEE == EES
        // MANY to MANY == MANY to ONE
        // 4,2 == 6,0
        testTPath = TestUtil.getAttributePath("b", "b", "b,ab,a,ab,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "b", "c,bc,b,ab,a,ab,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B AB A AB].AB --> [B BC C].z1 == [C BC B AB A AB].AB --> [C].z1
        // ERE == ERS
        // MANY to MANY == MANY to ONE
        // 3,2 == 5,0
        StructurePath testTPathStruct = TestUtil.getStructurePath("b", "ab", "b,ab,a,ab", "many");
        testOPath = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("b", testTPathStruct, testOPath);

        StructurePath expectedTPathStruct = TestUtil.getStructurePath("c", "ab", "c,bc,b,ab,a,ab", "many");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPathStruct, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testERE() {
        //test: [B BC C BC].yz1 --> [B AB A].x2 == [A AB B BC C BC].yz1 --> [A].x2
        // ERE == ERS
        // MANY to ONE == MANY to ONE
        // 3,2 == 5,0
        AttributePath testTPath = TestUtil.getAttributePath("b", "bc", "b,bc,c,bc", "many", "yz1");
        AttributePath testOPath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x2");
        Unit testUnit           = TestUtil.getUnit("b", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc", "many", "yz1");
        AttributePath expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x2");
        Unit expectedUnit           = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A AB].xy1 --> [A DA D].w1 == [D DA A AB].xy1 --> [D].w1
        // ERE == ERS
        // MANY to ONE == MANY to ONE
        // 1,2 == 3,0
        testTPath = TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy1");
        testOPath = TestUtil.getAttributePath("a", "d", "a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "ab", "d,da,a,ab", "many", "xy1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A DA].wx1 --> [A DA D].w1 == [D DA].wx1 --> [D].w1
        // ERE == ERS
        // ONE to ONE == ONE to ONE
        // 1,2 == 1,0
        testTPath = TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("a", "d", "a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "da", "d,da", "one", "wx1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A DA].wx1 --> [A AB B BC C BC B].y1 == [B BC C BC B AB A DA].wx1 --> [B].y1
        // ERE == ERS
        // ONE to MANY == MANY to ONE
        // 1,6 == 7,0
        testTPath = TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("a", "b", "a,ab,b,bc,c,bc,b", "many", "y1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("b", "da", "b,bc,c,bc,b,ab,a,da", "many", "wx1");
        expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        expectedUnit  = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testESR() {
        //test: [B].y1 --> [B AB A DA].wx1 == [DA A AB B].y1 --> [DA].wx1
        // ESR == RES
        // ONE to ONE == MANY to ONE
        // 0,3 == 3,0
        AttributePath testTPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        AttributePath testOPath = TestUtil.getAttributePath("b", "da", "b,ab,a,da", "one", "wx1");
        Unit testUnit           = TestUtil.getUnit("a", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("da", "b", "da,a,ab,b", "many", "y1");
        AttributePath expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        Unit expectedUnit           = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A].x1 --> [A DA].wx1 == [DA A].x1 --> [DA].wx1
        // ESR == RES
        // ONE to ONE == ONE to ONE
        // 0,1 == 1,0
        testTPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        testOPath = TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("da", "a", "da,a", "one", "x1");
        expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        expectedUnit  = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A].x1 --> [A AB B BC C BC].yz1 == [BC C BC B AB A].x1 --> [BC].yz1
        // ESR == RES
        // ONE to MANY == MANY to ONE
        // 0,5 == 5,0
        testTPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        testOPath = TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "a", "bc,c,bc,b,ab,a", "many", "x1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [C].z1 --> [C BC B BC].yz1 == [BC B BC C].z1 --> [BC].yz1
        // ESR == RES
        // ONE to MANY == MANY to ONE
        // 0,3 == 3,0
        testTPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        testOPath = TestUtil.getAttributePath("c", "bc", "c,bc,b,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("c", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "c", "bc,b,bc,c", "many", "z1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testEER() {
        //test: [B BC C].z1 --> [B AB].xy1 == [AB B BC C].z1 --> [AB].xy1
        // EER == RES
        // MANY to ONE == MANY to ONE
        // 2,1 == 3,0
        AttributePath testTPath = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        AttributePath testOPath = TestUtil.getAttributePath("b", "ab", "b,ab", "one", "xy1");
        Unit testUnit           = TestUtil.getUnit("b", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        AttributePath expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        Unit expectedUnit           = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [B AB A AB B].y1 --> [B BC C BC].yz1 == [BC C BC B AB A AB B].y1 --> [BC].yz1
        // EER == RES
        // MANY to MANY == MANY to ONE
        // 4,4 == 7,0
        testTPath = TestUtil.getAttributePath("b", "b", "b,ab,a,ab,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("b", "bc", "b,bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("b", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "b", "bc,c,bc,b,ab,a,ab,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [C BC B BC C].z1 --> [C BC B AB].xy1 == [AB B BC C].z1 --> [AB].xy1
        // EER == RES
        // MANY to MANY == MANY to ONE
        // 4,4 == 3,0
        testTPath = TestUtil.getAttributePath("c", "c", "c,bc,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("c", "ab", "c,bc,b,ab", "many", "xy1");
        testUnit  = TestUtil.getUnit("c", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [C BC B BC C].z1 --> [C BC B BC].yz1 == [BC C].z1 --> [BC].yz1
        // EER == RES
        // MANY to MANY == ONE to ONE
        // 4,4 == 1,0
        testTPath = TestUtil.getAttributePath("c", "c", "c,bc,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("c", "bc", "c,bc,b,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("c", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "c", "bc,c", "one", "z1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [C BC B BC].BC --> [C BC B AB].xy1 == [AB B BC].BC --> [AB].xy1
        // ERR == RRS
        // MANY to MANY == MANY to ONE
        // 3,4 == 2,0
        StructurePath testTPathStruct = TestUtil.getStructurePath("c", "bc", "c,bc,b,bc", "many");
        testOPath = TestUtil.getAttributePath("c", "ab", "c,bc,b,ab", "many", "xy1");
        testUnit  = TestUtil.getUnit("c", testTPathStruct, testOPath);

        StructurePath expectedTPathStruct = TestUtil.getStructurePath("ab", "bc", "ab,b,bc", "many");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPathStruct, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

    }

    public void testERR() {
        //test: [B BC].yz1 --> [B AB].xy1 == [AB B BC].yz1 --> [AB].xy1
        // ERR == RRS
        // MANY to ONE == MANY to ONE
        // 2,2 == 4,0
        AttributePath testTPath = TestUtil.getAttributePath("b", "bc", "b,bc", "many", "yz1");
        AttributePath testOPath = TestUtil.getAttributePath("b", "ab", "b,ab", "one", "xy1");
        Unit testUnit           = TestUtil.getUnit("b", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("ab", "bc", "ab,b,bc", "many", "yz1");
        AttributePath expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        Unit expectedUnit           = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A AB].xy1 --> [A DA].wx1 == [DA A AB].xy1 --> [DA].wx1
        // ERR == RRS
        // MANY to ONE == MANY to ONE
        // 2,2 == 3,0
        testTPath = TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy1");
        testOPath = TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("da", "ab", "da,a,ab", "many", "xy1");
        expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        expectedUnit  = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A AB].xy1 --> [A AB].xy2 == [AB].xy1 --> [AB].xy2
        // ERR == RSS
        // MANY to MANY == ONE to ONE
        // 2,2 == 0,0
        testTPath = TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy1");
        testOPath = TestUtil.getAttributePath("a", "ab", "a,ab", "many", "xy2");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy2");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [A DA].wx1 --> [A AB B BC C BC].yz1 == [BC C BC B AB A DA].wx1 --> [BC].yz1
        // ERR == RRS
        // ONE to MANY == MANY to ONE
        // 2,6 == 7,0
        testTPath = TestUtil.getAttributePath("a", "da", "a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("a", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "da", "bc,c,bc,b,ab,a,da", "many", "wx1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testRSE(){
        //test: [AB].xy1 --> [AB B].y1 == [B AB].xy1 --> [B].y1
        // RSE == ERS
        // ONE to ONE == ONE to ONE
        // 0,1 == 2,0
        AttributePath testTPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        AttributePath testOPath = TestUtil.getAttributePath("ab", "b", "ab,b", "one", "y1");
        Unit testUnit           = TestUtil.getUnit("ab", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("b", "ab", "b,ab", "one", "xy1");
        AttributePath expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit expectedUnit           = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB].xy1 --> [AB B BC C BC B].y1 == [B BC C BC B AB].xy1 --> [B].y1
        // RSE == ERS
        // ONE to MANY == MANY to ONE
        // 0,5 == 6,0
        testTPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        testOPath = TestUtil.getAttributePath("ab", "b", "ab,b,bc,c,bc,b", "many", "y1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("b", "ab", "b,bc,c,bc,b,ab", "many", "xy1");
        expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        expectedUnit  = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC].yz1 --> [BC B AB A DA D].w1 == [D DA A AB B BC].yz1 --> [D].w1
        // RSE == ERS
        // ONE to ONE == MANY to ONE
        // 0,5 == 6,0
        testTPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        testOPath = TestUtil.getAttributePath("bc", "d", "bc,b,ab,a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "bc", "d,da,a,ab,b,bc", "many", "yz1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC].yz1 --> [BC B BC C BC B AB A].x1 == [A AB B BC C BC B BC].yz1 --> [A].x1
        //RSE == ERS
        // ONE to MANY == MANY to ONE
        // 0,7 == 8,0
        testTPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        testOPath = TestUtil.getAttributePath("bc", "a", "bc,b,bc,c,bc,b,ab,a", "many", "x1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc,b,bc", "many", "yz1");
        expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        expectedUnit  = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testREE(){
        //test: [AB B BC C].z1 --> [AB B].y1 == [B BC C].z1 --> [B].y1
        // REE == EES
        // MANY to ONE == MANY to ONE
        // 3,1 == 2,0
        AttributePath testTPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        AttributePath testOPath = TestUtil.getAttributePath("ab", "b", "ab,b", "one", "y1");
        Unit testUnit           = TestUtil.getUnit("ab", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        AttributePath expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit expectedUnit           = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB B BC C BC B].y1 --> [AB B BC C].z1 == [C BC B].y1 --> [C].z1
        // REE == EES
        // MANY to MANY == MANY to ONE
        // 5,3 == 2,0
        testTPath = TestUtil.getAttributePath("ab", "b", "ab,b,bc,c,bc,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "b", "c,bc,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A AB B BC C].z1 --> [AB B BC C BC B AB A].x1 ==
        //                      [A AB B BC C BC B AB A AB B BC C].z1 --> [A].x1
	    //REE == EES
        // MANY to MANY == MANY to ONE
        // 5,7 == 12,0
        testTPath = TestUtil.getAttributePath("ab", "c", "ab,a,ab,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("ab", "a", "ab,b,bc,c,bc,b,ab,a", "many", "x1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("a", "c", "a,ab,b,bc,c,bc,b,ab,a,ab,b,bc,c", "many", "z1");
        expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        expectedUnit  = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B AB A AB B BC C].z1 --> [BC B AB A AB B BC C].z2 == [C].z1 --> [C].z2
        // REE == ESS
        // MANY to MANY == ONE to ONE
        // 7,7 == 0,0
        testTPath = TestUtil.getAttributePath("bc", "c", "bc,b,ab,a,ab,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("bc", "c", "bc,b,ab,a,ab,b,bc,c", "many", "z2");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A AB B BC C].z1 --> [AB A AB B BC C].z2 == [C].z1 --> [C].z2
        // REE == ESS
        // MANY to MANY == ONE to ONE
        // 5,5 == 0,0
        testTPath = TestUtil.getAttributePath("ab", "c", "ab,a,ab,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("ab", "c", "ab,a,ab,b,bc,c", "many", "z2");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B AB A AB B].y1 --> [BC C].z1 == [C BC B AB A AB B].y1 --> [C].z1
        // REE == EES
        // MANY to ONE == MANY to ONE
        // 5,1 == 6,0
        testTPath = TestUtil.getAttributePath("bc", "b", "bc,b,ab,a,ab,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("bc", "c", "bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("c", "b", "c,bc,b,ab,a,ab,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B AB A AB].AB --> [BC C].z1 == [C BC B AB A AB].AB --> [C].z1
        // RRE == ERS
        // MANY to ONE == MANY to ONE
        // 4,1 == 5,0
        StructurePath testTPathStruct = TestUtil.getStructurePath("bc", "ab", "bc,b,ab,a,ab", "many");
        testOPath = TestUtil.getAttributePath("bc", "c", "bc,c", "many", "z1");
        testUnit  = TestUtil.getUnit("bc", testTPathStruct, testOPath);

        StructurePath expectedTPathStruct = TestUtil.getStructurePath("c", "ab", "c,bc,b,ab,a,ab", "many");
        expectedOPath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        expectedUnit  = TestUtil.getUnit("c", expectedTPathStruct, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testRRE(){
        //test: [BC C BC].yz1 --> [BC B AB A].x2 == [A AB B BC C BC].yz1 --> [A].x2
        // RRE == ERS
        // MANY to ONE == MANY to ONE
        // 2,3 == 5,0
        AttributePath testTPath = TestUtil.getAttributePath("bc", "bc", "bc,c,bc", "many", "yz1");
        AttributePath testOPath = TestUtil.getAttributePath("bc", "a", "bc,b,ab,a", "one", "x2");
        Unit testUnit           = TestUtil.getUnit("bc", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("a", "bc", "a,ab,b,bc,c,bc", "many", "yz1");
        AttributePath expectedOPath = TestUtil.getAttributePath("a", "a", "a", "one", "x2");
        Unit expectedUnit           = TestUtil.getUnit("a", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A AB].xy1 --> [AB A DA D].w1 == [D DA A AB].xy1 --> [D].w1
        // RRE == ERS
        // MANY to ONE == MANY to ONE
        // 2,3 == 3,0
        testTPath = TestUtil.getAttributePath("ab", "ab", "ab,a,ab", "many", "xy1");
        testOPath = TestUtil.getAttributePath("ab", "d", "ab,a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "ab", "d,da,a,ab", "many", "xy1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A DA].wx1 --> [AB A DA D].w1 == [D DA].wx1 --> [D].w1
	    // RRE == ERS
        // ONE to ONE == ONE to ONE
        // 2,3 == 1,0
        testTPath = TestUtil.getAttributePath("ab", "da", "ab,a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("ab", "d", "ab,a,da,d", "one", "w1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("d", "da", "d,da", "one", "wx1");
        expectedOPath = TestUtil.getAttributePath("d", "d", "d", "one", "w1");
        expectedUnit  = TestUtil.getUnit("d", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A DA].wx1 --> [AB B BC C BC B].y1 == [B BC C BC B AB A DA].wx1 --> [B].y1
        // RRE == ERS
        // ONE to MANY == MANY to ONE
        // 2,5 == 7,0
        testTPath = TestUtil.getAttributePath("ab", "da", "ab,a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("ab", "b", "ab,b,bc,c,bc,b", "many", "y1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("b", "da", "b,bc,c,bc,b,ab,a,da", "many", "wx1");
        expectedOPath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        expectedUnit  = TestUtil.getUnit("b", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A DA].wx1 --> [AB B BC].BC == [BC B AB A DA].wx1 --> [BC].BC
        // RRR == RRS
        // ONE to MANY == MANY to ONE
        // 2,2 == 4,0
        testTPath = TestUtil.getAttributePath("ab", "da", "ab,a,da", "one", "wx1");
        StructurePath testOPathStruct = TestUtil.getStructurePath("ab", "bc", "ab,b,bc", "many");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPathStruct);

        expectedTPath = TestUtil.getAttributePath("bc", "da", "bc,b,ab,a,da", "one", "wx1");
        StructurePath expectedOPathStruct = TestUtil.getStructurePath("bc", "bc", "bc", "one");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPathStruct);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testRSR(){
        //test: [BC].yz1 --> [BC B AB A DA].wx1 == [DA A AB B BC].yz1 --> [DA].wx1
        // RSR == RRS
        // ONE to ONE == MANY to ONE
        // 0,4 == 4,0
        AttributePath testTPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        AttributePath testOPath = TestUtil.getAttributePath("bc", "da", "bc,b,ab,a,da", "one", "wx1");
        Unit testUnit           = TestUtil.getUnit("bc", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("da", "bc", "da,a,ab,b,bc", "many", "yz1");
        AttributePath expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        Unit expectedUnit           = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB].xy1 --> [AB A DA].wx1 == [DA A AB].xy1 --> [DA].wx1
        // RSR == RRS
        // ONE to ONE == MANY to ONE
        // 0,2 == 2,0
        testTPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        testOPath = TestUtil.getAttributePath("ab", "da", "ab,a,da", "one", "wx1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("da", "ab", "da,a,ab", "many", "xy1");
        expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        expectedUnit  = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [DA].wx1 --> [DA A AB B BC C BC].yz1 == [BC C BC B AB A DA].wx1 --> [BC].yz1
        // RSR == RRS
        // ONE to MANY == MANY to ONE
        // 0,6 == 6,0
        testTPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("da", "bc", "da,a,ab,b,bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("da", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "da", "bc,c,bc,b,ab,a,da", "many", "wx1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC].yz1 --> [BC B BC C BC B AB].xy1 == [AB B BC C BC B BC].yz1 --> [AB].xy1
        // RSR == RES
        // ONE to MANY == MANY to ONE
        // 0,6 == 6,0
        testTPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        testOPath = TestUtil.getAttributePath("bc", "ab", "bc,b,bc,c,bc,b,ab", "many", "xy1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("ab", "bc", "ab,b,bc,c,bc,b,bc", "many", "yz1");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testRER(){
        //test: [BC C].z1 --> [BC B AB].xy1 == [AB B BC C].z1 --> [AB].xy1
        // RER == RES
        // ONE to ONE == MANY to ONE
        // 1,2 == 3,0
        AttributePath testTPath = TestUtil.getAttributePath("bc", "c", "bc,c", "one", "z1");
        AttributePath testOPath = TestUtil.getAttributePath("bc", "ab", "bc,b,ab", "one", "xy1");
        Unit testUnit           = TestUtil.getUnit("bc", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        AttributePath expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        Unit expectedUnit           = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B AB A AB B].y1 --> [BC C BC].yz1 == [BC C BC B AB A AB B].y1 --> [BC].yz1
        // RER == RES
        // MANY to MANY == MANY to ONE
        // 5,2 == 7,0
        testTPath = TestUtil.getAttributePath("bc", "b", "bc,b,ab,a,ab,b", "many", "y1");
        testOPath = TestUtil.getAttributePath("bc", "bc", "bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "b", "bc,c,bc,b,ab,a,ab,b", "many", "y1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC C BC B BC C].z1 --> [BC B AB].xy1 == [AB B BC C BC B BC C].z1 --> [AB].xy1
        // RER == RES
        // MANY to ONE == MANY to ONE
        // 5,2 == 7,0
        testTPath = TestUtil.getAttributePath("bc", "c", "bc,c,bc,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("bc", "ab", "bc,b,ab", "one", "xy1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c,bc,b,bc,c", "many", "z1");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B BC C].z1 --> [BC B BC].yz1 == [BC C].z1 --> [BC].yz1
        // RER == RES
        // MANY to MANY == ONE to ONE
        // 3,2 == 1,0
        testTPath = TestUtil.getAttributePath("bc", "c", "bc,b,bc,c", "many", "z1");
        testOPath = TestUtil.getAttributePath("bc", "bc", "bc,b,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "c", "bc,c", "one", "z1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC C BC B BC].BC --> [BC B AB].xy1 == [AB B BC C BC B BC].BC --> [AB].xy1
        // RRR == RRS
        // MANY to ONE == MANY to ONE
        // 4,2 == 6,0
        StructurePath testTPathStruct = TestUtil.getStructurePath("bc", "bc", "bc,c,bc,b,bc", "many");
        testOPath = TestUtil.getAttributePath("bc", "ab", "bc,b,ab", "one", "xy1");
        testUnit  = TestUtil.getUnit("bc", testTPathStruct, testOPath);

        StructurePath expectedTPathStruct = TestUtil.getStructurePath("ab", "bc", "ab,b,bc,c,bc,b,bc", "many");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPathStruct, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

    }

    public void testRRR(){
        //test: [BC B BC].yz1 --> [BC B AB].xy1 == [AB B BC].yz1 --> [AB].xy1
        // RRR == RRS
        // MANY to ONE == MANY to ONE
        // 2,2 == 2,0
        AttributePath testTPath = TestUtil.getAttributePath("bc", "bc", "bc,b,bc", "many", "yz1");
        AttributePath testOPath = TestUtil.getAttributePath("bc", "ab", "bc,b,ab", "one", "xy1");
        Unit testUnit           = TestUtil.getUnit("bc", testTPath, testOPath);

        AttributePath expectedTPath = TestUtil.getAttributePath("ab", "bc", "ab,b,bc", "many", "yz1");
        AttributePath expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        Unit expectedUnit           = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [DA A AB].xy1 --> [DA A AB].xy2 == [AB].xy1 --> [AB].xy2
        // RRR == RSS
        // MANY to MANY == ONE to ONE
        // 2,2 == 0,0
        testTPath = TestUtil.getAttributePath("da", "ab", "da,a,ab", "many", "xy1");
        testOPath = TestUtil.getAttributePath("da", "ab", "da,a,ab", "many", "xy2");
        testUnit  = TestUtil.getUnit("da", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy1");
        expectedOPath = TestUtil.getAttributePath("ab", "ab", "ab", "one", "xy2");
        expectedUnit  = TestUtil.getUnit("ab", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [AB A DA].wx1 --> [AB B BC C BC].yz1 == [BC C BC B AB A DA].wx1 --> [BC].yz1
        // RRR == RRS
        // ONE to MANY == MANY to ONE
        // 2,4 == 6,0
        testTPath = TestUtil.getAttributePath("ab", "da", "ab,a,da", "one", "wx1");
        testOPath = TestUtil.getAttributePath("ab", "bc", "ab,b,bc,c,bc", "many", "yz1");
        testUnit  = TestUtil.getUnit("ab", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("bc", "da", "bc,c,bc,b,ab,a,da", "many", "wx1");
        expectedOPath = TestUtil.getAttributePath("bc", "bc", "bc", "one", "yz1");
        expectedUnit  = TestUtil.getUnit("bc", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));

        //test: [BC B AB A AB B BC].yz1 --> [BC B AB A DA].wx1 == [DA A AB B BC].yz1 --> [DA].wx1
        // RRR == RRS
        // MANY to MANY == MANY to ONE
        // 6,4 == 4,0
        testTPath = TestUtil.getAttributePath("bc", "bc", "bc,b,ab,a,ab,b,bc", "many", "yz1");
        testOPath = TestUtil.getAttributePath("bc", "da", "bc,b,ab,a,da", "many", "wx1");
        testUnit  = TestUtil.getUnit("bc", testTPath, testOPath);

        expectedTPath = TestUtil.getAttributePath("da", "bc", "da,a,ab,b,bc", "many", "yz1");
        expectedOPath = TestUtil.getAttributePath("da", "da", "da", "one", "wx1");
        expectedUnit  = TestUtil.getUnit("da", expectedTPath, expectedOPath);

        TestUtil.unitEquals(expectedUnit, DesignUtil.standardize(testUnit));
    }

    public void testComparePaths() {
        List<String> p1Str = new ArrayList<String>();
        List<String> p2Str = new ArrayList<String>();

        assertTrue(DesignUtil.comparePaths(SchemaUtil.listToSchemaItems(p1Str), SchemaUtil.listToSchemaItems(p2Str)));

        p1Str = new ArrayList<String>();
        p1Str.add("a");
        p2Str = new ArrayList<String>();

        assertFalse(DesignUtil.comparePaths(SchemaUtil.listToSchemaItems(p1Str), SchemaUtil.listToSchemaItems(p2Str)));

        p1Str = new ArrayList<String>();
        p1Str.add("a");
        p2Str = new ArrayList<String>();
        p2Str.add("b");

        assertFalse(DesignUtil.comparePaths(SchemaUtil.listToSchemaItems(p1Str), SchemaUtil.listToSchemaItems(p2Str)));

        p1Str = new ArrayList<String>();
        p1Str.add("a");
        p1Str.add("ab");
        p1Str.add("b");
        p2Str = new ArrayList<String>();
        p2Str.add("a");
        p2Str.add("ab");
        p2Str.add("b");

        assertTrue(DesignUtil.comparePaths(SchemaUtil.listToSchemaItems(p1Str), SchemaUtil.listToSchemaItems(p2Str)));

        p1Str = new ArrayList<String>();
        p1Str.add("a");
        p1Str.add("ab");
        p1Str.add("b");
        p2Str = new ArrayList<String>();
        p2Str.add("a");
        p2Str.add("da");
        p2Str.add("d");

        assertFalse(DesignUtil.comparePaths(SchemaUtil.listToSchemaItems(p1Str), SchemaUtil.listToSchemaItems(p2Str)));
    }

    public void testRewriteFromEntity() {
        Path p1 = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        assertNull(DesignUtil.rewriteFromEntity(p1));

        Path p2 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        assertNull(DesignUtil.rewriteFromEntity(p2));

        Path p3 = TestUtil.getAttributePath("ab", "b", "ab,b", "one", "y1");
        Path expected3 = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        assertEquals(expected3, DesignUtil.rewriteFromEntity(p3));

        Path p4 = TestUtil.getAttributePath("ab", "c", "ab,b,bc,c", "many", "z1");
        Path expected4 = TestUtil.getAttributePath("b", "c", "b,bc,c", "many", "z1");
        assertEquals(expected4, DesignUtil.rewriteFromEntity(p4));
    }
}
