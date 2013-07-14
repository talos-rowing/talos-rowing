package org.nargila.robostroke.media.gst;

import java.io.File;
import java.util.LinkedList;

import org.gstreamer.Gst;
import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.data.media.ExternalMedia.MediaFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GstInitializer {

	private static final Logger logger = LoggerFactory.getLogger(GstInitializer.class);
	
	private static final GstInitializer instance = new GstInitializer();
	
	private GstInitializer() {
	    
	    String jnaPath = buildJnaPath();
	    
	    String[] args = buildArgs();
	    

		logger.info("calling Gst.init() with jna.library.path {} and args {}", jnaPath, args);
		
		Gst.init("gst", args);
	}

	private String buildJnaPath() {
        String path = System.getProperty("jna.libray.path");
        File gstLibPath = Settings.getInstance().getMediaFrameworkNativeDir(MediaFramework.GST);
        
        if (gstLibPath != null) {
            path = (path == null) ? gstLibPath.getAbsolutePath() : (path + File.pathSeparator + gstLibPath.getAbsolutePath());
        }
        
        if (path != null) {
            System.setProperty("jna.library.path", path);
        }
        
        return path;
    }

    private String[] buildArgs() {
	    String pluginPath = System.getProperty("gst.plugin.path");

	    LinkedList<String> args = new LinkedList<String>();

	    if (pluginPath != null) {
	        args.add("--gst-plugin-path=" + pluginPath);
	    }

	    return args.toArray(new String[args.size()]);
	}
	
	public static GstInitializer getInstance() {
		return instance;
	}
}
