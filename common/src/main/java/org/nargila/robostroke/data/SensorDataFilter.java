/*
 * Copyright (c) 2024 Tal Shalif
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

package org.nargila.robostroke.data;

/**
 * sensor data filter/processing pipeline component base class.
 * @author tshalif
 *
 */
public abstract class SensorDataFilter extends SensorDataSource implements SensorDataSink {

	/**
	 * constructor without initial sink.
	 */
	public SensorDataFilter() {}
	
	/**
	 * constructor with initial sink
	 * @param sink
	 */
	public SensorDataFilter(SensorDataSink sink) {
		super(sink);
	}

	@Override
	public void onSensorData(long timestamp, Object value) {
		if (null != (value = filterData(timestamp, value))) {
			pushData(timestamp, value);
		}		
	}

	/**
	 * filter/process sensor event data.
	 * this method will scan/process the input data and return
	 * the processed values to be passed to registered sinks. If
	 * data is to be discarded (i.e. not delivered to sinks) it should
	 * return null
	 * @param timestamp event timestamp
	 * @param value sensor data
	 * @return filtered values or null when event data is not to be passed on to sinks 
	 */
	protected abstract Object filterData(long timestamp, Object value);
}
