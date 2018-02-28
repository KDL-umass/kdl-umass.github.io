/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 11:39:34 AM
 */
package rpc.model.util;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.schema.Schema;

public class StructureVariableTest extends TestCase {

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

    public void testGetRelationship() {
        StructureVariable sv = new StructureVariable(Schema.getRelationship("ab"));
        assertEquals(Schema.getRelationship("ab"), sv.getRelationship());
        assertEquals(Schema.getRelationship("ab"), sv.getSource());
    }
}
