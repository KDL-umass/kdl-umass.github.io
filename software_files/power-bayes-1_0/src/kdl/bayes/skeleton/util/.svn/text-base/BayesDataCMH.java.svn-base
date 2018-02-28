/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.skeleton.util;

import kdl.bayes.util.Assert;
import org.rosuda.JRI.Rengine;
import weka.core.Instances;

public class BayesDataCMH extends BayesData {

    public static Rengine re = null;

    public BayesDataCMH() {
        super();
        if (re == null) {
            log.info("Initializing R engine.");
            String[] args = new String[]{"--no-save", "--no-restore", "--no-readline"};
            re = new Rengine(args, false, null);
            if (!re.waitForR()) {
                Assert.condition(false, "Unable to open R");
            }
            re.eval("options(warn=-1);");
        }

    }

    public BayesDataCMH(Instances instances) {
        super(instances);

        if (re == null) {
            log.info("Initializing R engine.");
            String[] args = new String[]{"--no-save", "--no-restore", "--no-readline"};
            re = new Rengine(args, false, null);
            if (!re.waitForR()) {
                Assert.condition(false, "Unable to open R");
            }
            re.eval("options(warn=-1);");
        }

    }

    public double[] assoc(double alpha) {
        numStatisticalCalls++;

        if (empiricalDof == 0) {
            log.debug("Found 0 DOF.");
            return new double[]{-1, -1, -1, -1, -1, -1};  // cannot compute association with zero dof
        }

        if (counts[0][0].length == 1) {
            return super.assoc(alpha);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("array(c(");

        for (int k = 0; k < counts[0][0].length; k++) {
            for (int j = 0; j < counts[0].length; j++) {
                for (int i = 0; i < counts.length; i++) {
                    sb.append(counts[i][j][k]);
                    sb.append(",");
                }
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("), dim=c(");
        sb.append(counts.length);
        sb.append(", ");
        sb.append(counts[0].length);

        sb.append(", ");
        sb.append(counts[0][0].length);

        sb.append("))");

        //log.info(sb.toString());
        //log.info(empiricalDof);

        double statistic = 1.0;
        double dof = 1.0;
        double pValue = 1.0;

        try {
            re.eval("t <- mantelhaen.test(" + sb + ")");
            statistic = re.eval("t$statistic").asDouble();
            dof = re.eval("t$parameter").asDouble();
            pValue = re.eval("t$p.value").asDouble();
            //String method = re.eval("t$method").asString();
        } catch (Exception e) {
            e.printStackTrace();
            re.end();
            System.exit(1);
        }

        double score = Math.max(0, alpha - pValue);

        return new double[]{score, statistic, dof, zIdxs.length, 1, pValue};
    }

}
