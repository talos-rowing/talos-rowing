package org.nargila.robostroke.app;

import java.io.File;
import java.util.prefs.Preferences;

import org.nargila.robostroke.media.vlc.VlcSetup;

import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Platform;

public class Settings {
	private static final String PROP_VLC_LIB_DIR = "vlcLibDir";
	private static final String PROP_LAST_DIR = "lastDir";
	
	private static final Settings instance = new Settings();
	
	private final Preferences pref;
	
	private Settings() {
		pref = Preferences.userNodeForPackage(Settings.class);
	}
	
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T def) {
		
		if (def instanceof Integer)		
			return (T)(Integer)pref.getInt(key, (Integer)def);
		if (def instanceof Double)		
			return (T)(Double)pref.getDouble(key, (Double)def);
		if (def instanceof Long)		
			return (T)(Long)pref.getLong(key, (Long)def);

		return (T)(String)pref.get(key, (String)def);
	
	}
	
	public <T> void put(String key, T val) {
		pref.put(key, String.valueOf(val));
	}
	
	public void del(String key) {
		pref.remove(key);
	}

	public void setLastDir(File dir) {
		put(PROP_LAST_DIR, dir.getAbsoluteFile());
	}
	
	public File getLastDir() {
		return getPropAsDir(PROP_LAST_DIR, get(PROP_LAST_DIR, ""));
	}


	private File getPropAsDir(String propName, String path) {
		File res = new File(path);
		
		if (res.isDirectory()) {
			return res;
		} else {
			del(propName);
			return null;
		}
	}
	
	public static Settings getInstance() {
		return instance;
	}


	public File getVlcLibDir() {
		
		File res = getPropAsDir(PROP_VLC_LIB_DIR, get(PROP_VLC_LIB_DIR, ""));
		
		if (res == null) {
			String libPath = Platform.isWindows() ? "C:\\Program Files\\VideoLAN\\VLC" : "/usr/lib";
			File candidatePath = new File(Platform.isWindows() ? libPath : "/usr/lib");
			
			String soFile = RuntimeUtil.getLibVlcName();
			
			if (candidatePath.isDirectory() && new File(candidatePath, soFile).exists()) {
				setVlcLibDir(candidatePath);
				
				return candidatePath;
			}
		}
		
		return res;
	}
	
	public void setVlcLibDir(File dir) {
		if (VlcSetup.checkAddVlcPath(dir)) {
			put(PROP_VLC_LIB_DIR, dir.getAbsoluteFile());
		}
	}	
}
