/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 6, 2009
 * Time: 12:28:56 PM
 */
package rpc.util;

import rpc.schema.SchemaItem;
import rpc.schema.Schema;

import java.util.List;
import java.util.ArrayList;

/**
 * Utility class that provides methods about the schema.
 */
public class SchemaUtil {

    /**
     * Change a list of schema item names to a list of schema item objects.
     * @param itemNames a list of schema item names.
     * @return the corresponding list of schema item objects.
     */
    public static List<SchemaItem> listToSchemaItems(List<String> itemNames) {
        List<SchemaItem> items = new ArrayList<SchemaItem>();
        for (String item : itemNames) {
            items.add(Schema.getSchemaItem(item));
        }
        return items;
    }
}
