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

package org.nargila.robostroke.android.app;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.android.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.android.graph.LineGraphView;
import org.nargila.robostroke.android.graph.MultiXYSeries;
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
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class AccellGraphView extends LineGraphView implements  DataUpdatable  {
	/**
	 * subclass of LineGraphView for setting stroke specific parameters
	 */
	private class RollGraphOverlay implements SensorDataSink {
		private static final double ROLL_PANNEL_DIM_FACTOR = 0.60;

		private static final float Y_RANGE = 10f;

		private final int rollAccumSize = 2;
		private int rollAccumCount;
		private float rollAccum;

		private final LowpassFilter filter = new LowpassFilter(.5f);

		private long rollAccumTimestamp;

		private final MultiXYSeries multySeries;
		private final XYSeries rollSeries;
		private final CyclicArrayXYSeries rollPanelSeries = new CyclicArrayXYSeries(XMode.ROLLING);
		private final Paint rollGraphPaint = new Paint() {
			{
				setStyle(Style.STROKE);
				setColor(Color.YELLOW);
				setAlpha(170);
			}
		};
		
		private final Paint rollBackgroundPaint = new Paint() {
			{
				setStyle(Style.FILL);
				setAntiAlias(false);
				setStrokeWidth(0);
			}
		};

		private final CyclicArrayXYSeries rollSeriesImpl = new CyclicArrayXYSeries(XMode.ROLLING) {
			{
				setRenderer(new Renderer(rollGraphPaint, null));
				setIndependantYAxis(true);
				setyAxisSize(Y_RANGE);
			}
		};

		RollGraphOverlay(double xRange, MultiXYSeries multySeries) {
			rollSeries = multySeries.addSeries(rollSeriesImpl);

			rollPanelSeries.setxRange(xRange);

			this.multySeries = multySeries;
		}

		void setXRange(double val) {
			rollPanelSeries.setxRange(val);
		}

		void drawRollPanels(Canvas canvas, Rect rect, double xAxisSize) {
			XYSeries ser = rollPanelSeries;

			final int len = ser.getItemCount();

			if (len > 0) {
				final int red = Color.RED;
				final int green = Color.GREEN;

				final double maxYValue = Y_RANGE / 2;
				final double scaleX = rect.width() / xAxisSize;

				final double minX = multySeries.getMinX();

				double startX = ser.getX(0);
				double stopX;

				for (int i = 1; i < len; ++i, startX = stopX) {
					stopX = ser.getX(i);

					double avgY = Math.min(ser.getY(i), maxYValue);

					int color = avgY > 0 ? green : red;
					int alpha = (int) ((avgY / maxYValue) * 255 * ROLL_PANNEL_DIM_FACTOR);

					rollBackgroundPaint.setColor(color);
					rollBackgroundPaint.setAlpha(Math.abs(alpha));

					float left = (float) ((startX - minX) * scaleX);
					float right = (float) (((stopX - minX) * scaleX));

					canvas.drawRect(left, rect.top, right, rect.bottom, rollBackgroundPaint);
				}
			}
		}

		void reset() {
			synchronized (multySeries) {
				resetRollAccum();
				rollPanelSeries.clear();
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

				float y = filter
				.filter(new float[] { values[DataIdx.ORIENT_ROLL] })[0];

				rollAccum += y;

				if (rollAccumCount++ == 0) {
					rollAccumTimestamp = timestamp;
				}

				if (rollAccumCount == rollAccumSize) {
					rollPanelSeries.add(rollAccumTimestamp, rollAccum
							/ rollAccumSize);
					resetRollAccum();
				}

				rollSeries.add(timestamp, y);
			}
		}
	}

	private static final float Y_SCALE = 8f;
	private static final float INCR = 1f;
	private final XYSeries accelSeries;
	private final RollGraphOverlay rollGraph;
	private final RoboStroke roboStroke;

	private final SensorDataSink privateAccellDataSink = new SensorDataSink() {		
		@Override
		public void onSensorData(long timestamp, Object value) {
			float[] values = (float[]) value;
			accelSeries.add(timestamp, values[0]);
		}
	};
	private boolean disabled = true;

	public AccellGraphView(Context context, float xRange, RoboStroke roboStroke) {
		super(context, xRange, XYSeries.XMode.ROLLING, Y_SCALE, INCR);

		this.roboStroke = roboStroke;
		
		accelSeries = multySeries.addSeries(new CyclicArrayXYSeries(XMode.ROLLING));

		rollGraph = new RollGraphOverlay(xRange, multySeries);
	}

	@Override
	public void setXRange(double val) {
		rollGraph.setXRange(val);
		super.setXRange(val);
	}

	@Override
	protected void drawGraph(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize) {

		rollGraph.drawRollPanels(canvas, rect, xAxisSize);

		super.drawGraph(canvas, rect, xAxisSize, yAxisSize);
	}

	@Override
	public void reset() {
		synchronized (multySeries) {
			rollGraph.reset();

			super.reset();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		disableUpdate(false);
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		disableUpdate(true);
		super.onDetachedFromWindow();
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;		
	}
	
	public void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (disable) {
				roboStroke.getAccelerationFilter().removeSensorDataSink(privateAccellDataSink);
				roboStroke.getRollScanner().removeSensorDataSink(rollGraph);
			} else {
				reset();
				roboStroke.getAccelerationFilter().addSensorDataSink(privateAccellDataSink);
				roboStroke.getRollScanner().addSensorDataSink(rollGraph);
			}

			this.disabled = disable;
		}
	}
}