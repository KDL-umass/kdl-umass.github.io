/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 22, 2010
 * Time: 10:47:21 AM
 */
package rpc.design;

import junit.framework.TestCase;
import rpc.schema.Entity;
import rpc.schema.Relationship;
import rpc.schema.Schema;
import rpc.schema.SchemaItem;
import rpc.util.LogUtil;
import rpc.TestUtil;

import java.util.List;
import java.util.ArrayList;

public class UnitTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Entity b;
    private Entity c;
    private Relationship bc;

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);


        b = Schema.getEntity("b");
        c = Schema.getEntity("c");
        bc = Schema.getRelationship("bc");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEquals() {
        //[b bc].yz1 -> [b].y1
        List<SchemaItem> bcPath = new ArrayList<SchemaItem>();
        bcPath.add(b);
        bcPath.add(bc);
        List<SchemaItem> bPath = new ArrayList<SchemaItem>();
        bPath.add(b);
        Unit u1 = new Unit(b, new AttributePath(b, bc, bcPath, Cardinality.MANY, bc.getAttribute("yz1")), new AttributePath(b, b, bPath, Cardinality.ONE, b.getAttribute("y1")));

        //[b bc].yz1 -> [b].y1
        List<SchemaItem> bcPath2 = new ArrayList<SchemaItem>();
        bcPath2.add(b);
        bcPath2.add(bc);
        List<SchemaItem> bPath2 = new ArrayList<SchemaItem>();
        bPath2.add(b);
        Unit u2 = new Unit(b, new AttributePath(b, bc, bcPath2, Cardinality.MANY, bc.getAttribute("yz1")), new AttributePath(b, b, bPath2, Cardinality.ONE, b.getAttribute("y1")));

        //[b bc c].z1 -> [b].y1
        List<SchemaItem> cPath = new ArrayList<SchemaItem>();
        cPath.add(b);
        cPath.add(bc);
        cPath.add(c);
        List<SchemaItem> bPath3 = new ArrayList<SchemaItem>();
        bPath3.add(b);
        Unit u3 = new Unit(b, new AttributePath(b, c, cPath, Cardinality.MANY, c.getAttribute("z1")), new AttributePath(b, b, bPath3, Cardinality.ONE, b.getAttribute("y1")));

        assertEquals(u1, u2);
        int u1Hash = u1.hashCode();
        assertEquals("Hash codes should be equal", u1Hash, u2.hashCode());
        assertEquals("Hash code should not change", u1Hash, u1.hashCode());

        assertFalse(u1.equals(u3));
        assertFalse(u1.hashCode() == u3.hashCode());        
    }

    public void testReverse() {
        //[b bc].yz1 -> [b].y1
        List<SchemaItem> bTobcPath = new ArrayList<SchemaItem>();
        bTobcPath.add(b);
        bTobcPath.add(bc);
        List<SchemaItem> bPath = new ArrayList<SchemaItem>();
        bPath.add(b);
        Unit u = new Unit(b, new AttributePath(b, bc, bTobcPath, Cardinality.MANY, bc.getAttribute("yz1")), new AttributePath(b, b, bPath, Cardinality.ONE, b.getAttribute("y1")));

        //[bc b].y1 --> [bc].yz1
        List<SchemaItem> bcTobPath = new ArrayList<SchemaItem>();
        bcTobPath.add(bc);
        bcTobPath.add(b);
        List<SchemaItem> bcPath = new ArrayList<SchemaItem>();
        bcPath.add(bc);
        Unit expected = new Unit(bc, new AttributePath(bc, b, bcTobPath, Cardinality.ONE, b.getAttribute("y1")), new AttributePath(bc, bc, bcPath, Cardinality.ONE, bc.getAttribute("yz1")));
        assertEquals(expected, u.reverse());
    }
}
