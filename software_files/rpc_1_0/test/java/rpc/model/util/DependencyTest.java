/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 23, 2010
 * Time: 12:45:58 PM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.design.Unit;
import rpc.design.Path;

public class DependencyTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEquals() {
        Path t1 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path o1 = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Unit u1 = TestUtil.getUnit("a", t1, o1);
        Dependency d1 = new Dependency(u1);

        Path t2 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path o2 = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Unit u2 = TestUtil.getUnit("a", t2, o2);
        Dependency d2 = new Dependency(u2);

        Path t3 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path o3 = TestUtil.getAttributePath("a", "a", "a", "one", "x2");
        Unit u3 = TestUtil.getUnit("a", t3, o3);
        Dependency d3 = new Dependency(u3);

        assertEquals(d1, d2);
        int d1Hash = d1.hashCode();
        assertEquals("Hash codes should be equal", d1Hash, d2.hashCode());
        assertEquals("Hash code should not change", d1Hash, d1.hashCode());

        assertFalse(d1.equals(d3));
        assertFalse(d1.hashCode() == d3.hashCode());
    }

    public void testIsTrivial() {
        Path t1 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path o1 = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Unit u1 = TestUtil.getUnit("a", t1, o1);
        Dependency d1 = new Dependency(u1);
        assertFalse(d1.isTrivial());

        Dependency d2 = new Dependency(new Vertex("ab"), new Vertex("ab.xy1"));
        assertTrue(d2.isTrivial());
    }

    public void testReverse() {
        Path t1 = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path o1 = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Unit u1 = TestUtil.getUnit("a", t1, o1);
        Dependency d1 = new Dependency(u1);

        Path t2 = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        Path o2 = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Unit uRev = TestUtil.getUnit("b", t2, o2);
        Dependency expected = new Dependency(uRev);

        assertEquals(expected, d1.reverse());
    }
}
