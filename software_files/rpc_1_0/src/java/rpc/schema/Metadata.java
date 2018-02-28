/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Dec 10, 2009
 * Time: 1:42:25 PM
 */
package rpc.schema;

import rpc.dataretrieval.Database;
import rpc.dataretrieval.Aggregator;
import rpc.design.Path;
import rpc.querygen.PathTranslator;
import rpc.querygen.CountDistinctTranslator;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * The Metadata class offers a collection of methods that provide additional information on
 * statistics about the database.
 */
public class Metadata {

    private static Logger log = Logger.getLogger(Metadata.class);

    private static Map<SchemaItem, Integer> itemCounts = new HashMap<SchemaItem, Integer>();
    private static Map<Path, Map<Aggregator, Integer>> distinctValues = new HashMap<Path, Map<Aggregator, Integer>>();

    /**
     * Empties the caches that map the number of each schema item in the database and the
     * cache that maps the number of distinct values for each path and aggregator.
     */
    public static void reset() {
        itemCounts = new HashMap<SchemaItem, Integer>();
        distinctValues = new HashMap<Path, Map<Aggregator, Integer>>();
    }

    /**
     * Get the number of rows in the database for a particular schema item.
     * @param si the schema item.
     * @return the count of the number of rows for the schema item in the database.
     */
    public static int getItemCount(SchemaItem si) {
        if (! itemCounts.containsKey(si)) {
            int rows = 0;
            ResultSet results = Database.executeQuery(String.format("SELECT COUNT(*)\nFROM %s", si.name));
            try {
                results.beforeFirst();
                results.next();
                rows = results.getInt(1);
            }
            catch (SQLException e) {
                log.error("Error while getting number of rows: " + e);
            }

            itemCounts.put(si, rows);
        }

        return itemCounts.get(si);
    }

    /**
     * Get the number of distinct values for a particular path and an aggregator.
     * @param p the path to a particular variable.
     * @param agg the aggregator over the path.
     * @return the number of distinct values.
     */
    public static int getDistinctValues(Path p, Aggregator agg) {
        //Note: the distinct values are in ascending order
        if (! distinctValues.containsKey(p)) {
            distinctValues.put(p, new HashMap<Aggregator, Integer>());
        }
        if (! distinctValues.get(p).containsKey(agg)) {
            CountDistinctTranslator cdt = new CountDistinctTranslator(new PathTranslator(p, agg, "tempVar"));
            ResultSet distinctVals = Database.executeQuery(cdt.getQuery());
            try {
                distinctVals.beforeFirst();
                distinctVals.next();
                distinctValues.get(p).put(agg, distinctVals.getInt(1));
            }
            catch (SQLException e) {
                log.error("Error while iterating through distinct values result set: " + e);
            }
        }
        return distinctValues.get(p).get(agg);
    }
}