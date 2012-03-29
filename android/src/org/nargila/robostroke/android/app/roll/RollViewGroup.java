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

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.input.DataIdx;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.graph.DataUpdatable;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class RollViewGroup extends LinearLayout implements DataUpdatable {
	public enum ViewType {
		STROKE,
		RECOVERY,
		CURRENT,
		STROKE_RECOVERY
	}
	
	private class MyListener implements BusEventListener, SensorDataSink {

		@Override
		public void onBusEvent(DataRecord event) {
			float[] roll;
			RollView view;
			
			switch (event.type) {
			case RECOVERY_ROLL:
			case STROKE_ROLL:
				switch (mode) {
				case STROKE_RECOVERY:
					break;
				case RECOVERY:
					if (event.type != DataRecord.Type.RECOVERY_ROLL) {
						return;
					}
					break;
				case STROKE:
					if (event.type != DataRecord.Type.STROKE_ROLL) {
						return;
					}
					break;
				case CURRENT:
					return;
				}

				roll = (float[])event.data;
				ViewType type = 
					(event.type == DataRecord.Type.RECOVERY_ROLL) ? ViewType.RECOVERY : 
						ViewType.STROKE;
				view = rollViews[type.ordinal()];
				break;

			default:
				return;
			}

			view.setRoll(roll);		
		}
		@Override
		public void onSensorData(long timestamp, Object value) {
			switch (mode) {
			case CURRENT:

				float[] values = (float[]) value;

				RollView rollView = rollViews[ViewType.CURRENT.ordinal()];
				rollView.setRoll(values[DataIdx.ORIENT_ROLL]);
				break;
				default:
					// nothing to do
					break;
			}
		}		
	}
	
	private final MyListener privateListener = new MyListener();
	
	private ViewType mode = ViewType.STROKE_RECOVERY;
	
	private final RollView[] rollViews;

	private final RoboStroke roboStroke;

	private boolean disabled = true;
	
	public RollViewGroup(Context context, RoboStroke roboStroke) {
		super(context);
		
		this.roboStroke = roboStroke;

		setOrientation(LinearLayout.HORIZONTAL);

		setBackgroundColor(Color.WHITE);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		rollViews = new RollView[] {
				new RollView(context, Pair.create(RollView.ValueType.AVG, RollView.ValueType.MAX)),
				new RollView(context, Pair.create(RollView.ValueType.AVG, RollView.ValueType.MAX)),
				new RollView(context, Pair.create(RollView.ValueType.CUR, (RollView.ValueType)null)),
		};
		
		
		int count = 0;
		for (RollView rollView: rollViews) {
			rollView.setup(inflater);
			rollView.setLabel(ViewType.values()[count++].name().toLowerCase());
			LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			layoutParams.weight = 1;
			addView(rollView, layoutParams);
		}
		
		setMode(ViewType.STROKE_RECOVERY);
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
	
	public void setMode(ViewType mode) {
		if (mode == null) { // this means a request to flick to next mode
			int idx = (this.mode.ordinal() + 1) % ViewType.values().length;
			
			mode = ViewType.values()[idx];
		}
		
		this.mode = mode;
				
		RollView[] views;
		RollView.Mode viewMode;
		int rightMargine;
		
		switch (mode) {
		case STROKE_RECOVERY:
			views = new RollView[] {
					rollViews[ViewType.STROKE.ordinal()],
					rollViews[ViewType.RECOVERY.ordinal()]
			};
			rightMargine = 1;
			viewMode = RollView.Mode.SMALL;
			break;
			default:
				views = new RollView[] {rollViews[mode.ordinal()]};
				viewMode = RollView.Mode.BIG;
				rightMargine = 0;
				break;
		}
		
		for (RollView view: rollViews) {
			view.setVisibility(GONE);
			((LinearLayout.LayoutParams)view.getLayoutParams()).rightMargin = 0;
		}
				
		int i = 0;
		for (RollView view: views) {
			view.setMode(viewMode);
			
			if (++i < views.length) {
				((LinearLayout.LayoutParams)view.getLayoutParams()).rightMargin = rightMargine;
			}
			
			view.setVisibility(VISIBLE);
		}
	}
	
	@Override
	public boolean isDisabled() {
		return disabled ;		
	}
	
	@Override
	public void disableUpdate(boolean disable) {
		if (this.disabled != disable) {
			if (!disable) {
				roboStroke.getRollScanner().addSensorDataSink(privateListener);
				roboStroke.getBus().addBusListener(privateListener);
			} else {
				roboStroke.getRollScanner().removeSensorDataSink(privateListener);
				roboStroke.getBus().removeBusListener(privateListener);
			}

			this.disabled = disable;
		}
	}

	@Override
	public void reset() {
		// not a serious problem if we don't reset these graphs		
	}	
}
