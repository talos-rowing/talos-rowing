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

package org.nargila.robostroke.android.graph;

import org.nargila.robostroke.ui.MultiXYSeries;
import org.nargila.robostroke.ui.XYSeries;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public class LineGraphView extends View {
		

	private final LineGraphImpl impl;

	public LineGraphView(Context context, double xRange, XYSeries.XMode xMode, double yScale,
			double yGridInterval) {
		this(context, yScale, yGridInterval,  null);
	}
	
	/**
	 * constructor with standard View context, attributes, data window size, y
	 * scale and y data tic mark gap
	 * 
	 * @param context
	 *            the Android Activity
	 * @param attrs
	 *            layout and other common View attributes
	 * @param windowSize
	 *            size of data array to plot
	 * @param yScale
	 *            y value to pixel scale
	 * @param incr
	 *            y data tic mark gap
	 */
	public LineGraphView(Context context, double yRange,
			double yGridInterval, MultiXYSeries multiSeries) {
		super(context);
		
		impl = new LineGraphImpl(this, yRange, yGridInterval, multiSeries);

	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		impl.draw(canvas);
	}

	public void reset() {
		impl.reset();
	}


	public void setyRangeMax(double yRangeMax) {
		impl.setyRangeMax(yRangeMax);
	}

	public void setyRangeMin(double yRangeMin) {
		impl.setyRangeMin(yRangeMin);
	}

	public void setXRange(double val) {		
		impl.setXRange(val);
	}	
}
