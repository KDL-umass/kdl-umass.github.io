/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import junit.framework.TestCase;
import kdl.bayes.BayesNet;
import kdl.bayes.PowerBayesNet;
import kdl.bayes.util.adtree.StatCache;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class BayesNetFactoryTest extends TestCase {

    protected static Logger log = Logger.getLogger(BayesNetFactoryTest.class);

    protected void setUp() throws Exception {
        super.setUp();
        Util.initLog4J();
    }

    public void testReadFromXML() throws Exception {
        BayesNet bn = new BayesNetFactory().readFromXML("/Users/afast/research/mmhc/data/earthquake/earthquake.xml");

        StatCache cache = bn.getStatCache();

        Map<Integer, Integer> query = new HashMap<Integer, Integer>();
        query.put(0, 0);
        double prob = cache.getCount(query, 0);
        Assert.condition(Double.compare(0.99, prob) == 0, "Indexed first prob of parentless node.");

        query.clear();
        query.put(0, 1);
        query.put(1, 0);
        prob = cache.getCount(query, 2);
        Assert.condition(Double.compare(1, prob) == 0, "Get marginal count");
        query.put(2, 1);
        prob = cache.getCount(query, 2);
        Assert.condition(Double.compare(0.94, prob) == 0, "Get actual prob");
    }

    public void testCompareImplementations() throws Exception {
        String netName = "alarm";
        String prefix = "/Users/afast/research/mmhc/data/" + netName + "/" + netName;
        String biffile = prefix + ".xml";
        BayesNet bn = new BayesNetFactory().readFromXML(biffile);
        PowerBayesNet pbn = new PowerBayesNet(biffile);

        String instancesFile = prefix + ".train.1.500";
        Instances testInstances = new Instances(new FileReader(instancesFile));

        double pbnLL = pbn.logProbability(testInstances);
        double bnLL = bn.logProbability(testInstances);

        log.info("PBN: " + pbnLL);
        log.info("BN:  " + bnLL);

        Assert.condition(StatUtil.equalDoubles(pbnLL, bnLL, 0.001), "Loglikelihoods should be equal if implemented correctly.");


    }


}
