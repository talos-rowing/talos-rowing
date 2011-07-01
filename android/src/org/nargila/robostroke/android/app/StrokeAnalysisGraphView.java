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

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.StrokeEvent;
import org.nargila.robostroke.StrokeListener;
import org.nargila.robostroke.android.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.android.graph.LineGraphView;
import org.nargila.robostroke.android.graph.MultiXYSeries;
import org.nargila.robostroke.android.graph.XYSeries;
import org.nargila.robostroke.android.graph.XYSeries.XMode;
import org.nargila.robostroke.common.NumberHelper;
import org.nargila.robostroke.common.filter.LowpassFilter;
import org.nargila.robostroke.input.DataIdx;
import org.nargila.robostroke.input.SensorDataSink;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokeAnalysisGraphView extends FrameLayout implements DataUpdatable {
	
	private static final int MIN_STROKE_RATE = 10;

	private final Handler mainHanlder = new Handler();
	
	private int cur = 0;
	private int next = 1;
	
	private final StrokeAnalysisGraph[] graphs;

	private boolean aboveStrokeRateTreshold;

	private final RoboStroke roboStroke;
	
	public StrokeAnalysisGraphView(Context context, RoboStroke roboStroke) {
		super(context);
		
		this.roboStroke = roboStroke;
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		graphs = new StrokeAnalysisGraph[] {
				new StrokeAnalysisGraph(context),
				new StrokeAnalysisGraph(context)
		};
				
		graphs[next].setVisibility(GONE);

		for (View v: graphs) {
			addView(v, layoutParams);
		}
	}

	
	private final SensorDataSink privateRollDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (aboveStrokeRateTreshold) {
				synchronized (graphs) {
					graphs[next].getRollSink().onSensorData(timestamp, value);
				}
			}
		}
	};

	private final SensorDataSink privateAccelDataSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (aboveStrokeRateTreshold) {
				synchronized (graphs) {
					graphs[next].getAccelSink().onSensorData(timestamp, value);
				}
			}
		}
	};

	protected boolean needReset;

	
	private final StrokeListener privateBusListener = new StrokeListener() {
		
		@Override
		public void onStrokeEvent(StrokeEvent event) {
			switch (event.type) {
			case STROKE_RATE:
				aboveStrokeRateTreshold =  (Integer)event.data > MIN_STROKE_RATE;
				break;
			case STROKE_POWER_END:
				boolean hasPower = (Float)event.data > 0;
				
				if (!hasPower) {
					resetNext();					
				}
				
				if (aboveStrokeRateTreshold) {
					if (!needReset) {
						synchronized (graphs) {


							graphs[cur].reset();

							if (cur == 0) {
								cur = 1;
								next = 0;
							} else {
								cur = 0;
								next = 1;
							}
							mainHanlder.post(new Runnable() {

								@Override
								public void run() {
									graphs[next].setVisibility(GONE);
									graphs[cur].setVisibility(VISIBLE);
									graphs[cur].invalidate();
								}
							});
						}
					}

					needReset = false;
				}
			}
		}
	};

	private boolean disabled = true;

	public void reset() {
		graphs[cur].reset();
		graphs[next].reset();
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
	
	@Override
	public void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (!disable) {
				roboStroke.getBus().addStrokeListener(privateBusListener);
				roboStroke.getAccelerationFilter().addSensorDataSink(privateAccelDataSink);
				roboStroke.getRollScanner().addSensorDataSink(privateRollDataSink);
			} else {
				resetNext();
				roboStroke.getAccelerationFilter().removeSensorDataSink(privateAccelDataSink);
				roboStroke.getRollScanner().removeSensorDataSink(privateRollDataSink);
				roboStroke.getBus().removeStrokeListener(privateBusListener);
			}	

			this.disabled = disable;
		}
	}


	private void resetNext() {
		needReset = true;
		graphs[next].reset();
	}
}

enum RollOverlayType {
	BACKGROUND(1),
	TOP(0.2f),
	BOTTOM(0.2f);
	
	RollOverlayType(float clipHeightPercent) {
		this.clipHeightPercent = clipHeightPercent;
	}
	
	final float clipHeightPercent;
}

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
class StrokeAnalysisGraph extends LineGraphView { 
	private static final long MAX_TIME_CAPTURE = TimeUnit.SECONDS.toNanos(10);
	private long timeCaptureStart;
	RollOverlayType rollOverlayType = RollOverlayType.TOP;
	
	class RollGraphOverlay  {
		
		private static final double ROLL_PANNEL_DIM_FACTOR = 0.60;
		
		private final int rollAccumSize = 1;
		private int rollAccumCount;
		private float rollAccum;
		
		private final LowpassFilter filter = new LowpassFilter(.5f);
		
		private long rollAccumTimestamp;
		
		
		RollGraphOverlay(MultiXYSeries multySeries) {
		}
		
		void drawRollPanels(Canvas canvas, Rect rect, double xAxisSize) {
			XYSeries ser = rollPanelSeries;
			
			final int len = ser.getItemCount();
			
			if (len > 0) {
				final int red = Color.RED;
				final int green = Color.GREEN;
				
				Paint paint = new Paint() {
					{
						setStyle(Style.FILL);
						setAntiAlias(false);
						setStrokeWidth(0);
					}
				};
				
				final double maxYValue = Y_SCALE / 2;
				final double scaleX = rect.width() / xAxisSize;
				
				final double minX = multySeries.getMinX();
				
				double startX = ser.getX(0);
				double stopX;
				
				for (int i = 1; i < len; ++i, startX = stopX) {
					stopX = ser.getX(i);
					
					double avgY = Math.min(ser.getY(i), maxYValue);
					
					int color = avgY > 0 ? green : red;
					int alpha = (int) ((avgY / maxYValue) * 255 * (rollOverlayType == RollOverlayType.BACKGROUND ? ROLL_PANNEL_DIM_FACTOR : 1));
					
					paint.setColor(color);
					paint.setAlpha(Math.abs(alpha));
					
					float left = (float) ((startX - minX) * scaleX);
					float right = (float) (((stopX - minX) * scaleX));
					
					canvas.drawRect(left, rect.top, right, rect.bottom, paint);
				}
			}
		}
		
		void reset() {
			synchronized (multySeries) {
				resetRollAccum();
			}
		}
		
		private void resetRollAccum() {
			rollAccum = 0;
			rollAccumCount = 0;
		}
		
		void updateRoll(long timestamp, float roll) {
			synchronized (multySeries) {

				float y = filter
				.filter(new float[] {roll})[0];

				rollAccum += y;

				if (rollAccumCount++ == 0) {
					rollAccumTimestamp = timestamp;
				}

				if (rollAccumCount == rollAccumSize) {
					rollPanelSeries.add(rollAccumTimestamp, rollAccum
							/ rollAccumSize);
					resetRollAccum();
				}
			}
		}
	}
	
	private static final float Y_SCALE = 8f;
	private static final float INCR = 1f;
	private final CyclicArrayXYSeries accelSeries = new CyclicArrayXYSeries(XMode.GROWING);
	private final CyclicArrayXYSeries rollPanelSeries = new CyclicArrayXYSeries(XMode.GROWING) {
		{
			setIndependantYAxis(true);				
		}
	};
	
	private final Paint rollGraphPaint = new Paint() {
		{
			setStyle(Style.STROKE);
			setColor(Color.YELLOW);
			setAlpha(170);
		}
	};
	
	private final CyclicArrayXYSeries rollSeries = new CyclicArrayXYSeries(XMode.GROWING) {
		{
			setRenderer(new Renderer(rollGraphPaint, null));
			setIndependantYAxis(true);
			setyAxisSize(Y_SCALE);
			setxRange(0);
		}
	};
	
	private final RollGraphOverlay rollGraph;
	private final int next = 1;
	
	public StrokeAnalysisGraph(Context context) {
		super(context, Y_SCALE, INCR, null);
		
		multySeries = new MultiXYSeries(0, XYSeries.XMode.GROWING) {
			@Override
			public double getxRange() {
				return accelSeries.getMaxX() - accelSeries.getMinX();
			}
		};			
		
		multySeries.addSeries(rollPanelSeries, false);
		multySeries.addSeries(accelSeries, false);
		multySeries.addSeries(rollSeries, false);
		
		rollGraph = new RollGraphOverlay(multySeries);
	}
	
	@Override
	public void setXRange(double val) {
		// disable external call to setXRange()
	}
	
	
	@Override
	protected void drawSeries(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize, XYSeries series) {
		
		if (series != rollPanelSeries) { 
			super.drawSeries(canvas, rect, xAxisSize, yAxisSize, series);
		}
		
	}
	
	@Override
	protected void drawGraph(Canvas canvas, Rect rect, double xAxisSize,
			double yAxisSize) {
		
		Rect rollBarsRect = new Rect(rect);
		
		int rollBarsHeight = (int) (rollOverlayType.clipHeightPercent * rect.height());
		
		switch (rollOverlayType) {
		case BACKGROUND:
			// nothing to do
			break;
		case BOTTOM:
			rollBarsRect.top = rollBarsRect.bottom - rollBarsHeight;
			rect.bottom -= rollBarsHeight;
			break;
		case TOP:
			rollBarsRect.bottom = rollBarsRect.top + rollBarsHeight;
			rect.top += rollBarsHeight;
			break;
		}
		
		rollGraph.drawRollPanels(canvas, rollBarsRect, xAxisSize);
		
		super.drawGraph(canvas, rect, xAxisSize, yAxisSize);
	}
	
	
	@Override
	public void reset() {
		synchronized (multySeries) {
			rollGraph.reset();
			timeCaptureStart = 0;
			super.reset();
		}
	}
	
	private boolean checkCaptureTimeInRange(long timestamp) {
		
		if (timeCaptureStart == 0) {
			timeCaptureStart = timestamp;
			return true;
		} 
		
		return (Math.abs(timestamp - timeCaptureStart) < MAX_TIME_CAPTURE);
	}
	
	SensorDataSink rollSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (!checkCaptureTimeInRange(timestamp)) {
				return;
			}
			
			float[] values = (float[])value;
			float y = values[DataIdx.ORIENT_ROLL];
			rollGraph.updateRoll(timestamp, y);
			rollSeries.add(timestamp, NumberHelper.validRange(-(Y_SCALE / 2), y, Y_SCALE / 2));
		}
		
	};
	
	
	SensorDataSink getRollSink() {
		return rollSink;
	}
	
	SensorDataSink accelSink = new SensorDataSink() {
		
		@Override
		public void onSensorData(long timestamp, Object value) {
			if (!checkCaptureTimeInRange(timestamp)) {
				return;
			}
			float[] values = (float[]) value;
			accelSeries.add(timestamp, values[0]);
		}
	};

	SensorDataSink getAccelSink() {
		return accelSink;
	}
}