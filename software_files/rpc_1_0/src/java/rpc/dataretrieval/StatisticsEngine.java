/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 8, 2009
 * Time: 1:12:07 PM
 */
package rpc.dataretrieval;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import rpc.util.RHandler;

/**
 * Final class that creates a single object that evaluates expressions in the statistical package R.
 */
public final class StatisticsEngine {

    /**
     * The R statistics engine.
     */
    public static Rengine re = new Rengine(new String[]{"--no-save", "--no-restore", "--no-readline"},
                                    false, new RHandler());
    
    private StatisticsEngine() {
    }

    /**
     * Evaluates an expression in the statistics package R.
     * @param s the expression.
     * @return an object wrapper over the result of the expression.
     */
    public static REXP eval(String s) {
        return re.eval(s);
    }
}