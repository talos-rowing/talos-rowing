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

import org.nargila.robostroke.common.NumberHelper;

/**
 * Simple line graph plot view.
 * 
 * @author tshalif
 * 
 */
public abstract class LineGraph implements DataUpdatable {
	protected MultiXYSeries multySeries;
	private double yRangeMin;
	private double yRangeMax = Double.MAX_VALUE;
	private final double incr;

	private boolean disabled;
	
	private final GraphMargines margines = new GraphMargines();

	protected boolean positiveOnly = false;

	private final RSPaint gridPaint;

	private final RSPaint centreLinePaint;
	
	public GraphMargines getMargines() {
		return margines;
	}

	public LineGraph(double xRange, XYSeries.XMode xMode, double yScale,
			double yGridInterval) {
		this(yScale, yGridInterval,  null);
		
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

	protected abstract RSPaint createPaint();

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
	public LineGraph(double yRange,
			double yGridInterval, MultiXYSeries multiSeries) {
		
		this.yRangeMin = yRange;
		this.incr = yGridInterval;
		this.multySeries = multiSeries;
		
		gridPaint = createPaint();
		gridPaint.setARGB(0xff, 0x55, 0x55, 0x55);
		gridPaint.setStrokeWidth(0f);
		centreLinePaint = createPaint();
		centreLinePaint.setARGB(0xff, 0x55, 0x55, 0x55);
		centreLinePaint.setStrokeWidth(0f);
	}


	public void draw(Object canvas) {

		synchronized (multySeries) {

			RSRect rect = getClipBounds(canvas);

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

	protected void drawGraph(Object canvas, RSRect rect, double xAxisSize,
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

	protected abstract int getRedColor();
	protected abstract int getGreenColor();
	protected abstract RSPath createPath();
	protected abstract void drawRect(Object canvas, float left, int top, float right, int bottom, RSPaint paint);
	protected abstract RSRect getClipBounds(Object canvas);
	protected abstract void drawPath(Object canvas, RSPath path, RSPaint strokePaint);
	protected abstract void drawLine(Object canvas, int left, float y, int right, float y2, RSPaint gridPaint);

	
	protected void drawSeries(Object canvas, RSRect rect, double xAxisSize,
			double yAxisSize, XYSeries series) {

		final int len = series.getItemCount();

		if (len > 0) {

			double scaleX = rect.width() / xAxisSize;
			final int height = rect.height();
			double scaleY = height / yAxisSize;
			RSPath path = createPath();
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

			drawPath(canvas, path, series.getRenderer().strokePaint);
		}
	}

	
	protected void drawGrid(Object canvas, double yAxisSize, RSRect rect) {
		float y;
		final int top = rect.top;
		final int height = rect.height();

		for (double j = incr; j < yAxisSize; j += incr) {
			y = (float) ((j * height / yAxisSize) + top);
			drawLine(canvas, rect.left, y, rect.right, y, gridPaint);
		}
	}

	protected void drawLine(Object canvas, double yAxisSize, RSRect rect) {
		float y;
		final int top = rect.top;
		final int height = rect.height();

		for (double j = incr; j < yAxisSize; j += incr) {
			y = (float) ((j * height / yAxisSize) + top);
			drawLine(canvas, rect.left, y, rect.right, y, gridPaint);
		}
	}
	
	protected void drawCentreLine(Object canvas, RSRect rect) {
		final int yCenter = rect.top + rect.height() / 2;

		if (!positiveOnly) {
			drawLine(canvas, 0, yCenter, rect.width(), yCenter, centreLinePaint);
		}
	}

	@Override
	public void reset() {
		multySeries.clear();
	}

	public abstract void repaint(); /* {
		postInvalidate();
	} */

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
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void disableUpdate(boolean disabled) {
		this.disabled = disabled;
	}
}
