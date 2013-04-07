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
package org.nargila.robostroke.stroke;

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.nargila.robostroke.param.ParameterService;

/**
 * Rowing detectror. Generates rowing activity start/stop events based on value of parameter PARAM_ROWING_MODE
 * @author tshalif
 *
 */
public class RowingDetector implements SensorDataSink, ParameterListenerOwner {

    private final ParameterListenerRegistration[] listenerRegistrations = {

        new ParameterListenerRegistration(ParamKeys.PARAM_ROWING_STOP_TIMEOUT.getId(), new ParameterChangeListener() {
                                
                @Override
                public void onParameterChanged(Parameter param) {
                    paramStopTimeout = TimeUnit.SECONDS.toNanos((Integer)param.getValue());                                 
                }
            }),
        new ParameterListenerRegistration(ParamKeys.PARAM_ROWING_RESTART_WAIT_TIME.getId(), new ParameterChangeListener() {
                                
                @Override
                public void onParameterChanged(Parameter param) {
                    paramRestartWaitTime = TimeUnit.SECONDS.toNanos((Integer)param.getValue());                                     
                }
            }),
        new ParameterListenerRegistration(ParamKeys.PARAM_ROWING_MODE.getId(), new ParameterChangeListener() {
                                
                @Override
                public void onParameterChanged(Parameter param) {
                    rowingMode = RowingSplitMode.valueOf((String) param.getValue());                        
                }
            }),
        new ParameterListenerRegistration(ParamKeys.PARAM_ROWING_START_AMPLITUDE_TRESHOLD.getId(), new ParameterChangeListener() {
                                
                @Override
                public void onParameterChanged(Parameter param) {
                    paramStartMinAmplitude = (Float)param.getValue();                                       
                }
            })
    };
                
    private RowingSplitMode rowingMode;
        
    private long paramStopTimeout;

    private float paramStartMinAmplitude;

    private long paramRestartWaitTime;

    private final RoboStrokeEventBus bus;
        
    private boolean rowing;

    private static class SplitData {
                                
        private int strokeCount;
                
        private long lastStrokeEndTimestamp;
                
        private long lastTimestamp;
                
        private long rowingStartTimestamp;

        private long rowingStoppedTimestamp;
                
        private Pair<Long /* timestamp */, Float /* distance */> lastDistance;
                
        private Pair<Long, Float> startDistance;


        void reset(long timestamp) {
            strokeCount = 0;
            rowingStartTimestamp = rowingStoppedTimestamp = lastStrokeEndTimestamp = timestamp;
            startDistance = lastDistance = null;
        }
    }
        

        
    private boolean hasAmplitude;

    private boolean manuallyTriggered;


    private final ParameterService params;

    private final SplitData splitData = new SplitData();
                
    public RowingDetector(RoboStroke roboStroke) {
                
                
        ParameterService params = roboStroke.getParameters();
                
        this.params = params;

        rowingMode = RowingSplitMode.valueOf((String) params.getValue(ParamKeys.PARAM_ROWING_MODE.getId()));
                
        paramStopTimeout = TimeUnit.SECONDS.toNanos((Integer) params.getValue(ParamKeys.PARAM_ROWING_STOP_TIMEOUT.getId()));

        paramStartMinAmplitude = (Float)params.getValue(ParamKeys.PARAM_ROWING_START_AMPLITUDE_TRESHOLD.getId());

        paramRestartWaitTime = TimeUnit.SECONDS.toNanos((Integer) params.getValue(ParamKeys.PARAM_ROWING_RESTART_WAIT_TIME.getId()));

        bus = roboStroke.getBus();
                
        bus.addBusListener(new BusEventListener() {
                        

                @Override
                public void onBusEvent(DataRecord event) {
                    switch (event.type) {
                    case STROKE_DROP_BELOW_ZERO:
                        if (rowing) {
                            if (hasAmplitude) {

                                long timestamp = event.timestamp;

                                long msDiff = (timestamp - splitData.lastStrokeEndTimestamp) / 1000000;                                                 

                                if (msDiff > 0) { // was: msDiff > 1000 - disallow stroke rate above 60
                                                                
                                    splitData.strokeCount++;
                                    bus.fireEvent(Type.ROWING_COUNT, timestamp, splitData.strokeCount);                                                     
                                    splitData.lastStrokeEndTimestamp = timestamp;
                                }
                            }                                                                                               
                        }
                                        
                        hasAmplitude = false;
                                        
                        break;
                                        
                    case ROWING_START_TRIGGERED:
                        manuallyTriggered = true;
                        break;
                    case BOOKMARKED_DISTANCE:
                                        
                        Object[] values = (Object[]) event.data;
                                        
                        splitData.lastDistance = Pair.create((Long)values[0], (Float)values[1]);

                        if (rowing && splitData.startDistance == null) {
                            splitData.startDistance = splitData.lastDistance;
                            bus.fireEvent(Type.ROWING_START_DISTANCE, event.timestamp, splitData.startDistance.first, splitData.startDistance.second);      
                        }

                        break;
                    }
                }
            });
                
        params.addListeners(this);
    }
        
    @Override
    public void onSensorData(long timestamp, Object value) {
                
        backtimeProtection(timestamp);

        float[] values = (float[]) value;
        float amplitude = values[0];
        final boolean validAmplitude = amplitude > paramStartMinAmplitude;

        hasAmplitude = hasAmplitude || validAmplitude;
                
        if (!rowing) {
                        
            boolean enableNextStart = true;
            boolean forceStart = false;
                        
            switch (rowingMode) {
            case CONTINUOUS:
                forceStart = true;
                break;
            case MANUAL:
                forceStart = manuallyTriggered;
                enableNextStart = false;
                break;
            case SEMI_AUTO:
                enableNextStart = manuallyTriggered;
                break;
            case AUTO:
                enableNextStart = true;
                break;                                  
            }
                        
            if (forceStart || 
                (enableNextStart && validAmplitude && 
                 (timestamp > (splitData.rowingStoppedTimestamp + paramRestartWaitTime)))) {
                startRowing(timestamp);
            }                       
        } else {
                                
            boolean enableStop = true;
            boolean forceStop = false;
                        
            switch (rowingMode) {
            case MANUAL:
                forceStop = manuallyTriggered;
                /* no break; */
            case CONTINUOUS:
                enableStop = false;
                break;
            }

            if (forceStop || (enableStop && timestamp > (splitData.lastStrokeEndTimestamp + paramStopTimeout))) {
                stopRowing(timestamp);
            }
        }
                
        splitData.lastTimestamp = timestamp;
    }

    private void stopRowing(long timestamp) {
        rowing = hasAmplitude = false;
                
        float distance = splitData.lastDistance != null ? splitData.lastDistance.second : 0.0f;
        long travelTime = splitData.lastDistance != null ? splitData.lastDistance.first : 0;
                
        long stopTimestamp = (rowingMode == RowingSplitMode.MANUAL) ? timestamp : splitData.lastStrokeEndTimestamp;             
                
        /* stopTimestamp, distance, splitTime, travelTime, strokeCount */
        bus.fireEvent(Type.ROWING_STOP, timestamp, stopTimestamp, distance, (stopTimestamp -  splitData.rowingStartTimestamp), travelTime, splitData.strokeCount);
                
        splitData.rowingStoppedTimestamp = timestamp;
                
        manuallyTriggered = false;
    }

    private void startRowing(final long timestamp) {
        rowing = true;
                
        splitData.reset(timestamp);
                
        bus.fireEvent(Type.ROWING_START, timestamp, timestamp);
                
        manuallyTriggered = false;
    }

    private void backtimeProtection(long timestamp) {
        if (timestamp < splitData.lastTimestamp) {
            splitData.rowingStoppedTimestamp = splitData.lastStrokeEndTimestamp = timestamp;
        }
    }

    public ParameterListenerRegistration[] getListenerRegistrations() {
        return listenerRegistrations;
    }
    @Override
    protected void finalize() throws Throwable {
        params.removeListeners(this);
        super.finalize();
    }       
}
