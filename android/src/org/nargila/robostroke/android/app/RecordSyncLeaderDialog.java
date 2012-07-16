/*
 * Copyright (c) 2012 Tal Shalif
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

import org.nargila.robostroke.data.DataRecord;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

class RecordSyncLeaderDialog extends Dialog {
	
	private final TextView text;
	private final RoboStrokeActivity owner;
	private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
	private final ColorDrawable[] colors = {new ColorDrawable(Color.RED), new ColorDrawable(Color.YELLOW), new ColorDrawable(Color.GREEN)};
	private boolean stopped;
	private final Handler handler = new Handler();
	private String tag;
	
	private Runnable runAfter;
	
	RecordSyncLeaderDialog(RoboStrokeActivity owner) {
		super(owner);
		this.owner = owner;	
		FrameLayout layout = new FrameLayout(owner);
		text = new TextView(owner);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 62);
		text.setTextColor(Color.BLACK);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		layout.addView(text, lp);
		
		LayoutParams lp2 = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		setContentView(layout, lp2);
		layout.setBackgroundColor(Color.TRANSPARENT);
		getWindow().setBackgroundDrawable(colors[0]);
		getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onStart() {
		
		text.setText(tag == null ? "" : tag);
		
		getWindow().setBackgroundDrawable(colors[0]);
		
		stopped = false;
		
		owner.scheduler.submit(new Runnable() {
			@Override
			public void run() {

				try {
					for (int i = 0; i < colors.length && !stopped; ++i) {
						
						final ColorDrawable color = colors[i];
						
						handler.post(new Runnable() {							
							@Override
							public void run() {
								getWindow().setBackgroundDrawable(color);
							}
						});
						
						tg.startTone(ToneGenerator.TONE_PROP_BEEP);
						owner.roboStroke.getBus().fireEvent(DataRecord.Type.RECORDING_COUNTDOWN, (tag == null ? "" : tag + ",") + (i - colors.length + 1));
						Thread.sleep(1000);						
					}					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dismiss();
				}
			}
		});
	}
	
	@Override
	protected void onStop() {		
		stopped = true;		
		super.onStop();
		
		if (runAfter != null) {
			runAfter.run();
		}
	}
	
	void setRunAfter(Runnable runAfter) {
		this.runAfter = runAfter;
	}
	
	void setTag(String tag) {
		this.tag = tag;
	}
}
