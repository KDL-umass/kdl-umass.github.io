/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 11:18:31 AM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.schema.Schema;

public class VariableTest extends TestCase {

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

    public void testEquals() {
        MockVariable mv1 = new MockVariable(Schema.getSchemaItem("a"), "test1");
        MockVariable mv2 = new MockVariable(Schema.getSchemaItem("a"), "test1");
        MockVariable mv3 = new MockVariable(Schema.getSchemaItem("a"), "test2");

        assertEquals(mv1, mv2);
        int mv1Hash = mv1.hashCode();
        assertEquals("Hash codes should be equal", mv1Hash, mv2.hashCode());
        assertEquals("Hash code should not change", mv1Hash, mv1.hashCode());

        assertFalse(mv1.equals(mv3));
        assertFalse(mv1.hashCode() == mv3.hashCode());        
    }

    public void testGetSource() {
        MockVariable mv1 = new MockVariable(Schema.getSchemaItem("a"), "test1");
        MockVariable mv2 = new MockVariable(Schema.getSchemaItem("ab"), "test2");

        assertEquals(Schema.getSchemaItem("a"), mv1.getSource());
        assertEquals(Schema.getSchemaItem("ab"), mv2.getSource());
    }

    public void testName() {
        MockVariable mv1 = new MockVariable(Schema.getSchemaItem("a"), "test1");
        MockVariable mv2 = new MockVariable(Schema.getSchemaItem("ab"), "test2");

        assertEquals("test1", mv1.name);
        assertEquals("test2", mv2.name);
    }
}
