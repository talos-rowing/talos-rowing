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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.input.DataRecord.Type;
import org.nargila.robostroke.input.ErrorListener;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.input.SensorDataSource;

class SessionRecorder implements BusEventListener, SessionRecorderConstants {
	private ErrorListener errorListener;
	private BufferedWriter logger;
	private final RoboStroke roboStroke;
	
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
			logData(DataRecord.create(type, timestamp, data));
		}
		
		void unbind() {
			src.removeSensorDataSink(this);
		}
	}
		
	public SessionRecorder(RoboStroke roboStroke) {
		this.roboStroke = roboStroke;
	}
	
	private void connect() {
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getAccelerometerDataSource(), DataRecord.Type.ACCEL));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getOrientationDataSource(), DataRecord.Type.ORIENT));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getGPSDataSource(), DataRecord.Type.GPS));
		roboStroke.getBus().addBusListener(this);
	}
	
	private void disconnect() {
		for (SensorDataSourceBinder binder: sourceBinderList) {
			binder.unbind();
		}
				
		sourceBinderList.clear();
		
		roboStroke.getBus().removeBusListener(this);
	}
	
	synchronized void setDataLogger(File file) throws IOException {
			if (logger != null) {
				disconnect();
				logger.close();
				logger = null;
			}

			if (file != null) {
				logger = new BufferedWriter(new FileWriter(file));
				
				logEvent(new DataRecord(Type.LOGFILE_VERSION, -1, LOGFILE_VERSION));

				connect();
			}
	}
	
	private synchronized void logData(DataRecord record) {
		if (logger != null) {
			try {
				logger.write("" + System.currentTimeMillis() + " " + record.type + " " + record.timestamp + " ");
				logger.write(record.dataToString());
				logger.write(END_OF_RECORD + "\n");
			} catch (IOException e) {
				if (errorListener != null) {
					errorListener.onError(e);
				}
			}
		}
	}

	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	@Override
	public synchronized void onBusEvent(DataRecord event) {
		if (logger != null) {
			logEvent(event);
		}		
	}

	private void logEvent(DataRecord event) {
		StringBuffer sb = new StringBuffer();
		sb.append(System.currentTimeMillis()).append(" ")
		.append("EVENT ")
		.append(event);

		try {
			logger.write(sb.toString());
			logger.write(END_OF_RECORD + "\n");
			
			if (event.type == Type.CRASH_STACK) {
				logger.flush();
			}
		} catch (IOException e) {
			if (errorListener != null) {
				errorListener.onError(e);
			}
		}
	}
}
