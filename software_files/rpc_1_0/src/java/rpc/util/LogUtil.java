/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: Feb 16, 2010
 * Time: 11:09:21 AM
 */
package rpc.util;

import org.apache.log4j.*;

import java.io.File;

/**
 * Utility class used to call static methods to initialize the logging system.
 */
public class LogUtil {

    private static Logger log = Logger.getLogger(LogUtil.class);
    private static final String LCF_FILE_NAME = "rpc.lcf";     // standard log4j file

    private static boolean isLogInit = false;

    /**
     * Initializes the log4j logging package, using a config file. If no config
     * file is specified, it does basic init. Prints an info message if file is
     * found . Does basic init if not found, and warns user.
     */
    public static void initLog4J() {
        // do basic config (needed both for null and invalid LCF_FILE_NAME)
        LogManager.resetConfiguration();
        BasicConfigurator.configure();
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);

        // try to load the file
        File logConfigFile = new File(LCF_FILE_NAME);
        if (logConfigFile.exists()) {
            log.info("* found log config file: " + logConfigFile.getAbsolutePath() + "'");
            LogManager.resetConfiguration();
            PropertyConfigurator.configure(logConfigFile.getAbsolutePath());
        } else {
            log.warn("* log config file not found (using defaults): " +
                    logConfigFile.getAbsolutePath() + "'");
        }
    }

    /**
     * Initializes an AIQ Application by starting the logger.
     */
    public static void initApp() {
        if (! isLogInit) {
            initLog4J();
            isLogInit = true;
        }
    }

}
