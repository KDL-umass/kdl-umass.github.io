/**
 * Part of the open-source KDL Relational PC package
 * (see LICENSE for copyright and license information).
 * Created by IntelliJ IDEA.
 * User: maier
 * Date: May 13, 2009
 * Time: 2:39:30 PM
 */
package rpc.util;

import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/**
 * Implementation of a simple handler for the StatisticEngine for R.
 */
public class RHandler implements RMainLoopCallbacks {

    private static Logger log = Logger.getLogger(RHandler.class);

    /**
     * Default implementation. Does nothing.
     * @param re the R engine.
     * @param text text.
     * @param oType object type.
     */
    public void rWriteConsole(Rengine re, String text, int oType) {
    }

    /**
     * Logs the fact that the R engine is busy.
     * @param re the R engine.
     * @param which integer flag.
     */
    public void rBusy(Rengine re, int which) {
        log.debug("rBusy("+which+")");
    }

    /**
     * Reads the R console.
     * @param re the R engine.
     * @param prompt the prompt.
     * @param addToHistory
     * @return a string.
     */
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        log.debug(prompt);
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            String s=br.readLine();
            return (s==null||s.length()==0)?s:s+"\n";
        } catch (Exception e) {
            log.error("jriReadConsole exception: "+e.getMessage());
        }
        return null;
    }

    /**
     * Logs a message.
     * @param re the R engine.
     * @param message the message to log.
     */
    public void rShowMessage(Rengine re, String message) {
        log.debug("rShowMessage \""+message+"\"");
    }

    /**
     * Chooses a file.
     * @param re the R engine.
     * @param newFile the file.
     * @return a string.
     */
    public String rChooseFile(Rengine re, int newFile) {
        FileDialog fd = new FileDialog(new Frame(), (newFile==0)?"Select a file":"Select a new file", (newFile==0)?FileDialog.LOAD:FileDialog.SAVE);
        String res=null;
        if (fd.getDirectory()!=null) res=fd.getDirectory();
        if (fd.getFile()!=null) res=(res==null)?fd.getFile():(res+fd.getFile());
        return res;
    }

    /**
     * Default implementation. Does nothing.
     * @param re the R engine.
     */
    public void rFlushConsole (Rengine re) {
    }

    /**
     * Default implementation. Does nothing.
     * @param re the R engine.
     * @param filename the file name.
     */
    public void rLoadHistory (Rengine re, String filename) {
    }

    /**
     * Default implementation. Does nothing.
     * @param re the R engine.
     * @param filename the file name.
     */
    public void rSaveHistory (Rengine re, String filename) {
    }
}
