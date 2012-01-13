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

package org.nargila.robostroke.ui.graph;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.StrokeEvent;
import org.nargila.robostroke.StrokeListener;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.RSCanvas;
import org.nargila.robostroke.ui.UILiaison;

/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokeAnalysisGraph implements UpdatableGraphBase {
	
	private static final int MIN_STROKE_RATE = 10;
	
	private int cur = 0;
	private int next = 1;
	
	private final StrokeAnalysisGraphSingle[] graphs;

	private boolean aboveStrokeRateTreshold;

	private final RoboStroke roboStroke;
	
	private final UILiaison uiLiaision;
	
	public StrokeAnalysisGraph(UILiaison uiLiaision, RoboStroke roboStroke, StrokeAnalysisGraphSingle g1, StrokeAnalysisGraphSingle g2) {
		
		this.uiLiaision = uiLiaision;

		this.roboStroke = roboStroke;
		
		graphs = new StrokeAnalysisGraphSingle[] {
				g1,
				g2
		};
				
		graphs[next].setVisible(false);

	}

	private final SensorDataSink privateRollDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (aboveStrokeRateTreshold) {
				synchronized (graphs) {
					graphs[next].getRollSink().onSensorData(timestamp, value);
				}
			}
		}
	};

	private final SensorDataSink privateAccelDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (aboveStrokeRateTreshold) {
				synchronized (graphs) {
					graphs[next].getAccelSink().onSensorData(timestamp, value);
				}
			}
		}
	};

	protected boolean needReset;

	
	private final StrokeListener privateBusListener = new StrokeListener() {
		
		@Override
		public void onStrokeEvent(StrokeEvent event) {
			switch (event.type) {
			case STROKE_RATE:
				aboveStrokeRateTreshold =  (Integer)event.data > MIN_STROKE_RATE;
				break;
			case STROKE_POWER_END:
				boolean hasPower = (Float)event.data > 0;
				
				if (!hasPower) {
					resetNext();					
				}
				
				if (aboveStrokeRateTreshold) {
					if (!needReset) {
						synchronized (graphs) {


							graphs[cur].reset();

							if (cur == 0) {
								cur = 1;
								next = 0;
							} else {
								cur = 0;
								next = 1;
							}

							graphs[next].setVisible(false);
							graphs[cur].setVisible(true);
							graphs[cur].repaint();
							
						}
					}

					needReset = false;
				}
			}
		}
	};

	private boolean disabled = true;

	private boolean attached;

	public void reset() {
		graphs[cur].reset();
		graphs[next].reset();
	}



	
	@Override
	public boolean isDisabled() {
		return disabled;		
	}
	
	@Override
	public synchronized void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (!disable) {
				attachSensors();
			} else {
				resetNext();
				detachSensors();
			}	

			this.disabled = disable;
		}
	}




	private void detachSensors() {
		
		if (attached) {
			roboStroke.getAccelerationFilter().removeSensorDataSink(privateAccelDataSink);
			roboStroke.getRollScanner().removeSensorDataSink(privateRollDataSink);
			roboStroke.getBus().removeStrokeListener(privateBusListener);
			attached = false;
		}
	}




	private void attachSensors() {
		if (!attached) {
			roboStroke.getBus().addStrokeListener(privateBusListener);
			roboStroke.getAccelerationFilter().addSensorDataSink(privateAccelDataSink);
			roboStroke.getRollScanner().addSensorDataSink(privateRollDataSink);
			
			attached = true;
		}
	}


	private void resetNext() {
		needReset = true;
		graphs[next].reset();
	}




	@Override
	public void draw(RSCanvas canvas) {
		
	}




	@Override
	public void setVisible(boolean visible) {
		uiLiaision.setVisible(visible);
	}




	@Override
	public void repaint() {
		uiLiaision.repaint();
	}
}
