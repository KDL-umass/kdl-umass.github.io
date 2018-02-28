/**
 * $Id: SearchProblem.java 259 2008-08-29 20:16:45Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 */

package kdl.bayes.search;

import java.util.Collection;
import java.util.List;

/**
 * SearchProblem
 */
public interface SearchProblem {

    public SearchState getInitialState();

    public double getScore(SearchState state);

    public Collection getSuccessors(SearchState state);

    public int compareStates(SearchState state1, SearchState state2);

    public SearchState breakTies(List bestStates);
}
