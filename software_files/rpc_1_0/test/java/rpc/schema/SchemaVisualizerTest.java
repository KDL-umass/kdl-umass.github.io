/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 25, 2010
 * Time: 2:14:12 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

import java.util.Set;
import java.util.HashSet;

public class SchemaVisualizerTest extends TestCase {

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
        expected.add("//Entity, Relationship, and Attribute defintions");
        expected.add("entity_a [shape=box, style=bold, label=\"a\"];");
        expected.add("x1;");
        expected.add("x2;");
        expected.add("\n");
        expected.add("relationship_ab [shape=diamond, style=bold, label=\"ab\"];");
        expected.add("xy1;");
        expected.add("xy2;");
        expected.add("ab_exists [shape=point];");
        expected.add("\n");
        expected.add("entity_b [shape=box, style=bold, label=\"b\"];");
        expected.add("y1;");
        expected.add("\n");
        expected.add("relationship_bc [shape=diamond, style=bold, label=\"bc\"];");
        expected.add("yz1;");
        expected.add("bc_exists [shape=point];");
        expected.add("\n");
        expected.add("entity_c [shape=box, style=bold, label=\"c\"];");
        expected.add("z1;");
        expected.add("z2;");
        expected.add("z3;");
        expected.add("z4;");
        expected.add("\n");
        expected.add("relationship_da [shape=diamond, style=bold, label=\"da\"];");
        expected.add("wx1;");
        expected.add("da_exists [shape=point];");
        expected.add("\n");
        expected.add("entity_d [shape=box, style=bold, label=\"d\"];");
        expected.add("w1;");
        expected.add("\n");
        expected.add("//Rank entity and relationship groups the same, as well as attributes on same item");
        expected.add("{rank=same; entity_a; relationship_ab; entity_b};");
        expected.add("{rank=same; entity_b; relationship_bc; entity_c};");
        expected.add("{rank=same; entity_d; relationship_da; entity_a};");
        expected.add("{rank=same; x1; x2};");
        expected.add("{rank=same; xy1; xy2; ab_exists};");
        expected.add("{rank=same; y1};");
        expected.add("{rank=same; yz1; bc_exists};");
        expected.add("{rank=same; z1; z2; z3; z4};");
        expected.add("{rank=same; wx1; da_exists};");
        expected.add("{rank=same; w1};");
        expected.add("\n");
        expected.add("//Connect attributes to their items (try to keep them as straight edges)");
        expected.add("entity_a -> x1 [style=dashed, dir=none, weight=8];");
        expected.add("entity_a -> x2 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_ab -> xy1 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_ab -> xy2 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_ab -> ab_exists [style=dashed, dir=none, weight=8];");
        expected.add("entity_b -> y1 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_bc -> yz1 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_bc -> bc_exists [style=dashed, dir=none, weight=8];");
        expected.add("entity_c -> z1 [style=dashed, dir=none, weight=8];");
        expected.add("entity_c -> z2 [style=dashed, dir=none, weight=8];");
        expected.add("entity_c -> z3 [style=dashed, dir=none, weight=8];");
        expected.add("entity_c -> z4 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_da -> wx1 [style=dashed, dir=none, weight=8];");
        expected.add("relationship_da -> da_exists [style=dashed, dir=none, weight=8];");
        expected.add("entity_d -> w1 [style=dashed, dir=none, weight=8];");
        expected.add("\n");
        expected.add("//Connect entities and relationships with cardinalities");
        expected.add("entity_a -> relationship_ab [style=dashed, dir=none, minlen=2];");
        expected.add("relationship_ab -> entity_b [style=dashed, dir=forward, arrowhead=crow, minlen=2];");
        expected.add("entity_b -> relationship_bc [style=dashed, dir=back, arrowtail=crow, minlen=2];");
        expected.add("relationship_bc -> entity_c [style=dashed, dir=forward, arrowhead=crow, minlen=2];");
        expected.add("entity_d -> relationship_da [style=dashed, dir=none, minlen=2];");
        expected.add("relationship_da -> entity_a [style=dashed, dir=none, minlen=2];");

        TestUtil.verifySets("Comparing Schema dot output:", expected, new HashSet<String>(SchemaVisualizer.getDot()));

        Schema.getDotFile("./test/test-schema.dot");
    }
}
