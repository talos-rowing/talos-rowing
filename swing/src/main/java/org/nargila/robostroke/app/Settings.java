package org.nargila.robostroke.app;

import java.io.File;
import java.util.prefs.Preferences;

public class Settings {
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
		put("lastDir", dir.getAbsoluteFile());
	}
	
	public File getLastDir() {
		
		String path = get("lastDir", "");
		
		File res = new File(path);
		
		if (res.isDirectory()) {
			return res;
		} else {
			del("lastDir");
			return null;
		}
	}
	
	public static Settings getInstance() {
		return instance;
	}		
}
