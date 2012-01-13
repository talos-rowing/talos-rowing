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

package org.nargila.robostroke.ui.graph.swing;

import org.nargila.robostroke.ui.graph.LineGraph;
import org.nargila.robostroke.ui.graph.MultiXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;
import org.nargila.robostroke.ui.swing.SwingUILiaison;


/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public class LineGraphView extends SwingGraphViewBase<LineGraph> {
		

	private static final long serialVersionUID = 1L;


	public LineGraphView(double xRange, XYSeries.XMode xMode, double yScale,
			double yGridInterval) {
		setGraph(new LineGraph(new SwingUILiaison(this), xRange, xMode, yScale, yGridInterval));
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
	public LineGraphView(double yRange,
			double yGridInterval, MultiXYSeries multiSeries) {
		setGraph(new LineGraph(new SwingUILiaison(this), yRange, yGridInterval, multiSeries));
	}


	public XYSeries addSeries(XYSeries series) {
		return graph.getSeries().addSeries(series);
	}	

	public void setyRangeMax(double yRangeMax) {
		graph.setyRangeMax(yRangeMax);
	}

	public void setyRangeMin(double yRangeMin) {
		graph.setyRangeMin(yRangeMin);
	}

	public void setXRange(double val) {		
		graph.setXRange(val);
	}	
}
