package org.nargila.robostroke.media.vlc;

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class VlcSetup {

	private static final Logger logger = LoggerFactory.getLogger(VlcSetup.class);
	
	private VlcSetup() {
	}
	
	public static boolean setupCheckVlc(File vlcPath, Component comp) {

		if (vlcPath == null) {
			try {
				Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), Library.class);
				return true;
			} catch (UnsatisfiedLinkError e) {
				if (VlcSetup.checkVlcPath(vlcPath, comp)) {
					return checkAddVlcPath(vlcPath);
				}
			}
		}
		
		return false;
	}

	public static boolean checkAddVlcPath(File vlcPath) {
		VlcSetup.setupVlcLoadPath(vlcPath);
		
		try {
			Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), Library.class);
			return true;
		} catch (UnsatisfiedLinkError e2) {					
			return false;
		}
	}
	
	public static boolean checkVlcPath(File vlcPath, Component comp) {

		if (vlcPath == null) {
			
			logger.info("vlc load path can not be determined");
			
			JOptionPane.showMessageDialog(comp, 
					"You need to install vlc (available from videolan.org) \n" +
					"if you want to play synchronized video files. \n" +
					"If it is already installed on your computer, go to \n" +
					"the 'settings' menu item and specify \n" +
					"the location of the " + RuntimeUtil.getLibVlcName() + " and " + RuntimeUtil.getLibVlcCoreName() + " files." , "Missing VLC Player", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public static void setupVlcLoadPath(File vlcPath) {
		if (vlcPath != null)  {
			logger.info("add vlc path to JNA/Java load paths");
			
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), vlcPath.getAbsolutePath());
			NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName().replace("vlc", "vlccore"), vlcPath.getAbsolutePath());			
		}
	}
}
