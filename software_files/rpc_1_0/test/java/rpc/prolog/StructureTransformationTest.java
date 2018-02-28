/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Aug 3, 2009
 * Time: 1:53:58 PM
 */
package rpc.prolog;

import junit.framework.TestCase;
import rpc.TestUtil;
import rpc.util.PrologUtil;
import rpc.util.LogUtil;

import jpl.Query;
import jpl.Term;

import java.util.*;

import org.apache.log4j.Logger;

public class StructureTransformationTest extends TestCase {

    private static Logger log = Logger.getLogger(StructureTransformationTest.class);

    public static final String SCHEMA_FILE = "./test/test-schema.pl";
    public static final int HOP_THRESHOLD = 7;

    /****************************************TEST SCHEMA************************************************

          ______        / \        ______        / \       ______      / \       ______
         | D    |      /DA \      | A    |      /AB \     | B    |    /BC \     | C    |
         | (w1) |-----/(wx1)\-----| (x1) |-----/(xy1)\---<| (y1) |>--/(yz1)\---<| (z1) |
         |      |     \     /     | (x2) |     \(xy2)/    |      |   \     /    | (z2) |
         ------       \   /      |      |      \   /      ------     \   /     | (z3) |
                       \ /        -----         \ /                   \ /      | (z4) |
                                                                                ------
     ***************************************************************************************************/

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
        TestUtil.loadRPCOncePerTest();
        TestUtil.loadSchemaOncePerTest(SCHEMA_FILE);
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSingletonPathForEntity() {
        Query q = new Query("singletonPath(BaseEntity)");

		Hashtable[] solutions = q.allSolutions();
        List<String> results = new ArrayList<String>();
        for (Hashtable solution : solutions) {
			results.add(solution.get("BaseEntity").toString());
        }
        q.close();

        List<String> expected = new ArrayList<String>();
        //should have just 4
        expected.add("a");
        expected.add("b");
        expected.add("c");
        expected.add("d");
        TestUtil.verifyStringList("Testing singleton entity paths", expected, results);
    }
    
    public void testSingletonPathForRelationship() {
        Query q = new Query("singletonPathRelationship(BaseRelationship)");

		Hashtable[] solutions = q.allSolutions();
        Set<String> results = new HashSet<String>();
        for (Hashtable solution : solutions) {
			results.add(solution.get("BaseRelationship").toString());
        }
        q.close();

        Set<String> expected = new HashSet<String>();
        //should have just 3
        expected.add("ab");
        expected.add("bc");
        expected.add("da");
        TestUtil.verifySets("Testing singleton relationship paths", expected, results);
    }

    public void testEntityPathForEntity() {
        Query q = new Query(String.format("entityPath(BaseEntity, TargetEntity, Card, Path, %s)", HOP_THRESHOLD));

		Hashtable[] solutions = q.allSolutions();
        Set<String> results = new HashSet<String>();
        for (Hashtable solution : solutions) {
            String baseEntStr    = solution.get("BaseEntity").toString();
            String targetEntStr  = solution.get("TargetEntity").toString();
            String cardStr       = solution.get("Card").toString();
            String pathStr       = PrologUtil.termToList((Term) solution.get("Path")).toString();
            results.add(baseEntStr + ", " + targetEntStr + ", " + cardStr + ", " + pathStr);
        }
        q.close();

        Set<String> expected = new HashSet<String>();
        //Should have 21 paths (limit on hops is hard-coded in prolog to 7 hops)
        expected.add("a, b, many, [a, ab, b]");
        expected.add("a, b, many, [a, ab, b, bc, c, bc, b]");
        expected.add("a, c, many, [a, ab, b, bc, c]");
        expected.add("a, d, one, [a, da, d]");

        expected.add("b, a, one, [b, ab, a]");
        expected.add("b, a, many, [b, bc, c, bc, b, ab, a]");
        expected.add("b, b, many, [b, ab, a, ab, b]");
        expected.add("b, b, many, [b, bc, c, bc, b]");
        expected.add("b, c, many, [b, bc, c]");
        expected.add("b, c, many, [b, ab, a, ab, b, bc, c]");
        expected.add("b, c, many, [b, bc, c, bc, b, bc, c]");
        expected.add("b, d, one, [b, ab, a, da, d]");

        expected.add("c, a, many, [c, bc, b, ab, a]");
        expected.add("c, b, many, [c, bc, b]");
        expected.add("c, b, many, [c, bc, b, ab, a, ab, b]");
        expected.add("c, b, many, [c, bc, b, bc, c, bc, b]");
        expected.add("c, c, many, [c, bc, b, bc, c]");
        expected.add("c, d, many, [c, bc, b, ab, a, da, d]");

        expected.add("d, a, one, [d, da, a]");
        expected.add("d, b, many, [d, da, a, ab, b]");
        expected.add("d, c, many, [d, da, a, ab, b, bc, c]");

        log.debug("Found the following paths...");
        for (String el : results ) {
            log.debug(el);
        }
        TestUtil.verifySets("Testing entity path with a base entity", expected, results);
    }

    public void testEntityPathForRelationship() {
        Query q = new Query(String.format("entityPathRelationship(BaseRelationship, TargetEntity, Card, Path, %s)",
                HOP_THRESHOLD));

		Hashtable[] solutions = q.allSolutions();
        Set<String> results = new HashSet<String>();
        for (Hashtable solution : solutions) {
            String baseRelStr   = solution.get("BaseRelationship").toString();
            String targetEntStr = solution.get("TargetEntity").toString();
            String cardStr      = solution.get("Card").toString();
            String pathStr      = PrologUtil.termToList((Term) solution.get("Path")).toString();
            results.add(baseRelStr + ", " + targetEntStr + ", " + cardStr + ", " + pathStr);
        }
        q.close();

        Set<String> expected = new HashSet<String>();
        //Should have 31 paths (limit on hops is hard-coded in prolog to 7 hops)
        expected.add("ab, a, one, [ab, a]");
        expected.add("ab, a, many, [ab, b, bc, c, bc, b, ab, a]");
        expected.add("ab, b, one, [ab, b]");
        expected.add("ab, b, many, [ab, a, ab, b]");
        expected.add("ab, b, many, [ab, b, bc, c, bc, b]");
        expected.add("ab, b, many, [ab, a, ab, b, bc, c, bc, b]");
        expected.add("ab, c, many, [ab, b, bc, c]");
        expected.add("ab, c, many, [ab, b, bc, c, bc, b, bc, c]");
        expected.add("ab, c, many, [ab, a, ab, b, bc, c]");
        expected.add("ab, d, one, [ab, a, da, d]");

        expected.add("bc, a, one, [bc, b, ab, a]");
        expected.add("bc, a, many, [bc, c, bc, b, ab, a]");
        expected.add("bc, a, many, [bc, b, bc, c, bc, b, ab, a]");
        expected.add("bc, b, one, [bc, b]");
        expected.add("bc, b, many, [bc, c, bc, b]");
        expected.add("bc, b, many, [bc, b, bc, c, bc, b]");
        expected.add("bc, b, many, [bc, b, ab, a, ab, b]");
        expected.add("bc, b, many, [bc, c, bc, b, bc, c, bc, b]");
        expected.add("bc, b, many, [bc, c, bc, b, ab, a, ab, b]");
        expected.add("bc, c, one, [bc, c]");
        expected.add("bc, c, many, [bc, b, bc, c]");
        expected.add("bc, c, many, [bc, c, bc, b, bc, c]");
        expected.add("bc, c, many, [bc, b, ab, a, ab, b, bc, c]");
        expected.add("bc, c, many, [bc, b, bc, c, bc, b, bc, c]");
        expected.add("bc, d, one, [bc, b, ab, a, da, d]");
        expected.add("bc, d, many, [bc, c, bc, b, ab, a, da, d]");

        expected.add("da, a, one, [da, a]");
        expected.add("da, b, many, [da, a, ab, b]");
        expected.add("da, b, many, [da, a, ab, b, bc, c, bc, b]");
        expected.add("da, c, many, [da, a, ab, b, bc, c]");
        expected.add("da, d, one, [da, d]");

        log.debug("Found the following paths...");
        for (String el : results ) {
            log.debug(el);
        }
        TestUtil.verifySets("Testing entity path with a base relationship", expected, results);
    }

    public void testRelationshipPathForEntity() {
        Query q = new Query(String.format("relationshipPath(BaseEntity, TargetRelationship, Card, Path, %s)",
                HOP_THRESHOLD));

		Hashtable[] solutions = q.allSolutions();
        Set<String> results = new HashSet<String>();
        for (Hashtable solution : solutions) {
            String baseEntStr   = solution.get("BaseEntity").toString();
            String targetRelStr = solution.get("TargetRelationship").toString();
            String cardStr      = solution.get("Card").toString();
            String pathStr      = PrologUtil.termToList((Term) solution.get("Path")).toString();
            results.add(baseEntStr + ", " + targetRelStr + ", " + cardStr + ", " + pathStr);
        }
        q.close();

        Set<String> expected = new HashSet<String>();
        //Should have 31 paths (limit on hops is hard-coded in prolog to 7  hops)
        expected.add("a, ab, many, [a, ab]");
        expected.add("a, ab, many, [a, ab, b, bc, c, bc, b, ab]");
        expected.add("a, bc, many, [a, ab, b, bc, c, bc]");
        expected.add("a, bc, many, [a, ab, b, bc]");
        expected.add("a, bc, many, [a, ab, b, bc, c, bc, b, bc]");
        expected.add("a, da, one, [a, da]");

        expected.add("b, ab, one, [b, ab]");
        expected.add("b, ab, many, [b, bc, c, bc, b, ab]");
        expected.add("b, ab, many, [b, ab, a, ab]");
        expected.add("b, ab, many, [b, bc, c, bc, b, ab, a, ab]");
        expected.add("b, bc, many, [b, bc, c, bc]");
        expected.add("b, bc, many, [b, bc]");
        expected.add("b, bc, many, [b, ab, a, ab, b, bc]");
        expected.add("b, bc, many, [b, bc, c, bc, b, bc]");
        expected.add("b, bc, many, [b, bc, c, bc, b, bc, c, bc]");
        expected.add("b, bc, many, [b, ab, a, ab, b, bc, c, bc]");
        expected.add("b, da, one, [b, ab, a, da]");
        expected.add("b, da, many, [b, bc, c, bc, b, ab, a, da]");

        expected.add("c, ab, many, [c, bc, b, ab]");
        expected.add("c, ab, many, [c, bc, b, bc, c, bc, b, ab]");
        expected.add("c, ab, many, [c, bc, b, ab, a, ab]");
        expected.add("c, bc, many, [c, bc]");
        expected.add("c, bc, many, [c, bc, b, bc, c, bc]");
        expected.add("c, bc, many, [c, bc, b, bc]");
        expected.add("c, bc, many, [c, bc, b, bc, c, bc, b, bc]");
        expected.add("c, bc, many, [c, bc, b, ab, a, ab, b, bc]");
        expected.add("c, da, many, [c, bc, b, ab, a, da]");

        expected.add("d, da, one, [d, da]");
        expected.add("d, ab, many, [d, da, a, ab]");
        expected.add("d, bc, many, [d, da, a, ab, b, bc]");
        expected.add("d, bc, many, [d, da, a, ab, b, bc, c, bc]");

        log.debug("Found the following paths...");
        for (String el : results ) {
            log.debug(el);
        }
        TestUtil.verifySets("Testing relationship path with base entity", expected, results);
    }

    public void testRelationshipPathForRelationship() {
        Query q = new Query(String.format(
                "relationshipPathRelationship(BaseRelationship, TargetRelationship, Card, Path, %s)", HOP_THRESHOLD));

		Hashtable[] solutions = q.allSolutions();
        Set<String> results = new HashSet<String>();
        for (Hashtable solution : solutions) {
            String baseRelStr   = solution.get("BaseRelationship").toString();
            String targetRelStr = solution.get("TargetRelationship").toString();
            String cardStr      = solution.get("Card").toString();
            String pathStr      = PrologUtil.termToList((Term) solution.get("Path")).toString();
            results.add(baseRelStr + ", " + targetRelStr + ", " + cardStr + ", " + pathStr);
        }
        q.close();

        Set<String> expected = new HashSet<String>();
        //Should have 25 paths (limit on hops is hard-coded in prolog to 7 hops)
        expected.add("ab, ab, many, [ab, b, bc, c, bc, b, ab]");
        expected.add("ab, ab, many, [ab, a, ab]");
        expected.add("ab, bc, many, [ab, b, bc, c, bc]");
        expected.add("ab, bc, many, [ab, a, ab, b, bc, c, bc]");
        expected.add("ab, bc, many, [ab, b, bc]");
        expected.add("ab, bc, many, [ab, b, bc, c, bc, b, bc]");
        expected.add("ab, bc, many, [ab, a, ab, b, bc]");
        expected.add("ab, da, one, [ab, a, da]");

        expected.add("bc, ab, one, [bc, b, ab]");
        expected.add("bc, ab, many, [bc, c, bc, b, ab]");
        expected.add("bc, ab, many, [bc, b, bc, c, bc, b, ab]");
        expected.add("bc, bc, many, [bc, c, bc]");
        expected.add("bc, bc, many, [bc, b, bc, c, bc]");
        expected.add("bc, ab, many, [bc, b, ab, a, ab]");
        expected.add("bc, bc, many, [bc, c, bc, b, bc, c, bc]");
        expected.add("bc, ab, many, [bc, c, bc, b, ab, a, ab]");
        expected.add("bc, bc, many, [bc, b, bc]");
        expected.add("bc, bc, many, [bc, c, bc, b, bc]");
        expected.add("bc, bc, many, [bc, b, ab, a, ab, b, bc]");
        expected.add("bc, bc, many, [bc, b, bc, c, bc, b, bc]");
        expected.add("bc, da, one, [bc, b, ab, a, da]");
        expected.add("bc, da, many, [bc, c, bc, b, ab, a, da]");

        expected.add("da, ab, many, [da, a, ab]");
        expected.add("da, bc, many, [da, a, ab, b, bc, c, bc]");
        expected.add("da, bc, many, [da, a, ab, b, bc]");

        log.debug("Found the following paths...");
        for (String el : results ) {
            log.debug(el);
        }
        TestUtil.verifySets("Testing relationship path with base relationship", expected, results);
    }
}