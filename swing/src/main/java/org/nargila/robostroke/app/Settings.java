package org.nargila.robostroke.app;

import org.nargila.robostroke.data.media.ExternalMedia.MediaFramework;

import java.io.File;
import java.util.prefs.Preferences;

public class Settings {
    private static final String PROP_MEDIA_FRAMEWORK = "mediaFramework";
    private static final String PROP_MEDIA_FRAMEWORK_NATIVE_DIR_ = "mediaFrameworkLibDir_";
    private static final String PROP_LAST_DIR = "lastDir";

    private static final Settings instance = new Settings();

    private final Preferences pref;

    private Settings() {
        pref = Preferences.userNodeForPackage(Settings.class);
    }

    private Settings(String uuid, long timestamp) {
        pref = Preferences.userNodeForPackage(Settings.class).node(uuid + timestamp);
    }


    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {

        if (def instanceof Integer)
            return (T) (Integer) pref.getInt(key, (Integer) def);
        if (def instanceof Double)
            return (T) (Double) pref.getDouble(key, (Double) def);
        if (def instanceof Long)
            return (T) (Long) pref.getLong(key, (Long) def);

        return (T) pref.get(key, (String) def);

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

    public MediaFramework getMediaFramework() {
        return MediaFramework.valueOf(get(PROP_MEDIA_FRAMEWORK, MediaFramework.VLC.name()));
    }

    public void setMediaFramework(MediaFramework mediaFramework) {
        put(PROP_MEDIA_FRAMEWORK, mediaFramework.name());
    }


    public void setMediaFrameworkNativeDir(MediaFramework mediaFramework, File dir) {
        put(PROP_MEDIA_FRAMEWORK_NATIVE_DIR_ + mediaFramework.name(), (dir != null && dir.isDirectory()) ? dir.getAbsoluteFile() : null);
    }


    public File getMediaFrameworkNativeDir(MediaFramework mediaFramework) {
        return getAsDir(PROP_MEDIA_FRAMEWORK_NATIVE_DIR_ + mediaFramework.name());
    }


    public void setLastDir(File dir) {
        put(PROP_LAST_DIR, dir.getAbsoluteFile());
    }

    public File getLastDir() {
        return getAsDir(PROP_LAST_DIR, get(PROP_LAST_DIR, ""));
    }


    public File getAsFile(String propName) {
        return getAsFile(propName, get(propName, (String) null));
    }

    private File getAsFile(String propName, String path) {

        if (path != null && !path.equals("")) {

            File res = new File(path);

            if (res.exists()) {
                return res;
            } else {
                del(propName);
            }
        }

        return null;
    }

    public File getAsDir(String propName) {
        return getAsDir(propName, get(propName, (String) null));
    }

    public File getAsDir(String propName, String path) {

        File f = getAsFile(propName, path);

        if (f != null && !f.isDirectory()) {
            del(propName);
            return null;
        }

        return f;
    }

    public static Settings getInstance() {
        return instance;
    }

    public static Settings getInstance(String uuid, long timestamp) {
        return new Settings(uuid, timestamp);
    }
}
