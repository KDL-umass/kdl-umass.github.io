/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 11:16:38 AM
 */
package rpc.model.util;

import rpc.schema.SchemaItem;

public class MockVariable extends Variable {
    /**
     * Dummy object for testing abstract class, Variable
     */    
    public MockVariable(SchemaItem si, String name) {
        super(si);
        this.name = name;
    }
}
