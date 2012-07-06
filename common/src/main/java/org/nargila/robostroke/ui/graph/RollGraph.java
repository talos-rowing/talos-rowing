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

import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.data.DataIdx;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSCanvas;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSRect;
import org.nargila.robostroke.ui.UILiaison;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class RollGraph extends LineGraph implements SensorDataSink {
	private static final float Y_RANGE = 10f;
	private static final float INCR = 1f;
	private final  XYSeries bothSeries;
	
	private final int rollAccumSize = 3;
	private int rollAccumCount;
	private float rollAccum;
	
	private final LowpassFilter filter = new LowpassFilter(.5f);
	

	private long rollAccumTimestamp;
	
	
	@SuppressWarnings("serial")
	private final CyclicArrayXYSeries panelSeries = new CyclicArrayXYSeries(XYSeries.XMode.ROLLING, new XYSeries.Renderer(uiLiaison.createPaint())) {
		
		@Override
		public void setyAxisSize(double yAxisSize) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setxRange(double xRange) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setXMode(XMode mode) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setRenderer(Renderer renderer) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void setIndependantYAxis(boolean independantYAxis) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void remove(int index) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean isIndependantYAxis() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public double getyAxisSize() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getxRange() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getY(int index) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public XMode getXMode() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public double getX(int index) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Renderer getRenderer() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public double getMinY() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getMinX() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getMaxY() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public double getMaxX() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public int getItemCount() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void add(double x, double y) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public RollGraph(UILiaison uiLiaison, final double xRange) { 
		super(uiLiaison, xRange, XYSeries.XMode.ROLLING, Y_RANGE, INCR);
		
		setyRangeMax(Y_RANGE);
		
		bothSeries = multySeries.addSeries(new CyclicArrayXYSeries(XYSeries.XMode.ROLLING, new XYSeries.Renderer(uiLiaison.createPaint())));
		
		panelSeries.setxRange(xRange);
	}

	@Override
	protected void drawCentreLine(RSCanvas canvas, RSRect rect) {
	}
	
	@Override
	public void setXRange(double val) {
		panelSeries.setxRange(val);
		super.setXRange(val);
	}

	@Override
	protected void drawGraph(RSCanvas canvas, RSRect rect, double xAxisSize,
			double yAxisSize) {

		drawRollPanels(canvas, rect, xAxisSize);

		super.drawGraph(canvas, rect, xAxisSize, yAxisSize);
	}
	
	private void drawRollPanels(RSCanvas canvas, RSRect rect, double xAxisSize) {
		XYSeries ser = panelSeries;


		final int red = uiLiaison.getRedColor();
		final int green = uiLiaison.getGreenColor();

		RSPaint paint = uiLiaison.createPaint();
		paint.setStyle(PaintStyle.FILL);
		paint.setAntiAlias(false);
		paint.setStrokeWidth(0);				

		final double maxYValue = Y_RANGE / 2;
		final double scaleX = rect.width() / xAxisSize;

		final double minX = multySeries.getMinX();
		final int len = ser.getItemCount();

		for (int i = 0; i < len - 1; ++i) {

			double startX = ser.getX(i);
			double stopX = ser.getX(i + 1);

			double avgY = Math.min(ser.getY(i), maxYValue);

			int color = avgY > 0 ? green : red;
			int alpha = (int) ((avgY / maxYValue) * 255);

			paint.setColor(color);
			paint.setAlpha(Math.abs(alpha));

			float left = (float) ((startX - minX) * scaleX);
			float right = (float) (((stopX - minX) * scaleX));

			canvas.drawRect((int)left, rect.top, (int)right, rect.bottom, paint);
		}
	}
	
	@Override
	public void reset() {
		synchronized (multySeries) {
			resetRollAccum();
			super.reset();
		}
	}

	private void resetRollAccum() {
		rollAccum = 0;
		rollAccumCount = 0;
	}
	@Override
	public void onSensorData(long timestamp, Object value) {
		synchronized (multySeries) {
			float[] values = (float[]) value;

			float y = filter.filter(new float[] {values[DataIdx.ORIENT_ROLL]})[0];


			rollAccum += y;

			if (rollAccumCount++ == 0) {
				rollAccumTimestamp = timestamp;
			} else if (rollAccumCount == rollAccumSize) {
				panelSeries.add(rollAccumTimestamp, rollAccum / rollAccumSize);
				resetRollAccum();
			}

			bothSeries.add(timestamp, y);
		}
	}
}