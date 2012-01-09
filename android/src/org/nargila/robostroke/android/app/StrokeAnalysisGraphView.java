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
import org.nargila.robostroke.StrokeEvent;
import org.nargila.robostroke.StrokeListener;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.StrokeAnalysisGraph;
import org.nargila.robostroke.ui.graph.android.UILiaisonViewImpl;

import android.content.Context;
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
				new StrokeAnalysisGraph(new UILiaisonViewImpl(new View(context))),
				new StrokeAnalysisGraph(new UILiaisonViewImpl(new View(context)))
		};
				
		view(graphs[next]).setVisibility(GONE);

		for (StrokeAnalysisGraph g: graphs) {
			addView(view(g), layoutParams);
		}
	}

	private View view(StrokeAnalysisGraph g) {
		return (View)g.getUiLiaison().getComponent();
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
									view(graphs[next]).setVisibility(GONE);
									view(graphs[cur]).setVisibility(VISIBLE);
									view(graphs[cur]).invalidate();
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
