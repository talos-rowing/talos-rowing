/*
 * Copyright (c) 2024 Tal Shalif
 *
 * This file is part of Talos-Rowing.
 *
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.android.app;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PreferencesHelper {
    public static final int PREFERENCES_VERSION = 1;

    public static final String PREFERENCES_VERSION_RESET_KEY = "preferencesVersionReset" + PREFERENCES_VERSION;

    public static final String TALOS_APP_VERSION_KEY = "talosAppVersion";

    public static final String PREFERENCE_KEY_HRM_ENABLE = "org.nargila.talos.rowing.android.hrm.enable";

    public static final String PREFERENCE_KEY_PREFERENCES_RESET = "org.nargila.talos.rowing.android.preferences.reset";

    public static final String PREFERENCE_KEY_PREFERENCES_LOG = "org.nargila.talos.rowing.android.preferences.log";

    public static final String METERS_RESET_ON_START_PREFERENCE_KEY = "org.nargila.talos.rowing.android.stroke.detector.resetOnStart";

    public static final String GRAPHS_SHOW_PREFERENCE_KEY = "org.nargila.talos.rowing.android.layout.graphs.show";

    public static final String METERS_LAYOUT_MODE_KEY = "org.nargila.talos.rowing.android.layout.meters.layoutMode";

    public static final String PREFERENCE_KEY_LAYOUT_MODE_LANDSCAPE = "org.nargila.talos.rowing.android.layout.mode.landscape";

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final SharedPreferences preferences;

    // must keep onSharedPreferenceChangeListener as field, never as local variable or it will be garbage collected!
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            setParameterFromPreferences(key);

            switch (key) {
                case PREFERENCE_KEY_HRM_ENABLE:
                    owner.graphPanelDisplayManager.setEnableHrm(preferences.getBoolean(PREFERENCE_KEY_HRM_ENABLE, true), true);
                    break;
                case PREFERENCE_KEY_PREFERENCES_RESET:
                    preferences.edit().putBoolean(PREFERENCES_VERSION_RESET_KEY, true).apply();
                    owner.graphPanelDisplayManager.resetNextRun();
                    break;
                case METERS_RESET_ON_START_PREFERENCE_KEY:
                    owner.metersDisplayManager.setResetOnStart(preferences.getBoolean(METERS_RESET_ON_START_PREFERENCE_KEY, true));
                    break;
                case METERS_LAYOUT_MODE_KEY: {
                    String defaultValue = owner.getString(R.string.defaults_layout_meter_mode);
                    String val = preferences.getString(METERS_LAYOUT_MODE_KEY, defaultValue);
                    owner.metersDisplayManager.setLayoutMode(val);
                    break;
                }
                case PREFERENCE_KEY_LAYOUT_MODE_LANDSCAPE:
                    owner.setLandscapeLayout(preferences.getBoolean(PREFERENCE_KEY_LAYOUT_MODE_LANDSCAPE, false));
                    break;
                case GRAPHS_SHOW_PREFERENCE_KEY: {
                    boolean defaultValue = Boolean.parseBoolean(owner.getString(R.string.defaults_layout_show_graphs));
                    boolean val = preferences.getBoolean(GRAPHS_SHOW_PREFERENCE_KEY, defaultValue);
                    owner.graphPanelDisplayManager.setShowGraphs(val);
                    break;
                }
            }
        }
    };

    private final RoboStrokeActivity owner;

    private final String uuid;
    private final ParameterChangeListener orientationReversedListener;

    public PreferencesHelper(RoboStrokeActivity owner) {
        this.owner = owner;

        preferences = PreferenceManager.getDefaultSharedPreferences(owner);
        orientationReversedListener = new ParameterChangeListener() {

            @Override
            public void onParameterChanged(Parameter param) {
                preferences.edit().putBoolean(ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId(), (Boolean) param.getValue()).apply();
            }
        };

        { // create UUID, if no exist
            String tmpUuid = preferences.getString("uuid", null);
            uuid = tmpUuid == null ? UUID.randomUUID().toString() : tmpUuid;
        }
        ensureUUIDExists();
    }

    public void init() {
        syncParametersFromPreferences();
        applyPreferences();
        attachPreferencesListener();
    }

    public void resetPreferencesIfNeeded(Runnable onDone) {

        boolean firstRun = preferences.getString(TALOS_APP_VERSION_KEY, "").isEmpty();
        boolean newVersion = !preferences.getString(TALOS_APP_VERSION_KEY, "").equals(owner.getVersion());
        boolean resetRequested = preferences.getBoolean(PREFERENCES_VERSION_RESET_KEY, false);

        boolean resetPending = resetRequested || (newVersion && !firstRun);


        final Runnable runAtEnd = new Runnable() {

            @Override
            public void run() {
                preferences.edit().putBoolean(PREFERENCE_KEY_PREFERENCES_RESET, false).apply();
                preferences.edit().putBoolean(PREFERENCES_VERSION_RESET_KEY, false).apply();
                preferences.edit().putString(TALOS_APP_VERSION_KEY, owner.getVersion()).apply();
                if (newVersion) {
                    owner.showAbout();
                }
                if (onDone != null) {
                    onDone.run();
                }
            }
        };

        if (resetPending) {
            new AlertDialog.Builder(owner)
                    .setMessage(R.string.preference_reset_dialog_message)
                    .setPositiveButton(R.string.yes, (dialog, whichButton) -> {
                        dettachPreferencesListener();
                        preferences.edit().clear().apply();
                        init();
                    })
                    .setOnDismissListener(l -> {
                        runAtEnd.run();
                    })
                    .show();

        } else {
            runAtEnd.run();
        }
    }

    public String getUUID() {
        return uuid;
    }

    private void ensureUUIDExists() {

        if (preferences.getString("uuid", null) == null) {
            preferences.edit().putString("uuid", uuid).apply();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getPref(String key, T defValue) {

        if (defValue instanceof Boolean) {
            return (T) (Boolean) preferences.getBoolean(key, (Boolean) defValue);
        } else if (defValue instanceof Integer) {
            return (T) (Integer) preferences.getInt(key, (Integer) defValue);
        } else if (defValue instanceof Long) {
            return (T) (Long) preferences.getLong(key, (Long) defValue);
        } else if (defValue instanceof Float) {
            return (T) (Float) preferences.getFloat(key, (Float) defValue);
        } else {
            return (T) preferences.getString(key, (String) defValue);
        }
    }

    private void applyPreferences() {
        String[] keys = {
                METERS_RESET_ON_START_PREFERENCE_KEY,
                METERS_LAYOUT_MODE_KEY,
                GRAPHS_SHOW_PREFERENCE_KEY
        };

        for (String key : keys) {
            onSharedPreferenceChangeListener.onSharedPreferenceChanged(preferences, key);
        }

        owner.graphPanelDisplayManager.setEnableHrm(preferences.getBoolean(PREFERENCE_KEY_HRM_ENABLE, false), false);
        owner.setLandscapeLayout(preferences.getBoolean(PREFERENCE_KEY_LAYOUT_MODE_LANDSCAPE, false));
    }

    private void syncParametersFromPreferences() {
        logger.info("synchronizing Android preferences to back-end");

        for (String key : preferences.getAll().keySet()) {
            setParameterFromPreferences(key);
        }
    }

    private void attachPreferencesListener() {
        preferences.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        owner.getRoboStroke().getParameters().addListener(ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId(), orientationReversedListener);
    }

    private void dettachPreferencesListener() {
        preferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        owner.getRoboStroke().getParameters().removeListener(ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED.getId(), orientationReversedListener);
    }

    private void setParameterFromPreferences(String key) {
        if (key == null) {
            return;
        }
        if (key.startsWith("org.nargila.talos.rowing") && !key.startsWith("org.nargila.talos.rowing.android")) {
            logger.info("setting back-end parameter {} >>", key);

            final ParameterService ps = owner.getRoboStroke().getParameters();


            try {

                Parameter param = ps.getParam(key);
                Object defaultValue = param.getDefaultValue();
                Object val = preferences.getAll().get(key);
                String value = val == null ? defaultValue.toString() : val.toString(); //preferences.edit().remove(key).commit()

                ps.setParam(key, value);

                logger.info("done setting back-end parameter {} with value {} <<", key, value);

            } catch (Exception e) {
                logger.error("error while trying to set back-end parameter from an Android preference <<", e);
            }
        }
    }

}
