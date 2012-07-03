package org.nargila.robostroke.android.app;

import org.nargila.robostroke.input.DataRecord;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

class RecordSyncLeaderDialog extends Dialog {
	private final View view; 
	private final RoboStrokeActivity owner;
	private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
	private final ColorDrawable[] colors = {new ColorDrawable(Color.RED), new ColorDrawable(Color.YELLOW), new ColorDrawable(Color.GREEN)};
	private boolean stopped;
	private final Handler handler = new Handler();
	
	RecordSyncLeaderDialog(RoboStrokeActivity owner) {
		super(owner);
		this.owner = owner;	
		this.view = new View(owner);
		setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		view.setBackgroundColor(Color.TRANSPARENT);
		getWindow().setBackgroundDrawable(colors[0]);
	}

	@Override
	protected void onStart() {
		
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
						owner.roboStroke.getBus().fireEvent(DataRecord.Type.RECORDING_COUNTDOWN, i - colors.length);
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
	}
}
