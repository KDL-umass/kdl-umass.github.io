/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 12:51:26 PM
 */
package rpc.design;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.dataretrieval.Aggregator;
import rpc.dataretrieval.NopAggregator;
import rpc.schema.Schema;
import rpc.schema.Entity;
import rpc.schema.Relationship;
import rpc.schema.SchemaItem;
import rpc.model.util.MockVariable;

import java.util.List;
import java.util.ArrayList;

public class PathTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Entity a;
    private Entity b;
    private Entity c;
    private Relationship ab;
    private Relationship bc;

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);

        a = Schema.getEntity("a");
        b = Schema.getEntity("b");
        c = Schema.getEntity("c");
        ab = Schema.getRelationship("ab");
        bc = Schema.getRelationship("bc");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEquals() {
        //Singleton (mock) paths
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        MockVariable mv2 = new MockVariable(a, "x1");
        List<SchemaItem> aPath2 = new ArrayList<SchemaItem>();
        aPath2.add(a);
        MockPath mp2 = new MockPath(a, a, aPath2, Cardinality.ONE, mv2);

        MockVariable mv3 = new MockVariable(b, "y1");
        List<SchemaItem> bPath = new ArrayList<SchemaItem>();
        bPath.add(b);
        MockPath mp3 = new MockPath(b, b, bPath, Cardinality.ONE, mv3);

        assertEquals(mp1, mp2);
        int mp1Hash = mp1.hashCode();
        assertEquals("Hash codes should be equal", mp1Hash, mp2.hashCode());
        assertEquals("Hash code should not change", mp1Hash, mp1.hashCode());

        assertFalse(mp1.equals(mp3));
        assertFalse(mp1.hashCode() == mp3.hashCode());

        //Longer (mock) paths
        MockVariable mv4 = new MockVariable(c, "z1");
        List<SchemaItem> aTocPath = new ArrayList<SchemaItem>();
        aTocPath.add(a);
        aTocPath.add(ab);
        aTocPath.add(b);
        aTocPath.add(bc);
        aTocPath.add(c);
        MockPath mp4 = new MockPath(a, c, aTocPath, Cardinality.MANY, mv4);

        MockVariable mv5 = new MockVariable(c, "z1");
        List<SchemaItem> aTocPath2 = new ArrayList<SchemaItem>();
        aTocPath2.add(a);
        aTocPath2.add(ab);
        aTocPath2.add(b);
        aTocPath2.add(bc);
        aTocPath2.add(c);
        MockPath mp5 = new MockPath(a, c, aTocPath2, Cardinality.MANY, mv5);

        MockVariable mv6 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp6 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv6);

        assertEquals(mp4, mp5);
        int mp4Hash = mp4.hashCode();
        assertEquals("Hash codes should be equal", mp4Hash, mp5.hashCode());
        assertEquals("Hash code should not change", mp4Hash, mp4.hashCode());

        assertFalse(mp4.equals(mp6));
        assertFalse(mp4.hashCode() == mp6.hashCode());
    }

    public void testGetAggregators() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        List<Aggregator> actual = mp1.getAggregators();
        List<Aggregator> expected = new ArrayList<Aggregator>();
        expected.add(new NopAggregator(mv1));

        TestUtil.verifyLists(expected, actual);
    }

    public void testGetCardinality() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        assertEquals(Cardinality.ONE, mp1.getCardinality());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        assertEquals(Cardinality.MANY, mp2.getCardinality());
    }

    public void testGetBaseItem() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        assertEquals(a, mp1.getBaseItem());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        assertEquals(a, mp2.getBaseItem());
    }

    public void testGetPath() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        List<SchemaItem> expected = new ArrayList<SchemaItem>();
        expected.add(a);

        TestUtil.verifyLists(expected, mp1.getPath());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        expected = new ArrayList<SchemaItem>();
        expected.add(a);
        expected.add(ab);
        expected.add(b);

        TestUtil.verifyLists(expected, mp2.getPath());
    }

    public void testGetPathNames() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        List<String> expected = new ArrayList<String>();
        expected.add("a");

        TestUtil.verifyLists(expected, mp1.getPathNames());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        expected = new ArrayList<String>();
        expected.add("a");
        expected.add("ab");
        expected.add("b");

        TestUtil.verifyLists(expected, mp2.getPathNames());
    }

    public void testGetTarget() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        assertEquals(a, mp1.getTarget());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        assertEquals(b, mp2.getTarget());
    }

    public void testGetVariable() {
        MockVariable mv1 = new MockVariable(a, "x1");
        List<SchemaItem> aPath = new ArrayList<SchemaItem>();
        aPath.add(a);
        MockPath mp1 = new MockPath(a, a, aPath, Cardinality.ONE, mv1);

        assertEquals(mv1, mp1.getVariable());

        MockVariable mv2 = new MockVariable(b, "y1");
        List<SchemaItem> aTobPath = new ArrayList<SchemaItem>();
        aTobPath.add(a);
        aTobPath.add(ab);
        aTobPath.add(b);
        MockPath mp2 = new MockPath(a, b, aTobPath, Cardinality.MANY, mv2);

        assertEquals(mv2, mp2.getVariable());
    }

}
