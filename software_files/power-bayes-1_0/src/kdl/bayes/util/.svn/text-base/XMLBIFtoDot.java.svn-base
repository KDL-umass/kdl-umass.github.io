/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import org.apache.log4j.Logger;
import weka.core.FastVector;
import weka.gui.graphvisualizer.BIFParser;
import weka.gui.graphvisualizer.DotParser;

import java.io.File;
import java.io.FileInputStream;

public class XMLBIFtoDot {

    protected static Logger log = Logger.getLogger(XMLBIFtoDot.class);

    public static void main(String[] args) throws Exception {

        Util.initLog4J();

        String inputFilename = args[0] + ".xml";
        String outputFilename = args[0] + ".dot";

        File file = new File(inputFilename);

        FileInputStream ir = new FileInputStream(file);

        FastVector nodes = new FastVector();
        FastVector edges = new FastVector();

        BIFParser bif = new BIFParser(ir, nodes, edges);
        bif.parse();

        log.info(nodes.size());
        log.info(edges.size());

        DotParser.writeDOT(outputFilename, args[1], nodes, edges);

    }

}
