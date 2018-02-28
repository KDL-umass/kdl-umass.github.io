/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 10:40:44 AM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

public class VertexTripleTest extends TestCase {

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
        Vertex v1a = new Vertex("a.x1");
        Vertex v2a = new Vertex("b.y1");
        Vertex v3a = new Vertex("c.z1");
        VertexTriple vt1 = new VertexTriple(v1a, v2a, v3a);

        Vertex v1b = new Vertex("a.x1");
        Vertex v2b = new Vertex("b.y1");
        Vertex v3b = new Vertex("c.z1");
        VertexTriple vt2 = new VertexTriple(v1b, v2b, v3b);

        Vertex v1c = new Vertex("a.x2");
        Vertex v2c = new Vertex("b.y1");
        Vertex v3c = new Vertex("bc");
        VertexTriple vt3 = new VertexTriple(v1c, v2c, v3c);

        assertEquals(vt1, vt2);
        assertEquals(vt2, vt1);

        int vt1Hash = vt1.hashCode();
        assertEquals("Hash codes should be equal", vt1Hash, vt2.hashCode());
        assertEquals("Hash code should not change", vt1Hash, vt1.hashCode());

        assertFalse(vt1.equals(vt3));
        assertFalse(vt1.hashCode() == vt3.hashCode());
    }

    public void testReverse() {
        Vertex v1 = new Vertex("a.x1");
        Vertex v2 = new Vertex("b.y1");
        Vertex v3 = new Vertex("c.z1");
        VertexTriple vt = new VertexTriple(v1, v2, v3);
        assertEquals(new VertexTriple(v3, v2, v1), vt.reverse());
    }
}
