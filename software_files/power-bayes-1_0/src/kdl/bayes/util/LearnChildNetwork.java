/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import kdl.bayes.PowerBayesNet;
import org.apache.log4j.Logger;
import weka.core.Instances;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

public class LearnChildNetwork {

    protected static Logger log = Logger.getLogger(LearnChildNetwork.class);


    public static void main(String[] args) throws Exception {
        Util.initLog4J();

        String trainfile = "/Users/afast/research/mmhc/data/child/child_params.arff";
        Instances trainInstances = new Instances(new FileReader(trainfile));

        PowerBayesNet bn = new PowerBayesNet(trainInstances);

        //Add the edges to the true network
        bn.addEdge(0, 1);
        bn.addEdge(1, 2);
        bn.addEdge(1, 3);
        bn.addEdge(1, 4);
        bn.addEdge(1, 5);
        bn.addEdge(1, 6);
        bn.addEdge(1, 7);
        bn.addEdge(1, 8);
        bn.addEdge(8, 2);
        bn.addEdge(3, 14);
        bn.addEdge(4, 9);
        bn.addEdge(5, 9);
        bn.addEdge(5, 10);
        bn.addEdge(6, 10);
        bn.addEdge(6, 11);
        bn.addEdge(6, 12);
        bn.addEdge(6, 13);
        bn.addEdge(7, 12);
        bn.addEdge(8, 13);
        bn.addEdge(9, 15);
        bn.addEdge(10, 15);
        bn.addEdge(10, 16);
        bn.addEdge(11, 17);
        bn.addEdge(12, 18);
        bn.addEdge(13, 19);

        //Then Estimate the parameters

        bn.estimateCPTs();

        //and save to file.

        String saveFilename = "/Users/afast/research/mmhc/data/child/child.xml";
        String xmlbif = bn.toXMLBIF03();

        log.info(xmlbif);

        PrintWriter pw = new PrintWriter(new File(saveFilename));
        pw.println(xmlbif);
        pw.close();

    }
}
