/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Oct 7, 2009
 * Time: 2:25:28 PM
 */
package rpc.datagen;


/**
 * CPT is an abstract class for conditional probability tables.
 */
abstract class CPT {

    /**
     * Normalize the probabilities to make sure they add up to 1.0.
     */
    public abstract void normalize();

}