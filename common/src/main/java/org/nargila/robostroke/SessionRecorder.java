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

import org.nargila.robostroke.BusEvent.Type;
import org.nargila.robostroke.input.ErrorListener;
import org.nargila.robostroke.input.InputType;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.input.SensorDataSource;

class SessionRecorder implements BusEventListener, SessionRecorderConstants {
	private ErrorListener errorListener;
	private BufferedWriter logger;
	private final RoboStroke roboStroke;
	
	private final LinkedList<SensorDataSourceBinder> sourceBinderList = new LinkedList<SensorDataSourceBinder>();
	
	private class SensorDataSourceBinder implements SensorDataSink {
		private final SensorDataSource src;
		private final InputType type;
		
		SensorDataSourceBinder(SensorDataSource src, InputType type) {
			this.src = src;
			this.type = type;
			src.addSensorDataSink(this, true);
		}
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			logData(type, timestamp, value);
		}
		
		void unbind() {
			src.removeSensorDataSink(this);
		}
	}
		
	public SessionRecorder(RoboStroke roboStroke) {
		this.roboStroke = roboStroke;
	}
	
	private void connect() {
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getAccelerometerDataSource(), InputType.ACCEL));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getOrientationDataSource(), InputType.ORIENT));
		sourceBinderList.add(new SensorDataSourceBinder(roboStroke.getDataInput().getGPSDataSource(), InputType.GPS));
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
				
				logEvent(new BusEvent(Type.LOGFILE_VERSION, -1, LOGFILE_VERSION));

				connect();
			}
	}
	
	private synchronized void logData(InputType type, long timestamp, Object value) {
		if (logger != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(System.currentTimeMillis()).append(" ")
			.append(type.toString()).append(" ")
			.append(timestamp);

			switch (type) {
			case ACCEL:
			case ORIENT: {
				float[] values = (float[]) value;
				for (float f: values) {
					sb.append(" ").append(f);
				}
				break;
			}
			case GPS: {
				double[] values = (double[]) value;
				for (double f: values) {
					sb.append(" ").append(f);
				}
				break;
			}
			default:
				throw new RuntimeException("HDIGH!");
			}

			try {
				logger.write(sb.toString());
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
	public synchronized void onBusEvent(BusEvent event) {
		if (logger != null) {
			logEvent(event);
		}		
	}

	private void logEvent(BusEvent event) {
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
