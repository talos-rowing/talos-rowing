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

package org.nargila.robostroke.android.app;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.android.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.android.graph.LineGraphView;
import org.nargila.robostroke.android.graph.XYSeries;
import org.nargila.robostroke.android.graph.XYSeries.XMode;
import org.nargila.robostroke.input.SensorDataSink;

import android.content.Context;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class StrokeGraphView extends LineGraphView implements DataUpdatable {
	private static final float Y_RANGE = 4f;
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
	private boolean disabled = true;
	
	public StrokeGraphView(Context context, float xRange, RoboStroke roboStroke) 
	{ 
		super(context, xRange, XYSeries.XMode.ROLLING, Y_RANGE, INCR);
		
		this.roboStroke = roboStroke;
		strokeSeries = multySeries.addSeries(new CyclicArrayXYSeries(XMode.ROLLING));
	}


	@Override
	protected void onAttachedToWindow() {
		disableUpdate(false);
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		disableUpdate(true);
		super.onDetachedFromWindow();
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;		
	}
	
	public void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (!disable) {
				roboStroke.getStrokeRateScanner().addSensorDataSink(privateStrokeAccelDataSink);
			} else {
				reset();
				roboStroke.getStrokeRateScanner().removeSensorDataSink(privateStrokeAccelDataSink);			
			}

			this.disabled = disable;
		}
	}
}