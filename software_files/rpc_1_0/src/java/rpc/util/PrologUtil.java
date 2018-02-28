/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Nov 11, 2009
 * Time: 11:40:49 PM
 */
package rpc.util;

import rpc.schema.SchemaItem;
import rpc.schema.Schema;

import java.util.*;

import jpl.Term;
import jpl.Atom;

/**
 * Utility class that provides auxiliary methods dealing with and communicating with Prolog.
 */
public class PrologUtil {

    private static Map<Term, List<SchemaItem>> termsToList = new HashMap<Term, List<SchemaItem>>();

    /**
     * Constructs a list of schema items out the JPL representation of a list (i.e., a nested compound).
     * Adds the change to a cache.
     * @param term the JPL term to transform.
     * @return a list of schema items that was embedded in the JPL term.
     */
    public static List<SchemaItem> termToList(Term term) {
		//Problem: lists are returned as nested compounds with the "." operator
        if (termsToList.containsKey(term)) {
            return termsToList.get(term);
        }

        List<SchemaItem> listTerms = new ArrayList<SchemaItem>();
		if (term.hasFunctor(".", 2)) {
			Term[] arrayTerms = term.toTermArray();
            for (Term t : arrayTerms) {
                listTerms.add(Schema.getSchemaItem(t.name()));
            }
        }
        termsToList.put(term, listTerms);
        return listTerms;

	}

    /**
     * Constructs a new JPL term from a given term by changing minus functors back to hyphens
     * in names of atoms.
     * @param term the JPL term to fix.
     * @return a new JPL term with the hyphen appropriately embedded in the name.
     */
    public static Term fixHyphen(Term term) {
		//Status: non-issue if all hyphenated literals in prolog are quoted
		//Problem: hyphen in an atom is viewed as a compound with "-" as the functor
		if (term.hasFunctor("-", 2)){
			return new Atom(term.arg(1) + "-" + term.arg(2));
		}
		else {
			return term;
		}
	}

    /**
     * Empties the cache that maps terms to lists of schema items.
     */
    public static void resetCache() {
        termsToList = new HashMap<Term, List<SchemaItem>>();
    }

}