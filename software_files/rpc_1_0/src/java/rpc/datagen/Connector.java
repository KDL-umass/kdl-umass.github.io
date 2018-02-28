/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: May 10, 2009
 * Time: 6:46:16 PM
 */
package rpc.datagen;

import java.util.List;
import java.util.Random;

/**
 * Connector abstract class defines core methods needed to create links (i.e., relationships or connections) between
 * entities.  Implementations of a Connector have different ways of connecting entities.
 */
public abstract class Connector {

    /**
     * Flag stating whether or not entities can have multiple connections.
     */
    protected boolean withReplacement = false;
    /**
     * The full list of entity ids to connect to.
     */
    protected List<Integer> ids;
    /**
     * The SQL query used to retrieve the set of ids.
     */
    protected String query;

    /**
     * Random object.
     */
    protected Random random;

    /**
     * Randomly sample a list of entity ids to connect to based on the way the Connector works.
     * @return a list of entity ids to connect to.
     */
    public abstract List<Integer> sample();

    /**
     * Reset the list of entity ids.
     */
    public abstract void resetIds();

}