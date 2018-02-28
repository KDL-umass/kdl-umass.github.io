/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 24, 2009
 * Time: 2:40:29 PM
 */
package rpc.querygen;

/**
 * CountDistinctTranslator provides a SQL query to retrieve the number of distinct values for a given path.
 */
public class CountDistinctTranslator {

    private PathTranslator pt;

    /**
     * Initializes the CountDistinctTranslator object.
     * @param pt the inner path translator object
     */
    public CountDistinctTranslator(PathTranslator pt) {
        this.pt = pt;
    }

    /**
     * Builds the SQL query by wrapping the query for the path with selecting the count of distinct values.
     * @return the SQL query.
     */
    public String getQuery() {
        return String.format("SELECT COUNT(DISTINCT %s) \nFROM (%s) distinctTemp",
                pt.getNewVarName(), pt.getQuery());
    }
}
