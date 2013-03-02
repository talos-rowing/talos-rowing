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

package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.ui.android.AndroidUILiaison;
import org.nargila.robostroke.ui.graph.LineGraph;
import org.nargila.robostroke.ui.graph.MultiXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;

import android.content.Context;

/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public class LineGraphView extends AndroidGraphViewBase<LineGraph> {
		
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
		
		setGraph(new LineGraph(new AndroidUILiaison(this), yRange, yGridInterval, multiSeries));

	}
}
