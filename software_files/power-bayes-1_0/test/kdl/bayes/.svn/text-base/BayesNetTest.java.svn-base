/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes;

import cern.jet.math.Arithmetic;
import junit.framework.TestCase;
import kdl.bayes.util.Util;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.StringReader;

public class BayesNetTest extends TestCase {

    protected static Logger log = Logger.getLogger(PowerBayesNetTest.class);
    private double tolerance = 0.0000001;

    private String testInput1 =
            "@relation Grass\n" +
                    "@attribute Cloudy {false,true}\n" +
                    "@attribute Sprinkler {false,true}\n" +
                    "@attribute Rain {false,true}\n" +
                    "@attribute WetGrass {false,true}\n" +
                    "@data\n" +
                    "true,false,true,true\n" +
                    "false,true,false,true";

    private String testInput2 =
            "@relation TestBayesNet\n" +
                    "@attribute A {false,true}\n" +
                    "@attribute B {false,true}\n" +
                    "@attribute C {false,true}\n" +
                    "@data\n" +
                    "true,true,false\n" +
                    "true,true,false\n" +
                    "true,true,false\n" +
                    "true,true,false\n" +
                    "false,false,true\n" +
                    "false,false,true\n" +
                    "false,false,true\n" +
                    "false,false,true\n";

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testBDeu() throws Exception {
        Instances instances = new Instances(new StringReader(testInput1));
        instances.setClassIndex(0);
        BayesNet bn = new BayesNet(instances);
        double expected = Math.log(Math.pow(25, 3) * 30) - Math.log(Math.pow(110, 4));
        assertEquals(expected, bn.logBDeuScore(), tolerance);
    }

    public void testBDeu2() throws Exception {
        Instances instances = new Instances(new StringReader(testInput2));
        instances.setClassIndex(0);
        boolean[][] dag = new boolean[][]{
                {false, false, false},
                {false, false, true},
                {false, false, false}
        };
        BayesNet bn = new BayesNet(instances, dag);
        bn.setEquivalentSampleSize(4);  // math works out nicely with ess = 4

        // expected score is log [(3! 5! 5! / 11!)^2 * 1/25]
        double expectedScore = 0;
        expectedScore += Math.log(Arithmetic.factorial(3));
        expectedScore += Math.log(Arithmetic.factorial(5)) * 2;
        expectedScore -= Math.log(Arithmetic.factorial(11));
        expectedScore *= 2;
        expectedScore -= Math.log(25);
        assertEquals(expectedScore, bn.logBDeuScore(), tolerance);
    }

    public void testLogProbability() throws Exception {
        String trainInput =
                "@relation TestBayesNet\n" +
                        "@attribute A {T,F}\n" +
                        "@attribute B {T,F}\n" +
                        "@data\n" +

                        "T,T\n" +
                        "T,T\n" +
                        "T,T\n" +
                        "T,T\n" +

//                        "T,F\n" +

                        "F,T\n" +
                        "F,T\n" +

                        "F,F\n" +
                        "F,F\n" +
                        "F,F\n" +
                        "F,F\n" +

                        "F,F\n" +
                        "F,F\n" +
                        "F,F\n" +
                        "F,F\n";
        Instances instances = new Instances(new StringReader(trainInput));
        boolean[][] dag = new boolean[][]{
                {false, false},
                {true, false},
        };
        instances.setClassIndex(0);
        BayesNet bn = new BayesNet(instances, dag);
        bn.setEquivalentSampleSize(8);


        String testInput =
                "@relation TestBayesNet\n" +
                        "@attribute A {T,F}\n" +
                        "@attribute B {T,F}\n" +
                        "@data\n" +
                        "T,T\n" +
                        "T,F\n" +
                        "F,T\n" +
                        "F,F\n";
        Instances testInstances = new Instances(new StringReader(testInput));

        assertEquals(6.0 / 10 * 10 / 22, Math.exp(bn.logProbability(testInstances.instance(0))), tolerance);
        assertEquals(2.0 / 12 * 12 / 22, Math.exp(bn.logProbability(testInstances.instance(1))), tolerance);
        assertEquals(4.0 / 10 * 10 / 22, Math.exp(bn.logProbability(testInstances.instance(2))), tolerance);
        assertEquals(10.0 / 12 * 12 / 22, Math.exp(bn.logProbability(testInstances.instance(3))), tolerance);
    }

}
