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


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nargila.robostroke.common.LocationUtil;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.FileDataInput;
import org.nargila.robostroke.data.SensorDataInput;
import org.nargila.robostroke.data.remote.RemoteDataInput;
import org.nargila.robostroke.param.ParameterBusEventData;
import org.nargila.robostroke.stroke.RowingSplitMode;
import org.nargila.robostroke.way.DistanceResolver;

public class RoboStrokeTest {

	
	
	private static File inputFile;
	private static File logFile;
	
	private RoboStroke roboStroke;
	private RoboStrokeEventBus bus;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		inputFile = File.createTempFile("RoboStrokeTest-inputData-", ".txt");
//		inputFile.deleteOnExit();

		logFile = File.createTempFile("RoboStrokeTest-outputData-", ".txt");
		
		InputStream in = RoboStrokeTest.class.getResourceAsStream("100m-12-strokes-dataInput.txt");		
		
		try {
		
			FileOutputStream fout = new FileOutputStream(inputFile);
			
			try {
		
				fout.getChannel().transferFrom(Channels.newChannel(in), 0, Long.MAX_VALUE);
			} finally {
				fout.close();
			}
			
		} finally {
			in.close();
		}
	}

	@Before
	public void setUp() throws Exception {				

		roboStroke = new RoboStroke(new DistanceResolver() {
			
			@Override
			public float calcDistance(double[] loc1, double[] loc2) {
				return (float) LocationUtil.distVincenty(loc1[0], loc1[1], loc2[0], loc2[1]); 
			}
		});
		
		bus = roboStroke.getBus();

		
		System.setProperty("org.nargila.robostroke.data.FileDataInput.batchMode", "true");

	}
	
	private void start(SensorDataInput dataInput) throws IOException {
		
		if (dataInput == null) {
			dataInput = fileInput();
		}
		
		roboStroke.setInput(dataInput);
		
		roboStroke.setDataLogger(logFile);		
	}

	private FileDataInput fileInput() throws IOException {
		return new FileDataInput(roboStroke, inputFile);
	}
	
	@After
	public void tearDown() {
		roboStroke.destroy();
	}
	

	@Test
	public void testSplitRowingContinuousMode() throws Exception {
		
		BusEventListener listener = new BusEventListener() {
			
			@Override
			public void onBusEvent(DataRecord event) {
				switch (event.type) {
				case ROWING_COUNT:
					if ((Integer)event.data == 12) {
						roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_MODE.getId(), RowingSplitMode.AUTO + "");
					}

					break;
				}
			}
		};
		
		bus.addBusListener(listener);
		DataRecord event;
		
		try {							
			
			event = splitRowing(RowingSplitMode.CONTINUOUS, false);
		
		} finally {
			bus.removeBusListener(listener);
		}
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(49979969000L / 1000000000.0, splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(125, distance, 1);
		Assert.assertEquals(48000L, travelTime);
		Assert.assertEquals(12, strokes, 1);
		
	}
	

	@Test
	public void testStrokeRate() throws Exception {
		
		final int[] strokeRate = {0, 0}; // count, accum 
		
		
		BusEventListener listener = new BusEventListener() {
			boolean hasPower;
			
			@Override
			public void onBusEvent(DataRecord event) {
				switch (event.type) {
				case STROKE_POWER_END:
					hasPower = (Float)event.data > 0;

					break;
				case STROKE_RATE:
					if (hasPower) {
						strokeRate[0]++;
						strokeRate[1] += (Integer)event.data;
					}
					break;
				}
			}
		};
		
		bus.addBusListener(listener);
		
		try {					
			splitRowing(RowingSplitMode.AUTO, true);
			
			Assert.assertEquals(7, strokeRate[0], 1);
			Assert.assertEquals(19.25, (strokeRate[1] / (double)strokeRate[0]), 0.25);
			
		} finally {
			bus.removeBusListener(listener);
		}
	}
	
	@Test
	public void testSplitRowing() throws Exception {
		
		DataRecord event = splitRowing(RowingSplitMode.AUTO, false);
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(36419086000L / 1000000000.0, splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(114, distance, 5);
		Assert.assertEquals(37000L, travelTime);
		Assert.assertEquals(12, strokes, 1);
		
	}
	

	@Test
	public void testSplitRowingStraight() throws Exception {		
		
		DataRecord event = splitRowing(RowingSplitMode.AUTO, true);
		
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(36378872000L / 1000000000.0, splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(114, distance, 5);
		Assert.assertEquals(37000L / 1000.0, travelTime / 1000.0, 1);
		Assert.assertEquals(12, strokes);
	}
	
	private DataRecord splitRowing(RowingSplitMode mode, boolean straightMode) throws Exception {
		return splitRowing(null, mode, straightMode);
	}
	
	private DataRecord splitRowing(SensorDataInput dataInput, RowingSplitMode mode, boolean straightMode) throws Exception {
		return splitRowing(dataInput, mode, straightMode, null);
	}
	
	private DataRecord splitRowing(SensorDataInput dataInput, RowingSplitMode mode, boolean straightMode, final BusEventListener testEventListener) throws Exception {
		
		final AtomicReference<DataRecord> startEvent = new AtomicReference<DataRecord>();
		final AtomicReference<DataRecord> stopEvent = new AtomicReference<DataRecord>();
				
		BusEventListener listener = new BusEventListener() {
						
			@Override
			public void onBusEvent(DataRecord event) {
				System.out.println(event);
				
				if (null != testEventListener) {
					testEventListener.onBusEvent(event);
				}
				
				switch (event.type) {
				
				case SESSION_PARAMETER:					
					
					ParameterBusEventData pd = (ParameterBusEventData) event.data;
					
					if (!pd.id.equals(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE.getId()) &&  
							!pd.id.equals(ParamKeys.PARAM_ROWING_MODE.getId())) {
						roboStroke.getParameters().setParam(pd.id, pd.value);
					}
					break;
				case ROWING_START:
					synchronized (startEvent) {
						startEvent.set(event);
						startEvent.notify();
					}
					break;
				case ROWING_STOP:
					synchronized (stopEvent) {
						stopEvent.set(event);
						stopEvent.notify();
					}
					break;
				}
				
				synchronized (startEvent) {
					startEvent.set(event);
					startEvent.notify();
				}
			}
		};
		
		try {
			bus.addBusListener(listener);
			roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE.getId(), straightMode);
			roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_MODE.getId(), mode.name());
			start(dataInput);
			
			synchronized (startEvent) {				
								
				startEvent.wait(20000);
				
				Assert.assertNotNull("no start event after 20 seconds", startEvent.get());
			}
				
			synchronized (stopEvent) {
					
				if (stopEvent.get() == null) {
				
					int timeout = 90000;
					stopEvent.wait(timeout);
					
					Assert.assertNotNull(String.format("no stop event after %d seconds", timeout / 1000), stopEvent.get());
				}
				
			} 
			
			
			Thread.sleep(1000); // allow enough time for bus to log event to record file
			
		} finally {
			bus.removeBusListener(listener);
		}
		
		return stopEvent.get();
	}
	
	@Test
	public void testBroadcasting() throws Exception {
		
		final RoboStroke rs = new RoboStroke();

		rs.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId(), "localhost");
		roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId(), "localhost");
		
		rs.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_ON.getId(), true);
		rs.setInput(fileInput());
		
		DataRecord event = splitRowing(new RemoteDataInput(roboStroke), RowingSplitMode.AUTO, false);
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(36378872000L / 1000000000.0, splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(114, distance, 5);
		Assert.assertEquals(37000L, travelTime);
		Assert.assertEquals(12, strokes);
	}
}
