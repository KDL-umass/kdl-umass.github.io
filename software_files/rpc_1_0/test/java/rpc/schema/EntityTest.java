/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 17, 2010
 * Time: 2:35:17 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;

import java.util.Set;
import java.util.HashSet;

public class EntityTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Entity a;
    private Entity b;
    private Entity c;
    private Entity d;

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);

        a = Schema.getEntity("a");
        b = Schema.getEntity("b");
        c = Schema.getEntity("c");
        d = Schema.getEntity("d");
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetPrimaryKey() {
        assertEquals(new Attribute("a_id", a), a.getPrimaryKey());
        assertEquals(new Attribute("b_id", b), b.getPrimaryKey());
        assertEquals(new Attribute("c_id", c), c.getPrimaryKey());
        assertEquals(new Attribute("d_id", d), d.getPrimaryKey());
    }

    public void testGetAttribute() {
        assertEquals(new Attribute("x1", a), a.getAttribute("x1"));
        assertEquals(new Attribute("y1", b), b.getAttribute("y1"));
        assertEquals(new Attribute("z1", c), c.getAttribute("z1"));
        assertEquals(new Attribute("w1", d), d.getAttribute("w1"));
    }

    public void testGetAttributes() {
        Set<Attribute> actual = a.getAllAttributes();
        Set<Attribute> expected = new HashSet<Attribute>();
        expected.add(new Attribute("a_id", a));
        expected.add(new Attribute("x1", a));
        expected.add(new Attribute("x2", a));

        TestUtil.verifySets("Comparing attributes on a", expected, actual);

        actual = b.getAllAttributes();
        expected = new HashSet<Attribute>();
        expected.add(new Attribute("b_id", b));
        expected.add(new Attribute("y1", b));

        TestUtil.verifySets("Comparing attributes on b", expected, actual);

        actual = c.getAllAttributes();
        expected = new HashSet<Attribute>();
        expected.add(new Attribute("c_id", c));
        expected.add(new Attribute("z1", c));
        expected.add(new Attribute("z2", c));
        expected.add(new Attribute("z3", c));
        expected.add(new Attribute("z4", c));

        TestUtil.verifySets("Comparing attributes on c", expected, actual);

        actual = d.getAllAttributes();
        expected = new HashSet<Attribute>();
        expected.add(new Attribute("d_id", d));
        expected.add(new Attribute("w1", d));

        TestUtil.verifySets("Comparing attributes on d", expected, actual);
    }

    public void testEquals() {
        assertEquals(a, new Entity("a"));
        assertEquals(a.hashCode(), new Entity("a").hashCode());

        assertFalse(a.equals(b));
        assertFalse(a.hashCode() == b.hashCode());
    }

}
