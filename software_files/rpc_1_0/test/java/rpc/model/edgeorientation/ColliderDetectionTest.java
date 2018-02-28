/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 5, 2010
 * Time: 10:59:43 AM
 */
package rpc.model.edgeorientation;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.design.Unit;
import rpc.model.util.Vertex;
import rpc.model.util.Model;
import rpc.model.util.Dependency;
import rpc.model.util.VertexPair;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class ColliderDetectionTest extends TestCase {

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

    public void testOrient() {
        Vertex x1 = new Vertex("a.x1");
        Vertex x2 = new Vertex("a.x2");
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");

        Set<Vertex> vertices = new HashSet<Vertex>();
        vertices.add(x1);
        vertices.add(x2);
        vertices.add(y1);
        vertices.add(z1);

        Model m = new Model();

        //Input model:
        //x2---x1---z1
        // \         /
        //  \--y1---/

        //Expected output model:
        //x2--->x1<---z1
        // \         /
        //  \-->y1<-/

        Dependency d1 = new Dependency(x1, x2);
        d1.unit = new Unit(x1, x2);
        m.addDependence(new VertexPair(x1, x2), d1);

        Dependency d2 = new Dependency(x2, x1);
        d2.unit = new Unit(x2, x1);
        m.addDependence(new VertexPair(x2, x1), d2);

        Dependency d3 = new Dependency(x1, z1);
        d3.unit = new Unit(x1, z1);
        m.addDependence(new VertexPair(x1, z1), d3);

        Dependency d4 = new Dependency(z1, x1);
        d4.unit = new Unit(z1, x1);
        m.addDependence(new VertexPair(z1, x1), d4);

        Dependency d6 = new Dependency(y1, x2);
        d6.unit = new Unit(y1, x2);
        m.addDependence(new VertexPair(y1, x2), d6);

        Dependency d7 = new Dependency(x2, y1);
        d7.unit = new Unit(x2, y1);
        m.addDependence(new VertexPair(x2, y1), d7);

        Dependency d8 = new Dependency(z1, y1);
        d8.unit = new Unit(z1, y1);
        m.addDependence(new VertexPair(z1, y1), d8);

        Dependency d9 = new Dependency(y1, z1);
        d9.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d9);

        Map<VertexPair, Set<Vertex>> sepsets = new HashMap<VertexPair, Set<Vertex>>();
        sepsets.put(new VertexPair(x1, y1), new HashSet<Vertex>());
        sepsets.put(new VertexPair(x2, z1), new HashSet<Vertex>());

        ColliderDetection cd = new ColliderDetection(vertices, m, sepsets);
        assertTrue(cd.orient());
        assertTrue(m.hasDependence(new VertexPair(x2, x1)));
        assertFalse(m.hasDependence(new VertexPair(x1, x2)));

        assertTrue(m.hasDependence(new VertexPair(z1, x1)));
        assertFalse(m.hasDependence(new VertexPair(x1, z1)));

        assertTrue(m.hasDependence(new VertexPair(x2, y1)));
        assertFalse(m.hasDependence(new VertexPair(y1, x2)));

        assertTrue(m.hasDependence(new VertexPair(z1, y1)));
        assertFalse(m.hasDependence(new VertexPair(y1, z1)));
    }

    public void testOrientWithExistence() {
        Vertex x1 = new Vertex("a.x1");
        Vertex x2 = new Vertex("a.x2");
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");
        Vertex bc = new Vertex("bc");
        Vertex ab = new Vertex("ab");

        Set<Vertex> vertices = new HashSet<Vertex>();
        vertices.add(x1);
        vertices.add(x2);
        vertices.add(y1);
        vertices.add(z1);
        vertices.add(bc);
        vertices.add(ab);

        Model m = new Model();

        //Input model:
        //   bc
        //  /  \
        //x2---x1---z1
        // \         /\
        //  \--y1---/  \
        //      ^       \
        //       \-------ab

        //Expected output model:
        //   bc
        //  /  \
        //x2---x1-----z1
        // \          /\
        //  \-->y1<--/  \
        //      ^        \
        //       \-------ab

        Dependency d1 = new Dependency(x1, x2);
        d1.unit = new Unit(x1, x2);
        m.addDependence(new VertexPair(x1, x2), d1);

        Dependency d2 = new Dependency(x2, x1);
        d2.unit = new Unit(x2, x1);
        m.addDependence(new VertexPair(x2, x1), d2);

        Dependency d3 = new Dependency(x1, z1);
        d3.unit = new Unit(x1, z1);
        m.addDependence(new VertexPair(x1, z1), d3);

        Dependency d4 = new Dependency(z1, x1);
        d4.unit = new Unit(z1, x1);
        m.addDependence(new VertexPair(z1, x1), d4);

        Dependency d6 = new Dependency(y1, x2);
        d6.unit = new Unit(y1, x2);
        m.addDependence(new VertexPair(y1, x2), d6);

        Dependency d7 = new Dependency(x2, y1);
        d7.unit = new Unit(x2, y1);
        m.addDependence(new VertexPair(x2, y1), d7);

        Dependency d8 = new Dependency(z1, y1);
        d8.unit = new Unit(z1, y1);
        m.addDependence(new VertexPair(z1, y1), d8);

        Dependency d9 = new Dependency(y1, z1);
        d9.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d9);

        Dependency d10 = new Dependency(bc, x2);
        d10.unit = new Unit(bc, x2);
        m.addDependence(new VertexPair(bc, x2), d10);

        Dependency d11 = new Dependency(x2, bc);
        d11.unit = new Unit(x2, bc);
        m.addDependence(new VertexPair(x2, bc), d11);

        Dependency d12 = new Dependency(bc, x1);
        d12.unit = new Unit(bc, x1);
        m.addDependence(new VertexPair(bc, x1), d12);

        Dependency d13 = new Dependency(x1, bc);
        d13.unit = new Unit(x1, bc);
        m.addDependence(new VertexPair(x1, bc), d13);

        Dependency d14 = new Dependency(ab, z1);
        d14.unit = new Unit(ab, z1);
        m.addDependence(new VertexPair(ab, z1), d14);

        Dependency d15 = new Dependency(z1, ab);
        d15.unit = new Unit(z1, ab);
        m.addDependence(new VertexPair(z1, ab), d15);

        Dependency d16 = new Dependency(ab, y1);
        d16.unit = new Unit(ab, y1);
        m.addDependence(new VertexPair(ab, y1), d16);

        Map<VertexPair, Set<Vertex>> sepsets = new HashMap<VertexPair, Set<Vertex>>();
        sepsets.put(new VertexPair(x1, y1), new HashSet<Vertex>());
        sepsets.put(new VertexPair(x2, z1), new HashSet<Vertex>());

        ColliderDetection cd = new ColliderDetection(vertices, m, sepsets);
        assertTrue(cd.orient());
        assertTrue(m.hasDependence(new VertexPair(x2, x1)));
        assertTrue(m.hasDependence(new VertexPair(x1, x2)));

        assertTrue(m.hasDependence(new VertexPair(z1, x1)));
        assertTrue(m.hasDependence(new VertexPair(x1, z1)));

        assertTrue(m.hasDependence(new VertexPair(x2, y1)));
        assertFalse(m.hasDependence(new VertexPair(y1, x2)));

        assertTrue(m.hasDependence(new VertexPair(z1, y1)));
        assertFalse(m.hasDependence(new VertexPair(y1, z1)));
    }
}

