/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 17, 2010
 * Time: 2:01:47 PM
 */
package rpc.util;

import junit.framework.TestCase;
import rpc.TestUtil;
import rpc.schema.Entity;
import rpc.schema.SchemaItem;
import rpc.schema.Relationship;

import java.util.List;
import java.util.ArrayList;

public class SchemaUtilTest extends TestCase {

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

    public void testListToSchemaItems() {
        Entity a = new Entity("a");
        Entity b = new Entity("b");
        Entity c = new Entity("c");
        Entity d = new Entity("d");
        Relationship ab = new Relationship("ab", a, b);
        Relationship bc = new Relationship("bc", b, c);
        Relationship da = new Relationship("da", d, a);

        //set up list of item names to retrieve, all should exist in schema
        List<String> itemNames = new ArrayList<String>();
        itemNames.add("a");
        itemNames.add("c");
        itemNames.add("ab");
        itemNames.add("da");
        List<SchemaItem> actual = SchemaUtil.listToSchemaItems(itemNames);

        List<SchemaItem> expected = new ArrayList<SchemaItem>();

        expected.add(a);
        expected.add(c);
        expected.add(ab);
        expected.add(da);

        TestUtil.verifyLists(expected, actual);

        //set up list of item names to retrieve, throw in a nonexistent item (adds null, warns user)
        itemNames = new ArrayList<String>();
        itemNames.add("b");
        itemNames.add("d");
        itemNames.add("bc");
        itemNames.add("nonExistent");
        actual = SchemaUtil.listToSchemaItems(itemNames);

        expected = new ArrayList<SchemaItem>();

        expected.add(b);
        expected.add(d);
        expected.add(bc);
        expected.add(null);

        TestUtil.verifyLists(expected, actual);
    }
}
