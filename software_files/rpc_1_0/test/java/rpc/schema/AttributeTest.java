/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 17, 2010
 * Time: 3:14:55 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

public class AttributeTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Entity a;
    private Entity b;
    private Entity c;
    private Entity d;
    private Relationship ab;
    private Relationship bc;
    private Relationship da;

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);

        a = Schema.getEntity("a");
        b = Schema.getEntity("b");
        c = Schema.getEntity("c");
        d = Schema.getEntity("d");
        ab = Schema.getRelationship("ab");
        bc = Schema.getRelationship("bc");
        da = Schema.getRelationship("da");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetBaseTable() {
        assertEquals(a, a.getPrimaryKey().getBaseTable());
        assertEquals(bc, bc.getPrimaryKey().getBaseTable());
        assertEquals(b, b.getAttribute("y1").getBaseTable());
        assertEquals(da, da.getForeignKeys()[0].getBaseTable());

        for (Attribute attr : c.getAllAttributes()) {
            assertEquals(c, attr.getBaseTable());
        }

        for (Attribute attr : da.getAllAttributes()) {
            assertEquals(da, attr.getBaseTable());
        }
    }

    public void testGetRefTable() {
        //implicitly tests setRefTable since must have been called in schema initialization

        assertNull(a.getPrimaryKey().getRefTable());
        assertNull(bc.getPrimaryKey().getRefTable());
        assertNull(b.getAttribute("y1").getRefTable());

        assertEquals(b, bc.getAttribute("b_id").getRefTable());
        assertEquals(d, da.getForeignKeys()[0].getRefTable());
    }

    public void testIsForeignKey() {
        for (Attribute attr : ab.getForeignKeys()) {
            assertTrue(attr.isForeignKey());
        }
        for (Attribute attr : bc.getForeignKeys()) {
            assertTrue(attr.isForeignKey());
        }
        for (Attribute attr : da.getForeignKeys()) {
            assertTrue(attr.isForeignKey());
        }

        assertFalse(bc.getPrimaryKey().isForeignKey());
        assertFalse(b.getAttribute("y1").isForeignKey());
        assertFalse(d.getPrimaryKey().isForeignKey());
    }

    public void testIsPrimaryKey() {
        assertTrue(a.getPrimaryKey().isPrimaryKey());
        assertTrue(b.getPrimaryKey().isPrimaryKey());
        assertTrue(ab.getPrimaryKey().isPrimaryKey());
        assertTrue(bc.getPrimaryKey().isPrimaryKey());

        assertFalse(bc.getForeignKeys()[1].isPrimaryKey());
        assertFalse(b.getAttribute("y1").isPrimaryKey());
    }

    public void testEquals() {
        Attribute aX1 = a.getAttribute("x1");
        Attribute newAX1 = new Attribute("x1", a);
        Attribute bY1 = b.getAttribute("y1");

        assertEquals(aX1, newAX1);
        assertEquals(aX1.hashCode(), newAX1.hashCode());

        assertFalse(aX1.equals(bY1));
        assertFalse(aX1.hashCode() == bY1.hashCode());
    }

}
