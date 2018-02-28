/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 5, 2010
 * Time: 11:40:33 AM
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

public class KnownNonCollidersTest extends TestCase {

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
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");

        Set<Vertex> vertices = new HashSet<Vertex>();
        vertices.add(x1);
        vertices.add(y1);
        vertices.add(z1);

        Model m = new Model();

        //Input model:
        //x1-->y1---z1

        //Expected output model:
        //x1-->y1-->z1

        Dependency d1 = new Dependency(x1, y1);
        d1.unit = new Unit(x1, y1);
        m.addDependence(new VertexPair(x1, y1), d1);

        Dependency d2 = new Dependency(y1, z1);
        d2.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d2);

        Dependency d3 = new Dependency(z1, y1);
        d3.unit = new Unit(z1, y1);
        m.addDependence(new VertexPair(z1, y1), d3);

        Map<VertexPair, Set<Vertex>> sepsets = new HashMap<VertexPair, Set<Vertex>>();
        sepsets.put(new VertexPair(x1, z1), new HashSet<Vertex>());
        sepsets.get(new VertexPair(x1, z1)).add(y1);

        KnownNonColliders knc = new KnownNonColliders(vertices, m, sepsets);
        assertTrue(knc.orient());
        assertTrue(m.hasDependence(new VertexPair(y1, z1)));
        assertFalse(m.hasDependence(new VertexPair(z1, y1)));
    }

    public void testOrientWithExistence() {
        Vertex x1 = new Vertex("a.x1");
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");
        Vertex bc = new Vertex("bc");

        Set<Vertex> vertices = new HashSet<Vertex>();
        vertices.add(x1);
        vertices.add(y1);
        vertices.add(z1);
        vertices.add(bc);

        Model m = new Model();

        //Input model:
        //x1-->y1------z1
        //     \       /
        //      -->bc--

        //Expected output model (same):
        //x1-->y1------z1
        //     \       /
        //      -->bc--

        Dependency d1 = new Dependency(x1, y1);
        d1.unit = new Unit(x1, y1);
        m.addDependence(new VertexPair(x1, y1), d1);

        Dependency d2 = new Dependency(y1, z1);
        d2.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d2);

        Dependency d3 = new Dependency(z1, y1);
        d3.unit = new Unit(z1, y1);
        m.addDependence(new VertexPair(z1, y1), d3);

        Dependency d4 = new Dependency(y1, bc);
        d4.unit = new Unit(y1, bc);
        m.addDependence(new VertexPair(y1, bc), d4);

        Dependency d6 = new Dependency(z1, bc);
        d6.unit = new Unit(z1, bc);
        m.addDependence(new VertexPair(z1, bc), d6);

        Dependency d7 = new Dependency(bc, z1);
        d7.unit = new Unit(bc, z1);
        m.addDependence(new VertexPair(bc, z1), d7);

        Map<VertexPair, Set<Vertex>> sepsets = new HashMap<VertexPair, Set<Vertex>>();
        sepsets.put(new VertexPair(x1, z1), new HashSet<Vertex>());
        sepsets.get(new VertexPair(x1, z1)).add(y1);

        sepsets.put(new VertexPair(x1, bc), new HashSet<Vertex>());
        sepsets.get(new VertexPair(x1, bc)).add(y1);        

        KnownNonColliders knc = new KnownNonColliders(vertices, m, sepsets);
        assertFalse(knc.orient());
        assertTrue(m.hasDependence(new VertexPair(y1, z1)));
        assertTrue(m.hasDependence(new VertexPair(z1, y1)));        
    }

}
