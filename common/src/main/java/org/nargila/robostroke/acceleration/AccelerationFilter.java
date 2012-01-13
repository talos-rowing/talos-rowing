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

import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.input.SensorDataFilter;

/**
 * Joins gravity-filtered 3 axis sensor data into acceleration amplituded value
 * @author tshalif
 *
 */
public class AccelerationFilter extends SensorDataFilter {
	
	private final LowpassFilter ZERO_SHIFT_HACK_FILTER = new LowpassFilter(0.005f);
	
	public AccelerationFilter( ) {}
	
	@Override
	protected Object filterData(long timestamp, Object value) {
		float[] values = (float[]) value;
		return new float[] {
				calcAcceleration(values)
		};
	}
	
	/**
	 * calculate horizontal acceleration amplitude according to device pitch
	 * @param values accelerometer sensor data
	 * @return acceleration amplitude
	 */
	private float calcAcceleration(final float[] values) {
		final double ay = values[1];
		final double az = values[2];
		final int accelDir = az < 0 ? -1 : 1;
		float a = (float)(accelDir * Math.sqrt(ay*ay + az*az));
		final float ZERO_SHIFT_HACK = ZERO_SHIFT_HACK_FILTER.filter(new float[]{a})[0];//.075f; // TODO: calculation of a average is -.1f for some reason.. 
		return a - ZERO_SHIFT_HACK;
	}
}

