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

import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.acceleration.AccelerationFilter;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.nargila.robostroke.param.ParameterService;

/**
 * Sensor data pipeline component for detecting stroke rate.
 * Stroke rate detection is done by listening to uni-directional
 * acceleration data (i.e. output from {@link AccelerationFilter}) and 
 * applying low-pass smoothing filter and stroke-strength change damping filter. 
 * Much of the dynamic stroke power/rate detection is implemented in {@link HalfSinoidDetector}
 * 
 * @author tshalif
 *
 */
public class StrokeRateScanner extends StrokeScannerBase implements ParameterListenerOwner {

	private long lastStrokeTimestamp;

	private final RoboStrokeEventBus bus;
	
	private final ParameterListenerRegistration[] listenerRegistrations = {
			new ParameterListenerRegistration(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR, new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					setAmplitudeFiltering((Float)param.getValue());					
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_STROKE_RATE_MIN_AMPLITUDE, new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					setMinAmplitude((Float)param.getValue());					
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_DAMPER_FACTOR, new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					setAmplitudeChangeDamperFactor((Float)param.getValue());					
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_CHANGE_ACCEPT_FACTOR, new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					setAmplitudeChangeAcceptFactor((Float)param.getValue());					
				}
			})
	};

	private final ParameterService params;
	
	public StrokeRateScanner(RoboStroke owner) {
		super(owner.getBus(), (Float) owner.getParameters().getValue(ParamKeys.PARAM_STROKE_RATE_AMPLITUDE_FILTER_FACTOR));
		
		this.params = owner.getParameters();
		this.bus = owner.getBus();
		
		params.addListeners(this);
	}

	@Override
	public ParameterListenerRegistration[] getListenerRegistrations() {
		return listenerRegistrations;
	}

	@Override
	protected void onDecelerationTreshold(long timestamp, float amplitude) {
		bus.fireEvent(DataRecord.Type.STROKE_DECELERATION_TRESHOLD, timestamp, amplitude);		
	}

	@Override
	protected void onAccelerationTreshold(long timestamp, float amplitude)  {
		bus.fireEvent(DataRecord.Type.STROKE_ACCELERATION_TRESHOLD, timestamp, amplitude);		
	}
	
	@Override
	protected void onDropBelow(long timestamp, float maxVal)  {
		bus.fireEvent(DataRecord.Type.STROKE_DROP_BELOW_ZERO, timestamp, maxVal);		
	}
	
	/**
	 * setup and notify a stroke event
	 * @param timestamp stroke timestamp
	 */
	private void registerStroke(long timestamp) {
		if (lastStrokeTimestamp != 0) {
			long spm = 0;
			
			if (timestamp > lastStrokeTimestamp) { // prevent negative stroke rates due to session replay back-skip
				long msDiff = (timestamp - lastStrokeTimestamp) / 1000000;

				spm = 60 * 1000 / msDiff;
			}
			
			bus.fireEvent(DataRecord.Type.STROKE_RATE, timestamp, (int)spm);
			
		}
		
		lastStrokeTimestamp = timestamp;

	}
	
	@Override
	protected void onRiseAbove(long timestamp, float minVal) {
		bus.fireEvent(DataRecord.Type.STROKE_RISE_ABOVE_ZERO, timestamp, minVal);
		registerStroke(timestamp);
	}
	
	@Override
	protected void finalize() throws Throwable {
		params.removeListeners(this);
		super.finalize();
	}
}