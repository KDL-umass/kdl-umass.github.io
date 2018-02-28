/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 10:33:31 AM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

public class VertexPairTest extends TestCase {

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
        VertexPair vp1 = new VertexPair(v1a, v2a);

        Vertex v1b = new Vertex("a.x1");
        Vertex v2b = new Vertex("b.y1");
        VertexPair vp2 = new VertexPair(v1b, v2b);

        Vertex v1c = new Vertex("a.x2");
        Vertex v2c = new Vertex("b.y1");
        VertexPair vp3 = new VertexPair(v1c, v2c);

        assertEquals(vp1, vp2);
        assertEquals(vp2, vp1);

        int vp1Hash = vp1.hashCode();
        assertEquals("Hash codes should be equal", vp1Hash, vp2.hashCode());
        assertEquals("Hash code should not change", vp1Hash, vp1.hashCode());

        assertFalse(vp1.equals(vp3));
        assertFalse(vp1.hashCode() == vp3.hashCode());

    }

    public void testReverse() {
        Vertex v1 = new Vertex("a.x1");
        Vertex v2 = new Vertex("b.y1");
        VertexPair vp = new VertexPair(v1, v2);
        assertEquals(new VertexPair(v2, v1), vp.reverse());
    }

}
