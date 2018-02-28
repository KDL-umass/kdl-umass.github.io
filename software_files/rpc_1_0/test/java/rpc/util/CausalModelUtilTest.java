/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Mar 17, 2010
 * Time: 3:40:30 PM
 */
package rpc.util;

import junit.framework.TestCase;
import rpc.TestUtil;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class CausalModelUtilTest extends TestCase {

    private static Logger log = Logger.getLogger(CausalModelUtilTest.class);

    public void setUp() throws Exception {
        super.setUp();
        LogUtil.initApp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCrossList() {
        List<List<Object>> lists = new ArrayList<List<Object>>();
        List<Object> l1 = new ArrayList<Object>();
        l1.add(0);
        l1.add(1);
        List<Object> l2 = new ArrayList<Object>();
        l2.add(2);
        l2.add(3);
        List<Object> l3 = new ArrayList<Object>();
        l3.add(4);
        l3.add(5);
        l3.add(null);
        lists.add(l1);
        lists.add(l2);
        lists.add(l3);

        List<List<Object>> expected = new ArrayList<List<Object>>();
        List<Object> exp1 = new ArrayList<Object>();
        exp1.add(0);
        exp1.add(2);
        exp1.add(4);
        expected.add(exp1);
        List<Object> exp2 = new ArrayList<Object>();
        exp2.add(0);
        exp2.add(2);
        exp2.add(5);
        expected.add(exp2);
        List<Object> exp3 = new ArrayList<Object>();
        exp3.add(0);
        exp3.add(2);
        exp3.add(null);
        expected.add(exp3);
        List<Object> exp4 = new ArrayList<Object>();
        exp4.add(0);
        exp4.add(3);
        exp4.add(4);
        expected.add(exp4);
        List<Object> exp5 = new ArrayList<Object>();
        exp5.add(0);
        exp5.add(3);
        exp5.add(5);
        expected.add(exp5);
        List<Object> exp6 = new ArrayList<Object>();
        exp6.add(0);
        exp6.add(3);
        exp6.add(null);
        expected.add(exp6);
        List<Object> exp7 = new ArrayList<Object>();
        exp7.add(1);
        exp7.add(2);
        exp7.add(4);
        expected.add(exp7);
        List<Object> exp8 = new ArrayList<Object>();
        exp8.add(1);
        exp8.add(2);
        exp8.add(5);
        expected.add(exp8);
        List<Object> exp9 = new ArrayList<Object>();
        exp9.add(1);
        exp9.add(2);
        exp9.add(null);
        expected.add(exp9);
        List<Object> exp10 = new ArrayList<Object>();
        exp10.add(1);
        exp10.add(3);
        exp10.add(4);
        expected.add(exp10);
        List<Object> exp11 = new ArrayList<Object>();
        exp11.add(1);
        exp11.add(3);
        exp11.add(5);
        expected.add(exp11);
        List<Object> exp12 = new ArrayList<Object>();
        exp12.add(1);
        exp12.add(3);
        exp12.add(null);
        expected.add(exp12);

        List<List<Object>> actual = CausalModelUtil.crossList(lists);
        log.debug(actual);
        TestUtil.verifyLists(expected, actual);
    }

    public void testCrossListSingle() {
        List<List<Object>> lists = new ArrayList<List<Object>>();
        List<Object> l1 = new ArrayList<Object>();
        l1.add(0);
        l1.add(1);
        l1.add(2);
        lists.add(l1);

        List<List<Object>> expected = new ArrayList<List<Object>>();
        List<Object> exp1 = new ArrayList<Object>();
        exp1.add(0);
        expected.add(exp1);
        List<Object> exp2 = new ArrayList<Object>();
        exp2.add(1);
        expected.add(exp2);
        List<Object> exp3 = new ArrayList<Object>();
        exp3.add(2);
        expected.add(exp3);

        List<List<Object>> actual = CausalModelUtil.crossList(lists);
        log.debug(actual);
        TestUtil.verifyLists(expected, actual);
    }
}
