/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 18, 2010
 * Time: 2:20:02 PM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

public class VertexTest extends TestCase {

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
        Vertex v1 = new Vertex("a.x1");
        Vertex v2 = new Vertex("a.x1");
        Vertex v3 = new Vertex("a.x2");

        assertEquals(v1, v2);
        int v1Hash = v1.hashCode();
        assertEquals("Hash codes should be equal", v1Hash, v2.hashCode());
        assertEquals("Hash code should not change", v1Hash, v1.hashCode());

        assertFalse(v1.equals(v3));
        assertFalse(v1.hashCode() == v3.hashCode());
    }

    public void testIsAttribute() {
        Vertex v1 = new Vertex("a.x1");
        assertTrue(v1.isAttribute());

        Vertex v2 = new Vertex("ab");
        assertFalse(v2.isAttribute());
    }

    public void testIsStructure() {
        Vertex v1 = new Vertex("a.x1");
        assertFalse(v1.isStructure());

        Vertex v2 = new Vertex("ab");
        assertTrue(v2.isStructure());
    }

    public void testIsExistence() {
        Vertex v1 = new Vertex("a.x1");
        assertFalse(v1.isStructure());

        Vertex v2 = new Vertex("ab");
        assertTrue(v2.isStructure());
    }

    public void testGetBaseTable() {
        Vertex v1 = new Vertex("a.x1");
        assertEquals("a", v1.getBaseTable());

        Vertex v2 = new Vertex("ab");
        assertEquals("ab", v2.getBaseTable());
    }

    public void testGetAttribute() {
        Vertex v1 = new Vertex("a.x1");
        assertEquals("x1", v1.getAttribute());

        Vertex v2 = new Vertex("ab");
        assertEquals("ab", v2.getAttribute());
    }

}
