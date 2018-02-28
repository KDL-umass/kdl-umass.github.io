/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 18, 2010
 * Time: 1:04:00 PM
 */
package rpc.schema;

import junit.framework.TestCase;
import junit.framework.Assert;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.model.util.AttributeVariable;
import rpc.model.util.StructureVariable;
import rpc.design.Path;
import rpc.dataretrieval.*;
import rpc.datagen.DataGenerator;
import rpc.datagen.ConstantConnector;
import rpc.datagen.CPTPrior;

import java.util.HashMap;

public class MetadataTest extends TestCase {

    private static final String SCHEMA_FILE = "./test/test-schema.pl";

    private Entity a;
    private Entity b;
    private Entity d;
    private Relationship ab;
    private Relationship da;

    private DataGenerator dg;


    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);

        a = Schema.getEntity("a");
        b = Schema.getEntity("b");
        d = Schema.getEntity("d");
        ab = Schema.getRelationship("ab");
        da = Schema.getRelationship("da");

        dg = new DataGenerator("testdb");
        dg.populateEntityTable("a", 20);
        dg.populateEntityTable("b", 100);
        dg.populateEntityTable("d", 20);

        ConstantConnector fiveLinks = new ConstantConnector("SELECT b_id FROM b", 5, false);
        dg.populateRelationshipTable("ab", fiveLinks, null);

        ConstantConnector oneLink = new ConstantConnector("SELECT a_id FROM a", 1, false);
        dg.populateRelationshipTable("da", oneLink, null);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        Database.close();
    }

    public void testGetItemCount() {
        Assert.assertEquals(20, Metadata.getItemCount(a));
        Assert.assertEquals(100, Metadata.getItemCount(b));
        Assert.assertEquals(20, Metadata.getItemCount(d));
        Assert.assertEquals(100, Metadata.getItemCount(ab));
        Assert.assertEquals(20, Metadata.getItemCount(da));
    }

    public void testGetDistinctValues() {
        //Attribute with only a single value
        HashMap<Integer, Double> table = new HashMap<Integer, Double>();
        table.put(1, 1.0);
        dg.generateAttribute("a", "x1", new CPTPrior(table));

        Path x1Path = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        Attribute x1Attr = a.getAttribute("x1");
        AttributeVariable x1AttrVar = new AttributeVariable(x1Attr);
        Aggregator x1NopAgg = new NopAggregator(x1AttrVar);
        Assert.assertEquals(1, Metadata.getDistinctValues(x1Path, x1NopAgg));

        //Attribute that probabilistically assigns one of two values
        table = new HashMap<Integer, Double>();
        table.put(1, 0.5);
        table.put(0, 0.5);
        dg.generateAttribute("b", "y1", new CPTPrior(table));

        Path y1Path = TestUtil.getAttributePath("b", "b", "b", "one", "y1");
        Attribute y1Attr = b.getAttribute("y1");
        AttributeVariable y1AttrVar = new AttributeVariable(y1Attr);
        Aggregator y1NopAgg = new NopAggregator(y1AttrVar);
        Assert.assertEquals(2, Metadata.getDistinctValues(y1Path, y1NopAgg)); //could fail with prob 1/(2^100)

        //Structural attribute, is constant (5) since all As have 5 Bs
        Path abPath = TestUtil.getStructurePath("a", "ab", "a,ab", "many");
        StructureVariable abStructVar = new StructureVariable(ab);
        Aggregator abCountAgg = new CountAggregator(new NopAggregator(abStructVar));
        Assert.assertEquals(1, Metadata.getDistinctValues(abPath, abCountAgg));

        //Mode of attribute across one-to-many relationship
        Path modeY1Path = TestUtil.getAttributePath("a", "b", "a,ab,b", "many", "y1");
        ModeAggregator y1ModeAgg = new ModeAggregator(y1NopAgg);
        Assert.assertEquals(2, Metadata.getDistinctValues(modeY1Path, y1ModeAgg)); //could fail with prob 1/(2^20)
    }
}

