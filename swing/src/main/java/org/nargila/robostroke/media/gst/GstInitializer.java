package org.nargila.robostroke.media.gst;

import java.util.LinkedList;

import org.gstreamer.Gst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GstInitializer {

	private static final Logger logger = LoggerFactory.getLogger(GstInitializer.class);
	
	private static final GstInitializer instance = new GstInitializer();
	
	private GstInitializer() {
		
		String pluginPath = System.getProperty("gst.plugin.path");
		
		LinkedList<String> args = new LinkedList<String>();
		
		if (pluginPath != null) {
			args.add("--gst-plugin-path=" + pluginPath);
		}
		
		logger.info("calling Gst.init() with args {}", args);
		
		Gst.init("gst", args.toArray(new String[args.size()]));
	}

	public static GstInitializer getInstance() {
		return instance;
	}
}
