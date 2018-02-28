/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 */
package rpc.util;

/**
 * Utility class that defines static assertion methods.
 */
public class Assert {

    /**
     * Checks if a given condition is true or not.  Throws an IllegalArgumentException (containing
     * failMessage) if isTrueCondition is false. Does nothing if it is true.
     * Used to assert some important condition is true, for example:
     * <p/>
     * Assert.condition(threshold >= 0, "threshold must be positive");
     */
    public static void condition(boolean isTrueCondition, String failMessage)
            throws IllegalArgumentException {
        if (!isTrueCondition) {
            throw new IllegalArgumentException(failMessage);
        }
    }

    /**
     * Throws IllegalArgumentException if object is null. Does nothing otherwise.
     * @param object object to test.
     * @param arg used in the message if null.
     * @throws IllegalArgumentException
     */
    public static void notNull(Object object, String arg)
            throws IllegalArgumentException {
        condition(object != null, arg);
    }

}