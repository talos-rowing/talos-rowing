package org.nargila.robostroke.oggz;

public class CheckOS {

    private static String OS = System.getProperty("os.name").toLowerCase();


    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }
}
