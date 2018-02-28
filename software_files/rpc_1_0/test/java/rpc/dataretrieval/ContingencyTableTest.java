/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 22, 2010
 * Time: 11:28:34 AM
 */
package rpc.dataretrieval;

import junit.framework.TestCase;
import rpc.util.LogUtil;

import java.util.HashMap;

public class ContingencyTableTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddValues() {
        ContingencyTable ct = new ContingencyTable();

        ct.addValues(1, 1, 1);
        assertEquals(1, ct.getCounts().get(1.0).get(1.0).intValue());

        ct.addValues(1, 1, 1);
        assertEquals(2, ct.getCounts().get(1.0).get(1.0).intValue());

        ct.addValues(1, 1, 5);
        assertEquals(7, ct.getCounts().get(1.0).get(1.0).intValue());

        ct.addValues(0, 0, 1);
        assertEquals(1, ct.getCounts().get(0.0).get(0.0).intValue());
        assertEquals(7, ct.getCounts().get(1.0).get(1.0).intValue());
    }

    public void testSize() {
        ContingencyTable ct = new ContingencyTable();
        assertEquals(0, ct.size());

        ct.addValues(1, 1, 1);
        assertEquals(1, ct.size());

        ct.addValues(1, 1, 1);
        assertEquals(2, ct.size());

        ct.addValues(1, 1, 5);
        assertEquals(7, ct.size());

        ct.addValues(0, 0, 1);
        assertEquals(8, ct.size());

        ct.addValues(0, 0, 1);
        ct.addValues(1, 0, 5);
        ct.addValues(2, 2, 2);
        ct.addValues(3, 1, 1);

        assertEquals(17, ct.size());
    }

    public void testIsAllSingleRowOrColumn() {
        ContingencyTable ct = new ContingencyTable();
        assertTrue(ct.isAllSingleRowOrColumn());

        ct.addValues(1, 1, 1);
        assertTrue(ct.isAllSingleRowOrColumn());
        ct.addValues(1, 1, 1);
        assertTrue(ct.isAllSingleRowOrColumn());
        ct.addValues(0, 1, 5);
        assertTrue(ct.isAllSingleRowOrColumn());
        ct.addValues(0, 2, 5);
        assertFalse(ct.isAllSingleRowOrColumn());

        ct = new ContingencyTable();
        ct.addValues(0, 0, 1);
        assertTrue(ct.isAllSingleRowOrColumn());
        ct.addValues(0, 1, 1);
        assertTrue(ct.isAllSingleRowOrColumn());
        ct.addValues(1, 1, 1);
        assertFalse(ct.isAllSingleRowOrColumn());        
    }

    public void testGetCounts() {
        ContingencyTable ct = new ContingencyTable();
        ct.addValues(0, 0, 3);
        ct.addValues(0, 1, 5);
        ct.addValues(1, 0, 1);
        ct.addValues(1, 1, 7);
        ct.addValues(2, 1, 2);

        HashMap<Double, HashMap<Double, Integer>> expected = new HashMap<Double, HashMap<Double, Integer>>();
        expected.put(0.0, new HashMap<Double, Integer>());
        expected.put(1.0, new HashMap<Double, Integer>());
        expected.put(2.0, new HashMap<Double, Integer>());
        expected.get(0.0).put(0.0, 3);
        expected.get(0.0).put(1.0, 5);
        expected.get(1.0).put(0.0, 1);
        expected.get(1.0).put(1.0, 7);
        expected.get(2.0).put(1.0, 2);

        HashMap<Double, HashMap<Double, Integer>> actual = ct.getCounts();
        assertEquals(expected.size(), actual.size());
        for (Double d1 : expected.keySet()) {
            assertTrue(actual.containsKey(d1));
            for (Double d2 : expected.get(d1).keySet()) {
                assertTrue(actual.get(d1).containsKey(d2) && expected.get(d1).get(d2).equals(actual.get(d1).get(d2)));
            }
        }
    }    

    public void testGetTable() {
        ContingencyTable ct = new ContingencyTable();
        ct.addValues(0, 0, 3);
        ct.addValues(0, 1, 5);
        ct.addValues(1, 0, 1);
        ct.addValues(1, 1, 7);
        ct.addValues(2, 1, 2);

        double[][] expected = new double[3][2];
        expected[0][0] = 3;
        expected[0][1] = 5;
        expected[1][0] = 1;
        expected[1][1] = 7;
        expected[2][0] = 0;
        expected[2][1] = 2;

        double[][] actual = ct.getTable();
        assertEquals(expected.length, actual.length);
        for (int i=0; i<expected.length; i++) {
            assertEquals(expected[i].length, actual[i].length);
        }

        //Order of table rows/columns may be shuffled, but pairs should be together
        if (actual[0][0] == 3) {
            assertEquals(expected[0][0], actual[0][0]);
            assertEquals(expected[0][1], actual[0][1]);
        }
        else if (actual[0][0] == 5) {
            assertEquals(expected[0][0], actual[0][1]);
            assertEquals(expected[0][1], actual[0][0]);
        }
        else if (actual[0][0] == 1) {
            assertEquals(expected[1][0], actual[0][0]);
            assertEquals(expected[1][1], actual[0][1]);
        }
        else if (actual[0][0] == 7) {
            assertEquals(expected[1][0], actual[0][1]);
            assertEquals(expected[1][1], actual[0][0]);
        }
        else if (actual[0][0] == 0) {
            assertEquals(expected[2][0], actual[0][0]);
            assertEquals(expected[2][1], actual[0][1]);
        }
        else {
            assertEquals(expected[2][0], actual[0][1]);
            assertEquals(expected[2][1], actual[0][0]);
        }
    }

    public void testEquals() {
        ContingencyTable ct1 = new ContingencyTable();
        ct1.addValues(0, 0, 3);
        ct1.addValues(0, 1, 5);
        ct1.addValues(1, 0, 1);
        ct1.addValues(1, 1, 7);
        ct1.addValues(2, 1, 2);

        ContingencyTable ct2 = new ContingencyTable();
        ct2.addValues(0, 0, 1);
        ct2.addValues(0, 0, 2);
        ct2.addValues(0, 1, 5);
        ct2.addValues(1, 0, 1);
        ct2.addValues(1, 1, 3);
        ct2.addValues(1, 1, 4);
        ct2.addValues(2, 1, 2);

        assertEquals(ct1, ct2);

        ContingencyTable ct3 = new ContingencyTable();
        ct2.addValues(0, 0, 1);
        ct2.addValues(0, 0, 2);
        ct2.addValues(0, 1, 5);
        ct2.addValues(1, 0, 1);
        ct2.addValues(1, 1, 3);
        ct2.addValues(2, 1, 2);

        assertFalse(ct1.equals(ct3));        
    }



}
