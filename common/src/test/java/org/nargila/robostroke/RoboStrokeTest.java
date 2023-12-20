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


import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SensorDataInput;
import org.nargila.robostroke.data.remote.RemoteDataInput;
import org.nargila.robostroke.param.ParameterBusEventData;
import org.nargila.robostroke.stroke.RowingSplitMode;

public class RoboStrokeTest extends TestProperties {
	
	private TestRoboStroke test;

	public RoboStrokeTest() throws Exception {
	}
			
	@Test
	public void testSplitRowingContinuousMode() throws Exception {
		
		
		test = new TestRoboStroke(v("dataInput"));
        
		final int strokesVal = v("strokes", Integer.class);
		
		BusEventListener listener = new BusEventListener() {
			
			@SuppressWarnings("incomplete-switch")
			@Override
			public void onBusEvent(DataRecord event) {
				switch (event.type) {
				case ROWING_COUNT:
					if ((Integer)event.data == strokesVal) {
						test.roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_MODE.getId(), RowingSplitMode.AUTO + "");
					}

					break;
				}
			}
		};
		
		test.bus.addBusListener(listener);
		DataRecord event;
		
		try {							
			
			event = splitRowing(RowingSplitMode.CONTINUOUS, false);
		
		} finally {
			test.bus.removeBusListener(listener);
		}
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(v("splitTime", Double.class), splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(v("distance", Double.class), distance, 2);
		Assert.assertEquals(v("travelTime", Double.class), travelTime / 1000.0, 1);
		Assert.assertEquals((double)strokesVal, strokes, 1);
		
	}
	

	@Test
	public void testStrokeRate() throws Exception {

		test = new TestRoboStroke(v("dataInput"));

		final int[] strokeRate = {0, 0}; // count, accum 
		
		
		BusEventListener listener = new BusEventListener() {
			boolean hasPower;
			
			@SuppressWarnings("incomplete-switch")
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
		
		test.bus.addBusListener(listener);
		
		try {					
			splitRowing(RowingSplitMode.AUTO, true);
			
			Assert.assertEquals(v("eventCount", Double.class), strokeRate[0], 1);
			Assert.assertEquals(v("strokeRate", Double.class), (strokeRate[1] / (double)strokeRate[0]), 0.25);
			
		} finally {
			test.bus.removeBusListener(listener);
		}
	}
	
	@Test
	public void testSplitRowing() throws Exception {
		
		test = new TestRoboStroke(v("dataInput"));
		
		DataRecord event = splitRowing(RowingSplitMode.AUTO, false);
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(v("splitTime", Double.class), splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(v("distance", Double.class), distance, 5);
		Assert.assertEquals(v("travelTime", Double.class), travelTime, 0.0);
		Assert.assertEquals(v("strokes", Double.class), strokes, 1);
		
	}
	

	@Test
	public void testSplitRowingStraight() throws Exception {		
		
		test = new TestRoboStroke(v("dataInput"));

		DataRecord event = splitRowing(RowingSplitMode.AUTO, true);
		
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(v("splitTime", Double.class), splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(v("distance", Double.class), distance, 5);
		Assert.assertEquals(v("travelTime", Double.class), travelTime, 1);
		Assert.assertEquals(v("strokes", Double.class), strokes, 0.0);
	}
	
	private DataRecord splitRowing(RowingSplitMode mode, boolean straightMode) throws Exception {
		return splitRowing(null, mode, straightMode);
	}
	
	private DataRecord splitRowing(SensorDataInput dataInput, RowingSplitMode mode, boolean straightMode) throws Exception {
		return splitRowing(dataInput, mode, straightMode, null, 0);
	}
	
	private DataRecord splitRowing(SensorDataInput dataInput, RowingSplitMode mode, boolean straightMode, final BusEventListener testEventListener, long startTime) throws Exception {
		
		final AtomicReference<DataRecord> startEvent = new AtomicReference<DataRecord>();
		final AtomicReference<DataRecord> stopEvent = new AtomicReference<DataRecord>();
				
		BusEventListener listener = new BusEventListener() {
						
			@SuppressWarnings("incomplete-switch")
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
						test.roboStroke.getParameters().setParam(pd.id, pd.value);
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
			test.bus.addBusListener(listener);
			test.roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_STRAIGHT_LINE_MODE.getId(), straightMode);
			test.roboStroke.getParameters().setParam(ParamKeys.PARAM_ROWING_MODE.getId(), mode.name());
			test.start(dataInput, v("startTime", 0));
			
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
			test.bus.removeBusListener(listener);
		}
		
		return stopEvent.get();
	}
	
	// @Test
	public void disabled_testBroadcasting() throws Exception {
		
		test = new TestRoboStroke(v("dataInput"));

		final RoboStroke rs = new RoboStroke();

		rs.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId(), "localhost");
		test.roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_HOST.getId(), "localhost");
		
		rs.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_ON.getId(), true);
		rs.setInput(test.fileInput(v("startTime", 0)));
		
		DataRecord event = splitRowing(new RemoteDataInput(test.roboStroke), RowingSplitMode.AUTO, false);
		
		/* ROWING_STOP 169103868297216 169098851909632 114.224663 36272340992 37000 12 */
		Object[] data = (Object[]) event.data;
		
		float distance = (Float)data[1];
		long splitTime = (Long) data[2];
		long travelTime = (Long) data[3];
		int strokes = (Integer) data[4];
		
		Assert.assertEquals(v("splitTime", Double.class), splitTime / 1000000000.0, 0.5);
		Assert.assertEquals(v("distance", Double.class), distance, 5);
		Assert.assertEquals((long)v("travelTime", Long.class), travelTime);
		Assert.assertEquals((int)v("strokes", Integer.class), strokes);
	}
}
