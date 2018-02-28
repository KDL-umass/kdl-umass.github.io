/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Aug 18, 2009
 * Time: 12:39:12 PM
 */
package rpc.datagen;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import rpc.TestUtil;
import rpc.design.Unit;
import rpc.design.Path;

import java.util.*;

public class DataGeneratorTest extends TestCase {

    public static final String SCHEMA_FILE = "./test/test-schema.pl";
    public Random random = new Random();

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMonetDB() {
        DataGenerator dg = new DataGenerator("testdb");
        int numARows = 10 + random.nextInt(10);
        int numBRows = 2 * numARows;
        int numCRows = numBRows/2;

        dg.populateEntityTable("a", numARows);
        dg.populateEntityTable("b", numBRows);
        dg.populateEntityTable("c", numCRows);
        dg.populateEntityTable("d", numARows);

        dg.close();
    }


    public void testInitDB() {

        // initialize the datagenerator
        DataGenerator dg = new DataGenerator("testdb");

        // create basic relations in database
        int numARows = 10 + random.nextInt(10);
        int numBRows = 2 * numARows;
        int numCRows = numBRows/2;

        dg.populateEntityTable("a", numARows);
        dg.populateEntityTable("b", numBRows);
        dg.populateEntityTable("c", numCRows);
        dg.populateEntityTable("d", numARows);

        String tempString = "SELECT b_id from B";

        //make sure "one" entity goes first, depends on cardinality, overlapping params
        dg.populateRelationshipTable("ab", new ConstantConnector(tempString, 2, true), null);

        //since one-to-one, lambda function MUST be constant of 1
        tempString = "SELECT a_id from A";
        dg.populateRelationshipTable("da", new ConstantConnector(tempString, 1, true), null);

        // generate b.y1 as a coin-flip
        HashMap<Integer, Double> coinFlip = new HashMap<Integer, Double>();
        coinFlip.put(0, 0.5);
        coinFlip.put(1, 0.5);
        CPTPrior cpt1 = new CPTPrior(coinFlip);
        dg.generateAttribute("b", "y1", cpt1);

        HashMap<List<Object>, CPTPrior> parentsToCPTs = new HashMap<List<Object>, CPTPrior>();

        // if b.y1 == 0, then it connects to c's according to:
        HashMap<Integer, Double> table = new HashMap<Integer, Double>();
        table.put(3, 0.6);
        table.put(5, 0.2);
        table.put(8, 0.2);
        parentsToCPTs.put(Arrays.asList((Object) 0), new CPTPrior(table));

        // if b.y1 == 1, then it connects to c's according to:
        table = new HashMap<Integer, Double>();
        table.put(3, 0.1);
        table.put(5, 0.2);
        table.put(8, 0.7);
        parentsToCPTs.put(Arrays.asList((Object) 1), new CPTPrior(table));

        CPTParent cpt2 = new CPTParent(parentsToCPTs);

        List<String> parentQueries = new ArrayList<String>();
        parentQueries.add("SELECT b_id, y1 from b");
        tempString = "SELECT c_id from c";
        dg.populateRelationshipTable("bc", new CPTConnector(tempString, cpt2, true), parentQueries);

        dg.generateAttribute("d", "w1", cpt1);
        dg.generateAttribute("ab", "xy1", cpt1);
        dg.generateAttribute("da", "wx1", cpt1);
        
        // generate remaining attributes respecting variable ordering of directed acyclic dependency graph
        // single item tuples MUST have trailing comma in order to be unique
        parentsToCPTs = new HashMap<List<Object>, CPTPrior>();
        table = new HashMap<Integer, Double>();
        table.put(0, 0.8);
        table.put(1, 0.2);
        parentsToCPTs.put(Arrays.asList((Object) 0), new CPTPrior(table));

        table = new HashMap<Integer, Double>();
        table.put(0, 0.1);
        table.put(1, 0.9);
        parentsToCPTs.put(Arrays.asList((Object) 1), new CPTPrior(table));

        CPTParent cpt3 = new CPTParent(parentsToCPTs);

        List<Unit> parentUnits = new ArrayList<Unit>();
        Path tPath = TestUtil.getAttributePath("a", "b", "a, ab, b", "many", "y1");
        Path oPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        parentUnits.add(TestUtil.getUnit("a", tPath, oPath));

        dg.generateAttributeWithParents("a", "x1", cpt3, parentUnits);

        dg.close();
    }

    public void testSmallDB() {

        // initialize the datagenerator
        DataGenerator dg = new DataGenerator("testdbsmall");

        // create basic relations in database
        int numARows = 50 + random.nextInt(10);
        int numBRows = 2 * numARows;
        int numCRows = numBRows/2;

        dg.populateEntityTable("a", numARows);
        dg.populateEntityTable("b", numBRows);
        dg.populateEntityTable("c", numCRows);
        dg.populateEntityTable("d", numARows);

        String tempString = "SELECT b_id from B";

        //make sure "one" entity goes first, depends on cardinality, overlapping params
        dg.populateRelationshipTable("ab", new ConstantConnector(tempString, 2, true), null);

        //since one-to-one, lambda function MUST be constant of 1
        tempString = "SELECT a_id from A";
        dg.populateRelationshipTable("da", new ConstantConnector(tempString, 1, true), null);

        // generate b.y1 as a coin-flip
        HashMap<Integer, Double> coinFlip = new HashMap<Integer, Double>();
        coinFlip.put(0, 0.5);
        coinFlip.put(1, 0.5);
        CPTPrior cpt1 = new CPTPrior(coinFlip);
        dg.generateAttribute("b", "y1", cpt1);

        HashMap<Integer, Double> weightedCoinFlip = new HashMap<Integer, Double>();
        weightedCoinFlip.put(0, 0.7);
        weightedCoinFlip.put(1, 0.3);
        CPTPrior cpt2 = new CPTPrior(weightedCoinFlip);
        dg.generateAttribute("c", "z1", cpt2);

        HashMap<List<Object>, CPTPrior> parentsToCPTs = new HashMap<List<Object>, CPTPrior>();

        // if b.y1 == 0, then it connects to c's according to:
        HashMap<Integer, Double> table = new HashMap<Integer, Double>();
        table.put(2, 0.6);
        table.put(5, 0.2);
        table.put(7, 0.2);
        parentsToCPTs.put(Arrays.asList((Object) 0), new CPTPrior(table));

        // if b.y1 == 1, then it connects to c's according to:
        table = new HashMap<Integer, Double>();
        table.put(2, 0.1);
        table.put(5, 0.2);
        table.put(7, 0.7);
        parentsToCPTs.put(Arrays.asList((Object) 1), new CPTPrior(table));

        CPTParent cpt3 = new CPTParent(parentsToCPTs);

        List<String> parentQueries = new ArrayList<String>();
        parentQueries.add("SELECT b_id, y1 from b");
        tempString = "SELECT c_id from c";
        dg.populateRelationshipTable("bc", new CPTConnector(tempString, cpt3, true), parentQueries);

        dg.generateAttribute("d", "w1", cpt1);
        dg.generateAttribute("ab", "xy1", cpt1);
        dg.generateAttribute("da", "wx1", cpt1);
        dg.generateAttribute("bc", "yz1", cpt1);

        // generate remaining attributes respecting variable ordering of directed acyclic dependency graph
        // single item tuples MUST have trailing comma in order to be unique

        // a.x
        parentsToCPTs = new HashMap<List<Object>, CPTPrior>();
        table = new HashMap<Integer, Double>();
        table.put(0, 0.8);
        table.put(1, 0.2);
        parentsToCPTs.put(Arrays.asList((Object) 0), new CPTPrior(table));

        table = new HashMap<Integer, Double>();
        table.put(0, 0.1);
        table.put(1, 0.9);
        parentsToCPTs.put(Arrays.asList((Object) 1), new CPTPrior(table));

        table = new HashMap<Integer, Double>();
        table.put(0, 0.5);
        table.put(1, 0.5);
        parentsToCPTs.put(Arrays.asList((Object) 2), new CPTPrior(table));

        CPTParent cpt4 = new CPTParent(parentsToCPTs);

        List<Unit> parentUnits = new ArrayList<Unit>();
        Path tPath = TestUtil.getAttributePath("a", "b", "a, ab, b", "many", "y1");
        Path oPath = TestUtil.getAttributePath("a", "a", "a", "one", "x1");
        parentUnits.add(TestUtil.getUnit("a", tPath, oPath));

        dg.generateAttributeWithParents("a", "x1", cpt4, parentUnits);

        dg.close();
    }
}
