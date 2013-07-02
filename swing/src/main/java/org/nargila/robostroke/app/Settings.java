package org.nargila.robostroke.app;

import java.io.File;
import java.util.prefs.Preferences;

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
		if (val == null) {
			del(key);
		} else {
			pref.put(key, String.valueOf(val));
		}
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

		if (path != null && !path.equals("")) {

			File res = new File(path);

			if (res.isDirectory()) {
				return res;
			} else {
				del(propName);
			}
		}
		
		return null;
	}
	
	public static Settings getInstance() {
		return instance;
	}


	public File getVlcLibDir() {		
		return getPropAsDir(PROP_VLC_LIB_DIR, get(PROP_VLC_LIB_DIR, ""));
	}
	
	public void setVlcLibDir(File dir) {
		put(PROP_VLC_LIB_DIR, dir == null ? null : dir.getAbsoluteFile());
	}	
}
