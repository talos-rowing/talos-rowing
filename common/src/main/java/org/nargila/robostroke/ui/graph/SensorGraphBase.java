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
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.UILiaison;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public abstract class SensorGraphBase extends LineGraph {
	
	private static final float INCR = 1f;
	protected final XYSeries accelSeries;
	protected final RoboStroke roboStroke;
	
	private boolean attached;
	
	protected final SensorDataSink accelDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			float[] values = (float[]) value;
			accelSeries.add(timestamp, values[0]);
		}
	};
		
	public SensorGraphBase(UILiaison factory, XYSeries.XMode xMode, float xRange, float yRange, RoboStroke roboStroke)	{ 
		super(factory, xRange, xMode, yRange, INCR);
		
		this.roboStroke = roboStroke;
		accelSeries = multySeries.addSeries(new CyclicArrayXYSeries(xMode,  new XYSeries.Renderer(uiLiaison.createPaint())));
	}


	@Override
	public synchronized void disableUpdate(boolean disable) {
		if (isDisabled() != disable) {
			if (!disable) {
				if (!attached) {
					attachSensors();
					attached = true;
				}
			} else {
				reset();
				
				if (attached) {
					detachSensors();
					attached = false;
				}
			}

			super.disableUpdate(disable);
		}
	}


	protected void attachSensors() {
		attachSensors(accelDataSink);
	}

	protected void detachSensors() {
		detachSensors(accelDataSink);
	}

	
	protected abstract void detachSensors(SensorDataSink lineDataSink);


	protected abstract void attachSensors(SensorDataSink lineDataSink);
	
}