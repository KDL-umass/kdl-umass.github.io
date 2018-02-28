/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 17, 2010
 * Time: 2:56:58 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.design.Cardinality;

import java.util.Set;
import java.util.HashSet;

public class RelationshipTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Relationship ab;
    private Relationship bc;
    private Relationship da;

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);

        ab = Schema.getRelationship("ab");
        bc = Schema.getRelationship("bc");
        da = Schema.getRelationship("da");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetPrimaryKey() {
        assertEquals(new Attribute("ab_id", ab), ab.getPrimaryKey());
        assertEquals(new Attribute("bc_id", bc), bc.getPrimaryKey());
        assertEquals(new Attribute("da_id", da), da.getPrimaryKey());
    }

    public void testGetForeignKeys() {
        Attribute[] expected = new Attribute[]{new Attribute("a_id", ab), new Attribute("b_id", ab)};
        TestUtil.verifyArrays(expected, ab.getForeignKeys());

        expected = new Attribute[]{new Attribute("b_id", bc), new Attribute("c_id", bc)};
        TestUtil.verifyArrays(expected, bc.getForeignKeys());

        expected = new Attribute[]{new Attribute("d_id", da), new Attribute("a_id", da)};
        TestUtil.verifyArrays(expected, da.getForeignKeys());
    }

    public void testGetAttribute() {
        assertEquals(new Attribute("xy1", ab), ab.getAttribute("xy1"));
        assertEquals(new Attribute("xy2", ab), ab.getAttribute("xy2"));
        assertEquals(new Attribute("yz1", bc), bc.getAttribute("yz1"));
        assertEquals(new Attribute("wx1", da), da.getAttribute("wx1"));
    }

    public void testGetAttributes() {
        Set<Attribute> actual = ab.getAllAttributes();
        Set<Attribute> expected = new HashSet<Attribute>();
        expected.add(new Attribute("ab_id", ab));
        expected.add(new Attribute("a_id", ab));
        expected.add(new Attribute("b_id", ab));
        expected.add(new Attribute("xy1", ab));
        expected.add(new Attribute("xy2", ab));

        TestUtil.verifySets("Comparing attributes on ab", expected, actual);

        actual = bc.getAllAttributes();
        expected = new HashSet<Attribute>();
        expected.add(new Attribute("bc_id", bc));
        expected.add(new Attribute("b_id", bc));
        expected.add(new Attribute("c_id", bc));
        expected.add(new Attribute("yz1", bc));

        TestUtil.verifySets("Comparing attributes on bc", expected, actual);

        actual = da.getAllAttributes();
        expected = new HashSet<Attribute>();
        expected.add(new Attribute("da_id", da));
        expected.add(new Attribute("d_id", da));
        expected.add(new Attribute("a_id", da));
        expected.add(new Attribute("wx1", da));

        TestUtil.verifySets("Comparing attributes on da", expected, actual);
    }

    public void testCardinality() {
        //implicitly tests assignCardinality since schema was loaded

        assertEquals(Cardinality.ONE, ab.entity1Card);
        assertEquals(Cardinality.MANY, ab.entity2Card);

        assertEquals(Cardinality.MANY, bc.entity1Card);
        assertEquals(Cardinality.MANY, bc.entity2Card);

        assertEquals(Cardinality.ONE, da.entity1Card);
        assertEquals(Cardinality.ONE, da.entity2Card);
    }

    public void testEquals() {
        Relationship newAB = new Relationship("ab", new Entity("a"), new Entity("b"));
        assertEquals(ab, newAB);
        assertEquals(ab.hashCode(), newAB.hashCode());

        assertFalse(ab.equals(bc));
        assertFalse(ab.hashCode() == bc.hashCode());
    }

}
