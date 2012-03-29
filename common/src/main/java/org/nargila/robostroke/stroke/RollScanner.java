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

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStrokeEventBus;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.input.DataIdx;
import org.nargila.robostroke.input.SensorDataFilter;
import org.nargila.robostroke.input.SensorDataInput;

/**
 * Sensor data pipeline component for detecting boat tilt.
 * StrokeTiltScanner instance is to be added as a stroke listener to {@link StrokePowerScanner} 
 * and as data sink to {@link SensorDataInput#getOrientationDataSource()}
 * 
 * @author tshalif
 *
 */
public class RollScanner extends SensorDataFilter implements BusEventListener {
	
	private static final float DEFAULT_TILT_DAMP_FACTOR = .01f;
	
	private final LowpassFilter tiltDamper = new LowpassFilter(DEFAULT_TILT_DAMP_FACTOR);

	private float[] tiltDamperValues = {0,0,0};

	private boolean tiltFrozen;
	
	private boolean insideStrokePower;
	
	private static class Roll {
		int sampleCount;
		float accummulatedRoll;
		float maxRoll;
		
		void add(float val) {
			sampleCount++;
			accummulatedRoll += val;
			
			if (Math.abs(val) > Math.abs(maxRoll)) {
				maxRoll = val;
			}
		}
		
		void reset() {
			sampleCount = 0;
			accummulatedRoll = 0;
			maxRoll = 0;
		}
		
		float[] get() {
			return new float[]{accummulatedRoll / sampleCount, maxRoll};
		}
	}
	
	private final Roll strokeRoll = new Roll();
	private final Roll recoveryRoll = new Roll();
	
	private final RoboStrokeEventBus bus;

	private boolean hadPower;	

	public RollScanner(RoboStrokeEventBus bus) {
		this.bus = bus;
		
		bus.addBusListener(this);
	}
	
	@Override
	protected Object filterData(long timestamp, Object value) {
		
		float[] values = (float[]) value;
		
		float[] filtered = new float[values.length];
		
		float unfilteredRoll = values[DataIdx.ORIENT_ROLL];
		
		System.arraycopy(values, 0, filtered, 0, values.length);
				
		float filteredRoll = unfilteredRoll - tiltDamperValues[DataIdx.ORIENT_ROLL];
		
		filtered[DataIdx.ORIENT_ROLL] = filteredRoll;
		
		if (insideStrokePower) {
			strokeRoll.add(filteredRoll);
		} else {
			recoveryRoll.add(filteredRoll);
		}
		
		if (!tiltFrozen) {
			tiltDamperValues  = tiltDamper.filter(values);
		}
		
		return filtered;
	}
	
	@Override
	public void onBusEvent(DataRecord event) {
		switch (event.type) {
		case STROKE_POWER_START:
			insideStrokePower = true;
			
			if (hadPower) {
				bus.fireEvent(DataRecord.Type.RECOVERY_ROLL, event.timestamp, recoveryRoll.get());
			}
			
			hadPower = false;
			recoveryRoll.reset();
			break;
		case STROKE_POWER_END:
			hadPower = (Float)event.data > 0;
			insideStrokePower = false;
			
			if (hadPower) {
				bus.fireEvent(DataRecord.Type.STROKE_ROLL, event.timestamp, strokeRoll.get());
			}
			
			strokeRoll.reset();
			break;
		case FREEZE_TILT:
			tiltFrozen = (Boolean)event.data;
			break;	
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		bus.removeBusListener(this);
		super.finalize();
	}
}