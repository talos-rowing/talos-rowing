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

package org.nargila.robostroke.input;


/**
 * SensorDataInput interface.
 * This interface must be implemented 
 * in order to get real sensor data from the device.
 * @author tshalif
 *
 */
public interface SensorDataInput {
	/**
	 * stop data input
	 */
	public void stop();
	/**
	 * start data input
	 */
	public void start();
	/**
	 * set an error listener
	 * @param errorListener listener
	 */
	public void setErrorListener(ErrorListener errorListener);
	/**
	 * get the row GPS data source 
	 * @return sensor data source 
	 */
	public SensorDataSource getGPSDataSource();
	/**
	 * get the row accelerometer data source
	 * @return sensor data source 
	 */
	public SensorDataSource getAccelerometerDataSource();
	/**
	 * get the row orientation data source
	 * @return sensor data source 
	 */
	public SensorDataSource getOrientationDataSource();
	/**
	 * seek/skip input depending on positive/negative value
	 * @param velocityX usually a value from a 'fling' event on the X axis
	 */
	public void skipReplayTime(float velocityX);
	/**
	 * Pause event input
	 * @param pause true to pause false to continue
	 */
	public void setPaused(boolean pause);
}
