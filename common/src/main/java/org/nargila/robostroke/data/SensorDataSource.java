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

package org.nargila.robostroke.data;

import java.util.LinkedList;

/**
 * a handy class to ease building of sensor data processing pipelines.
 * <code>SensorDataSource</code> makes itself useful by managing <code>SensorDataSink</code>
 * lists and providing an easy event-push mechanism with {@link #pushData(long, Object)}
 * @author tshalif
 *
 */
public class SensorDataSource {
	
	/**
	 * Holder of an event sink when there is only one
	 */
	private SensorDataSink sink;
	/**
	 * holder of event sinks when there are more then one
	 */
	private LinkedList<SensorDataSink>	sinkList;
	
	/**
	 * construct with no initial event sinks
	 */
	public SensorDataSource() {}
	
	/**
	 * construct with an initial event sink
	 * @param sink event sink
	 */
	public SensorDataSource(SensorDataSink sink) {
		addSensorDataSink(sink);
	}
	
	/**
	 * add an event sink
	 * @param sink event sink
	 */
	public void addSensorDataSink(SensorDataSink sink) {
		addSensorDataSink(sink, false);
	}
	/**
	 * add an event sink, optionally prepend to head of sink list (to ensure getting of unfiltered raw data)
	 * @param sink event sink
	 * @param prepend prepend to head of sink list insteed of appending
	 */
	public synchronized void addSensorDataSink(SensorDataSink sink, boolean prepend) {
		if (this.sink == null) {
			this.sink = sink;		
		} else {
			if (this.sink == sink) {
				throw new IllegalArgumentException("sink is already registered");
			}
			
			if (sinkList == null) {
				sinkList = new LinkedList<SensorDataSink>();
				sinkList.add(this.sink);				
			} else if (sinkList.contains(sink)) {
				throw new IllegalArgumentException("sink is already registered");
			}
			
			if (prepend) {
				sinkList.addFirst(sink);
			} else {
				sinkList.add(sink);
			}
		}
	}
	
	/**
	 * remove event sink
	 * @param sink event sink
	 */
	public synchronized void removeSensorDataSink(SensorDataSink sink) {
		boolean removed = false;
		
		if (sinkList != null) {
			removed = sinkList.remove(sink);
			this.sink = sinkList.getFirst();
			
			if (sinkList.size() == 1) {
				sinkList = null;
			}
		} else {
			if (sink == this.sink) {
				this.sink = null;
				removed = true;
			}
		}
		
		if (!removed) {
			throw new IllegalArgumentException("trying to remove non-existing sink");
		}
	}
	
	/**
	 * push event data to all registered sinks
	 * @param timestamp event timestamp
	 * @param value sensor data
	 */
	public synchronized void pushData(long timestamp, Object value) {
		if (sinkList != null) { // more then one sink inside sinkList
			for (SensorDataSink sink: sinkList) {
				sink.onSensorData(timestamp, value);				
			}
		} else if (this.sink != null) { // only one sink in this.sink
			this.sink.onSensorData(timestamp, value);		
		}
	}
}
