package org.nargila.robostroke.media.vlc;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.data.media.ExternalMedia.MediaFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VlcSetup {

    private static final Logger logger = LoggerFactory.getLogger(VlcSetup.class);


    private static Object loaded;

    private VlcSetup() {
    }

    public static synchronized boolean setupCheckVlc(Component comp) {

        File vlcPath = Settings.getInstance().getMediaFrameworkNativeDir(MediaFramework.VLC);

        if (loaded == null) {
            if (checkAddVlcPath(vlcPath)) {
                return true;
            } else if (Platform.isWindows()) {

                vlcPath = new File("C:\\Program Files\\VideoLAN\\VLC");

                if (checkAddVlcPath(vlcPath)) {
                    Settings.getInstance().setMediaFrameworkNativeDir(MediaFramework.VLC, vlcPath);
                    return true;
                } else {
                    vlcNotFoundMessage(comp);
                }
            }
        }

        return false;
    }

    public static boolean checkAddVlcPath(File vlcPath) {

        VlcSetup.setupJnaLoadPath(vlcPath);

        try {
            loaded = Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), Library.class);
            logger.info("'vlc' resolves with path set to {}", vlcPath);
            return true;
        } catch (UnsatisfiedLinkError e) {
            logger.info("'vlc' can not be resolved with path set to {}", vlcPath);
        }

        return false;
    }

    private static void vlcNotFoundMessage(Component comp) {

        logger.info("vlc load path can not be determined");

        JOptionPane.showMessageDialog(comp,
                "You need to install vlc (available from videolan.org) \n" +
                        "if you want to play synchronized video files. \n" +
                        "If it is already installed on your computer, go to \n" +
                        "the 'settings' menu item and specify \n" +
                        "the location of the " + RuntimeUtil.getLibVlcName() + " and " + RuntimeUtil.getLibVlcCoreName() + " files.", "Missing VLC Player", JOptionPane.WARNING_MESSAGE);
    }

    private static void setupJnaLoadPath(File vlcPath) {
        if (vlcPath != null && vlcPath.isDirectory()) {
            logger.info("add vlc path to JNA/Java load paths");

            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath.getAbsolutePath());
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName().replace("vlc", "vlccore"), vlcPath.getAbsolutePath());
        }
    }
}
