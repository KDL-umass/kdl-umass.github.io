/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Aug 20, 2009
 * Time: 6:41:22 AM
 */
package rpc.datagen;

import rpc.dataretrieval.Database;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

/**
 * The Constant Connector class creates a constant number of relationships for each given entity.
 */
public class ConstantConnector extends Connector {

    private static Logger log = Logger.getLogger(ConstantConnector.class);

    private int constant;

    /**
     * Construct a constant connector object.
     * @param query the SQL query used to retrieve the set of all entity ids to connect to.
     * @param constant the number of links to create for a given entity.
     * @param withReplacement whether or not entities can be reused to connect to.
     */
    public ConstantConnector(String query, int constant, boolean withReplacement) {
        this.query = query;
        this.constant = constant;
        this.withReplacement = withReplacement;

        // init the random number generator for this object
        this.random = new Random();
    }

    /**
     * Get the list of unique ids from the query.
     */
    private void getIds() {
        ResultSet results = Database.executeQuery(this.query);

        // convert this into a set
        try {
            results.beforeFirst();
            while(results.next()) {
                this.ids.add(results.getInt(1));
            }
        }
        catch (SQLException e) {
            log.error("Error while iterating through result set: " + e);
        }

    }

    /**
     * Randomly sample a constant number of entity ids to connect to.
     * @return a list of entity ids to connect to.
     */
    public List<Integer> sample() {

        List<Integer> results = new ArrayList<Integer>();
        int i;

        // grab the desired set size of ids up to the remaining ids
        for (i = 0; i < this.constant && i < this.ids.size(); i++) {
            results.add(this.ids.get(i));
        }

        // do we replace?
        if (this.withReplacement) {
            // simply re-shuffle.  best option to re-use ids we have already sampled?
            java.util.Collections.shuffle(this.ids, this.random);
        }
        else {
            // remove the first set of ids
            for (;i > 0; i--)
                this.ids.remove(0);
        }

        return results;
    }

    /**
     * Reset the set of ids which the connector uses to sample from.
     */
    public void resetIds() {

        // clear the current set of ids
        this.ids = new ArrayList<Integer>();

        // obtain the set of ids (into an array)
        getIds();

        // shuffle the array so we access it randomly
        java.util.Collections.shuffle(this.ids, this.random);
    }

    /**
     * Returns a string representation of the constant connector.
     * @return the string representation.
     */
    public String toString() {
        return "Constant connector (" + this.constant + ")";
    }
}