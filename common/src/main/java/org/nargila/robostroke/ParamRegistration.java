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

import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterLevel;
import org.nargila.robostroke.param.ParameterService;

class ParamRegistration {
	private final Parameter<?>[] WAY_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_GPS_SPEED_CHANGE_DAMPER,
					"Speed change damper",
					"GPS",
					ParameterLevel.ADVANCED, 1.0f),
			new Parameter.INTEGER(ParamKeys.PARAM_GPS_MIN_DISTANCE,
							"min distance",
							"GPS",
							ParameterLevel.ADVANCED, 20) {

				@Override
				protected Integer[] makeChoices() {
					return new Integer[] {
							10, 20, 30, 40, 50
					};
				}
			},
			new Parameter.FLOAT(ParamKeys.PARAM_GPS_DATA_FILTER_MAX_SPEED,
					"max speed",
					"GPS",
					ParameterLevel.ADVANCED, 6.5f)
	};
	
	private final Parameter<?>[] GRAVITY_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_SENSOR_ORIENTATION_DAMP_FACTOR, 
					"orientation damper", "Sensors", ParameterLevel.ADVANCED, 0.01f)
	};
	
	private final Parameter<?>[] SESSION_PARAMS = {
			new Parameter.BOOLEAN(ParamKeys.PARAM_SESSION_RECORDING_ON, 
	
			"", "", ParameterLevel.PRIVATE, false)
	};
	
	private final Parameter<?>[] DETECTOR_PARAMS = {
			new Parameter.INTEGER(ParamKeys.PARAM_ROWING_STOP_TIMEOUT, 
							"stop timeout", "Autorow", 
							ParameterLevel.BASIC, 5),
			new Parameter.INTEGER(ParamKeys.PARAM_ROWING_RESTART_WAIT_TIME, 
							"Auto Restart Wait Time", "Autorow", 
							ParameterLevel.BASIC, 5),
			new Parameter.FLOAT(ParamKeys.PARAM_ROWING_START_AMPLITUDE_TRESHOLD, 
							"min amplitude", "Autorow", 
							ParameterLevel.BASIC, 1.0f),
			new Parameter.STRING(ParamKeys.PARAM_ROWING_MODE, 
							"Rowing Start Mode", "Autorow", 
							ParameterLevel.BASIC, "MANUAL"),
			new Parameter.BOOLEAN(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE, 
							"Rowing Straight Line Mode", "Autorow", 
							ParameterLevel.BASIC, false)
	};
	
	private final Parameter<?>[] POWER_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_POWER_AMPLITUDE_FILTER_FACTOR, 
					"power filter", "Stroke", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_POWER_MIN_POWER, 
					"power filter", "Stroke", 
					ParameterLevel.ADVANCED, 5f)
	};
	
	private final Parameter<?>[] STROKE_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR, 
					"stroke amplitude filter", "Stroke", 
					ParameterLevel.ADVANCED, .05f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_DAMPER_FACTOR,
					"stroke amplitude change filter", "Stroke", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_ACCEPT_FACTOR,
					"stroke amplitude change accept filter", "Stroke", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_MIN_AMPLITUDE,
					"stroke min amplitude", "Stroke", 
					ParameterLevel.ADVANCED, .02f)
	};
	
	private ParamRegistration() {
		// ParamRegistration is a dummy object
	}
	
	static void installParams(ParameterService ps) {
		
		ParamRegistration pr = new ParamRegistration();
		
		ps.registerParam(pr.DETECTOR_PARAMS);
		ps.registerParam(pr.GRAVITY_PARAMS);
		ps.registerParam(pr.POWER_PARAMS);
		ps.registerParam(pr.SESSION_PARAMS);
		ps.registerParam(pr.STROKE_PARAMS);
		ps.registerParam(pr.WAY_PARAMS);
	}
}
