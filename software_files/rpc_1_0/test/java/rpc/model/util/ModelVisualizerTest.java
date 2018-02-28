/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 8, 2010
 * Time: 11:12:09 AM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.design.Path;

import java.util.Set;
import java.util.HashSet;

public class ModelVisualizerTest extends TestCase {

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

    public void testGetDot() {
        Set<String> expected = new HashSet<String>();
        expected.add("//Add in dependencies");
	    expected.add("x1 -> y1 [label=\"[b, ab, a].x1 --> [b].y1\\n[a, ab, b].y1 --> [a].x1\", dir=none];");
	    expected.add("y1 -> z2 [label=\"[c, bc, b].y1 --> [c].z2\"];");
	    expected.add("y1 -> z2 [label=\"[c, bc, b, bc, c, bc, b].y1 --> [c].z2\"];");
	    expected.add("w1 -> z1 [label=\"[c, bc, b, ab, a, da, d].w1 --> [c].z1\"];");
        expected.add("x2 -> bc_exists [label=\"[bc, b, ab, a].x2 --> [bc].bc_id\"];");
        expected.add("ab_exists -> z3 [label=\"[c, bc, b, ab].ab_id --> [c].z3\"];");

        Vertex x1 = new Vertex("a.x1");
        Vertex x2 = new Vertex("a.x2");
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");
        Vertex z2 = new Vertex("c.z2");
        Vertex z3 = new Vertex("c.z3");
        Vertex w1 = new Vertex("d.w1");
        Vertex ab = new Vertex("ab");
        Vertex bc = new Vertex("bc");

        Model model = new Model();
        Dependency d1 = new Dependency(x1, y1);
        Path d1Tpath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        Path d1Opath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        d1.unit = TestUtil.getUnit("b", d1Tpath, d1Opath);
        model.addDependence(new VertexPair(x1, y1), d1);

        Dependency d1Rev = new Dependency(y1, x1);
        Path d1RevTpath = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path d1RevOpath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        d1Rev.unit = TestUtil.getUnit("a", d1RevTpath, d1RevOpath);
        model.addDependence(new VertexPair(y1, x1), d1Rev);

        Dependency d2 = new Dependency(y1, z2);
        Path d2Tpath = TestUtil.getAttributePath("c", "b", "c,bc,b", "many", "y1");
        Path d2Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        d2.unit = TestUtil.getUnit("c", d2Tpath, d2Opath);
        model.addDependence(new VertexPair(y1, z2), d2);

        Dependency d3 = new Dependency(y1, z2);
        Path d3Tpath = TestUtil.getAttributePath("c", "b", "c,bc,b,bc,c,bc,b", "many", "y1");
        Path d3Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        d3.unit = TestUtil.getUnit("c", d3Tpath, d3Opath);
        model.addDependence(new VertexPair(y1, z2), d3);

        Dependency d4 = new Dependency(w1, z1);
        Path d4Tpath = TestUtil.getAttributePath("c", "d", "c,bc,b,ab,a,da,d", "many", "w1");
        Path d4Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        d4.unit = TestUtil.getUnit("c", d4Tpath, d4Opath);
        model.addDependence(new VertexPair(w1, z1), d4);

        Dependency d5 = new Dependency(x2, bc);
        Path d5Tpath = TestUtil.getAttributePath("bc", "a", "bc,b,ab,a", "one", "x2");
        Path d5Opath = TestUtil.getStructurePath("bc", "bc", "bc", "one");
        d5.unit = TestUtil.getUnit("bc", d5Tpath, d5Opath);
        model.addDependence(new VertexPair(x2, bc), d5);

        Dependency d6 = new Dependency(ab, z3);
        Path d6Tpath = TestUtil.getStructurePath("c", "ab", "c,bc,b,ab", "many");
        Path d6Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z3");
        d6.unit = TestUtil.getUnit("c", d6Tpath, d6Opath);
        model.addDependence(new VertexPair(ab, z3), d6);

        System.out.println(new HashSet<String>(ModelVisualizer.getDot(model, true)));
        TestUtil.verifySets("Comparing model dot output:", expected,
                new HashSet<String>(ModelVisualizer.getDot(model, true)));

        model.getDotFile("./test/test-model.dot", true);
    }

    public void testGetDotNoLabels() {
        Set<String> expected = new HashSet<String>();
        expected.add("//Add in dependencies");
	    expected.add("x1 -> y1 [dir=none];");
	    expected.add("y1 -> z2;");
	    expected.add("y1 -> z2;");
	    expected.add("w1 -> z1;");
        expected.add("x2 -> bc_exists;");
        expected.add("ab_exists -> z3;");

        Vertex x1 = new Vertex("a.x1");
        Vertex x2 = new Vertex("a.x2");
        Vertex y1 = new Vertex("b.y1");
        Vertex z1 = new Vertex("c.z1");
        Vertex z2 = new Vertex("c.z2");
        Vertex z3 = new Vertex("c.z3");
        Vertex w1 = new Vertex("d.w1");
        Vertex ab = new Vertex("ab");
        Vertex bc = new Vertex("bc");

        Model model = new Model();
        Dependency d1 = new Dependency(x1, y1);
        Path d1Tpath = TestUtil.getAttributePath("b", "a", "b,ab,a", "one", "x1");
        Path d1Opath = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        d1.unit = TestUtil.getUnit("b", d1Tpath, d1Opath);
        model.addDependence(new VertexPair(x1, y1), d1);

        Dependency d1Rev = new Dependency(y1, x1);
        Path d1RevTpath = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        Path d1RevOpath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        d1Rev.unit = TestUtil.getUnit("a", d1RevTpath, d1RevOpath);
        model.addDependence(new VertexPair(y1, x1), d1Rev);

        Dependency d2 = new Dependency(y1, z2);
        Path d2Tpath = TestUtil.getAttributePath("c", "b", "c,bc,b", "many", "y1");
        Path d2Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        d2.unit = TestUtil.getUnit("c", d2Tpath, d2Opath);
        model.addDependence(new VertexPair(y1, z2), d2);

        Dependency d3 = new Dependency(y1, z2);
        Path d3Tpath = TestUtil.getAttributePath("c", "b", "c,bc,b,bc,c,bc,b", "many", "y1");
        Path d3Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z2");
        d3.unit = TestUtil.getUnit("c", d3Tpath, d3Opath);
        model.addDependence(new VertexPair(y1, z2), d3);

        Dependency d4 = new Dependency(w1, z1);
        Path d4Tpath = TestUtil.getAttributePath("c", "d", "c,bc,b,ab,a,da,d", "many", "w1");
        Path d4Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z1");
        d4.unit = TestUtil.getUnit("c", d4Tpath, d4Opath);
        model.addDependence(new VertexPair(w1, z1), d4);

        Dependency d5 = new Dependency(x2, bc);
        Path d5Tpath = TestUtil.getAttributePath("bc", "a", "bc,b,ab,a", "one", "x2");
        Path d5Opath = TestUtil.getStructurePath("bc", "bc", "bc", "one");
        d5.unit = TestUtil.getUnit("bc", d5Tpath, d5Opath);
        model.addDependence(new VertexPair(x2, bc), d5);

        Dependency d6 = new Dependency(ab, z3);
        Path d6Tpath = TestUtil.getStructurePath("c", "ab", "c,bc,b,ab", "many");
        Path d6Opath = TestUtil.getAttributePath("c", "c", "c", "one", "z3");
        d6.unit = TestUtil.getUnit("c", d6Tpath, d6Opath);
        model.addDependence(new VertexPair(ab, z3), d6);

        TestUtil.verifySets("Comparing model dot output:", expected, 
                new HashSet<String>(ModelVisualizer.getDot(model, false)));

        model.getDotFile("./test/test-model-no-labels.dot", false);
    }
}
