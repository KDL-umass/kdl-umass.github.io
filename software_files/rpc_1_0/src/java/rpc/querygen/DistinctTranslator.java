/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 18, 2009
 * Time: 11:02:05 AM
 */
package rpc.querygen;

/**
 * DistinctTranslator provides a SQL query to retrieve the set of distinct values for a given path.
 */
public class DistinctTranslator {

    private PathTranslator pt;

    /**
     * Initializes the DistinctTranslator object.
     * @param pt the inner path translator object.
     */
    public DistinctTranslator(PathTranslator pt) {
        this.pt = pt;
    }

    /**
     * Builds the SQL query by wrapping the query for the path with selecting distinct values.
     * @return the SQL query.
     */
    public String getQuery() {
        return String.format("SELECT DISTINCT %s \nFROM (%s) distinctTemp \nORDER BY %s ASC",
                pt.getNewVarName(), pt.getQuery(), pt.getNewVarName());
    }
}