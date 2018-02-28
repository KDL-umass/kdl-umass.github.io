/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import junit.framework.TestCase;
import kdl.bayes.util.Assert;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;
import java.util.Map;

public class ZQueryIteratorTest extends TestCase {

    protected static Logger log = Logger.getLogger(ZQueryIteratorTest.class);

    private String testInput =
            "@relation TestData\n" +
                    "@attribute A {T,F}\n" +
                    "@attribute B {T,F}\n" +
                    "@attribute C1 {T,F}\n" +
                    "@attribute C2 {T,F}\n" +
                    "@data\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "T,F,T,T\n" +
                    "F,T,T,T\n" +
                    "T,T,F,F\n" +
                    "T,T,F,F\n" +
                    "T,T,F,F\n" +
                    "T,F,F,F\n";


    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testIterator() throws Exception {
        Instances instances = new Instances(new StringReader(testInput));
        int[] zIdxs = new int[]{2, 3};
        ZQueryIterator iter = new ZQueryIterator(zIdxs, instances);

        int count = 0;
        while (iter.hasNext()) {
            Map<Integer, Integer> query = iter.next();
            count++;
            log.info("Query:");
            for (Integer key : query.keySet()) {
                log.info("\t" + key + "," + query.get(key));
            }
        }

        Assert.condition(count == 4, "Should be 4 different queries.");

        String testData =
                "@relation TestData\n" +
                        "@attribute A {0,1}\n" +
                        "@attribute B {0,1}\n" +
                        "@attribute C {0,1,2}\n" +
                        "@data\n" +
                        "0,0,0\n" +
                        "0,0,0\n" +
                        "0,0,1\n" +
                        "0,0,2\n" +
                        "0,0,2\n" +
                        "0,0,2\n" +
                        "0,0,2\n" +
                        "0,1,0\n" +
                        "0,1,0\n" +
                        "0,1,0\n" +
                        "0,1,2\n" +
                        "1,0,0\n" +
                        "1,0,1\n" +
                        "1,0,2\n" +
                        "1,0,2\n" +
                        "1,1,2\n" +
                        "1,1,2\n" +
                        "1,1,2\n" +
                        "1,1,2\n";

        instances = new Instances(new StringReader(testData));
        zIdxs = new int[]{2};

        iter = new ZQueryIterator(zIdxs, instances);

        count = 0;
        while (iter.hasNext()) {
            Map<Integer, Integer> query = iter.next();
            count++;
            log.info("Query:");
            for (Integer key : query.keySet()) {
                log.info("\t" + key + "," + query.get(key));
            }
        }
        Assert.condition(count == instances.attribute(2).numValues(), "Should be 1 query for each value.");

    }

}
