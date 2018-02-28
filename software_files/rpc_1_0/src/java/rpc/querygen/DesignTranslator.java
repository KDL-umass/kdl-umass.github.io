/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 11, 2009
 * Time: 11:32:34 AM
 */
package rpc.querygen;

import rpc.design.*;
import rpc.dataretrieval.*;

import java.util.List;
import java.util.ArrayList;

/**
 * DesignTranslator provides a SQL query for a given design (unit and design elements, such
 * as conditioning variables) and their aggregators.
 */
public class DesignTranslator {

    private Design design;
    private Aggregator treatmentAggregator;
    private Aggregator outcomeAggregator;

    /**
     * Initializes a DesignTranslator object.
     * @param d the design to translate
     * @param treatmentAgg the treatment aggregator.
     * @param outcomeAgg the outcome aggregator.
     */
    public DesignTranslator(Design d, Aggregator treatmentAgg, Aggregator outcomeAgg) {
        this.design = d;
        this.treatmentAggregator = treatmentAgg;
        this.outcomeAggregator = outcomeAgg;
    }

    /**
     * Builds the SQL query for the design by combining the queries for the
     * unit and the design elements.
     * @return the SQL query.
     */
    public String getQuery() {
        String query;

        UnitTranslator unitTrans = new UnitTranslator(this.design.unit, this.treatmentAggregator, this.outcomeAggregator);
        String currentDesignQuery = unitTrans.getQuery();
        int numEmbeds = 0;

        List<String> designElNames = new ArrayList<String>();
        if (this.design.getDesignElements().size() > 0) {
            for (DesignElement de : this.design.getDesignElements()) {
                if (de instanceof ConditioningSet) {
                    ConditioningSet cs = (ConditioningSet) de;
                    int i=0;
                    for (Path control : cs.getConditioningSet()) {
                        String controlVarName = "control" + i;
                        PathTranslator  ct = new PathTranslator(control, cs.getConditioningAggregate(control), controlVarName);
                        String controlQuery = ct.getQuery();

                        String controlBaseEntityKey = control.getBaseItem().getPrimaryKey().name;
                        String designSubBaseEntityKey = control.getBaseItem().getPrimaryKey().name;

                        String embedTabName = "designSub" + numEmbeds;
                        numEmbeds++;
                        String controlTabName = "controlTable" + i;
                        query = String.format("SELECT %s.*, %s", embedTabName, controlVarName);
                        query += String.format("\nFROM\n(%s) %s,\n(%s) %s", currentDesignQuery, embedTabName, controlQuery, controlTabName);
                        query += String.format("\nWHERE %s.%s = %s.%s", embedTabName, designSubBaseEntityKey, controlTabName, controlBaseEntityKey);
                        currentDesignQuery = query;
                        designElNames.add("control" + i);
                        i++;
                    }
                }
            }
        }

        query = "SELECT v1, v2";
        for (String deName : designElNames) {
            query += ", " + deName;
        }
        query += "\nFROM (" + currentDesignQuery + ") design";

        return query;
    }
}