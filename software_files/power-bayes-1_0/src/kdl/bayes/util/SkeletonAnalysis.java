/**
 * $Id: SkeletonAnalysis.java 237 2008-04-07 16:54:03Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.util;

import kdl.bayes.PowerBayesNet;
import kdl.bayes.experiments.ExptUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SkeletonAnalysis {
    protected static Logger log = Logger.getLogger(SkeletonAnalysis.class);
    private static String path = "/nfs/aeolus/kdl/afast/research/mmhc/data/";

    private static final String MMPC_COLOR = "blue";
    private static final String GS_COLOR = "yellow";
    private static final String ALL_COLOR = "green";
    private static final String TN_COLOR = "black";

    private static final String SOLID_STYLE = "solid";
    private static final String DASHED_STYLE = "\"solid,bold\", dir=none";
    private static final String BOLD_STYLE = "bold";

    static int trainIndex;
    static int sampleSize;
    static String netName;


    public static void main(String[] args) throws Exception {
        Util.initLog4J(); // init log4j

        netName = args[0];
        trainIndex = Integer.parseInt(args[1]);
        sampleSize = Integer.parseInt(args[2]);

        String prefix = path + netName + "/" + netName + ".";

        // load true gold standard Bayes net (for computing SHD)
        String xmlFile = prefix + "xml";
        PowerBayesNet trueBn = new PowerBayesNet(xmlFile);

        String gsFileName = prefix + "bn.gs." + trainIndex + "." + sampleSize + ".xml";
        //PowerBayesNet gsBn = new PowerBayesNet(gsFileName);

        //gsBn.

        //Map<String, Set<String>> cpc = ExptUtils.loadCPC(prefix, "mmpc", trainIndex, sampleSize, -1.0);

        String dotFilename = prefix + "viz." + trainIndex + "." + sampleSize + ".dot";

        //String nodeDataFile = prefix + "node_data.txt";
        //writeNodeCharacteristicsFromTrue(nodeDataFile, xmlFile);

        //writeOverlapToDot(dotFilename, trueBn, gsBn, gsBn); //todo make sure to change this
        writeSkeletonsToDot(prefix, sampleSize, trainIndex);

    }

    private static int getCountFromList(List<Map<String, Set<String>>> list, String from, String to) {
        int count = 0;
        for (Iterator<Map<String, Set<String>>> iterator = list.iterator(); iterator.hasNext();) {
            Map<String, Set<String>> map = iterator.next();
            Set<String> neighbors = map.get(from);
            if (neighbors.contains(to)) {
                count++;
            }
        }
        return count;
    }

    public static Map writeSkeletonsToDot(String prefix, int sampleSize, int trainIndex) {

        String filename = prefix + "viz.skeletons." + sampleSize + (trainIndex > 0 ? "." + trainIndex : "") + ".dot";

        String xmlFile = prefix + "xml";
        PowerBayesNet trueBn = new PowerBayesNet(xmlFile);

        List<Map<String, Set<String>>> mmpcList = new ArrayList<Map<String, Set<String>>>();
        List<Map<String, Set<String>>> gsList = new ArrayList<Map<String, Set<String>>>();
        if (trainIndex > 0) {
            Map<String, Set<String>> mmpc = ExptUtils.loadCPC(prefix, "mmpc", trainIndex, sampleSize, -1.0);
            Map<String, Set<String>> gs = ExptUtils.loadCPC(prefix, "power", trainIndex, sampleSize, 0.85);

            mmpcList.add(mmpc);
            gsList.add(gs);
        } else {
            for (int i = 1; i <= 5; i++) {
                Map<String, Set<String>> mmpc = ExptUtils.loadCPC(prefix, "mmpc", i, sampleSize, -1.0);
                Map<String, Set<String>> gs = ExptUtils.loadCPC(prefix, "power", i, sampleSize, 0.85);

                mmpcList.add(mmpc);
                gsList.add(gs);

            }
        }
        int tAll = 0;
        int tMmpcTrue = 0;
        int tGsTrue = 0;
        int tMmpc = 0;
        int tGs = 0;
        int tGsMmpc = 0;
        int tTrue = 0;

        Map<String, Integer[]> nMap = new HashMap();

        for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
            String nodeName = trueBn.getNodeName(i);
            Integer[] initialCounts = new Integer[]{0, 0, 0, 0, 0, 0, 0};
            nMap.put(nodeName, initialCounts);
        }


        try {
            PrintWriter outfile = new PrintWriter(new FileWriter(filename));

            outfile.println("digraph mmpc {");
            outfile.println("size=\"8.5,11\"");
            outfile.println("rotate=90");
            outfile.println("center=\"true\"");
            outfile.println("layers=\"true:mmpc:gs\"");

            for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
                outfile.println("\"" + trueBn.getNodeName(i) + "\" [layer=all, label=\"\"]");
            }

            boolean[][] trueDag = trueBn.getDag();

            //For every possible edge, check both directions and edge accordingly
            for (int i = 0; i < trueDag.length; i++) {
                for (int j = i + 1; j < trueDag[0].length; j++) {
                    String iName = trueBn.getNodeName(i);
                    String jName = trueBn.getNodeName(j);

                    boolean inTrue = trueDag[i][j] || trueDag[j][i];
                    int timesInGS = getCountFromList(gsList, iName, jName);
                    int timesInMMPC = getCountFromList(mmpcList, iName, jName);

                    String color = TN_COLOR;
                    String style = SOLID_STYLE;
                    String layer = "true";

                    if (!inTrue) {
                        style = DASHED_STYLE;
                    }

                    if (timesInMMPC > 0) {
                        color = MMPC_COLOR;
                        layer = "\"mmpc\"";
                    }

                    if (timesInGS > 0) {
                        color = GS_COLOR;
                        layer = "\"gs\"";
                    }

                    if (timesInMMPC > 0 && timesInGS > 0) {
                        color = ALL_COLOR;
                        if (!inTrue) {
                            layer = "\"mmpc:gs\"";
                        } else {
                            layer = "\"true:gs\"";
                        }
                    }

                    //String label = "\"g=" + timesInGS + ", m=" + timesInMMPC + "\"";
                    String label = "";

                    if (inTrue || timesInMMPC > 0 || timesInGS > 0) {
                        if (style.equalsIgnoreCase(SOLID_STYLE) && !color.equalsIgnoreCase(ALL_COLOR) && !layer.equalsIgnoreCase("true")) {
                            outfile.println("\"" + iName + "\"->\"" + jName + "\" [style=" + style + ", color=" + color + (!label.equalsIgnoreCase("") ? ", label=" + label : "") + ", layer=true]");
                        }
                        outfile.println("\"" + iName + "\"->\"" + jName + "\" [style=" + style + ", color=" + color + (!label.equalsIgnoreCase("") ? ", label=" + label : "") + ", layer=" + layer + "]");

                        Integer[] iNodeScores = nMap.get(iName);
                        Integer[] jNodeScores = nMap.get(jName);

                        if (style.equalsIgnoreCase(SOLID_STYLE) || style.equalsIgnoreCase(BOLD_STYLE)) { //In True
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tAll++;
                                iNodeScores[0]++;
                                jNodeScores[0]++;
                            } else if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpcTrue++;
                                iNodeScores[1]++;
                                jNodeScores[1]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGsTrue++;
                                iNodeScores[2]++;
                                jNodeScores[2]++;
                            } else if (color.equalsIgnoreCase(TN_COLOR)) {
                                tTrue++;
                                iNodeScores[6]++;
                                jNodeScores[6]++;
                            }
                        } else {
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tGsMmpc++;
                                iNodeScores[3]++;
                                jNodeScores[3]++;
                            }
                            if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpc++;
                                iNodeScores[4]++;
                                jNodeScores[4]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGs++;
                                iNodeScores[5]++;
                                jNodeScores[5]++;
                            }
                        }

                    }
                }
            }

            outfile.println("}");
            outfile.close();

            log.info("TOTAL: netName=" + netName +
                    " sampleSize=" + sampleSize +
                    " all=" + tAll + " mt=" + tMmpcTrue + " gt=" + tGsTrue + " gm=" + tGsMmpc + " m=" + tMmpc + " g=" + tGs + " t=" + tTrue);

            Set<String> nodes = nMap.keySet();
            for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                String node = iterator.next();
                Integer[] values = nMap.get(node);

                log.info("NODE: netName=" + netName +
                        " sampleSize=" + sampleSize +
                        " node=" + node +
                        " all=" + values[0] + " mt=" + values[1] + " gt=" + values[2] + " gm=" + values[3] + " m=" + values[4] + " g=" + values[5] + " t=" + values[6]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nMap;

    }

    public static void writeNodeCharacteristicsFromTrue(String filename, String trueFilename) throws Exception {
        PrintWriter outfile = new PrintWriter(new FileWriter(filename));

        outfile.println("netName\tnode\tnumParents\tcardinality\tnumNeighbors\tparentCard\tmax\tmin\tvar\tsd");


        PowerBayesNet trueBn = new PowerBayesNet(trueFilename);

        //Build map from node name to distributions
        Map<String, String> tableMap = new HashMap<String, String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        Document doc = factory.newDocumentBuilder().parse(new File(trueFilename));
        doc.normalize();

        NodeList dists = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < dists.getLength(); i++) {
            Element dist = (Element) dists.item(i);
            NodeList forNodeList = dist.getElementsByTagName("FOR");
            Node forNode = forNodeList.item(0);


            NodeList tableNodeList = dist.getElementsByTagName("TABLE");
            Node tableNode = tableNodeList.item(0);

            tableMap.put(forNode.getTextContent(), tableNode.getTextContent());
        }

        boolean[][] dag = trueBn.getDag();
        boolean[][] neighbors = trueBn.getNeighborsAdjList();

        for (int node = 0; node < dag.length; node++) {
            String nodeName = trueBn.getNodeName(node);
            int numParents = trueBn.getNrOfParents(node);
            int cardinality = trueBn.getCardinality(node);
            int numNeighbors = getNrOfNeighbors(node, neighbors); //Neighbors - parents = children so we don't need children
            int parentCard = trueBn.getParentCardinality(node);

            Map<String, Double> statMap = getTableSummary(tableMap.get(nodeName));

            double max = statMap.get("max");
            double min = statMap.get("min");
            double var = statMap.get("var");
            double sd = statMap.get("sd");

            outfile.println(trueBn.getName() + "\t" + nodeName + "\t" +
                    numParents + "\t" +
                    cardinality + "\t" +
                    numNeighbors + "\t" +
                    parentCard + "\t" +
                    max + "\t" +
                    min + "\t" +
                    var + "\t" +
                    sd);
        }
        outfile.close();
    }

    public static Map<String, Double> getTableSummary(String table) {
        double max = 0.0;
        double min = 1.0;

        double sum = 0.0;
        double sumSquared = 0.0;

        StringTokenizer tokens = new StringTokenizer(table);

        int n = tokens.countTokens();

        while (tokens.hasMoreTokens()) {
            double currVal = Double.parseDouble(tokens.nextToken());
            if (currVal > max) {
                max = currVal;
            }

            if (currVal < min) {
                min = currVal;
            }

            sum += currVal;
            sumSquared += (currVal * currVal);
        }

        double var = (n * sumSquared - (sum * sum)) / (n * (n - 1));
        double sd = Math.sqrt(var);

        Map<String, Double> returnMap = new HashMap<String, Double>();
        returnMap.put("max", max);
        returnMap.put("min", min);
        returnMap.put("var", var);
        returnMap.put("sd", sd);
        return returnMap;
    }

    public static int getNrOfNeighbors(int i, boolean[][] neighbors) {
        int neighCount = 0;
        boolean[] possibleNeighbors = neighbors[i];
        for (int j = 0; j < possibleNeighbors.length; j++) {
            boolean neighbor = possibleNeighbors[j];
            if (neighbor) {
                neighCount++;
            }
        }
        return neighCount;
    }


    private static Map<String, Integer[]> writeOverlapToDot(String filename, PowerBayesNet trueBn, PowerBayesNet gsBn, PowerBayesNet mmhcBn) {
        int tAll = 0;
        int tMmpcTrue = 0;
        int tGsTrue = 0;
        int tMmpc = 0;
        int tGs = 0;
        int tGsMmpc = 0;
        int tTrue = 0;

        Map<String, Integer[]> nMap = new HashMap();

        for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
            String nodeName = trueBn.getNodeName(i);
            Integer[] initialCounts = new Integer[]{0, 0, 0, 0, 0, 0, 0};
            nMap.put(nodeName, initialCounts);
        }


        try {
            PrintWriter outfile = new PrintWriter(new FileWriter(filename));

            outfile.println("digraph mmpc {");

            for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
                outfile.println("\"" + trueBn.getNodeName(i) + "\"");
            }

            boolean[][] trueDag = trueBn.getDag();
            boolean[][] gsDag = gsBn.getDag();
            boolean[][] mmhcDag = mmhcBn.getDag();

            //For every possible edge, check both directions and edge accordingly
            for (int i = 0; i < trueDag.length; i++) {
                for (int j = i + 1; j < trueDag[0].length; j++) {
                    String iName = trueBn.getNodeName(i);
                    String jName = trueBn.getNodeName(j);

                    String name1 = iName;
                    String name2 = jName;

                    boolean inTrue = trueDag[i][j] || trueDag[j][i];
                    boolean inGs = gsDag[i][j] || gsDag[j][i];
                    boolean sameDir = trueDag[i][j] == gsDag[i][j];
                    boolean isReversed = trueDag[j][i];
                    boolean inMmpc = mmhcDag[i][j] || mmhcDag[j][i];

                    String color = TN_COLOR;
                    String style = SOLID_STYLE;

                    if (sameDir) {
                        style = BOLD_STYLE;
                    }

                    if (isReversed) {
                        name1 = jName;
                        name2 = iName;
                    }

                    if (!inTrue) {
                        style = DASHED_STYLE;
                    }

                    if (inMmpc) {
                        color = MMPC_COLOR;
                    }

                    if (inGs) {
                        color = GS_COLOR;
                    }

                    if (inMmpc && inGs) {
                        color = ALL_COLOR;
                    }

                    if (inTrue || inMmpc || inGs) {
                        outfile.println("\"" + name1 + "\"->\"" + name2 + "\" [style=" + style + ", color=" + color + "]");

                        Integer[] iNodeScores = nMap.get(iName);
                        Integer[] jNodeScores = nMap.get(jName);

                        if (style.equalsIgnoreCase(SOLID_STYLE) || style.equalsIgnoreCase(BOLD_STYLE)) { //In True
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tAll++;
                                iNodeScores[0]++;
                                jNodeScores[0]++;
                            } else if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpcTrue++;
                                iNodeScores[1]++;
                                jNodeScores[1]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGsTrue++;
                                iNodeScores[2]++;
                                jNodeScores[2]++;
                            } else if (color.equalsIgnoreCase(TN_COLOR)) {
                                tTrue++;
                                iNodeScores[6]++;
                                jNodeScores[6]++;
                            }
                        } else {
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tGsMmpc++;
                                iNodeScores[3]++;
                                jNodeScores[3]++;
                            }
                            if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpc++;
                                iNodeScores[4]++;
                                jNodeScores[4]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGs++;
                                iNodeScores[5]++;
                                jNodeScores[5]++;
                            }
                        }

                    }
                }
            }

            outfile.println("}");
            outfile.close();

            log.info("TOTAL: netName=" + netName +
                    " trainIndex=" + trainIndex +
                    " sampleSize=" + sampleSize +
                    " all=" + tAll + " mt=" + tMmpcTrue + " gt=" + tGsTrue + " gm=" + tGsMmpc + " m=" + tMmpc + " g=" + tGs + " t=" + tTrue);

            Set<String> nodes = nMap.keySet();
            for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                String node = iterator.next();
                Integer[] values = nMap.get(node);

                log.info("NODE: netName=" + netName +
                        " trainIndex=" + trainIndex +
                        " sampleSize=" + sampleSize +
                        " node=" + node +
                        " all=" + values[0] + " mt=" + values[1] + " gt=" + values[2] + " gm=" + values[3] + " m=" + values[4] + " g=" + values[5] + " t=" + values[6]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nMap;
    }

    public static Map<String, Integer[]> writeOverlapToDotMMPC(String filename, PowerBayesNet trueBn, PowerBayesNet gsBn, Map<String, Set<String>> mmpc) {
        int tAll = 0;
        int tMmpcTrue = 0;
        int tGsTrue = 0;
        int tMmpc = 0;
        int tGs = 0;
        int tGsMmpc = 0;
        int tTrue = 0;

        Map<String, Integer[]> nMap = new HashMap();

        for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
            String nodeName = trueBn.getNodeName(i);
            Integer[] initialCounts = new Integer[]{0, 0, 0, 0, 0, 0, 0};
            nMap.put(nodeName, initialCounts);
        }


        try {
            PrintWriter outfile = new PrintWriter(new FileWriter(filename));

            outfile.println("digraph mmpc {");

            for (int i = 0; i < trueBn.getNrOfNodes(); i++) {
                outfile.println("\"" + trueBn.getNodeName(i) + "\"");
            }

            boolean[][] trueDag = trueBn.getDag();
            boolean[][] gsDag = gsBn.getDag();

            //For every possible edge, check both directions and edge accordingly
            for (int i = 0; i < trueDag.length; i++) {
                for (int j = i + 1; j < trueDag[0].length; j++) {
                    String iName = trueBn.getNodeName(i);
                    String jName = trueBn.getNodeName(j);

                    String name1 = iName;
                    String name2 = jName;

                    boolean inTrue = trueDag[i][j] || trueDag[j][i];
                    boolean inGs = gsDag[i][j] || gsDag[j][i];
                    boolean sameDir = trueDag[i][j] == gsDag[i][j];
                    boolean isReversed = trueDag[j][i];
                    boolean inMmpc = mmpc.get(iName).contains(jName) || mmpc.get(jName).contains(iName);

                    String color = TN_COLOR;
                    String style = SOLID_STYLE;

                    if (sameDir) {
                        style = BOLD_STYLE;
                    }

                    if (isReversed) {
                        name1 = jName;
                        name2 = iName;
                    }

                    if (!inTrue) {
                        style = DASHED_STYLE;
                    }

                    if (inMmpc) {
                        color = MMPC_COLOR;
                    }

                    if (inGs) {
                        color = GS_COLOR;
                    }

                    if (inMmpc && inGs) {
                        color = ALL_COLOR;
                    }

                    if (inTrue || inMmpc || inGs) {
                        outfile.println("\"" + name1 + "\"->\"" + name2 + "\" [style=" + style + ", color=" + color + "]");

                        Integer[] iNodeScores = nMap.get(iName);
                        Integer[] jNodeScores = nMap.get(jName);

                        if (style.equalsIgnoreCase(SOLID_STYLE) || style.equalsIgnoreCase(BOLD_STYLE)) { //In True
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tAll++;
                                iNodeScores[0]++;
                                jNodeScores[0]++;
                            } else if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpcTrue++;
                                iNodeScores[1]++;
                                jNodeScores[1]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGsTrue++;
                                iNodeScores[2]++;
                                jNodeScores[2]++;
                            } else if (color.equalsIgnoreCase(TN_COLOR)) {
                                tTrue++;
                                iNodeScores[6]++;
                                jNodeScores[6]++;
                            }
                        } else {
                            if (color.equalsIgnoreCase(ALL_COLOR)) {
                                tGsMmpc++;
                                iNodeScores[3]++;
                                jNodeScores[3]++;
                            }
                            if (color.equalsIgnoreCase(MMPC_COLOR)) {
                                tMmpc++;
                                iNodeScores[4]++;
                                jNodeScores[4]++;
                            } else if (color.equalsIgnoreCase(GS_COLOR)) {
                                tGs++;
                                iNodeScores[5]++;
                                jNodeScores[5]++;
                            }
                        }

                    }
                }
            }

            outfile.println("}");
            outfile.close();

            log.info("TOTAL: netName=" + netName +
                    " trainIndex=" + trainIndex +
                    " sampleSize=" + sampleSize +
                    " all=" + tAll + " mt=" + tMmpcTrue + " gt=" + tGsTrue + " gm=" + tGsMmpc + " m=" + tMmpc + " g=" + tGs + " t=" + tTrue);

            Set<String> nodes = nMap.keySet();
            for (Iterator<String> iterator = nodes.iterator(); iterator.hasNext();) {
                String node = iterator.next();
                Integer[] values = nMap.get(node);

                log.info("NODE: netName=" + netName +
                        " trainIndex=" + trainIndex +
                        " sampleSize=" + sampleSize +
                        " node=" + node +
                        " all=" + values[0] + " mt=" + values[1] + " gt=" + values[2] + " gm=" + values[3] + " m=" + values[4] + " g=" + values[5] + " t=" + values[6]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nMap;
    }
}
