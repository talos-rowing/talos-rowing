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

import android.graphics.Paint;

public interface XYSeries {

	public enum XMode {
		FIXED,
		GROWING,
		ROLLING
	}
	
	public class Renderer {
		public Paint strokePaint;
		public Paint fillPaint;

		public Renderer() {
			this(new Paint() {
				{
					setARGB(0xff, 0xff, 0xff, 0xff);
					setStyle(Style.STROKE);
					setAntiAlias(false);
					setStrokeWidth(2);
				}
			}, null);
		}

		public Renderer(Paint strokePaint, Paint fillPaint) {
			this.strokePaint = strokePaint;
			this.fillPaint = fillPaint;
		}
	}

	/**
	 * Adds a new value to the series.
	 * 
	 * @param x
	 *            the value for the X axis
	 * @param y
	 *            the value for the Y axis
	 */
	public abstract void add(double x, double y);

	public void setRenderer(Renderer renderer);

	public Renderer getRenderer();

	/**
	 * Removes an existing value from the series.
	 * 
	 * @param index
	 *            the index in the series of the value to remove
	 */
	public abstract void remove(int index);

	/**
	 * Removes all the existing values from the series.
	 */
	public abstract void clear();

	/**
	 * Returns the X axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the X value
	 */
	public abstract double getX(int index);

	/**
	 * Returns the Y axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the Y value
	 */
	public abstract double getY(int index);

	/**
	 * Returns the series item count.
	 * 
	 * @return the series item count
	 */
	public abstract int getItemCount();

	/**
	 * Returns the minimum value on the X axis.
	 * 
	 * @return the X axis minimum value
	 */
	public abstract double getMinX();

	/**
	 * Returns the minimum value on the Y axis.
	 * 
	 * @return the Y axis minimum value
	 */
	public abstract double getMinY();

	/**
	 * Returns the maximum value on the X axis.
	 * 
	 * @return the X axis maximum value
	 */
	public abstract double getMaxX();

	/**
	 * Returns the maximum value on the Y axis.
	 * 
	 * @return the Y axis maximum value
	 */
	public abstract double getMaxY();

	double getxRange();

	void setXMode(XMode mode);
	
	XMode getXMode();
	
	void setxRange(double xRange);

	public boolean isIndependantYAxis();

	public void setIndependantYAxis(boolean independantYAxis);

	public double getyAxisSize();

	public void setyAxisSize(double yAxisSize);

}