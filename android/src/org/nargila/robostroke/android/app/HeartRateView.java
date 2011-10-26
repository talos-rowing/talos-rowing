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
import org.nargila.robostroke.ui.DataUpdatable;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HeartRateView extends FrameLayout implements DataUpdatable {

	private final Handler handler = new Handler();
	
	private TextView bpm_text;

	private boolean addDot = true;

	private final RoboStroke roboStroke;
	
	private final StrokeListener busListener = new StrokeListener() {
		@Override
		public void onStrokeEvent(final StrokeEvent event) {
			switch (event.type) {
			case HEART_BPM:
				final int bpm = (Integer)event.data;
				
				updateDisplay(bpm);
			}
		}

	};

	private boolean disabled = true;

	public HeartRateView(Context context, RoboStroke roboStroke) {
		super(context);
		
		this.roboStroke = roboStroke;
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		setup(inflater);		
	}

	private void updateDisplay(final int bpm) {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				bpm_text.setText(String.format("%s%3s", (addDot ? "." : " "), bpm + ""));
				addDot = !addDot;
			}
		});
	}

	private void setup(LayoutInflater inflater) {
		View layout = inflater.inflate(R.layout.hrm_view, null);
		addView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		this.bpm_text = (TextView) layout.findViewById(R.id.bpm);
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
				roboStroke.getBus().addStrokeListener(busListener);
			} else {
				roboStroke.getBus().removeStrokeListener(busListener);
			}

			this.disabled = disable;
		}
	}

	@Override
	public void reset() {
		addDot = false;
		updateDisplay(0);
	}
}
