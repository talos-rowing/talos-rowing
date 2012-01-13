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

import java.io.Serializable;

/**
 * An XY series encapsulates values for XY charts like line, time, area,
 * scatter... charts.
 */
public class CyclicArrayXYSeries implements XYSeries, Serializable {

	private static final long serialVersionUID = 1469528419619828841L;

	private int bufferSize = 200;

	private int pos = 0;
	private int itemCount = 0;

	private double[] xArr = new double[bufferSize];
	private double[] yArr = new double[bufferSize];

	/** The minimum value for the X axis. */
	private double mMinX = Double.MAX_VALUE;
	/** The maximum value for the X axis. */
	private double mMaxX = -Double.MAX_VALUE;
	/** The minimum value for the Y axis. */
	private double mMinY = Double.MAX_VALUE;
	/** The maximum value for the Y axis. */
	private double mMaxY = -Double.MAX_VALUE;

	private Renderer renderer;

	private double xRange;

	private XMode xMode;

	private boolean independantYAxis;

	private double yAxisSize = 0;


	/**
	 * Builds a new XY series.
	 * 
	 * @param title
	 *            the series title.
	 */
	public CyclicArrayXYSeries(XMode xMode, Renderer renderer) {
		this.renderer = renderer;
		this.xMode = xMode;
	}

	/**
	 * Initializes the range for both axes.
	 */
	private void initYRange() {
		mMinY = Double.MAX_VALUE;
		mMaxY = -Double.MAX_VALUE;

		for (int i = 0; i < itemCount; ++i) {
			updateYRange(getY(i));
		}
	}

	/**
	 * Updates the range on both axes.
	 * 
	 * @param x
	 *            the new x value
	 * @param y
	 *            the new y value
	 */
	private void updateYRange(double y) {
		mMinY = Math.min(mMinY, y);
		mMaxY = Math.max(mMaxY, y);
	}

	/**
	 * Adds a new value to the series.
	 * 
	 * @param x
	 *            the value for the X axis
	 * @param y
	 *            the value for the Y axis
	 */
	@Override
	public synchronized void add(double x, double y) {

		double xrangeTreshold = mMinX + Math.abs(xRange);
		
		if (x > xrangeTreshold && xMode == XMode.FIXED) {
			return;
		}

		if (itemCount == bufferSize) {
			reallocBuffers((int) (bufferSize * 1.5));
		}

		int idx = (pos + itemCount) % bufferSize;

		xArr[idx] = x;
		yArr[idx] = y;

		mMaxY = Math.max(mMaxY, y);
		mMinY = Math.min(mMinY, y);
		mMinX = Math.min(mMinX, x);
		mMaxX = Math.max(mMaxX, x);

		++itemCount;

		if (x > xrangeTreshold && xMode == XMode.ROLLING) {
			shiftXrange();
		}
	}

	private void reallocBuffers(int newSize) {
		for (double[] arr : new double[][] { xArr, yArr }) {
			double[] tmpArr = new double[newSize];
			int idx = pos % bufferSize;
			System.arraycopy(arr, idx, tmpArr, 0, itemCount - idx);
			System.arraycopy(arr, 0, tmpArr, itemCount - idx, idx);

			if (arr == xArr) {
				xArr = tmpArr;
			} else {
				yArr = tmpArr;
			}
		}

		bufferSize = newSize;

		pos = 0;
	}

	/**
	 * Removes an existing value from the series.
	 * 
	 * @param index
	 *            the index in the series of the value to remove
	 */
	@Override
	public synchronized void remove(int index) {
		remove(index, false);
	}

	/**
	 * Removes an existing value from the series.
	 * 
	 * @param index
	 *            the index in the series of the value to remove
	 * @param skipUpdateYRange if set to 'true', updating of Y scale is skipped 
	 */
	private synchronized void remove(int index, boolean skipUpdateYRange) {
		double removedY = getY(index);
		double removedX = getX(index);

		if (index == 0) {
			++pos;
			--itemCount;
		} else if (index == (itemCount - 1)) {
			--itemCount;
		} else {
			for (int i = index; i < itemCount; ++i) {

				int curIdx = (pos + i) % bufferSize;
				int nextIdx = (pos + i + 1) % bufferSize;
				xArr[curIdx] = xArr[nextIdx];
				yArr[curIdx] = yArr[nextIdx];
			}
			--itemCount;
		}

		if (!skipUpdateYRange) {
			if (removedY == mMinY || removedY == mMaxY) {
				initYRange();
			}
		}

		if (itemCount > 0) {
			mMinX = getX(0);
		} else {
			mMinX = Double.MAX_VALUE;
			mMaxX = -Double.MAX_VALUE;
		}
	}

	private void shiftXrange() {
		
		if (xMode != XYSeries.XMode.ROLLING) {
			throw new AssertionError("meaningless calling of shiftXrange() for xMode != ROLLING");
		}
		
		double minXTreshold = mMaxX - xRange;

		boolean needsYRehash = false;

		final boolean skipUpdateYRange = false;

		while (itemCount > 0 && getX(0) < minXTreshold) {
			double y = getY(0);

			if (y == mMinY || y == mMaxY) {
				needsYRehash = true;
			}

			remove(0, skipUpdateYRange);
		}

		if (needsYRehash) {
			initYRange();
		}
	}

	/**
	 * Removes all the existing values from the series.
	 */
	@Override
	public synchronized void clear() {
		pos = itemCount = 0;
		initYRange();
		mMinX = Double.MAX_VALUE;
		mMaxX = -Double.MAX_VALUE;
	}

	/**
	 * Returns the X axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the X value
	 */
	@Override
	public double getX(int index) {
		return xArr[(pos + index) % bufferSize];
	}

	/**
	 * Returns the Y axis value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the Y value
	 */
	@Override
	public double getY(int index) {
		return yArr[(pos + index) % bufferSize];
	}

	/**
	 * Returns the series item count.
	 * 
	 * @return the series item count
	 */
	@Override
	public int getItemCount() {
		return itemCount;
	}

	/**
	 * Returns the minimum value on the X axis.
	 * 
	 * @return the X axis minimum value
	 */
	@Override
	public double getMinX() {
		return mMinX == Double.MAX_VALUE ? 0 : mMinX;
	}

	/**
	 * Returns the minimum value on the Y axis.
	 * 
	 * @return the Y axis minimum value
	 */
	@Override
	public double getMinY() {
		return mMinY == Double.MAX_VALUE ? 0 : mMinY;
	}

	/**
	 * Returns the maximum value on the X axis.
	 * 
	 * @return the X axis maximum value
	 */
	@Override
	public double getMaxX() {
		return mMaxX == -Double.MAX_VALUE ? 0 : mMaxX;
	}

	/**
	 * Returns the maximum value on the Y axis.
	 * 
	 * @return the Y axis maximum value
	 */
	@Override
	public double getMaxY() {
		return mMaxY == -Double.MAX_VALUE ? 0 : mMaxY;
	}

	@Override
	public Renderer getRenderer() {
		return renderer;
	}

	@Override
	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public double getxRange() {
		return xRange;
	}

	@Override
	public void setxRange(double xRange) {
		this.xRange = xRange;
	}

	@Override
	public boolean isIndependantYAxis() {
		return independantYAxis;
	}

	@Override
	public void setIndependantYAxis(boolean independantYAxis) {
		this.independantYAxis = independantYAxis;
	}

	@Override
	public double getyAxisSize() {
		return yAxisSize;
	}

	@Override
	public void setyAxisSize(double yAxisSize) {
		this.yAxisSize = yAxisSize;
	}

	@Override
	public XMode getXMode() {
		return xMode;
	}

	@Override
	public void setXMode(XMode mode) {
		this.xMode = mode;		
	}

}
