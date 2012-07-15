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

package org.nargila.robostroke.acceleration;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.data.DataIdx;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SensorDataFilter;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.nargila.robostroke.param.ParameterService;

/**
 * filers-out gravity from acceleration values.
 * An instance of this class is placed immediately after the accelerometer
 * data source in the data processing pipeline. The adjustment of the acceleration 
 * data is based on low-pass filtered values from the orientation sensor. 
 * @author tshalif
 *
 */
public class GravityFilter extends SensorDataFilter implements ParameterListenerOwner, BusEventListener {


	private static final float G = 9.8f;

	private final LowpassFilter orientationDamper;
	
	
	private float[] orientationVals;
	private double zGravity = G;
	private double yGravity = G;
	private boolean orientationFrozen;
	
	
	private final ParameterListenerRegistration[] listenerRegistrations = {
			new ParameterListenerRegistration(ParamKeys.PARAM_SENSOR_ORIENTATION_DAMP_FACTOR.getId(), new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					orientationDamper.setFilteringFactor((Float)param.getValue());
				}
			})
	};

	private final ParameterService params;

	private final RoboStrokeEventBus bus;
	
	public GravityFilter(RoboStroke owner, SensorDataSink sink) {
		super(sink);
		
		
		this.params = owner.getParameters();
		
		orientationDamper = new LowpassFilter((Float)params.getValue(ParamKeys.PARAM_SENSOR_ORIENTATION_DAMP_FACTOR.getId()));
		
		this.bus = owner.getBus();
		
		bus.addBusListener(this);
		
		params.addListeners(this);
	}
	
	/**
	 * return data sink for orientation sensor data
	 * @return orientation sensor data sink
	 */
	public SensorDataSink getOrientationDataSink() {
		return new SensorDataSink() {
			
			@Override
			public void onSensorData(long timestamp, Object value) {
				updateOrientation((float[])value);
			}
		};
	}
	
	@Override
	protected Object filterData(long timestamp, Object value) {
		float[] values = (float[]) value;
		// values[DataIdx.ACCEL_X] -= xGravity // TODO
		values[DataIdx.ACCEL_Y] -= yGravity;
		values[DataIdx.ACCEL_Z] -= zGravity;
		return value;
	}
	
	/**
	 * apply damping filter and update device orientation values
	 * @param values orientation sensor values
	 */
	private void updateOrientation(float[] values) {
		if (!orientationFrozen) {
			orientationVals = orientationDamper.filter(values);
			final float pitch = orientationVals[DataIdx.ORIENT_PITCH];
			final double zRadians = Math.toRadians(pitch);
			final double yRadians = Math.toRadians(pitch);
			zGravity = Math.abs(Math.cos(zRadians) * G);
			yGravity = Math.abs(Math.sin(yRadians) * G);
		}
	}


	
	@Override
	public ParameterListenerRegistration[] getListenerRegistrations() {
		return listenerRegistrations;
	}

	@Override
	public void onBusEvent(DataRecord event) {
		switch (event.type) {
		case FREEZE_TILT:
			orientationFrozen = (Boolean)event.data;
			break;
		}		
	}
	
	@Override
	protected void finalize() throws Throwable {
		params.removeListeners(this);
		bus.removeBusListener(this);
		super.finalize();
	}
}
