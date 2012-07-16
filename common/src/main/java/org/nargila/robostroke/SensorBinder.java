/*
 * Copyright (c) 2012 Tal Shalif
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

import java.util.LinkedList;

import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.data.SensorDataSource;

public abstract class SensorBinder implements BusEventListener {

	protected final RoboStroke roboStroke;
	private final LinkedList<SensorDataSourceBinder> sourceBinderList = new LinkedList<SensorDataSourceBinder>();

	private class SensorDataSourceBinder implements SensorDataSink {
		private final SensorDataSource src;
		private final DataRecord.Type type;
		
		SensorDataSourceBinder(SensorDataSource src, DataRecord.Type type) {
			this.src = src;
			this.type = type;
			src.addSensorDataSink(this, true);
		}
		
		@Override
		public void onSensorData(long timestamp, Object data) {
			switch (type) {
			case ACCEL:
				data = ((float[])data).clone();
				break;
			}
			
			SensorBinder.this.onSensorData(DataRecord.create(type, timestamp, data));
		}
		
		void unbind() {
			src.removeSensorDataSink(this);
		}
	}
		
	public SensorBinder(RoboStroke roboStroke) {
		this.roboStroke = roboStroke;
	}

	protected synchronized void connect() {
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getAccelerometerDataSource(), DataRecord.Type.ACCEL));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getOrientationDataSource(), DataRecord.Type.ORIENT));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getGPSDataSource(), DataRecord.Type.GPS));
		roboStroke.getBus().addBusListener(this);
	}

	protected synchronized void disconnect() {
		for (SensorDataSourceBinder binder: sourceBinderList) {
			binder.unbind();
		}
				
		sourceBinderList.clear();

		roboStroke.getBus().removeBusListener(this);
	}

	protected abstract void onSensorData(DataRecord record);
}