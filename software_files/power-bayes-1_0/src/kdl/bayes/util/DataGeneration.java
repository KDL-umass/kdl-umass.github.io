/**
 * $Id: DataGeneration.java 278 2009-05-27 17:55:25Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import kdl.bayes.PowerBayesNet;
import org.apache.log4j.Logger;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * DataGeneration
 * NB: copied from Weka because Weka version has a bug and it catches
 * all exceptions so is uninformative when there is an error.
 * Author: mhay
 */
public class DataGeneration {

    protected static Logger log = Logger.getLogger(DataGeneration.class);
    private static String path = "/Users/afast/research/mmhc/data/";

    public static void main(String[] args) throws Exception {
        Util.initLog4J(); // init log4j
        String netName = args[0];
        int numInstances = Integer.parseInt(args[1]);
        int randomSeed = Integer.parseInt(args[2]);
        boolean isTrain = Boolean.parseBoolean(args[3]);
        boolean uniqueInstances = false;
        if (args.length >= 5) {
            uniqueInstances = Boolean.parseBoolean(args[4]);
        }
        if (args.length == 6) {
            path = args[5];
        }

        String prefix = path + netName + "/" + netName + ".";

        String xmlFile = prefix + "xml";

        int index = randomSeed;

        if (!isTrain && (netName.equals("synthetic") || netName.equals("binary"))) {
            randomSeed = 9999;
        }

        if (netName.equals("synthetic") || netName.equals("binary")) {
            xmlFile = prefix + index + ".xml";
        }

        // initialize m_Instances
        log.info("Reading bayes net from file " + xmlFile);
        PowerBayesNet bn = new PowerBayesNet(xmlFile);
        final int numNodes = bn.getNrOfNodes();
        FastVector attInfo = new FastVector(numNodes);
        for (int iNode = 0; iNode < numNodes; iNode++) {
            int nValues = bn.getCardinality(iNode);
            FastVector nomStrings = new FastVector(nValues + 1);
            for (int iValue = 0; iValue < nValues; iValue++) {
                nomStrings.addElement(bn.getNodeValue(iNode, iValue));
            }
            Attribute att = new Attribute(bn.getNodeName(iNode), nomStrings);
            attInfo.addElement(att);
        }
        Instances instances = new Instances(bn.getName(), attInfo, 100);
        instances.setClassIndex(numNodes - 1);
        bn.m_Instances = instances;

        log.info("Generating " + (uniqueInstances ? "unique " : "") + numInstances + " instances using random seed " + randomSeed);
        bn.generateInstances(numInstances, randomSeed);

        if (uniqueInstances) {
            log.info("Eliminating duplicates");
            bn.m_Instances = KLDiv.eliminateDuplicates(bn.m_Instances);
        }

        // $bn_name.test.unique.$size
        String outFilename = prefix + (isTrain ? "train." : (netName.equals("synthetic") ? "test." + index + "." : "test.")) + (uniqueInstances ? "unique." : "") + (isTrain ? randomSeed + "." : "") + numInstances;

        log.info("Writing " + bn.m_Instances.numInstances() + " instances to file " + outFilename);
        PrintWriter outfile = new PrintWriter(new FileWriter(outFilename));
        outfile.print(bn.m_Instances.toString());
        outfile.close();
    }
}
