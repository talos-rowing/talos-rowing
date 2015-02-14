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

import org.nargila.robostroke.ParamKeys;
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
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

class RecordSyncLeaderDialog extends Dialog {
	
	private static final int QR_BLANK_COUNT = 5;
	
	private final TextView text;
	private final RoboStrokeActivity owner;
	private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
	private final ColorDrawable[] colors = {new ColorDrawable(Color.RED), new ColorDrawable(Color.YELLOW), new ColorDrawable(Color.GREEN)};
	private boolean stopped;
	private final Handler handler = new Handler();
	private String tag;
	
	private Runnable runAfter;
	private ImageView qrView;
	
	RecordSyncLeaderDialog(RoboStrokeActivity owner) {
		super(owner);
		this.owner = owner;	
		LinearLayout layout = new LinearLayout(owner);
				
		layout.setOrientation(LinearLayout.VERTICAL);
		
		qrView = new ImageView(owner);
		qrView.setScaleType(ScaleType.CENTER);
		
		text = new TextView(owner);
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 62);
		text.setTextColor(Color.BLACK);
		text.setGravity(Gravity.CENTER_HORIZONTAL);
		
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);

		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		
		
		layout.addView(qrView, lp1);
		layout.addView(text, lp2);
		
		LayoutParams lp3 = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		
		setContentView(layout, lp3);
		layout.setBackgroundColor(Color.TRANSPARENT);
		getWindow().setBackgroundDrawable(colors[0]);
		getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
	}

	@Override
	protected void onStart() {
		
		final boolean isLandscape = owner.roboStroke.getParameters().getValue(ParamKeys.PARAM_SENSOR_ORIENTATION_LANDSCAPE.getId());

		owner.setLandscapeLayout(false);
		
		text.setText(tag == null ? "" : tag);
		
		getWindow().setBackgroundDrawable(colors[0]);
		
		stopped = false;
		
		owner.scheduler.submit(new Runnable() {
			@Override
			public void run() {

				try {
					for (int i = 0; i < (30 + QR_BLANK_COUNT) && !stopped; ++i) {
						
						final int qrres = QRCODES[i];
						
						final int counter = i + 1 - QR_BLANK_COUNT;
						
						handler.post(new Runnable() {							
							@Override
							public void run() {
								
								if (counter > 0) {
									text.setText((tag == null ? "" : tag) + "(" + counter + ")");
								}
								
								qrView.setBackgroundResource(qrres);
							}
						});
						
						if ((i - QR_BLANK_COUNT) % 10 == 0 || i == 30 + QR_BLANK_COUNT - 1) {
							tg.startTone(ToneGenerator.TONE_PROP_BEEP);
						}
						
						if (counter > 0) {
							owner.roboStroke.getBus().fireEvent(DataRecord.Type.RECORDING_COUNTDOWN, new Object[] {tag, counter});
						}
						
						Thread.sleep(100);						
					}					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dismiss();
					owner.setLandscapeLayout(isLandscape);
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
	
	
	private static final 	int[] QRCODES = {
		R.drawable.qrblank,
		R.drawable.qrblank,
		R.drawable.qrblank,
		R.drawable.qrblank,
		R.drawable.qrblank,
		R.drawable.qrs1,
		R.drawable.qrs2,
		R.drawable.qrs3,
		R.drawable.qrs4,
		R.drawable.qrs5,
		R.drawable.qrs6,
		R.drawable.qrs7,
		R.drawable.qrs8,
		R.drawable.qrs9,
		R.drawable.qrs10,
		R.drawable.qrs11,
		R.drawable.qrs12,
		R.drawable.qrs13,
		R.drawable.qrs14,
		R.drawable.qrs15,
		R.drawable.qrs16,
		R.drawable.qrs17,
		R.drawable.qrs18,
		R.drawable.qrs19,
		R.drawable.qrs20,
		R.drawable.qrs21,
		R.drawable.qrs22,
		R.drawable.qrs23,
		R.drawable.qrs24,
		R.drawable.qrs25,
		R.drawable.qrs26,
		R.drawable.qrs27,
		R.drawable.qrs28,
		R.drawable.qrs29,
		R.drawable.qrs30,
		R.drawable.qrs31,
		R.drawable.qrs32,
		R.drawable.qrs33,
		R.drawable.qrs34,
		R.drawable.qrs35,
		R.drawable.qrs36,
		R.drawable.qrs37,
		R.drawable.qrs38,
		R.drawable.qrs39,
		R.drawable.qrs40,
		R.drawable.qrs41,
		R.drawable.qrs42,
		R.drawable.qrs43,
		R.drawable.qrs44,
		R.drawable.qrs45,
		R.drawable.qrs46,
		R.drawable.qrs47,
		R.drawable.qrs48,
		R.drawable.qrs49,
		R.drawable.qrs50,
		R.drawable.qrs51,
		R.drawable.qrs52,
		R.drawable.qrs53,
		R.drawable.qrs54,
		R.drawable.qrs55,
		R.drawable.qrs56,
		R.drawable.qrs57,
		R.drawable.qrs58,
		R.drawable.qrs59,
		R.drawable.qrs60
};

}
