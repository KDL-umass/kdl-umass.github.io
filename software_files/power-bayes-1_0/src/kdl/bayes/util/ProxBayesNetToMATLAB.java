/**
 * $Id: ProxBayesNetToMATLAB.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import kdl.bayes.PowerBayesNet;
import org.apache.log4j.Logger;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: afast
 * Date: Feb 26, 2007
 * Time: 4:35:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProxBayesNetToMATLAB {

    protected static Logger log = Logger.getLogger(ProxBayesNetToMATLAB.class);
    private static String path = "/nfs/aeolus/kdl/mhay/mmhc_local/data/";

    public void convertData(String netName, int trainIndex, int sampleSize, String folderName) throws IOException {

        log.info("Starting: netName=" + netName +
                " trainIndex=" + trainIndex +
                " sampleSize=" + sampleSize
        );

        String prefix = path + netName + ".";

        // load training data (e.g. insurance/insurance.train.1.500 )
        String trainfile = prefix +
                "train." + trainIndex + "." + sampleSize;
        Instances trainInstances = new Instances(new FileReader(trainfile));

        String outFilename = folderName + netName + "_s" + sampleSize + "_v" + trainIndex + ".txt";
        PrintWriter outfile = new PrintWriter(new FileWriter(outFilename));

        for (int i = 0; i < trainInstances.numInstances(); i++) {
            Instance currInst = trainInstances.instance(i);
            for (int j = 0; j < currInst.numValues(); j++) {
                double value = currInst.value(j);
                int index = (new Double(Math.rint(value))).intValue() + 1;

                outfile.print(index);

                if (i < trainInstances.numInstances() - 1) {
                    outfile.print("\t");
                }
            }
            outfile.println();
        }
        outfile.close();
    }

    public static void main(String[] args) throws IOException {
        Util.initLog4J(); // init log4j

        ProxBayesNetToMATLAB pbnToML = new ProxBayesNetToMATLAB();

        String[] netNames = {"alarm", "barley", "grass", "hailfinder", "insurance", "mildew", "munin", "pigs"};
        int[] sampleSizes = {500, 1000, 2000, 5000};


        for (int i = 0; i < netNames.length; i++) {
            String netName = netNames[i];
            // load true gold standard Bayes net (for computing SHD)
            String xmlFile = path + netName + "/" + netName + ".xml";
            PowerBayesNet trueBn = new PowerBayesNet(xmlFile);

            //Create a new folder
            String folderName = path + netName + "_data/";
            File newDir = new File(folderName);
            if (!newDir.isDirectory()) {
                log.warn("New File is not a directory");
            }
            //Write true Bayes net as netName_graph.txt
            boolean[][] trueDag = trueBn.getDag();
            String trueDagName = folderName + netName + "_graph.txt";
            pbnToML.writeDagToFile(trueDag, trueDagName);
            //write data Samples as netName_s500_v1.txt
            for (int j = 0; j < sampleSizes.length; j++) {
                int sampleSize = sampleSizes[j];
                for (int k = 1; k <= 10; k++) {
                    pbnToML.convertData(netName, k, sampleSize, folderName);
                }
            }
        }
    }

    private void writeDagToFile(boolean[][] trueDag, String trueDagName) throws IOException {
        PrintWriter outfile = new PrintWriter(new FileWriter(trueDagName));
        for (int i = 0; i < trueDag.length; i++) {
            for (int j = 0; j < trueDag[0].length; j++) {
                outfile.print(trueDag[i][j]);
                if (j < trueDag[0].length - 1) {
                    outfile.print("\t");
                }
            }
            outfile.println();
        }
        outfile.close();
    }

    private static void printUsage() {
        log.info("MainExpt expects: net_name train_index sample_size alg_type");
    }


}
