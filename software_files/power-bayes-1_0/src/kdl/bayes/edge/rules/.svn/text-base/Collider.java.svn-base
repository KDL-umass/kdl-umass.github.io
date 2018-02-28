/**
 * $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

package kdl.bayes.edge.rules;

import kdl.bayes.PowerBayesNet;

public class Collider implements Comparable {
    int node1;
    int node2;
    int v;

    double bdeu;

    //Orient triple: X - Y - Z as X -> Y <- Z
    //Orient triple node1 - v - node2 as node1 -> v <- node2
    public Collider(int xIdx, int yIdx, int zIdx) {
        node1 = xIdx;
        node2 = zIdx;
        v = yIdx;
    }


    public void apply(PowerBayesNet bn) {
        if (isValid(bn)) {
            bn.addEdge(node1, v);
            bn.addEdge(node2, v);
        }
    }

    public int compareTo(Object o) {
        if (!(o instanceof Collider)) {
            throw new IllegalArgumentException("Must compare two Colliders");
        }
        return Double.compare(((Collider) o).getScore(), bdeu);
    }

    public double getScore() {
        return bdeu;
    }

    /**
     * @param bn
     * @return
     */
    public boolean isValid(PowerBayesNet bn) {
        boolean[][] dag = bn.getDag();

        //Orient triple: X - Y - Z as X -> Y <- Z
        //Either both directions exist or neither direction currently exists
        //Implicitly check whether one piece of the collider has already been oriented in a different collider.
        return (((!dag[node1][v] && !dag[v][node1]) || (dag[node1][v] && dag[v][node1]))
                && ((!dag[node2][v] && !dag[v][node2]) || (dag[node2][v] && dag[v][node2])));

        //return ( (!pdag[v][node1] && !pdag[v][node2]) || (pdag[node1][v] && pdag[v][node1] && pdag[node2][v] && pdag[v][node2]) );
    }

    public void remove(PowerBayesNet bn) {
        bn.removeEdge(node1, v);
        bn.removeEdge(node2, v);
    }

    public String toString() {
        return node1 + "->" + v + "<-" + node2;
    }

    /**
     * Update score based on the score if this collider were to be oriented
     * orients the collider, computes the score and removes the collider.
     * Does not perform caching on the BDeu score to quickly update the score based on the changes.
     *
     * @param bn
     */
    public void updateScore(PowerBayesNet bn) {
        if (isValid(bn)) {
            apply(bn);
            bdeu = bn.logBDeuScore();
            remove(bn);
        }
    }
}
