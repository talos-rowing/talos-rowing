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

package org.nargila.robostroke.ui;

import org.nargila.robostroke.ui.graph.LineGraph;
import org.nargila.robostroke.ui.graph.MultiXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;


/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public class LineGraphView extends SwingViewBase {
		

	private static final long serialVersionUID = 1L;

	private final LineGraph impl;

	public LineGraphView(double xRange, XYSeries.XMode xMode, double yScale,
			double yGridInterval) {
		impl = new LineGraph(new SwingUILiaison(this), xRange, xMode, yScale, yGridInterval);
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
		
		impl = new LineGraph(new SwingUILiaison(this), yRange, yGridInterval, multiSeries);
	}

	@Override
	protected void onDraw(SwingCanvas swingCanvas) {
		impl.draw(swingCanvas);			
	}
	

	public XYSeries addSeries(XYSeries series) {
		return impl.getSeries().addSeries(series);
	}
	
	@Override
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

	@Override
	public void disableUpdate(boolean disable) {
		impl.disableUpdate(disable);		
	}

	@Override
	public boolean isDisabled() {
		return impl.isDisabled();
	}
}
