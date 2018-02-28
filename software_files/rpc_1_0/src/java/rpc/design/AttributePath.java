/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 8, 2009
 * Time: 1:23:50 PM
 */
package rpc.design;

import rpc.schema.SchemaItem;
import rpc.schema.Attribute;
import rpc.dataretrieval.NopAggregator;
import rpc.dataretrieval.ModeAggregator;
import rpc.model.util.AttributeVariable;

import java.util.List;

/**
 * Implementation of a Path for attributional variables.
 */
public class AttributePath extends Path {

    /**
     * Initializes an attribute path object.
     * @param baseItem the base schema item of the path.
     * @param target the target schema item of the path.
     * @param path the list of schema items from the base to the target.
     * @param card the cardinality of the path.
     * @param attr the attribute on the target item.
     */
    public AttributePath(SchemaItem baseItem, SchemaItem target, List<SchemaItem> path, Cardinality card, Attribute attr) {
        super(baseItem, target, path, card);
        this.var = new AttributeVariable(attr);

        NopAggregator nopAgg = new NopAggregator(new AttributeVariable(attr));

        if (this.cardinality == Cardinality.ONE) {
            aggs.add(nopAgg);
        }
        else {
            aggs.add(new ModeAggregator(nopAgg));
        }
    }

    /**
     * Returns a string representation of the attribute path.
     * @return the string representation.
     */
    protected String getStringRep() {
        String s = "";
        s += "Path: " + this.path + ", ";
        s += "AttrVar: " + this.var;
        return s;
    }
}
