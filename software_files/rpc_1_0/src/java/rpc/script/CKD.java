/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 7, 2009
 * Time: 4:16:20 PM
 */
package rpc.script;

import org.python.util.PythonInterpreter;
import org.python.core.PySystemState;

import java.io.File;
import java.util.*;

import rpc.model.util.ModelSupport;
import rpc.model.scoring.ModelScoring;
import rpc.schema.Schema;
import rpc.schema.Metadata;
import rpc.design.*;
import rpc.util.UnitUtil;
import rpc.util.PrologUtil;
import rpc.dataretrieval.Database;
import rpc.datagen.DataGenerator;
import jpl.Query;

/**
 * The class whose instances are made available to Jython scripts as the
 * variable "ckd". Methods in this class are called like any python method.
 * Note: you cannot access additional arguments passed to CKD's Jython
 * script runner (script.sh, which call the
 * <CODE>rpc.app.PythonScript</CODE> application) via the standard
 * <CODE>sys.argv</CODE> variable. Instead you can get them via
 * <CODE>ckd.getArgs()</CODE>, which is set to contain the args when you run
 * the script.
 *
 * @see rpc.app.PythonScript
 */
public class CKD {

    private PythonInterpreter pyInterpreter;
    private File scriptFile;
    private String[] args;
    private final String RPC_HOME = System.getenv().get("RPC_HOME");
    private int HOP_THRESHOLD;

    /**
     * Access to the model support data structure that stores the set of units generated
     * for the given hop threshold.
     */
    public ModelSupport modelSupport;

    /**
     * Static initializer that does the one-time only intialization of Jython.
     * Needed because we pass different PySystemStates to RunFileJFrame.initJython().
     */
    static {
        PySystemState.initialize();
    }

    /**
     * Full-arg constructor for use with Jython, saves command line arguments.
     * @param scriptFile the Jython script file.
     * @param args the set of arguments to the script.
     */
    public CKD(File scriptFile, String[] args) {
        this.scriptFile = scriptFile;
        this.args = args;

        // create the interpreter
        // pass a new PySystemState so that output from each interpreter will be
        // redirected for that interpreter. the default is all interpreters
        // sharing the same output
        pyInterpreter = new PythonInterpreter(null, new PySystemState());

        pyInterpreter.set("ckd", this); // set the 'ckd' variable for scripts;
    }

    /**
     * Gets the array of arguments passed to the script.
     * Useful for application-specific inputs.
     * @return additional arguments passed to the script.
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * Gets the Python interpreter object.
     * @return the Python interpreter.
     */
    public PythonInterpreter getInterpreter() {
        return pyInterpreter;
    }

    /**
     * Returns the absolute path of the script file.
     * @return absolute path of the script file.
     */
    public String getScriptFile() {
        return scriptFile.getAbsolutePath();
    }

    /**
     * Loads the RPC Prolog modules.
     */
    public void loadRPC() {
        String s = String.format("consult('%s/src/prolog/rpc-modules.pl')", RPC_HOME);
		Query.oneSolution(s); //static method to run given query
	}

    /**
     * Loads the Prolog schema file.
     * @param schemaFile the path name of the schema file.
     */
    public void loadSchema(String schemaFile) {
        Schema.loadSchema(schemaFile);
    }

    /**
     * Gets all possible units generated at the current hop threshold.
     * @return a list of all units.
     */
    public List<Unit> getUnits() {
        return UnitUtil.getUnits(this.HOP_THRESHOLD);
	}

    /**
     * Gets the unique units that correspond to potential dependencies for the current hop threshold.
     * @return a list of unique units.
     */
    public List<Unit> getUniqueUnits() {
        return UnitUtil.getUniqueUnits(HOP_THRESHOLD);
    }

    /**
     * Gets the currently used hop threshold.
     * @return the hop threshold.
     */
    public int getHopThreshold() {
        return this.HOP_THRESHOLD;
    }

    /**
     * Set the hop threshold to use to generate the set of units.
     * @param threshold the hop threshold to use.
     */
    public void setHopThreshold(int threshold) {
        this.HOP_THRESHOLD = threshold;
    }

    /**
     * Resets all caches system-wide.  Useful to avoid ever-expanding caches and memory sharing
     * across repeated experiments.
     */
    public void resetCaches() {
        PrologUtil.resetCache();
        Database.resetCache();
        DataGenerator.resetCache();
        Metadata.reset();
        ModelScoring.reset();
    }
}
