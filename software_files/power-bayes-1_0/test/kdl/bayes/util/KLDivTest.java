/**
 * $Id: KLDivTest.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: KLDivTest.java 237 2008-04-07 16:54:03Z afast $
 */

package kdl.bayes.util;

import junit.framework.TestCase;
import weka.core.Instance;
import weka.core.Instances;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * KLDivTest
 * Author: mhay
 */
public class KLDivTest extends TestCase {

    private double tolerance = 0.00001;

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testZeroKL() throws IOException {
        String testInstancesStr2 =
                "@relation TestData\n" +
                        "@attribute A {0,1,2,3}\n" +
                        "@data\n" +
                        "0\n" +
                        "1\n" +
                        "2\n" +
                        "3\n";
        Instances instances = new Instances(new StringReader(testInstancesStr2));
        String pStr = "0:0.5\n1:0.25\n2:0.125\n3:0.125";
        String qStr = "0:0.25\n1:0.25\n2:0.25\n3:0.25";
        ProbDistribution p = new TestProbDistribution(pStr);
        assertEquals(0, KLDiv.klDivergence(p, p, instances), tolerance);
    }

    public void testKL() throws IOException {
        String testInstancesStr2 =
                "@relation TestData\n" +
                        "@attribute A {0,1,2,3}\n" +
                        "@data\n" +
                        "0\n" +
                        "1\n" +
                        "2\n" +
                        "3\n";
        Instances instances = new Instances(new StringReader(testInstancesStr2));
        String pStr = "0:0.5\n1:0.25\n2:0.125\n3:0.125";
        String qStr = "0:0.25\n1:0.25\n2:0.25\n3:0.25";
        ProbDistribution p = new TestProbDistribution(pStr);
        ProbDistribution q = new TestProbDistribution(qStr);
        assertEquals(.25, KLDiv.klDivergence(q, p, instances), tolerance);
    }

    public void testNegativeKL() throws IOException {
        String testInstancesStr2 =
                "@relation TestData\n" +
                        "@attribute A {0,1,2,3}\n" +
                        "@data\n" +
                        "0\n" +
                        "1\n" +
                        "2\n";  // last config not in test instances
        Instances instances = new Instances(new StringReader(testInstancesStr2));
        String pStr = "0:0.25\n1:0.25\n2:0.25\n3:0.25";
        String qStr = "0:0.125\n1:0.125\n2:0.125\n3:0.625";  // most of weight on 3
        ProbDistribution p = new TestProbDistribution(pStr);
        ProbDistribution q = new TestProbDistribution(qStr);
        assertEquals(-3.0 / 8, KLDiv.klDivergence(q, p, instances), tolerance);
    }

    class TestProbDistribution implements ProbDistribution {
        private Map<String, Double> instToProb;

        public TestProbDistribution(String distribStr) {
            instToProb = new HashMap<String, Double>();
            String[] lines = distribStr.split("\n");
            double total = 0.0;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String key = line.split(":")[0];
                double prob = Double.parseDouble(line.split(":")[1]);
                instToProb.put(key, prob);
                total += prob;
            }
            Assert.condition(StatUtil.equalDoubles(1, total), "Prob distribution does not sum to 1");
        }

        public double logProbability(Instance instance) {
            Assert.condition(instToProb.containsKey(instance.toString()),
                    "Do not have a probability for '" + instance.toString() +
                            "' in " + instToProb.keySet());
            return Math.log(instToProb.get(instance.toString()));
        }
    }

}
