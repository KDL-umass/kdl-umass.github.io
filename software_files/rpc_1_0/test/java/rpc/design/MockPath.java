/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 19, 2010
 * Time: 12:46:55 PM
 */
package rpc.design;

import rpc.schema.SchemaItem;
import rpc.model.util.Variable;
import rpc.dataretrieval.NopAggregator;

import java.util.List;

public class MockPath extends Path {
    /**
     * Dummy object for testing abstract class, Path
     */
    public MockPath(SchemaItem baseItem, SchemaItem target, List<SchemaItem> path, Cardinality card, Variable var) {
        super(baseItem, target, path, card);
        this.var = var;

        this.aggs.add(new NopAggregator(this.var));
    }

    protected String getStringRep() {
        String s = "";
        s += "MockPath: " + this.path + ", ";
        s += "MockVar: " + this.var;
        return s;
    }
}
