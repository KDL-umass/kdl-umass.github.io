/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 26, 2009
 * Time: 2:11:44 PM
 */
package rpc.util;

import org.python.core.PyDictionary;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import java.util.*;

import rpc.datagen.CPTPrior;

/**
 * Utility class that provides methods to translate Python objects to Java.
 */
public class PythonUtil {

    /**
     * Converts a Python dictionary to a Java map from integers to doubles.
     * @param pyDict the Python dictionary.
     * @return a map from integers to doubles.
     */
    public static HashMap<Integer, Double> mapFromIntegerDoublePyDict(PyDictionary pyDict) {
        Assert.notNull(pyDict, "pyDict null");
        // continue
        HashMap<Integer, Double> map = new HashMap<Integer, Double>();
        PyList keys = pyDict.keys();
        int size = keys.__len__();
        for (int i = 0; i < size; i++) {
            PyObject key = keys.pop();
            PyObject value = pyDict.get(key);
            map.put((Integer) key.__tojava__(Object.class), (Double) value.__tojava__(Object.class));
        }
        return map;
    }

    /**
     * Converts a Python dictionary to a Java map from Strings to doubles.
     * @param pyDict the Python dictionary.
     * @return a map from Strings to doubles.
     */
    public static HashMap<String, Double> mapFromStringDoublePyDict(PyDictionary pyDict) {
        Assert.notNull(pyDict, "pyDict null");
        // continue
        HashMap<String, Double> map = new HashMap<String, Double>();
        PyList keys = pyDict.keys();
        int size = keys.__len__();
        for (int i = 0; i < size; i++) {
            PyObject key = keys.pop();
            PyObject value = pyDict.get(key);
            map.put((String) key.__tojava__(Object.class), (Double) value.__tojava__(Object.class));
        }
        return map;
    }

    /**
     * Converts a Python dictionary to a Java map from Strings to integers.
     * @param pyDict the Python dictionary.
     * @return a map from Strings to integers.
     */
    public static HashMap<String, Integer> mapFromStringIntegerPyDict(PyDictionary pyDict) {
        Assert.notNull(pyDict, "pyDict null");
        // continue
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        PyList keys = pyDict.keys();
        int size = keys.__len__();
        for (int i = 0; i < size; i++) {
            PyObject key = keys.pop();
            PyObject value = pyDict.get(key);
            map.put((String) key.__tojava__(Object.class), (Integer) value.__tojava__(Object.class));
        }
        return map;
    }

    /**
     * Converts a Python dictionary to a Java map from Lists of objects to CPTPriors.
     * @param pyDict the Python dictionary.
     * @return a map from Lists of objects to CPTPriors.
     */
    public static HashMap<List<Object>, CPTPrior> mapFromCPTParentPyDict(PyDictionary pyDict) {
        Assert.notNull(pyDict, "pyDict null");
        // continue
        HashMap<List<Object>, CPTPrior> map = new HashMap<List<Object>, CPTPrior>();
        PyList keys = pyDict.keys();
        int size = keys.__len__();
        for (int i = 0; i < size; i++) {
            PyObject key = keys.pop();
            PyObject value = pyDict.get(key);
            List<Object> curList = new ArrayList<Object>(Arrays.asList((Object[]) key.__tojava__(Object[].class)));
            CPTPrior cpt = new CPTPrior((PyDictionary) value);
            //map.put(curList, (CPTPrior) value.__tojava__(Object.class));
            map.put(curList, cpt);
        }
        return map;

    }

    /**
     * Converts a Python tuple into a Java list of objects.
     * @param pyTuple the Python tuple.
     * @return a list of objects.
     */
    public static List<Object> listFromPyTuple(PyTuple pyTuple) {
        Assert.notNull(pyTuple, "pyTyple null");
        return new ArrayList<Object>(Arrays.asList((Object[]) pyTuple.__tojava__(Object[].class)));
    }
}