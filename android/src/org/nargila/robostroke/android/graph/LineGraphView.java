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

import org.nargila.robostroke.common.NumberHelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public class LineGraphView extends View {
	protected MultiXYSeries multySeries;
	private double yRangeMin;
	private double yRangeMax = Double.MAX_VALUE;
	private final double incr;

	private final GraphMargines margines = new GraphMargines();

	protected boolean positiveOnly = false;

	private final Paint gridPaint = new Paint() {
		{
			setARGB(0xff, 0x55, 0x55, 0x55);
			setStrokeWidth(0);
		}
	};

	private final Paint centreLinePaint = new Paint() {
		{
			setARGB(0xff, 0xff, 0x00, 0x00);
			setStrokeWidth(0);
		}
	};
	public GraphMargines getMargines() {
		return margines;
	}

	public LineGraphView(Context context, double xRange, XYSeries.XMode xMode, double yScale,
			double yGridInterval) {
		this(context, yScale, yGridInterval,  null);
		
		multySeries = new MultiXYSeries(xRange, xMode) {
			@Override
			protected void onAdd(double x, double y, XYSeries series) {
				repaint();
			}

			@Override
			protected void onRemove(int index, XYSeries series) {
				repaint();
			}

			@Override
			public void clear() {
				super.clear();
				repaint();
			}
		};

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
		
		this.yRangeMin = yRange;
		this.incr = yGridInterval;
		this.multySeries = multiSeries;
	}


	@Override
	protected void onDraw(Canvas canvas) {

		synchronized (multySeries) {
			super.onDraw(canvas);

			Rect rect = canvas.getClipBounds();

			rect.top += margines.top;
			rect.bottom -= margines.bottom;
			rect.left += margines.left;
			rect.right -= margines.right;

			if (rect.width() > 0 && rect.height() > 0) {
				double xAxisSize = multySeries.getxRange();
				double yAxisSize = calcYAxisSize();

				drawGraph(canvas, rect, xAxisSize, yAxisSize);
			}
		}
	}

	protected void drawGraph(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize) {

		drawGrid(canvas, yAxisSize, rect);
		drawCentreLine(canvas, rect);

		for (XYSeries series : multySeries.getSeries()) {
			double seriesYAxisSize = series.getyAxisSize();
			drawSeries(canvas, rect, xAxisSize,
					seriesYAxisSize > 0 ? seriesYAxisSize : yAxisSize, series);
		}
	}

	private double calcYAxisSize() {
		double res = NumberHelper.validRange(multySeries.getyRange(),
				yRangeMin, yRangeMax);

		return positiveOnly ? res / 2 : res;
	}

	protected void drawSeries(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize, XYSeries series) {

		final int len = series.getItemCount();

		if (len > 0) {

			double scaleX = rect.width() / xAxisSize;
			final int height = rect.height();
			double scaleY = height / yAxisSize;
			Path path = new Path();
			final int bottom = rect.bottom;
			final int hHalf = height / 2;
			double minX = multySeries.getMinX();
			float x = (float) ((series.getX(0) - minX) * scaleX);
			float y = (float) (bottom - (series.getY(0) * scaleY));

			if (!positiveOnly) {
				y -= hHalf;
			}

			path.moveTo(x, y);

			double prevYVal = 0;
			for (int i = 1; i < len; i++) {
				double yVal = series.getY(i);

				x = (float) ((series.getX(i) - minX) * scaleX);
				y = (float) (bottom - (yVal * scaleY));

				if (!positiveOnly) {
					y -= hHalf;
				}

				if (prevYVal == 0 && yVal == 0) {
					path.moveTo(x, y);
				} else {
					path.lineTo(x, y);
				}

				prevYVal = yVal;
			}

			canvas.drawPath(path, series.getRenderer().strokePaint);
		}
	}

	protected void drawGrid(Canvas canvas, double yAxisSize, Rect rect) {
		float y;
		final int top = rect.top;
		final int height = rect.height();

		for (double j = incr; j < yAxisSize; j += incr) {
			y = (float) ((j * height / yAxisSize) + top);
			canvas.drawLine(rect.left, y, rect.right, y, gridPaint);
		}
	}

	protected void drawCentreLine(Canvas canvas, Rect rect) {
		final int yCenter = rect.top + rect.height() / 2;

		if (!positiveOnly) {
			canvas.drawLine(0, yCenter, rect.width(), yCenter, centreLinePaint);
		}
	}

	public void reset() {
		multySeries.clear();
	}

	public void repaint() {
		postInvalidate();
	}

	public double getyRangeMax() {
		return yRangeMax;
	}

	public void setyRangeMax(double yRangeMax) {
		this.yRangeMax = yRangeMax;
	}

	public void setyRangeMin(double yRangeMin) {
		this.yRangeMin = yRangeMin;
	}

	public double getyRangeMin() {
		return yRangeMin;
	}

	public double getxRange() {
		return multySeries.getxRange();
	}

	public void setXRange(double val) {
		multySeries.setxRange(val);
	}	
}
