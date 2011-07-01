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


abstract class XSeriesProxy implements XYSeries {

	protected final XYSeries impl;

	XSeriesProxy(XYSeries series) {
		impl = series;
	}

	public double getyAxisSize() {
		return impl.getyAxisSize();
	}

	public void setyAxisSize(double yAxisSize) {
		impl.setyAxisSize(yAxisSize);
	}

	public boolean isIndependantYAxis() {
		return impl.isIndependantYAxis();
	}

	public void setIndependantYAxis(boolean independantYAxis) {
		impl.setIndependantYAxis(independantYAxis);
	}

	public double getxRange() {
		return impl.getxRange();
	}

	public void setxRange(double xRange) {
		impl.setxRange(xRange);
	}

	@Override
	public void setXMode(XMode mode) {
		impl.setXMode(mode);		
	}
	
	@Override
	public XMode getXMode() {
		return impl.getXMode();
	}
	
	public Renderer getRenderer() {
		return impl.getRenderer();
	}

	public void setRenderer(Renderer renderer) {
		impl.setRenderer(renderer);
	}

	public void add(double x, double y) {
		impl.add(x, y);
	}

	public void clear() {
		impl.clear();
	}

	public int getItemCount() {
		return impl.getItemCount();
	}

	public double getMaxX() {
		return impl.getMaxX();
	}

	public double getMaxY() {
		return impl.getMaxY();
	}

	public double getMinX() {
		return impl.getMinX();
	}

	public double getMinY() {
		return impl.getMinY();
	}

	public double getX(int index) {
		return impl.getX(index);
	}

	public double getY(int index) {
		return impl.getY(index);
	}

	public void remove(int index) {
		impl.remove(index);
	}
}