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

import java.io.File;
import java.io.IOException;

import org.nargila.robostroke.acceleration.AccelerationFilter;
import org.nargila.robostroke.acceleration.GravityFilter;
import org.nargila.robostroke.input.ErrorListener;
import org.nargila.robostroke.input.FileSensorDataInput;
import org.nargila.robostroke.input.SensorDataFilter;
import org.nargila.robostroke.input.SensorDataInput;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.input.SensorDataSource;
import org.nargila.robostroke.param.ParameterService;
import org.nargila.robostroke.stroke.RollScanner;
import org.nargila.robostroke.stroke.RowingDetector;
import org.nargila.robostroke.stroke.StrokePowerScanner;
import org.nargila.robostroke.stroke.StrokeRateScanner;
import org.nargila.robostroke.way.DistanceResolver;
import org.nargila.robostroke.way.GPSDataFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RoboStroke engine initializer.
 * This is a handy class for initializing and connecting the various data input
 * filters and processors. A user of this class will usually set event listeners
 * by calling {@link StrokeRateScanner#setStrokeListener(org.nargila.robostroke.stroke.StrokeRateListener) strokeRateScanner.setStrokeRateListener}
 * and {@link GPSDataFilter#setWayListener(org.nargila.robostroke.way.WayListener) gpsFilter.setWayListener}. The client can also register for
 * raw Sensor data events by attaching a {@link SensorDataSink} to any {@link SensorDataSource} or {@link SensorDataFilter} object. 
 *
 */
public class RoboStroke {
	
	private static final Logger logger = LoggerFactory.getLogger(RoboStroke.class);
	
	/**
	 * wraps the real SensorDataInput and does event recording
	 */
	private SensorDataInput dataInput;
	
	/**
	 * filters-out gravity from acceleration data 
	 */
	private GravityFilter gravityFilter;
	
	/**
	 * scans acceleration event to detect stroke-rate
	 */
	private StrokeRateScanner strokeRateScanner;
	
	/**
	 * detects and notify ROWING_START and ROWING_STOP
	 */
	
	private RowingDetector rowingDetector;
	
	/**
	 * scans acceleration event to detect stroke-power
	 */
	private StrokePowerScanner strokePowerScanner;	

	/**
	 * scans orientation and stroke events to detect boat-roll
	 */
	private RollScanner rollScanner;	

	/**
	 * combines gravity-filtered acceleration forces to uni-directional acceleration/deceleration data
	 */
	private SensorDataFilter accelerationFilter;

	/**
	 * processes GPS/Location sensor data for determining stroking distance and speed 
	 */
	private GPSDataFilter gpsFilter;

	/**
	 * error listener
	 */
	private ErrorListener errorListener;

	/**
	 * Singleton event bus instance
	 */
	private final RoboStrokeEventBus bus = new RoboStrokeEventBus();	
	
	private final ParameterService parameters = new ParameterService(bus);
	

	/**
	 * data/event logger when recording is on
	 */
	private final SessionRecorder recorder = new SessionRecorder(this);
	
	/**
	 * constructor with the <code>DistanceResolver</code> implementation.
	 * @param distanceResolver a client provided implementation that can extract distance from location events 
	 */
	public RoboStroke(DistanceResolver distanceResolver) {
		
		ParamRegistration.installParams(parameters);
		
		initPipeline(distanceResolver);
	}

	/**
	 * get shared event bus instance
	 * @return global event bus
	 */
	public RoboStrokeEventBus getBus() {
		return bus;
	}

	/**
	 * sets the error listener of the event pipeline
	 * @param errorListener
	 */
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	/**
	 * initialize and connect the sensor data pipelines
	 * @param distanceResolver
	 */
	private void initPipeline(DistanceResolver distanceResolver) {
		accelerationFilter = new AccelerationFilter();
		gravityFilter = new GravityFilter(this, accelerationFilter);
		strokeRateScanner = new StrokeRateScanner(this);
		rowingDetector = new RowingDetector(this); 
		strokePowerScanner = new StrokePowerScanner(this, strokeRateScanner);
		accelerationFilter.addSensorDataSink(strokeRateScanner);
		accelerationFilter.addSensorDataSink(strokePowerScanner);
		accelerationFilter.addSensorDataSink(rowingDetector);
		gpsFilter = new GPSDataFilter(this, distanceResolver);
		rollScanner = new RollScanner(bus);
	}
	
	/**
	 * Set the sensor data input to replay from a file.
	 * look at the code in {@link LoggingSensorDataInput#logData} to see what
	 * the data file content should look like.
	 * @param file replay input file
	 * @throws IOException
	 */
	public void setFileInput(File file) throws IOException {
		setInput(new FileSensorDataInput(bus, file));
	}
	
	/**
	 * Set the sensor data input to a real device dependant implementation
	 * @param impl device input implementation
	 */
	public void setInput(SensorDataInput dataInput) {
		stop();
		
		if (dataInput != null) {
			this.dataInput = dataInput;
			connectPipeline();
			dataInput.start();
		}
	}

	/**
	 * Stop processing
	 */
	public void stop() {
		if (dataInput != null) {
			dataInput.setErrorListener(null);
			dataInput.stop();
		}		
	}
	
	/**
	 * get <code>SensorDataInput</code> implemention currently in use
	 * @return SensorDataInput implemention
	 */
	public SensorDataInput getDataInput() {
		return dataInput;
	}

	/**
	 * Get the gravity filter object.
	 * GravityFilter normalizes-out gravity from row acceleration data 
	 * @return
	 */
	public GravityFilter getGravityFilter() {
		return gravityFilter;
	}

	/**
	 * Get the stroke rate scanner object.
	 * <code>StrokeRateScanner</code> scans acceleration event to detect the stroke-rate
	 * @return StrokeRateScanner object
	 */
	public StrokeRateScanner getStrokeRateScanner() {
		return strokeRateScanner;
	}

	/**
	 * Get the stroke power scanner object.
	 * <code>StrokePowerScanner</code> scans acceleration event to detect the stroke-power
	 * @return StrokePowerScanner object
	 */
	public StrokePowerScanner getStrokePowerScanner() {
		return strokePowerScanner;
	}

	/**
	 * Get the acceleration combiner object.
	 * <code>AccelerationFilter</code> combines the gravity-filtered acceleration forces to uni-directional acceleration/deceleration data
	 * @return AccelerationFilter object
	 */
	public SensorDataFilter getAccelerationFilter() {
		return accelerationFilter;
	}

	/**
	 * Get GPS data processor.
	 * @return AccelerationFilter object
	 */
	public GPSDataFilter getGpsFilter() {
		return gpsFilter;
	}
	
	
	/**
	 * get roll scanner
	 * @return roll scanner
	 */
	public RollScanner getRollScanner() {
		return rollScanner;
	}

	/**
	 * connects a new DataInputSource to the sensor data pipelines
	 */
	private void connectPipeline() {
		dataInput.setErrorListener(errorListener);		
		dataInput.getOrientationDataSource().addSensorDataSink(gravityFilter.getOrientationDataSink());
		dataInput.getOrientationDataSource().addSensorDataSink(rollScanner);
		dataInput.getAccelerometerDataSource().addSensorDataSink(gravityFilter);	
		dataInput.getGPSDataSource().addSensorDataSink(gpsFilter);
	}

	public void setDataLogger(File logFile) throws IOException {
		recorder.setDataLogger(logFile);		
	}

	public ParameterService getParameters() {
		return parameters;
	}	
	
	@Override
	protected void finalize() throws Throwable {
		
		destroy();
		
		super.finalize();
	}

	public void destroy() {
		bus.shutdown();
		try {
			setDataLogger(null);
		} catch (IOException e) {
			logger.error("exception thrown when closing session log file", e);
		}
	}
}
