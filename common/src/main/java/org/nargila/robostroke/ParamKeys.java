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

import org.nargila.robostroke.data.SessionRecorderConstants;
import org.nargila.robostroke.param.ParameterInfo;
import org.nargila.robostroke.param.ParameterLevel;

public enum ParamKeys implements ParameterInfo {
    PARAM_GPS_SPEED_CHANGE_DAMPER("org.nargila.talos.rowing.gps.speedChangeDamper",                                         
                                  "Speed change damper",
                                  "Boat speed lowpass filter: any value lower then 1.0 will cause delayed updates of boat speed value",
                                  "GPS",
                                  ParameterLevel.ADVANCED, 1.0f),

        PARAM_GPS_MIN_DISTANCE("org.nargila.talos.rowing.gps.minDistance",                                                      
                               "min distance",
                               "Minimum distance boat must travel between each distance/speed calculation",
                               "GPS",
                               ParameterLevel.ADVANCED, 20) {

        @Override
            public Integer[] makeChoices() {
            return new Integer[] {
                10, 20, 30, 40, 50
            };
        }
    },
        
        PARAM_GPS_DATA_FILTER_MAX_SPEED("org.nargila.talos.rowing.gps.maxSpeed",                                        
                                        "max speed",
                                        "Maximum permited boat speed - useful for catching and throwing away erratic GPS readings",
                                        "GPS",
                                        ParameterLevel.ADVANCED, 6.5f),
                        
            PARAM_SENSOR_ORIENTATION_DAMP_FACTOR("org.nargila.talos.rowing.sensor.orientationDampFactor",
                                                 "orientation damper", 
                                                 "Lowpass filter used as stabalizer for the 'down' notion of the application", 
                                                 "Sensors", 
                                                 ParameterLevel.ADVANCED, 0.01f),
        
            PARAM_SENSOR_ORIENTATION_LANDSCAPE("org.nargila.talos.rowing.sensor.orientationLandscape",
                                               "session recording on", "", "{internal}", ParameterLevel.PRIVATE, false),
                                        
            PARAM_SENSOR_ORIENTATION_REVERSED("org.nargila.talos.rowing.sensor.reverseDeviceOrientation",
                                              "coax mode", 
                                              "indicates the device user is forward facing - such as a coax or trainer. Normally the rower sits with her back to the front of the boat", 
                                              "Sensors", ParameterLevel.BASIC, false),
                        
                        
            PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR("org.nargila.talos.rowing.stroke.rate.amplitudeFilterFactor",
                                                      "stroke amplitude filter", 
                                                      "Lowpass filter to 'smooth' the rowing sinusoids and establish single rise-above/drop-below zero points in each rowing cycle", 
                                                      "Stroke", 
                                                      ParameterLevel.ADVANCED, .05f),
        
                        
            PARAM_STROKE_RATE_AMPLITUDE_CHANGE_DAMPER_FACTOR("org.nargila.talos.rowing.stroke.rate.amplitudeChangeDamperFactor",
                                                             "stroke amplitude change filter", 
                                                             "Lowpass filter to stabalize stroke rate and protect against displaying erratic changes",
                                                             "Stroke", 
                                                             ParameterLevel.ADVANCED, .5f),
                                                             
            PARAM_STROKE_RATE_MIN_AMPLITUDE("org.nargila.talos.rowing.stroke.rate.minAmplitude",
                                                       		 "stroke min amplitude", 
                                                       		 "Minimum acceleration amplitude required during a rowing cycle in order for it to register", 
                                                       		 "Stroke", 
                                                       		 ParameterLevel.ADVANCED, .02f),
                        
            PARAM_STROKE_RATE_AMPLITUDE_CHANGE_ACCEPT_FACTOR("org.nargila.talos.rowing.stroke.rate.amplitudeChangeAcceptFactor",
                                                             "stroke amplitude change accept filter", 
                                                             "Rowing events are accepted based on exit amplitude - as long as it is - e.g. half the current avg/smoothed value",
                                                             "Stroke", 
                                                             ParameterLevel.ADVANCED, .5f),                  
        
           PARAM_STROKE_RATE_RATE_CHANGE_ACCEPT_FACTOR("org.nargila.talos.rowing.stroke.rate.rateChangeAcceptFactor",
                                                       		 "stroke rate change accept filter", 
                                                       		 "Filters out rowing events apearing too close to a previous one - e.g. to prevent a rowing event detected during recovery",
                                                       		 "Stroke", 
                                                       		 ParameterLevel.ADVANCED, .5f),                  

            PARAM_STROKE_POWER_AMPLITUDE_FILTER_FACTOR("org.nargila.talos.rowing.stroke.power.amplitudeFilterFactor",
                                                       "power filter",
                                                       "*FIXME*", // FIXME
                                                       "Stroke Power", 
                                                       ParameterLevel.ADVANCED, .5f),
                        
            PARAM_STROKE_POWER_MIN_POWER("org.nargila.talos.rowing.stroke.power.minPower",
                                         "power filter",
                                         "Minimum power (sum of acceleration points) required during a rowing cycle in order for it to register",
                                         "Stroke Power", 
                                         ParameterLevel.ADVANCED, 5f),
                        
            PARAM_ROWING_STOP_TIMEOUT("org.nargila.talos.rowing.stroke.detector.stopTimeout",
                                      "stop timeout", 
                                      "How many seconds since end of last stroke before finishing a rowing lap when in 'Autorow' mode", 
                                      "Autorow", 
                                      ParameterLevel.BASIC, 5),       
                        
            PARAM_ROWING_START_AMPLITUDE_TRESHOLD("org.nargila.talos.rowing.stroke.detector.minAmplitude",
                                                  "min amplitude", 
                                                  "Arbitrary min acceleration/deceleration amplitude value must be detected in order for the application to consider it the beginning of a starting stroke when in 'Autorow' mode", 
                                                  "Autorow", 
                                                  ParameterLevel.BASIC, 1.0f),                    

            PARAM_ROWING_RESTART_WAIT_TIME("org.nargila.talos.rowing.stroke.detector.restartWaitTime",
                                           "Auto Restart Wait Time", 
                                           "Number of seconds application must wait before allowed to auto start another rowing lap when in 'Autorow' mode", 
                                           "Autorow", 
                                           ParameterLevel.BASIC, 5),       
                        
            PARAM_ROWING_MODE("org.nargila.talos.rowing.stroke.detector.startMode",
                              "Split rowing start/stop mode", 
                              "Rowing lap display start mode - in AUTO mode application will automatically start and stop a split rowing session. In SEMI_AUTO mode the rower needs to press the device once to enable each auto-start. In MANUAL mode the rower must press to start and stop split rowing mode. In CONTINUOUS mode the application will immediately enter split mode like as if 'start' was pressed", 
                              "Autorow",
                              ParameterLevel.BASIC, 
                              "AUTO"),                        

            PARAM_ROWING_STRAIGHT_LINE_MODE("org.nargila.talos.rowing.stroke.detector.straightLineMode",
                                            "Rowing Straight Line Mode", 
                                            "Only calculate GPS distance between original start point and current location - if usefull at all, only to be used when the rowing course is completely streight", 
                                            "Autorow", 
                                            ParameterLevel.BASIC, false),
                        
            PARAM_SESSION_RECORDING_ON("org.nargila.talos.rowing.android.sessionRecordingOn",
                                       "session recording on", "", "{internal}", ParameterLevel.PRIVATE, false),
                                                                
            PARAM_SESSION_RECORDING_LEADER_ENABLE("org.nargila.talos.rowing.android.record.leader.enable",
                                                  "session recording sync mark", 
                                                  "Display a 'film leader' countdown dialog to assist synchronizing session recording with external audio/video media", 
                                                  "Session", ParameterLevel.BASIC, false),
    
            PARAM_SESSION_BROADCAST_ON("org.nargila.talos.rowing.session.broadcast.on",
                                       "session broadcast on", 
                                       "Activate broadcasting Talos sensor data, events to a remote Talos device", 
                                       "Session",
                                       ParameterLevel.BASIC, false),
                                        
            PARAM_SESSION_BROADCAST_PORT("org.nargila.talos.rowing.session.broadcast.port",
                                         "session broadcast port", 
                                         "Port number on which to braodcast/receive real-time device sensor data", 
                                         "Session", 
                                         ParameterLevel.BASIC, 
                                         SessionRecorderConstants.BROADCAST_PORT),
                
            PARAM_SESSION_BROADCAST_HOST("org.nargila.talos.rowing.android.session.remote.host",
                                         "session broadcast host", 
                                         "Remote device to receive real-time session data from", 
                                         "Session", 
                                         ParameterLevel.BASIC, SessionRecorderConstants.BROADCAST_HOST);
                        
        
        private final String id;
        private final String name;
        private final String description;
        private final String category;
        private final ParameterLevel level;
        private final Object defaultValue;
        

        private ParamKeys(String id, String name, String description,
                          String category, ParameterLevel level, Object defaultValue) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.level = level;
            this.defaultValue = defaultValue;
        }
        
        
        public String getId() {
            return id;
        }


        @Override
            public String getName() {
            return name;
        }


        @Override
            public String getDescription() {
            return description;
        }


        @Override
            public String getCategory() {
            return category;
        }


        @Override
            public ParameterLevel getLevel() {
            return level;
        }


        @Override
            public Object getDefaultValue() {
            return defaultValue;
        }


        @Override
            public Object[] makeChoices() {
            return null;
        }
        
        @Override
            public Object convertFromString(String val) {
                
            if (val == null || val.equals("null")) {
                return null;
            } else if (defaultValue instanceof String) {
                return val;
            } else if (defaultValue instanceof Boolean) {
                return new Boolean(val);
            } else if (defaultValue instanceof Integer) {
                return new Integer(val);
            } else if (defaultValue instanceof Long) {
                return new Long(val);
            } else if (defaultValue instanceof Float) {
                return new Float(val);
            } else if (defaultValue instanceof Double) {
                return new Double(val);
            } else {
                throw new IllegalStateException("don't know how to convert from string for parameter data of type " + defaultValue.getClass());
            }
        }
        
        @Override
            public String convertToString(Object val) {
            return String.valueOf(val);
        }
}
