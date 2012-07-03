package org.nargila.robostroke.android.app;

import org.nargila.robostroke.input.DataRecord;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;

class RecordSyncLeaderDialog {
	private static final int COUNTDOWN = 3;
	
	final ProgressDialog progress;
	private final RoboStrokeActivity owner;
	private final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
	
	RecordSyncLeaderDialog(RoboStrokeActivity owner) {
		this.owner = owner;
		progress = new ProgressDialog(owner);
		progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress.setMax(COUNTDOWN);
		progress.setIndeterminate(false);
		progress.setCancelable(true);
		progress.setMessage("Countdown..");
		
		progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				progress.dismiss();
			}
		});
	}
	
	void start() {				

		progress.setProgress(0);

		progress.show();	
		
		owner.scheduler.submit(new Runnable() {
			@Override
			public void run() {

				try {
					for (int i = 0; i <= COUNTDOWN && progress.isShowing(); ++i) {
						tg.startTone(ToneGenerator.TONE_PROP_BEEP);
						owner.roboStroke.getBus().fireEvent(DataRecord.Type.RECORDING_COUNTDOWN, i - COUNTDOWN);
						progress.setProgress(i);

						Thread.sleep(1000);						
					}					
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					progress.dismiss();
				}
			}
		});

	}
}
