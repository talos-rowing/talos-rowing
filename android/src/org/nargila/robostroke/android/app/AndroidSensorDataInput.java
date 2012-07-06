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

package org.nargila.robostroke.android.app;

import org.nargila.robostroke.data.DataIdx;
import org.nargila.robostroke.data.SensorDataInputBase;
import org.nargila.robostroke.data.SensorDataSource;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

/**
 * Android sensor data input implementation
 * @author tshalif
 *
 */
public class AndroidSensorDataInput extends SensorDataInputBase {

	private final SensorDataThread sensorThread;
	private final GPSDataThread gpsThread;	
	
	public AndroidSensorDataInput(Activity owner) {
		sensorThread = new SensorDataThread(owner);
		gpsThread = new GPSDataThread(owner);		
	}
	
	@Override
	public void setPaused(boolean pause) {
	}

	@Override
	public void skipReplayTime(float velocityX) {
	}

	@Override
	public synchronized void start() {
		gpsThread.start();
		sensorThread.start();
	}

	@Override
	public synchronized void stop() {
		sensorThread.quit();
		gpsThread.quit();
	}

	private static class CompatHandlerThread extends HandlerThread {
		public CompatHandlerThread(String name) {
			super(name);
		}

		public boolean quit() {
			
			Looper looper = getLooper(); 
			
			if (null != looper) {
				looper.quit();
			}
			
			interrupt();
			
			return true;
		}				
	}
	/**
	 * Accelerometer/Orientation sensor data handling thread
	 */
	private class SensorDataThread extends CompatHandlerThread implements SensorEventListener  {
		private Handler handler;
		private SensorManager sensorManager;
		private Sensor accelSensor;
		private Sensor orientSensor;
		private final Activity owner;

		public SensorDataThread(Activity owner) {
			super("SensorDataThread");
			this.owner = owner;
			setDaemon(true);
		}
		
		@Override
		public boolean quit() {
			if (sensorManager != null) {
				sensorManager.unregisterListener(this, accelSensor);
				sensorManager.unregisterListener(this, orientSensor);
			}
		
			return super.quit();
		}
		
		
		@Override
		protected void onLooperPrepared() {
			handler = new Handler(getLooper());
			sensorManager = (SensorManager) this.owner.getSystemService(Context.SENSOR_SERVICE);
			accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			orientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

			sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
			sensorManager.registerListener(this, orientSensor, SensorManager.SENSOR_DELAY_FASTEST, handler);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			final long timestamp = SystemClock.uptimeMillis() * 1000000;
			final float[] values = event.values;
			SensorDataSource dataSource;
			
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				dataSource = accelerometerDataSource;
				break;
			case Sensor.TYPE_ORIENTATION:
				dataSource = orientationDataSource;
				break;
				default:
					throw new RuntimeException("HDIGH!");
			}
			
			dataSource.pushData(timestamp, values);
		}
	}
	
	/**
	 * GPS sensor data handling thread 
	 */
	private class GPSDataThread extends CompatHandlerThread implements LocationListener {
		private final Activity owner;
		private LocationManager locationManager;
		private Looper looper;
		private final float gpsMinDistance = 0;
		public float gpsMinTime = 1000;
		
		public GPSDataThread(Activity owner) {
			super("GPSDataThread");
			this.owner = owner;
			setDaemon(true);
		}

		@Override
		protected void onLooperPrepared() {
			looper = getLooper();			
			locationManager = (LocationManager) this.owner.getSystemService(Context.LOCATION_SERVICE);
			
			resetListener();
		}

		private void resetListener() {
			locationManager.removeUpdates(this);		
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) gpsMinTime, gpsMinDistance, this, looper);
		}

		@Override
		public boolean quit() {
			if (locationManager != null) {
				locationManager.removeUpdates(this);
			}
			
			return super.quit();
		}

		@Override
		public void onLocationChanged(Location location) {
			final long timestamp = location.getTime();
			
			double[] values = new double[DataIdx.GPS_ITEM_COUNT_];
			
			values[DataIdx.GPS_LAT] = location.getLatitude();
			values[DataIdx.GPS_LONG] = location.getLongitude();
			values[DataIdx.GPS_ALT] = (location.hasAltitude() ? location.getAltitude() : -1);
			values[DataIdx.GPS_SPEED] = (location.hasSpeed() ? location.getSpeed() : -1);
			values[DataIdx.GPS_BEARING] = (location.hasBearing() ? location.getBearing() : -1);
			values[DataIdx.GPS_ACCURACY] = (location.hasAccuracy() ? location.getAccuracy() : -1);

			gpsDataSource.pushData(timestamp, values);
			
		}

		@Override
		public void onProviderDisabled(String arg0) {
		}

		@Override
		public void onProviderEnabled(String arg0) {
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		}
	}
}
