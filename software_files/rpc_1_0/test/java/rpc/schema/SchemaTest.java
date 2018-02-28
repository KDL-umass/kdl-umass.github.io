/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 17, 2010
 * Time: 12:51:48 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

import java.util.List;
import java.util.ArrayList;

public class SchemaTest extends TestCase {

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

        a = new Entity("a");
        b = new Entity("b");
        c = new Entity("c");
        d = new Entity("d");
        ab = new Relationship("ab", a, b);
        bc = new Relationship("bc", b, c);
        da = new Relationship("da", d, a);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEntities() {
        //Implicitly tests loadEntities and addEntity since schema must have been loaded correctly

        //Get entity that should exist
        assertEquals(a, Schema.getEntity("a"));
        //Schema should know which schema items are entities
        assertTrue(Schema.isEntity("a"));
        //Should be able to get entities without knowing if it's an entity or relationship
        assertEquals(a, Schema.getSchemaItem("a"));

        //Schema should not think a relationship is an entity
        assertFalse(Schema.isEntity("ab"));
        //Schema should not think nonexistent name is an entity
        assertFalse(Schema.isEntity("nonEntity"));

        assertNull(Schema.getEntity("nonEntity"));
    }

    public void testRelationships() {
        //Implicitly tests loadRelationships and addRelationship since schema must have been loaded correctly

        //Get relationship that should exist
        assertEquals(ab, Schema.getRelationship("ab"));
        //Schema should know which schema items are relationships
        assertTrue(Schema.isRelationship("ab"));
        //Should be able to get relationships without knowing if it's an entity or relationship
        assertEquals(ab, Schema.getSchemaItem("ab"));

        //Schema should not think an entity is a relationship
        assertFalse(Schema.isRelationship("a"));
        //Schema should not think nonexistent name is a relationship
        assertFalse(Schema.isRelationship("nonRelationship"));

        assertNull(Schema.getRelationship("nonRelationship"));
    }

    public void testGetAllSchemaItems() {
        List<SchemaItem> actual = Schema.getAllSchemaItems();

        List<SchemaItem> expected = new ArrayList<SchemaItem>();

        expected.add(a);
        expected.add(b);
        expected.add(c);
        expected.add(d);
        expected.add(ab);
        expected.add(bc);
        expected.add(da);

        TestUtil.verifyLists(expected, actual);        
    }

}
