/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 22, 2010
 * Time: 2:56:46 PM
 */
package rpc.dataretrieval;

import junit.framework.TestCase;
import rpc.util.LogUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ContingencyTable3DTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddValues() {
        ContingencyTable3D ct = new ContingencyTable3D();
        List<Object> config1 = new ArrayList<Object>();
        config1.add(0);
        ct.addValues(0, 0, config1, 1);
        assertEquals(1, ct.getCounts().get(config1).getCounts().get(0.0).get(0.0).intValue());
        ct.addValues(0, 0, config1, 3);
        assertEquals(4, ct.getCounts().get(config1).getCounts().get(0.0).get(0.0).intValue());

        List<Object> config2 = new ArrayList<Object>();
        config2.add(1);
        ct.addValues(0, 1, config2, 2);
        assertEquals(2, ct.getCounts().get(config2).getCounts().get(0.0).get(1.0).intValue());

        List<Object> config3 = new ArrayList<Object>();
        config3.add(0);
        config3.add(1);
        ct.addValues(1, 2, config3, 5);
        assertEquals(5, ct.getCounts().get(config3).getCounts().get(1.0).get(2.0).intValue());
    }

    public void testGetNumDimensions() {
        ContingencyTable3D ct = new ContingencyTable3D();
        List<Object> config1 = new ArrayList<Object>();
        config1.add(0);
        ct.addValues(0, 0, config1, 1);
        assertEquals(1, ct.getCounts().get(config1).getCounts().get(0.0).get(0.0).intValue());
        ct.addValues(0, 0, config1, 3);
        assertEquals(1, ct.getNumDimensions());

        List<Object> config2 = new ArrayList<Object>();
        config2.add(1);
        ct.addValues(0, 1, config2, 2);
        assertEquals(2, ct.getNumDimensions());

        List<Object> config3 = new ArrayList<Object>();
        config3.add(0);
        config3.add(1);
        ct.addValues(1, 2, config3, 5);
        assertEquals(3, ct.getNumDimensions());
    }

    public void testGetCounts() {
        ContingencyTable3D ct = new ContingencyTable3D();
        List<Object> config1 = new ArrayList<Object>();
        config1.add(0);
        ct.addValues(0, 0, config1, 1);
        ct.addValues(0, 0, config1, 3);

        List<Object> config2 = new ArrayList<Object>();
        config2.add(1);
        ct.addValues(0, 1, config2, 2);

        List<Object> config3 = new ArrayList<Object>();
        config3.add(0);
        config3.add(1);
        ct.addValues(1, 2, config3, 5);

        Map<List<Object>, ContingencyTable> expected = new HashMap<List<Object>, ContingencyTable>();
        expected.put(config1, new ContingencyTable());
        expected.put(config2, new ContingencyTable());
        expected.put(config3, new ContingencyTable());
        expected.get(config1).addValues(0, 0, 4);
        expected.get(config2).addValues(0, 1, 2);
        expected.get(config3).addValues(1, 2, 5);

        Map<List<Object>, ContingencyTable> actual = ct.getCounts();

        assertEquals(expected.size(), actual.size());
        for (List<Object> config : expected.keySet()) {
            assertTrue(actual.containsKey(config));
            //compare the inner 2D contingency tables
            assertEquals(expected.get(config), actual.get(config));
        }
    }
}
