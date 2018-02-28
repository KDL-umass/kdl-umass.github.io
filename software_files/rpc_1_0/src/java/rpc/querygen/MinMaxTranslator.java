/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 24, 2009
 * Time: 2:29:40 PM
 */
package rpc.querygen;

/**
 * MinMaxTranslator provides a SQL query to retrieve the minimum and maximum values for a given path.
 */
public class MinMaxTranslator {

    private PathTranslator pt;

    /**
     * Initializes the MinMaxTranslator object.
     * @param pt the inner path translator object
     */
    public MinMaxTranslator(PathTranslator pt) {
        this.pt = pt;
    }

    /**
     * Builds the SQL query by wrapping the query for the path with selecting the min and max values.
     * @return the SQL query.
     */
    public String getQuery() {
        return String.format("SELECT MIN(%s), MAX(%s) \nFROM (%s) minMaxTemp",
                pt.getNewVarName(), pt.getNewVarName(), pt.getQuery());
    }
}
