/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Oct 7, 2009
 * Time: 3:45:06 PM
 */
package rpc.app;

import org.python.core.*;
import org.apache.log4j.Logger;

import java.io.File;
import rpc.util.Assert;
import rpc.util.LogUtil;
import rpc.script.CKD;
import jpl.fli.Prolog;

/**
 * Main application for executing a Jython script file.
 */
public class PythonScript {

    private static Logger log = Logger.getLogger(PythonScript.class);

    /**
     * Processes the input Jython file
     */
    public static void main(String[] args) {
        //check args
        if (args.length < 1) {
            System.out.println("wrong number of args (" + args.length + ")");
            printUsage();
            return;
        }

        String scriptName = args[0];
        LogUtil.initApp();

        // continue checking args
        File scriptFile = new File(scriptName);
        log.debug("main(): " + scriptName);
        if (scriptName.length() == 0) {
            System.out.println("scriptName was empty: '" + ", " + scriptName + "'");
            printUsage();
            System.exit(1); // abnormal
        }

        // test scriptFile
        if (!scriptFile.exists()) {
            System.out.println("couldn't find script: " + scriptFile.getAbsolutePath());
            System.exit(1); // abnormal
        }

        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length-1);

        runScript(scriptFile, newArgs);
        Prolog.halt(0);
    }

    private static void printUsage() {
        System.out.println("Usage: java " + PythonScript.class.getName() + " scriptName [applicationArgs]\n" +
                "\tscriptName: script file to run\n" +
                "\tapplicationArgs: optional arguments to script\n");
    }

    /**
     * @param scriptFile
     * @param args
     * @return Termination status. By convention, a nonzero status code indicates
     *         abnormal termination.
     */
    private static int runScript(File scriptFile,
                                 String[] args) {
        int status = 0;  // conventional normal termination code
        Assert.notNull(scriptFile, "scriptFile null");
        try {
            // create a Jython interpreter
            log.info("* executing script: " + scriptFile.getAbsolutePath());
            CKD aiq = new CKD(scriptFile, args);
            aiq.getInterpreter().execfile(scriptFile.getAbsolutePath());

            log.info("* done executing script");
        } catch (PyException pyExc) {
            System.out.println("* Python exception running script:" + pyExc);
        } catch (Exception exc) {
            System.out.println("* exception running script:" + exc);
            status = -1;
        }
        return status;
    }


}
