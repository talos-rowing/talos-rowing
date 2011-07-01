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

package org.nargila.robostroke.android.app.roll;

import org.nargila.robostroke.android.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.android.graph.LineGraphView;
import org.nargila.robostroke.android.graph.XYSeries;
import org.nargila.robostroke.android.graph.XYSeries.XMode;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.input.DataIdx;
import org.nargila.robostroke.input.SensorDataSink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class RollGraphView extends LineGraphView implements SensorDataSink {
	private static final float Y_RANGE = 10f;
	private static final float INCR = 1f;
	private final  XYSeries bothSeries;
	
	private final int rollAccumSize = 3;
	private int rollAccumCount;
	private float rollAccum;
	
	private final LowpassFilter filter = new LowpassFilter(.5f);
	

	private long rollAccumTimestamp;
	
	private final CyclicArrayXYSeries panelSeries = new CyclicArrayXYSeries(XMode.ROLLING);
	private boolean disableUpdate;	

	public RollGraphView(Context context, final double xRange) 
	{ 
		super(context, xRange, XYSeries.XMode.ROLLING, Y_RANGE, INCR);
		
		setyRangeMax(Y_RANGE);
		
		bothSeries = multySeries.addSeries(new CyclicArrayXYSeries(XMode.ROLLING));
		
		panelSeries.setxRange(xRange);
	}

	@Override
	protected void drawCentreLine(Canvas canvas, Rect rect) {
	}
	
	@Override
	public void setXRange(double val) {
		panelSeries.setxRange(val);
		super.setXRange(val);
	}

	@Override
	protected void drawGraph(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize) {

		drawRollPanels(canvas, rect, xAxisSize);

		super.drawGraph(canvas, rect, xAxisSize, yAxisSize);
	}

	private void drawRollPanels(Canvas canvas, Rect rect, double xAxisSize) {
		XYSeries ser = panelSeries;


		final int red = Color.RED;
		final int green = Color.GREEN;

		Paint paint = new Paint() {
			{
				setStyle(Style.FILL);
				setAntiAlias(false);
				setStrokeWidth(0);				
			}
		};

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

			canvas.drawRect(left, rect.top, right, rect.bottom, paint);
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

	public void disableUpdate(boolean update) {
		disableUpdate = update;		
	}
}