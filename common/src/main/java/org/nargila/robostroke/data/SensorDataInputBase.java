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

/**
 * Base class to ease implementation of SensorDataInput 
 * @author tshalif
 *
 */
public abstract class SensorDataInputBase implements SensorDataInput {

	protected ErrorListener errorListener;
	protected SensorDataSource gpsDataSource = new SensorDataSource();
	protected SensorDataSource accelerometerDataSource = new SensorDataSource();
	protected SensorDataSource orientationDataSource = new SensorDataSource();

	public SensorDataInputBase() {
		super();
	}

	@Override
	public SensorDataSource getAccelerometerDataSource() {
		return accelerometerDataSource;
	}

	@Override
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	
	}

	@Override
	public SensorDataSource getGPSDataSource() {
		return gpsDataSource;
	}

	@Override
	public SensorDataSource getOrientationDataSource() {
		return orientationDataSource;
	}

	@Override
	public boolean isLocalSensorInput() {
		return false;
	}
	
	@Override
	public boolean isSeekable() {
		return false;
	}
}