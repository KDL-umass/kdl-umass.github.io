/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.util;

import kdl.bayes.BayesNet;
import kdl.bayes.util.adtree.StatCache;
import kdl.bayes.util.adtree.StatTable;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BayesNetFactory {

    protected static Logger log = Logger.getLogger(BayesNetFactory.class);

    public BayesNetFactory() {

    }

    public BayesNet readFromXML(String xmlFilename) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(xmlFilename);

        Element bif = document.getRootElement();
        Element network = bif.element("NETWORK");

        //Get all variable names and values
        List<String> varNames = new ArrayList<String>();
        List<List<String>> varValues = new ArrayList<List<String>>();

        Iterator iter = network.elementIterator("VARIABLE");

        while (iter.hasNext()) {
            Element var = (Element) iter.next();
            //log.info(var.element("NAME").getText());
            varNames.add(var.element("NAME").getText());

            Iterator valIter = var.elementIterator("OUTCOME");
            List<String> values = new ArrayList<String>();
            while (valIter.hasNext()) {
                Element value = (Element) valIter.next();
                String s = value.getText();
                //log.info("\t" + s);
                values.add(s);
            }
            varValues.add(values);
        }

        int numNodes = varNames.size();

        //Initialize the instances for creating a BayesNet
        FastVector attInfo = new FastVector(numNodes);
        for (int iNode = 0; iNode < numNodes; iNode++) {
            List<String> values = varValues.get(iNode);
            int nValues = values.size();
            FastVector nomStrings = new FastVector(nValues + 1);
            for (int iValue = 0; iValue < nValues; iValue++) {
                nomStrings.addElement(values.get(iValue));
            }
            Attribute att = new Attribute(varNames.get(iNode), nomStrings);
            attInfo.addElement(att);
        }
        Instances instances = new Instances(network.element("NAME").getText(), attInfo, 100);

        //Initial the structure
        boolean[][] dag = new boolean[numNodes][numNodes];

        //Now get all probability distributions
        List<List<Integer>> parentSets = new ArrayList<List<Integer>>();


        List<String> probValues = new ArrayList<String>(); //Will parse the strings later.

        Iterator distIter = network.elementIterator("DEFINITION");

        while (distIter.hasNext()) {
            Element def = (Element) distIter.next();
            String varName = def.element("FOR").getText();
            //log.info(varName);
            int varIdx = varNames.indexOf(varName);

            List<Integer> parents = new ArrayList<Integer>();
            Iterator parentIter = def.elementIterator("GIVEN");
            while (parentIter.hasNext()) {
                Element parent = (Element) parentIter.next();
                int parentIdx = varNames.indexOf(parent.getText());
                parents.add(parentIdx);
                dag[parentIdx][varIdx] = true;
            }
            parentSets.add(parents);
            probValues.add(def.element("TABLE").getText());
        }

        //Now for the hard part, create an ADTree from the prob. distributions.
        //Counts at every point in the tree should return normalized marginal probabilities
        //Probably a better way than ADTrees to store this efficiently.

        StatCache cache = new StatTable(instances, parentSets, probValues);

        return new BayesNet(instances, cache, dag);
    }

}
