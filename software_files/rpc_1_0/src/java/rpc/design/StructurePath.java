/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 11, 2009
 * Time: 3:42:11 PM
 */
package rpc.design;

import rpc.schema.SchemaItem;
import rpc.schema.Relationship;
import rpc.model.util.StructureVariable;
import rpc.dataretrieval.NopAggregator;
import rpc.dataretrieval.CountAggregator;

import java.util.List;

/**
 * Implementation of a Path for structural variables (e.g., cardinality or existence of relationships).
 */
public class StructurePath extends Path {

    /**
     * Initializes a structure path object.  This corresponds to the cardinality or existence of a relationship.
     * @param baseItem the base schema item of the path.
     * @param target the target schema item of the path.
     * @param path the list of schema items from the base to the target.
     * @param card the cardinality of the path.
     */
    public StructurePath(SchemaItem baseItem, SchemaItem target, List<SchemaItem> path, Cardinality card) {
        super(baseItem, target, path, card);
        this.var = new StructureVariable((Relationship) target);

        CountAggregator countAgg = new CountAggregator(new NopAggregator(new StructureVariable((Relationship) target)));
        aggs.add(countAgg);
    }

    /**
     * Returns a string representation of the structure path.
     * @return the string representation.
     */
    protected String getStringRep() {
        String s = "";
        s += "Path: " + this.path + ", ";
        s += "StructureVar: " + this.var;
        return s;
    }
}
