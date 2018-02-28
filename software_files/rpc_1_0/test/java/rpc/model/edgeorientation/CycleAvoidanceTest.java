/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 5, 2010
 * Time: 11:33:18 AM
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

public class CycleAvoidanceTest extends TestCase {

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
        //x1-------------x2
        // \             ^
        //  \-->y1-->z1-/

        //Expected output model:
        //x1------------>x2
        // \             ^
        //  \-->y1-->z1-/

        Dependency d1 = new Dependency(x1, x2);
        d1.unit = new Unit(x1, x2);
        m.addDependence(new VertexPair(x1, x2), d1);

        Dependency d2 = new Dependency(x2, x1);
        d2.unit = new Unit(x2, x1);
        m.addDependence(new VertexPair(x2, x1), d2);

        Dependency d3 = new Dependency(x1, y1);
        d3.unit = new Unit(x1, y1);
        m.addDependence(new VertexPair(x1, y1), d3);

        Dependency d9 = new Dependency(y1, z1);
        d9.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d9);

        Dependency d10 = new Dependency(z1, x2);
        d10.unit = new Unit(z1, x2);
        m.addDependence(new VertexPair(z1, x2), d10);

        CycleAvoidance ca = new CycleAvoidance(vertices, m, null);
        assertTrue(ca.orient());
        assertTrue(m.hasDependence(new VertexPair(x1, x2)));
        assertFalse(m.hasDependence(new VertexPair(x2, x1)));


        m = new Model();

        //Input model:
        //x1-------------x2
        // \             |
        //  \-->y1-->z1<-/

        //Expected output model (same):
        //x1------------x2
        // \             |
        //  \-->y1-->z1<-/

        d1 = new Dependency(x1, x2);
        d1.unit = new Unit(x1, x2);
        m.addDependence(new VertexPair(x1, x2), d1);

        d2 = new Dependency(x2, x1);
        d2.unit = new Unit(x2, x1);
        m.addDependence(new VertexPair(x2, x1), d2);

        d3 = new Dependency(x1, y1);
        d3.unit = new Unit(x1, y1);
        m.addDependence(new VertexPair(x1, y1), d3);

        d9 = new Dependency(y1, z1);
        d9.unit = new Unit(y1, z1);
        m.addDependence(new VertexPair(y1, z1), d9);

        d10 = new Dependency(x2, z1);
        d10.unit = new Unit(x2, z1);
        m.addDependence(new VertexPair(x2, z1), d10);

        ca = new CycleAvoidance(vertices, m, null);
        assertFalse(ca.orient());
        assertTrue(m.hasDependence(new VertexPair(x1, x2)));
        assertTrue(m.hasDependence(new VertexPair(x2, x1)));        
    }

}
