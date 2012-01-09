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
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class SensorGraphBase extends LineGraph {
	private static final float INCR = 1f;
	private final XYSeries strokeSeries;
	private final RoboStroke roboStroke;
	private final SensorDataSink privateStrokeAccelDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			float[] values = (float[]) value;
			strokeSeries.add(timestamp, values[0]);
		}
	};
		
	public SensorGraphBase(UILiaison factory, float xRange, float yRange, RoboStroke roboStroke)	{ 
		super(factory, xRange, XYSeries.XMode.ROLLING, yRange, INCR);
		
		this.roboStroke = roboStroke;
		strokeSeries = multySeries.addSeries(new CyclicArrayXYSeries(XMode.ROLLING,  new XYSeries.Renderer(uiLiaison.createPaint())));
	}


	@Override
	public void disableUpdate(boolean disable) {
		if (isDisabled() != disable) {
			if (!disable) {
				attachSensors();
			} else {
				reset();
				detachSensors();			
			}

			super.disableUpdate(disable);
		}
	}


	protected void detachSensors() {
		roboStroke.getStrokeRateScanner().removeSensorDataSink(privateStrokeAccelDataSink);
	}


	protected void attachSensors() {
		roboStroke.getStrokeRateScanner().addSensorDataSink(privateStrokeAccelDataSink);
	}
}