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

    public static void configure(String name) {
        configure(Level.WARN, name);
    }

    public static void configure(Level rootLevel, String name) {

        final LogConfigurator logConfigurator = new LogConfigurator();

        if (name != null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logFilePath = new File(Environment.getExternalStorageDirectory() + File.separator + name + ".log");
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
