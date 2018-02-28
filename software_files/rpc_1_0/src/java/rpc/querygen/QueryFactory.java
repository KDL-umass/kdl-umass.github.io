/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 11, 2009
 * Time: 11:26:42 AM
 */
package rpc.querygen;

import rpc.design.Path;
import rpc.dataretrieval.Aggregator;
import rpc.schema.Schema;

import java.util.List;

/**
 * The QueryFactory class contains a set of useful routines that can be used in data retrieval.
 */
public class QueryFactory {

    /**
     * Builds a SQL query that joins the queries for a list of paths, adds a group by
     * for each variable on those paths, and adds a column that counts the number of occurrences
     * for each combination of values.  Note that the paths must have a common base item.
     * @param paths the list of paths to join.
     * @param aggs the aggregators for each of the paths.
     * @param countName the name of the count column.
     * @return the SQL query.
     */
    public static String joinPathsGrouped(List<Path> paths, List<Aggregator> aggs, String countName) {
        String query = "";
        for (int i=0; i<paths.size(); i++) {
            Path p = paths.get(i);
            Aggregator agg = aggs.get(i);
            PathTranslator pt = new PathTranslator(p, agg, "join"+i);
            query = joinPathToQuery(query, pt, i);
        }

        String joinVars = "join0";
        for (int j=1; j<paths.size(); j++) {
            joinVars += ", join" + j;
        }

        String groupQuery = "SELECT " + joinVars + ", COUNT(*) AS " + countName;
        groupQuery += "\nFROM (" + query + ") joinPaths";
        groupQuery += "\nGROUP BY " + joinVars;

        return groupQuery;
    }

    /**
     * Builds a SQL query that combines the query for a given path with a pre-existing query.
     * @param query the pre-existing query.
     * @param pt the path translator to add to the query.
     * @param numEmbeds the number of times this has been called, used as a suffix for sub table names.
     * @return the combined SQL query.
     */
    public static String joinPathToQuery(String query, PathTranslator pt, int numEmbeds) {
        if (query.equals("")) {
            return pt.getQuery();
        }

        String embedTabName = "joinSub" + numEmbeds;
        String joinTabName = "joinPath" + numEmbeds;
        String joinKey = Schema.getSchemaItem(pt.getBaseItem()).getPrimaryKey().name;
        String joinQuery = String.format("SELECT %s.*, %s", embedTabName, pt.getNewVarName());
        joinQuery += String.format("\nFROM\n(%s) %s,\n(%s) %s", query, embedTabName, pt.getQuery(), joinTabName);
        joinQuery += String.format("\nWHERE %s.%s = %s.%s", embedTabName, joinKey, joinTabName, joinKey);

        return joinQuery;
    }

    /**
     * Builds a SQL query used for computing the number of potential links between two queries.
     * This assumes that the queries produce columns named ct1 and ct2.
     * @param query1 the first query, which must contain column ct1.
     * @param query2 the second query, which must contain column ct2.
     * @return the SQL query that returns all columns plus the product of the two counts.
     */
    public static String potentialLinkCounts(String query1, String query2) {
        String query = "";

        query += "SELECT joinPaths1.*, joinPaths2.*, ct1*ct2 AS ct";
        query += String.format("\nFROM (%s) joinPaths1,\n(%s) joinPaths2", query1, query2);

        return query;
    }

    /**
     * Builds a SQL query that joins the queries for a list of paths.  The paths all
     * must have a common base item.
     * @param paths the list of paths to join.
     * @param aggs the aggregators for each of the paths.
     * @return the SQL query that joins the paths together.
     */
    public static String joinPaths(List<Path> paths, List<Aggregator> aggs) {
        String query = "";
        for (int i=0; i<paths.size(); i++) {
            Path p = paths.get(i);
            Aggregator agg = aggs.get(i);
            PathTranslator pt = new PathTranslator(p, agg, "join"+i);
            query = joinPathToQuery(query, pt, i);
        }

        return query;
    }

}