/**
 * $Id: TestSearchState.java 260 2008-09-08 14:06:12Z afast $
 *
 * Part of the open-source PowerBayes system
 *   (see LICENSE for copyright and license information).
 *
 */

/**
 * $Id: TestSearchState.java 260 2008-09-08 14:06:12Z afast $
 */

package kdl.bayes.search;

/**
 * TestSearchState
 */
public class TestSearchState extends SearchState {
    private int currentX = 0;
    private int currentY = 0;
    private int candidateX = 0;
    private int candidateY = 0;

    public TestSearchState(int x, int y) {
        candidateX = x;
        candidateY = y;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TestSearchState)) {
            return false;
        }
        TestSearchState rhs = (TestSearchState) obj;
        return currentX == rhs.getX() && currentY == rhs.getY() && candidateX == rhs.getCandidateX() && candidateY == rhs.getCandidateY();
    }

    public int compareTo(Object obj) {
        if (equals(obj)) {
            return 0;
        } else {
            return -1;
        }
    }

    public int getCandidateX() {
        return candidateX;
    }

    public int getCandidateY() {
        return candidateY;
    }

    public int getX() {
        return currentX;
    }

    public int getY() {
        return currentY;
    }

    public void makeCurrentState() {
        currentX = candidateX;
        currentY = candidateY;
    }

    public boolean isSame(SearchState other) {
        if (other instanceof TestSearchState) {
            TestSearchState otherState = (TestSearchState) other;
            if (candidateX == otherState.candidateX &&
                    candidateY == otherState.candidateY) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "(" + candidateX + "," + candidateY + ")";
    }

    public String getScoreStr() {
        return toString();
    }
}
