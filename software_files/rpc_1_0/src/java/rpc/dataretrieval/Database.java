/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: May 9, 2009
 * Time: 9:38:24 PM
 */
package rpc.dataretrieval;

import rpc.design.Path;

import java.sql.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Final class that provides methods to connect to a relational database and execute queries.
 */
public final class Database {

    private static Logger log = Logger.getLogger(Database.class);

    /**
     * The name of the database.
     */
    public static String dbname = "";
    private static Connection conn;
    private static Statement stmt;

    /**
     * Username defaults to the system username.
     */
    private static String userName = System.getProperty("user.name");
    /**
     * Password defaults to the empty string.
     */
    private static String password = "";
    
    /**
     * The number of bins used for discretizing quantitative variables.
     */
    public static int NUM_BINS = 5;

    /**
     * A cache that holds the bin thresholds for each path that is discretized.
     */
    public static Map<Path, List<Integer>> discretizeCache = new HashMap<Path, List<Integer>>();

    /**
     * A cache that holds the set of all paths that are categorical and should never be discretized.
     */
    public static Set<Path> categoricalPaths = new HashSet<Path>();

    /**
     * Private constructor ensures no one creates Database objects.  The Database class is used as a collection
     * of static methods to open connections to relational databases and execute queries.
     */
    private Database() {
    }

    /**
     * Set the username to use for the database connection.
     * @param user the username.
     */
    public static void setUserName(String user) {
        userName = user;
    }

    /**
     * Set the password to use for the database connection.
     * @param pw the password.
     */
    public static void setPassword(String pw) {
        password = pw;
    }

    /**
     * Opens a connection to a specified relational database.  Uses default connection information: Assumes
     * the username is the system username and the password is blank (i.e., trusted local connections).
     * @param name the name of the database to connect to.
     */
    public static void open(String name) {

        if (name == null) {
            dbname = "";
        }
        else {
            dbname = name;
        }

        String url = "jdbc:postgresql://localhost:5432/" + dbname;
        String driver = "org.postgresql.Driver";

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, userName, password);
            stmt = conn.createStatement(
                                          ResultSet.TYPE_SCROLL_INSENSITIVE,
                                          ResultSet.CONCUR_UPDATABLE
                                        );
        }
        catch (Exception e) {
            log.error("Error connecting to database: " + e);
        }
    }

    /**
     * Close the database connection.
     */
    public static void close() {
        try {
            stmt.close();
            conn.close();
        }
        catch (SQLException e) {
            log.error("Error while closing statement: " + e);
        }
    }

    /**
     * Execute a query.
     * @param query the query string.
     * @return a ResultSet that holds the rows corresponding to the result of the given query.
     */
    public static ResultSet executeQuery(String query) {
        try {
            return stmt.executeQuery(query);
        }
        catch (SQLException e) {
            // If a single row query produces an empty result set, an SQLException with SQLState equal to ?02000? (no data) is thrown.
            // We don't really care about these as several normal DB commands will return with no result,
            // and if a query really does come back with no results, the null is returned and can be handled by the calling method.
            if (!e.toString().equalsIgnoreCase("org.postgresql.util.PSQLException: No results were returned by the query.")) {
                log.error("Error executing query (" + query + "): " + e);
            }
            return null;
        }
    }

    /**
     * Clear the contents of the current database. This will drop the database (if it exists) and re-create it.
     */
    public static void clearDB() {
        // disconnect from the database (if connected)
        if (conn != null)
            close();

        // save the database name because it will be deleted
        String oldname = dbname;

        // connect to the server
        open("postgres");

        // drop the database (if it exists)
        executeQuery("DROP DATABASE IF EXISTS " + oldname);

        // re-create the database
        executeQuery("CREATE DATABASE " + oldname);

        // disconnect from the server
        close();

        // reconnect to the db?
        open(oldname);
    }

    /**
     * Resets the caches holding discretization and categorical information.
     */
    public static void resetCache() {
        discretizeCache = new HashMap<Path, List<Integer>>();
        categoricalPaths = new HashSet<Path>();
    }
}