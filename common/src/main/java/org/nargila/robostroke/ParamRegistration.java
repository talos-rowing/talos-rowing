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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.nargila.robostroke.data.SessionRecorderConstants;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterLevel;
import org.nargila.robostroke.param.ParameterService;

public class ParamRegistration {
	
	private final Parameter<?>[] WAY_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_GPS_SPEED_CHANGE_DAMPER,
					"Speed change damper",
					"Boat speed lowpass filter: any value lower then 1.0 will cause delayed updates of boat speed value",
					"GPS",
					ParameterLevel.ADVANCED, 1.0f),
			new Parameter.INTEGER(ParamKeys.PARAM_GPS_MIN_DISTANCE,
							"min distance",
							"Minimum distance boat must travel between each distance/speed calculation",
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
					"Maximum permited boat speed - useful for catching and throwing away erratic GPS readings",
					"GPS",
					ParameterLevel.ADVANCED, 6.5f)
	};
	
	private final Parameter<?>[] GRAVITY_PARAMS = {
			new Parameter.BOOLEAN(ParamKeys.PARAM_SENSOR_ORIENTATION_REVERSED, 
					"coax mode", "indicates the device user is forward facing - such as a coax or trainer. Normally the rower sits with her back to the front of the boat", "Sensors", ParameterLevel.BASIC, false),
			new Parameter.FLOAT(ParamKeys.PARAM_SENSOR_ORIENTATION_DAMP_FACTOR, 
					"orientation damper", "Lowpass filter used as stabalizer for the 'down' notion of the application", "Sensors", ParameterLevel.ADVANCED, 0.01f)
	};
	
	private final Parameter<?>[] SESSION_PARAMS = {
			new Parameter.STRING(ParamKeys.PARAM_SESSION_BROADCAST_HOST, 	
			"session broadcast host", "Remote device to receive real-time session data from", "Session", ParameterLevel.BASIC, SessionRecorderConstants.BROADCAST_HOST),
			new Parameter.INTEGER(ParamKeys.PARAM_SESSION_BROADCAST_PORT, 	
			"session broadcast port", "Port number on which to braodcast/receive real-time device sensor data", "Session", ParameterLevel.BASIC, SessionRecorderConstants.BROADCAST_PORT),
			new Parameter.BOOLEAN(ParamKeys.PARAM_SESSION_RECORDING_LEADER_ENABLE, 	
			"session recording sync mark", "Display a 'film leader' countdown dialog to assist synchronizing session recording with external audio/video media", "Session", ParameterLevel.BASIC, false),
			new Parameter.BOOLEAN(ParamKeys.PARAM_SESSION_RECORDING_ON, 	
					"session recording on", "", "{internal}", ParameterLevel.PRIVATE, false)
	};
	
	private final Parameter<?>[] DETECTOR_PARAMS = {
			new Parameter.INTEGER(ParamKeys.PARAM_ROWING_STOP_TIMEOUT, 
							"stop timeout", "How many seconds since end of last stroke before finishing a rowing lap when in 'Autorow' mode", "Autorow", 
							ParameterLevel.BASIC, 5),
			new Parameter.INTEGER(ParamKeys.PARAM_ROWING_RESTART_WAIT_TIME, 
							"Auto Restart Wait Time", "Number of seconds application must wait before allowed to auto start another rowing lap when in 'Autorow' mode", "Autorow", 
							ParameterLevel.BASIC, 5),
			new Parameter.FLOAT(ParamKeys.PARAM_ROWING_START_AMPLITUDE_TRESHOLD, 
							"min amplitude", "Arbitrary min acceleration/deceleration amplitude value must be detected in order for the application to consider it the beginning of a starting stroke when in 'Autorow' mode", "Autorow", 
							ParameterLevel.BASIC, 1.0f),
			new Parameter.STRING(ParamKeys.PARAM_ROWING_MODE, 
							"Split rowing start/stop mode", 
							"Rowing lap display start mode - in AUTO mode application will automatically start and stop a split rowing session. In SEMI_AUTO mode the rower needs to press the device once to enable each auto-start. In MANUAL mode the rower must press to start and stop split rowing mode. In CONTINUOUS mode the application will immediately enter split mode like as if 'start' was pressed", 
							"Autorow",
							ParameterLevel.BASIC, 
							"MANUAL"),
			new Parameter.BOOLEAN(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE, 
							"Rowing Straight Line Mode", 
							"Only calculate GPS distance between original start point and current location - if usefull at all, only to be used when the rowing course is completely streight", 
							"Autorow", 
							ParameterLevel.BASIC, false)
	};
	
	private final Parameter<?>[] POWER_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_POWER_AMPLITUDE_FILTER_FACTOR, 
					"power filter",
					"*FIXME*", // TODO
					"Stroke Power", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_POWER_MIN_POWER, 
					"power filter",
					"Minimum power (sum of acceleration points) required during a rowing cycle in order for it to register",
					"Stroke Power", 
					ParameterLevel.ADVANCED, 5f)
	};
	
	private final Parameter<?>[] STROKE_PARAMS = {
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR, 
					"stroke amplitude filter", 
					"Lowpass filter to 'smooth' the rowing sinusoids and establish single rise-above/drop-below zero points in each rowing cycle", 
					"Stroke", 
					ParameterLevel.ADVANCED, .025f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_DAMPER_FACTOR,
					"stroke amplitude change filter", 
					"Lowpass filter to stabalize stroke rate and protect against displaying erratic changes",
					"Stroke", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_ACCEPT_FACTOR,
					"stroke amplitude change accept filter", 
					"*FIXME*", // TODO
					"Stroke", 
					ParameterLevel.ADVANCED, .5f),
			new Parameter.FLOAT(ParamKeys.PARAM_STROKE_RATE_MIN_AMPLITUDE,
					"stroke min amplitude", 
					"Minimum acceleration amplitude required during a rowing cycle in order for it to register", 
					"Stroke", 
					ParameterLevel.ADVANCED, .02f)
	};
	
	private final Parameter<?>[][] PARAMS = {
			DETECTOR_PARAMS,
			GRAVITY_PARAMS,
			POWER_PARAMS,
			SESSION_PARAMS,
			STROKE_PARAMS,
			WAY_PARAMS
	};
	
	private ParamRegistration() {
		// ParamRegistration is a dummy object
	}
	
	public static void printParams(OutputStream os) throws IOException {
		
		ParamRegistration pr = new ParamRegistration();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		
		writer.write("<div class='robostroke-params'>\n" +
				"<table class='robostroke-param-table' border='1'>\n" +
				"<tr>" +
				"<th>Category</th><th>Name</th><th>Level</th><th>Default Value</th><th>Description</th>" +
				"</tr>\n");
		
		for (Parameter<?>[] paramGroup: pr.PARAMS) {
			
			int i = 0;
			for (Parameter<?> param: paramGroup) {
				writer.write("<tr class='robostroke-param' id='" + param.getId() + "'>\n");
			
				if (i++ == 0) {
					writer.write("<td class='robostroke-param-category' rowspan='" + paramGroup.length + "'>" + param.getCategory() + "</td>\n");
				}
				
				writer.write("<td class='robostroke-param-name'>" + param.getName() + "</td>\n" +
						"<td class='robostroke-param-level'>" + param.getLevel() + "</td>\n" +
						"<td class='robostroke-param-default'>" + param.getDefaultValue() + "</td>\n" +
								"<td class='robostroke-param-description'>" + param.getDescription() + "</td>\n");
				writer.write("</tr>\n");
			}			
		}
		
		writer.write("</table></div>");
		
		writer.flush();
	}
	
	static void installParams(ParameterService ps) {
		
		ParamRegistration pr = new ParamRegistration();
		
		for (Parameter<?>[] paramGroup: pr.PARAMS) {
			ps.registerParam(paramGroup);
		}
	}
	
	
	public static void main(String[] args) throws IOException {
		printParams(System.out);
	}
	
}
