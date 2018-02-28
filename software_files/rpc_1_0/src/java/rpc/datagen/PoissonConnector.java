/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 6, 2009
 * Time: 1:49:53 PM
 */
package rpc.datagen;

import rpc.dataretrieval.Database;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

import cern.jet.random.Poisson;
import cern.jet.random.engine.RandomEngine;
import org.apache.log4j.Logger;

/**
 * The Poisson Connector class creates a number of relationships drawn from a Poisson distribution.
 */
public class PoissonConnector extends Connector {

    private static Logger log = Logger.getLogger(PoissonConnector.class);

    private Poisson poissonDistr;
    private double mean;

    /**
     * Construct a Poisson connector object.
     * @param query the SQL query used to retrieve the set of all entity ids to connect to.
     * @param mean the mean of the Poisson distribution used to determine the number of links to create
     * for a given entity. Note that this mean should be supplied as ONE LESS than the actual mean because 1 will
     * always be added to the number drawn from the distribution (to prevent unconnected entities).
     * @param withReplacement whether or not entities can be reused to connect to.
     */
    public PoissonConnector(String query, double mean, boolean withReplacement) {
        this.query = query;
        this.withReplacement = withReplacement;
        this.mean = mean;

        // init the random number generator for this object
        final Random rand = new Random();
        this.random = rand;
        RandomEngine randEng = new RandomEngine() {
            public int nextInt() {
                return rand.nextInt();
            }
        };

        this.poissonDistr = new Poisson(this.mean, randEng);
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
     * Randomly sample a number of entity ids to connect to based on a draw from a Poisson distribution with the
     * given mean.
     * @return a list of entity ids to connect to.
     */
    public List<Integer> sample() {

        List<Integer> results = new ArrayList<Integer>();
        int i;

        // grab the desired set size of ids up to the remaining ids
        int numLinks = this.poissonDistr.nextInt() + 1;
        for (i = 0; i < numLinks && i < this.ids.size(); i++) {
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
     * Returns a string representation of the Poisson connector.
     * @return the string representation.
     */
    public String toString() {
        return "Poisson connector (mean=" + (this.mean + 1) + ")";
    }

}