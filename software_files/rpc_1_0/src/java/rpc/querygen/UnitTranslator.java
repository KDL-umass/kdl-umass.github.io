/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 4, 2009
 * Time: 11:24:13 AM
 */
package rpc.querygen;

import rpc.schema.Schema;
import rpc.design.Unit;
import rpc.design.AttributePath;
import rpc.dataretrieval.Aggregator;
import rpc.dataretrieval.NopAggregator;

/**
 * UnitTranslator provides a SQL query for a given unit and treatment and outcome aggregators.
 */
public class UnitTranslator {

    private Unit unit;
    private Aggregator treatmentAggregator;
    private Aggregator outcomeAggregator;

    private String baseItem;

    /**
     * Initializes a UnitTranslator object.
     * @param u the unit to translate.
     * @param treatmentAgg the treatment aggregator.
     * @param outcomeAgg the outcome aggregator.
     */
    public UnitTranslator(Unit u, Aggregator treatmentAgg, Aggregator outcomeAgg) {
        this.unit = u;
        this.treatmentAggregator = treatmentAgg;
        this.outcomeAggregator = outcomeAgg;

        this.baseItem = this.unit.baseItem.name;
    }

    /**
     * Builds the SQL query for the unit by combining the queries for the
     * treatment and outcome paths in the unit.
     * @return the SQL query.
     */
    public String getQuery() {
        //If there are no aggregators (both are passed as null), we want to retrieve the actual source data
        //without collapsing rows.  This will result in potentially multiple rows for each unit instance.
        if (this.treatmentAggregator == null && this.outcomeAggregator == null) {
            return new SourceTranslator(this.unit).getQuery();
        }
        else if (this.treatmentAggregator == null) {
            NopAggregator nopAgg = new NopAggregator(this.unit.treatment);
            String query1 = new PathTranslator(this.unit.treatmentPath, nopAgg, "v1").getQuery();
            String query2 = new PathTranslator(this.unit.outcomePath, this.outcomeAggregator, "v2").getQuery();

            String query = "";

            String baseItemKey = Schema.getSchemaItem(this.baseItem).getPrimaryKey().name;
            query += String.format("SELECT outcome.%s, v1, v2", baseItemKey);
            query += String.format("\nFROM\n(%s) treatment,\n(%s) outcome", query1, query2);
            query += String.format("\nWHERE treatment.%s = outcome.%s", baseItemKey, baseItemKey);

            return query;
        }
        else if (this.outcomeAggregator == null) {
            NopAggregator nopAgg = new NopAggregator(this.unit.outcome);
            String query1 = new PathTranslator(this.unit.treatmentPath, this.treatmentAggregator, "v1").getQuery();
            String query2 = new PathTranslator(this.unit.outcomePath, nopAgg, "v2").getQuery();

            String query = "";

            String baseItemKey = Schema.getSchemaItem(this.baseItem).getPrimaryKey().name;
            query += String.format("SELECT outcome.%s, v1, v2", baseItemKey);
            query += String.format("\nFROM\n(%s) treatment,\n(%s) outcome", query1, query2);
            query += String.format("\nWHERE treatment.%s = outcome.%s", baseItemKey, baseItemKey);

            return query;
        }
        else {
            //Both treatment and outcome make use of an aggregator
            //in this case, we must retrieve a single value of treatment and a single value of outcome
            //for each unit instance
            String baseItemKey = Schema.getSchemaItem(this.baseItem).getPrimaryKey().name;

            String query1 = new PathTranslator(this.unit.treatmentPath, this.treatmentAggregator, "v1").getQuery();

            String query2;
            if (this.unit.outcomePath instanceof AttributePath) { //attribute uncertainty
                query2 = new PathTranslator(this.unit.outcomePath, this.outcomeAggregator, "v2").getQuery();
            }
            else { //existence uncertainty
                //Here, we first just retrieve all links that do exist, so simple return 1
                query2 = String.format("SELECT %s, 1 AS v2\nFROM %s", baseItemKey, this.baseItem);
            }

            String query = "";
            query += String.format("SELECT outcome.%s, v1, v2", baseItemKey);
            query += String.format("\nFROM\n(%s) treatment,\n(%s) outcome", query1, query2);
            query += String.format("\nWHERE treatment.%s = outcome.%s", baseItemKey, baseItemKey);

            return query;
        }
    }
}