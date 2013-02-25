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

package org.nargila.robostroke.way;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.data.DataIdx;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.nargila.robostroke.param.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GPSDataFilter implements SensorDataSink, ParameterListenerOwner {

	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private double accumulatedDistance = 0;

	private static class LocationData {
		long timestamp;
		double[] values;
		
		LocationData(long timestamp, double[] values) {
			this.timestamp = timestamp;
			this.values = values;
		}
	}
	
	private final LowpassFilter speedChangeDamperFilter;

	private float minDistance;
	private float maxSpeed;
	
	private LocationData lastLocation;
	private final DistanceResolver distanceResolver;
	
	private final RoboStrokeEventBus bus;

	/**
	 * force calculation of distance/speed on next Location, even if distance diff smaller than preferences
	 */
	private boolean immediateDistanceRequested;

	private boolean splitRowingOn;

	private LocationData bookMarkedLocation;

	private LocationData lastSensorDataLocation;
	
	private RoboStroke owner;	

	private final ParameterService params;

	private boolean straightLineModeOn;

	protected float travelDistance;

	protected LocationData lastStrokeLocation;

	public GPSDataFilter(RoboStroke owner, final DistanceResolver distanceResolver) {
		this.owner = owner;
		this.params = owner.getParameters();
		this.bus = owner.getBus();
		
		this.distanceResolver = distanceResolver;
		
		speedChangeDamperFilter = new LowpassFilter((Float)params.getValue(ParamKeys.PARAM_GPS_SPEED_CHANGE_DAMPER.getId()));
		
		straightLineModeOn = (Boolean)params.getValue(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE.getId());
		
		minDistance = (Integer)params.getValue(ParamKeys.PARAM_GPS_MIN_DISTANCE.getId());
		
		maxSpeed = (Float)params.getValue(ParamKeys.PARAM_GPS_DATA_FILTER_MAX_SPEED.getId());
		
		params.addListeners(this);
		
		bus.addBusListener(new BusEventListener() {
			
			@Override
			public void onBusEvent(DataRecord event) {
				switch (event.type) {
				case ROWING_COUNT: // means DROP_BELOW_ZERO with a valid stroke amplitude - see RowingDetector
					if (splitRowingOn && bookMarkedLocation != null) {
						if (straightLineModeOn) {
							travelDistance = distanceResolver.calcDistance(bookMarkedLocation.values, lastSensorDataLocation.values);
						} else {
							travelDistance += distanceResolver.calcDistance(lastStrokeLocation.values, lastSensorDataLocation.values);
							
							lastStrokeLocation = lastSensorDataLocation;
						}

						long travelTime = lastSensorDataLocation.timestamp - bookMarkedLocation.timestamp;

						bus.fireEvent(Type.BOOKMARKED_DISTANCE, event.timestamp, travelTime, travelDistance);
					}
					break;
				case ROWING_START:
					immediateDistanceRequested = true;
					splitRowingOn = true;
					travelDistance = 0;
					bookMarkedLocation = lastStrokeLocation = lastSensorDataLocation;
					
					break;
				case ROWING_STOP:
					splitRowingOn = false;
					bookMarkedLocation = null;
					break;
				}
				
			}
		});
	}
	
	public static int calcMilisecondsPer500m(float speed) {
		
		double seconds = 0;
		
		if (speed > 0) {
			seconds = 500 / speed;	
				
			if (seconds > 1000) {
				seconds = 0;
			}
		}	
		
		return (int)seconds * 1000;
	}
	
	private int calcSpeed(float speed) {
		
		if (speed > maxSpeed) {
			return -1;
		}
		
		float[] values = {speed};
		speed = speedChangeDamperFilter.filter(values)[0];
		
		return calcMilisecondsPer500m(speed);
	}
	
	private int calcSpeed(float distance, long timeDiff) { 
		float speed = distance / timeDiff * 1000; // meters/second
		return calcSpeed(speed);
	}
	
	@Override
	public void onSensorData(long timestamp, Object value) {
		double[] values = (double[]) value;
		
		
		bus.fireEvent(DataRecord.Type.GPS, timestamp, value);

		if (lastSensorDataLocation == null) {
			lastSensorDataLocation = lastLocation = new LocationData(timestamp, values);
		} else {

			lastSensorDataLocation = new LocationData(timestamp, values);
			
			if (splitRowingOn && bookMarkedLocation == null) {
				bookMarkedLocation = lastStrokeLocation = lastSensorDataLocation;
			}

			final float distance = distanceResolver.calcDistance(lastLocation.values, values);
			
			if (distance > minDistance || immediateDistanceRequested) {
				final int finalSpeed = calcSpeed(distance, timestamp - lastLocation.timestamp);

				
				if (finalSpeed != -1) {
					
					accumulatedDistance += distance;
					
					double accuracy = values[DataIdx.GPS_ACCURACY];
					
					bus.fireEvent(DataRecord.Type.WAY, timestamp, new double[]{distance, finalSpeed, accuracy});

					lastLocation = new LocationData(timestamp, values);

					immediateDistanceRequested = false;

				}
			}
			

		}
		
		if (!owner.isSeekableDataInput()) { // ACCUM_DISTANCE is replayed when read from recorded file
			bus.fireEvent(DataRecord.Type.ACCUM_DISTANCE, accumulatedDistance);
		}
		
	}
	
	private final ParameterListenerRegistration[] listenerRegistrations = {
			new ParameterListenerRegistration(ParamKeys.PARAM_GPS_SPEED_CHANGE_DAMPER.getId(), new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					float value = (Float)param.getValue();
					logger.info("setting speedChangeDamperFilter to {}", value);
					speedChangeDamperFilter.setFilteringFactor(value);
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_GPS_MIN_DISTANCE.getId(), new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					float value = (Integer)param.getValue();
					logger.info("setting minDistance to {}", value);
					minDistance = value;
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_GPS_DATA_FILTER_MAX_SPEED.getId(), new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter param) {
					float value = (Float)param.getValue();
					logger.info("setting maxSpeed to {}", value);
					maxSpeed = value;
				}
			}),
			new ParameterListenerRegistration(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE.getId(), new ParameterChangeListener() {

				@Override
				public void onParameterChanged(Parameter param) {
					straightLineModeOn = (Boolean) param.getValue();
				}
			})
	};

	@Override
	public ParameterListenerRegistration[] getListenerRegistrations() {
		return listenerRegistrations;
	}
	
	@Override
	protected void finalize() throws Throwable {
		params.removeListeners(this);
		super.finalize();
	}
}
