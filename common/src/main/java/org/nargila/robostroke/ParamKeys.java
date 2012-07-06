/*
 * Copyright (c) 2011 Tal Shalif
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


package org.nargila.robostroke;

public interface ParamKeys {
	static final String PARAM_GPS_SPEED_CHANGE_DAMPER = "org.nargila.talos.rowing.gps.speedChangeDamper";
	static final String PARAM_GPS_MIN_DISTANCE = "org.nargila.talos.rowing.gps.minDistance";
	static final String PARAM_GPS_DATA_FILTER_MAX_SPEED = "org.nargila.talos.rowing.gps.maxSpeed";
	static final String PARAM_SENSOR_ORIENTATION_DAMP_FACTOR = "org.nargila.talos.rowing.sensor.orientationDampFactor";
	static final String PARAM_SENSOR_ORIENTATION_REVERSED = "org.nargila.talos.rowing.sensor.reverseDeviceOrientation";
	static final String PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR = "org.nargila.talos.rowing.stroke.rate.amplitudeFilterFactor";
	static final String PARAM_STROKE_RATE_MIN_AMPLITUDE = "org.nargila.talos.rowing.stroke.rate.minAmplitude";
	static final String PARAM_STROKE_RATE_AMPLITUDE_CHANGE_DAMPER_FACTOR = "org.nargila.talos.rowing.stroke.rate.amplitudeChangeDamperFactor";
	static final String PARAM_STROKE_RATE_AMPLITUDE_CHANGE_ACCEPT_FACTOR = "org.nargila.talos.rowing.stroke.rate.amplitudeChangeAcceptFactor";
	static final String PARAM_STROKE_POWER_AMPLITUDE_FILTER_FACTOR = "org.nargila.talos.rowing.stroke.power.amplitudeFilterFactor";
	static final String PARAM_STROKE_POWER_MIN_POWER = "org.nargila.talos.rowing.stroke.power.minPower";
	static final String PARAM_ROWING_STOP_TIMEOUT = "org.nargila.talos.rowing.stroke.detector.stopTimeout";
	static final String PARAM_ROWING_START_AMPLITUDE_TRESHOLD = "org.nargila.talos.rowing.stroke.detector.minAmplitude";
	static final String PARAM_ROWING_RESTART_WAIT_TIME = "org.nargila.talos.rowing.stroke.detector.restartWaitTime";
	static final String PARAM_ROWING_MODE = "org.nargila.talos.rowing.stroke.detector.startMode";
	static final String PARAM_ROWING_STRAIGHT_LINE_MODE = "org.nargila.talos.rowing.stroke.detector.straightLineMode";
	static final String PARAM_SESSION_RECORDING_ON = "org.nargila.talos.rowing.android.sessionRecordingOn";
	static final String PARAM_SESSION_RECORDING_LEADER_ENABLE = "org.nargila.talos.rowing.android.record.leader.enable";
	static final String PARAM_SESSION_BROADCAST_PORT = "org.nargila.talos.rowing.session.broadcast.port";
	static final String PARAM_SESSION_BROADCAST_HOST = "org.nargila.talos.rowing.android.session.remote.host";
}
