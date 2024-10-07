package org.nargila.robostroke.android.common;

import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import org.apache.log4j.Level;

import java.io.File;

/**
 * Call {@link #configure()}} from your application's activity.
 */
public class ConfigureLog4J {

    private static File logFilePath;

    public static void configure(File dir, String name) {
        configure(Level.WARN, dir, name);
    }

    public static void configure(Level rootLevel, File dir, String name) {

        final LogConfigurator logConfigurator = new LogConfigurator();

        if (dir != null && name != null) {
            logFilePath = new File(dir, name + ".log");
            logFilePath.delete();
            logConfigurator.setFileName(logFilePath.getAbsolutePath());
            logConfigurator.setUseFileAppender(true);
        } else {
            logConfigurator.setUseFileAppender(false);
        }

        logConfigurator.setRootLevel(rootLevel);
        logConfigurator.configure();
    }

    public static File getLogFilePath() {
        return logFilePath;
    }
}
