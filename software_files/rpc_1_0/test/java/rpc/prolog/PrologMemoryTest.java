/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: btaylor
 * Date: Jul 14, 2009
 * Time: 4:19:49 PM
 */
package rpc.prolog;

import junit.framework.TestCase;
import rpc.util.LogUtil;
import jpl.Term;
import jpl.Query;
import org.apache.log4j.Logger;

public class PrologMemoryTest extends TestCase {

    private static Logger log = Logger.getLogger(PrologMemoryTest.class);

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMemoryLeak1() {

        int ls1 = ((Term) (Query.oneSolution("statistics(localused,LS)").get("LS"))).intValue();

        int ls2 = ((Term) (Query.oneSolution("statistics(localused,LS)").get("LS"))).intValue();

        log.debug(ls1 + " to " + ls2 );
        assertTrue("local stack size unchanged after query", ls1 == ls2);
    }

    public void testMemoryLeak2() {
        Query q1 = new Query("statistics(localused,LS)");
        //q1.open();
        int ls1 = ((Term) (q1.oneSolution().get("LS"))).intValue();
        q1.close();

        Query q2 = new Query("statistics(localused,LS)");
        //q2.open();
        int ls2 = ((Term) (q2.oneSolution().get("LS"))).intValue();
        q2.close();

        log.debug(ls1 + " to " + ls2 );
        assertTrue("local stack size unchanged after query", ls1 == ls2);
    }

    public void testMemoryLeak3() {

        Query.hasSolution("statistics(localused,LS)");

        int ls1 = ((Term) (Query.oneSolution("statistics(localused,LS)").get("LS"))).intValue();
        int ls2 = ((Term) (Query.oneSolution("statistics(localused,LS)").get("LS"))).intValue();

        log.debug(ls1 + " to " + ls2 );
        assertTrue("local stack size unchanged after query", ls1 == ls2);
    }

}
