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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.acra.ErrorReporter;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.StrokeEvent;
import org.nargila.robostroke.android.app.roll.RollViewGroup;
import org.nargila.robostroke.android.common.FileHelper;
import org.nargila.robostroke.android.common.NotificationHelper;
import org.nargila.robostroke.android.common.PreviewFrameLayout;
import org.nargila.robostroke.android.common.ScreenStayupLock;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.common.SimpleLock;
import org.nargila.robostroke.input.ErrorListener;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.android.AccellGraphView;
import org.nargila.robostroke.ui.graph.android.StrokeGraphView;
import org.nargila.robostroke.ui.graph.android.StrokePowerGraphView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * AndroidRoboStroke application entry point and main activity/
 */
public class RoboStrokeActivity extends Activity implements RoboStrokeConstants , ParameterListenerOwner {

	private static final String MIME_TYPE_ROBOSTROKE_SESSION = "application/vnd.robostroke.session";

	private static final String ROBOSTROKE_DATA_DIR = "RoboStroke";

	private static final int RECORDING_ON_COLOUR = Color.argb(255, 255, 0, 0);

	private static final int REPLAYING_ON_COLOUR = Color.argb(255, 0, 255, 0);

	private static final int HIGHLIGHT_PADDING_SIZE = 5;
	
	private static final Logger logger = LoggerFactory.getLogger(TAG);
	
	private final ParameterListenerRegistration[] listenerRegistrations = {
			new ParameterListenerRegistration(ParamKeys.PARAM_SESSION_RECORDING_ON, new ParameterChangeListener() {
				
				@Override
				public void onParameterChanged(Parameter<?> param) {
					final boolean recording = (Boolean)param.getValue();
					
					setRecordingOn(recording);
					
					final int padding = recording ? HIGHLIGHT_PADDING_SIZE : 0;
					
					updateRecordingStateIndication(padding, RECORDING_ON_COLOUR);
				}
			})
	};
	
	private final Intent hrmService = new Intent(HRM_SERVICE_ACTION);

	private AccellGraphView accel_graph;
	private HeartRateView heart_rate_view;
	private StrokePowerGraphView stroke_power_graph;
	private StrokeAnalysisGraphView stroke_analysis_graph;
	private StrokeGraphView stroke_graph;
	private StrokePowerBarGraphView stroke_power_bar_graph;
	private RollViewGroup roll_view_group;
	private boolean recordingOn;
	final Handler handler = new Handler();
	private boolean tiltFreezeOn;
	
	private final long graphXRange = TimeUnit.SECONDS.toNanos(8);

	private ScheduledExecutorService scheduler;
	
	final RoboStroke roboStroke = new RoboStroke(
			new AndroidLocationDistanceResolver());

	public NotificationHelper notificationHelper;

	private MetersDisplayManager metersDisplayManager;

	private final ScreenStayupLock screenLock;
	private Menu menu;
	private boolean replayPaused;

	private HXMDataReceiver hxmDataReceiver;

	
	PreferencesHelper preferencesHelper;

	private boolean stopped = true;

	GraphPanelDisplayManager graphPanelDisplayManager;

	private static AlertDialog m_AlertDlg;
	
	class PendingReset implements Runnable {
		int delay = 250;
		private ScheduledFuture<?> pending;
		boolean[] restoreStates;
		
		synchronized void trigger() {
			if (pending == null) {
				logger.debug("initializing pending graph reset");
				restoreStates = resetGraphs(true, null);
			} else {
				logger.debug("deffering pending graph reset");
				pending.cancel(true);
			}
			
			pending = scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);				
		}
		@Override
		public synchronized void run() {
			resetGraphs(true, restoreStates);
			pending = null;
		}
	}
	
	private class SessionFileHandler {
		
		private void resetSessionRecording() {

			try {
				if (recordingOn && !isReplaying()) {
					File logFile = recordingOn ? FileHelper.getFile(ROBOSTROKE_DATA_DIR, "" + System.currentTimeMillis() + "-dataInput.txt") : null;
							
					roboStroke.setDataLogger(logFile);
					
					roboStroke.getBus().fireEvent(StrokeEvent.Type.UUID, preferencesHelper.getUUID());
							
				} else {
					roboStroke.setDataLogger(null);
				}
			} catch (IOException e) {
				logger.error("failed to start session data logger", e);
			}
		}
		
		private void shareSession() {
			File toShare = replayFile.file;

			if (toShare != null) {


				if (toShare.length() > 30000000) {
					if (m_AlertDlg != null) {
						m_AlertDlg.cancel();
					}

					m_AlertDlg = new AlertDialog.Builder(RoboStrokeActivity.this).setMessage(
							getString(
									R.string.session_record_upload_size_too_large,
									90)).setTitle(
											getString(R.string.file_too_large)).setCancelable(true)
											.show();
				} else {

					File tmpdir = FileHelper.getFile(ROBOSTROKE_DATA_DIR, "tmp");

					tmpdir.mkdir();

					String name = toShare.getName().replaceFirst("txt$", "trsd");

					final File trsd = new File(tmpdir, name);			


					try {


						Runnable runnable = new Runnable() {

							@Override
							public void run() {

								Intent intent = new Intent(Intent.ACTION_SEND);

								intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);
								intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(trsd));
								intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);
								intent.putExtra(Intent.EXTRA_EMAIL,new String[] { getString(R.string.default_session_record_dispatch_address) });
								intent.putExtra(Intent.EXTRA_SUBJECT, String.format("Talos Rowing Session: %s", preferencesHelper.getUUID()));
								intent.putExtra(Intent.EXTRA_TEXT, "Description:\n");
								intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);									

								startActivityForResult(Intent.createChooser(intent, "Email:"), 42);
							}
						};

						prepareData(Uri.fromFile(toShare), trsd, 1, runnable);

					} catch (Exception e) {
						reportError(e, "error preparing session data for sharing");

					}
				}
			}
		}

		/**
		 * check either intent contains session data to replay
		 * @param intent
		 * @return
		 */
		private boolean startPreviewIntent(final Intent intent) {
				
			if (Intent.ACTION_VIEW.equals(intent.getAction()) && MIME_TYPE_ROBOSTROKE_SESSION.equals(intent.getType())) {
		
				Uri inputUri = intent.getData();
				
				File outFile;
				
				try {
					
					if (!"file".equals(inputUri.getScheme())) {
						File dataFile = createTmpSessionOutputFile(".trsd");
						FileOutputStream fout = new FileOutputStream(dataFile);
						InputStream ins = getContentResolver().openInputStream(inputUri);
					
						byte[] buff = new byte[4096];
						
						for (int i = ins.read(buff); i != -1; i = ins.read(buff)) {
							fout.write(buff, 0, i);
						}
						
						fout.close();
						ins.close();
		
						inputUri = Uri.fromFile(dataFile);
					}
					
					outFile = createTmpSessionOutputFile(".txt");
					
				} catch (IOException e) {
					reportError(e, "preview error: failed to create preview file");
					
					return false;
				}
		
				final SessionFileInfo sessionFileInfo = new SessionFileInfo(outFile, true);
				
				prepareData(inputUri, outFile, -1, new Runnable() {
		
					@Override
					public void run() {
						restart(sessionFileInfo);
					}
				});
		
		
				return true;
		
			}
			
			return false;
		}
		
		private void prepareData(final Uri inputUri, final File outFile, int compressDecompress, final Runnable runnable) {
			
			final boolean decompress = compressDecompress == -1;
			final boolean compress = compressDecompress == 1;
			
			final ProgressDialog progress = new ProgressDialog(RoboStrokeActivity.this);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setMessage("Preparing...");
			
			final AtomicReference<Future<?>> job = new AtomicReference<Future<?>>();
			
			progress.setMax(100);
		
			progress.setIndeterminate(true);
			
			
			progress.setCancelable(true);
			
			progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					job.get().cancel(true);
					progress.dismiss();
				}
			});
			
			progress.show();
			
			final AtomicBoolean res = new AtomicBoolean(true);
			
			synchronized (res) {
				Runnable jobRunnable = new Runnable() {
		
					@Override
					public void run() {
						byte[] buff = new byte[4096];
		
						long accum = 0;
						
		
		
						try {
							
							Pair<InputStream, Long> openInfo;
							
							try {
								openInfo = openIntentData(inputUri);
							} catch (IOException e) {
								throw new MyError("can not open " + inputUri, e);
							}
							
							long size = openInfo.second;
							
							if (size != -1) {
								handler.post(new Runnable() {
									
									@Override
									public void run() {
										progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
										progress.setIndeterminate(false);
									}
								});
							}
							
							InputStream is = openInfo.first;
							OutputStream os;
							try {
								os = new FileOutputStream(outFile);
							} catch (FileNotFoundException e) {
								throw new MyError("can not open " + outFile, e);
							}
							
							try {
								if (compress) {
									os = new GZIPOutputStream(os);
								} else if (decompress) {
									is = new GZIPInputStream(is);
								} else {
									throw new AssertionError("HDIGH!");
								}
							} catch (IOException e) {
								throw new MyError("failed to set stream compression/decompression", e);
							} catch (AssertionError e) {
								throw new MyError("internal error", e);
							}
							
							while (!Thread.currentThread().isInterrupted()) {
								
								int i;
								try {
									i = is.read(buff);
								} catch (IOException e) {
									throw new MyError("data read error", e);
								} 
								
								if (i < 0) {
									break;
								}
								
								try {
									os.write(buff, 0, i);
								} catch (IOException e) {
									throw new MyError("data write error", e);
								}
		
								accum += i;
		
								if (size != -1) {
									int pos = (int) (100.0 * (accum / (double) size));
									progress.setProgress(pos);
								}
							}
							
							try {
								is.close();
								os.close();
							} catch (IOException e) {
								logger.error("failed to close open streams", e);
							}
		
							if (!job.get().isCancelled()) {
								if (runnable == null) {
									res.set(true);
								} else {
									handler.post(runnable);
								}
							}
						} catch (MyError e) {
							reportError(e.getCause(), "session data preparation failed: " + e.getMessage());
						} finally {
							progress.dismiss();
		
							synchronized (res) {
								res.notify();
							}
						}
					}
				};
		
				job.set(scheduler.submit(jobRunnable));
		
				if (runnable == null) {
					try {
						res.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		private File createTmpSessionOutputFile(String suffix) throws IOException {
			File tmpdir = FileHelper.getFile(ROBOSTROKE_DATA_DIR, "tmp");
			
			tmpdir.mkdir();
			
			File tmp =  File.createTempFile("dataInput-", suffix, tmpdir);
			
			tmp.deleteOnExit();
			
			return tmp;
		}

		private Pair<InputStream, Long> openIntentData(final Uri inputUri) throws MyError, IOException {
			
			InputStream is;
			long sz;
			
			if ("file".equals(inputUri.getScheme())) {
				File f = new File(inputUri.getPath());
				sz = f.length();
				is = new FileInputStream(f);
			} else {
				ParcelFileDescriptor bla = getContentResolver().openFileDescriptor(inputUri, "r");
				sz = bla.getStatSize();
				is = new FileInputStream(bla.getFileDescriptor());
			}
			
			return Pair.create(is, sz);
		
		}
		
	}

	@SuppressWarnings("serial")
	private static class MyError extends Throwable {
		
		public MyError(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}			
	}

	private static class SessionFileInfo {
		public final File file;
		public final boolean temporary;
		
		public SessionFileInfo(File file, boolean temporary) {
			this.file = file;
			this.temporary = temporary;
		}		
	}

	private final SessionFileHandler sessionFileHandler = new SessionFileHandler();

	private final PendingReset pendingReset = new PendingReset();

	private SessionFileInfo replayFile;

	public RoboStrokeActivity() {
		
		Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(this));
		
		screenLock = new ScreenStayupLock(this, getClass().getSimpleName());
		
		roboStroke.getParameters().addListeners(this);		
	}

	public Handler getHandler() {
		return handler;
	}

	private void setPaused(boolean paused) {
		replayPaused = paused;
		roboStroke.getDataInput().setPaused(replayPaused);
	}

	private void togglePause() {
		setPaused(!replayPaused);
	}

	public boolean isRecordingOn() {
		return recordingOn;
	}

	public synchronized void setRecordingOn(boolean recordingOn) {
		this.recordingOn = recordingOn;		
		
		if (!stopped) {
			sessionFileHandler.resetSessionRecording();
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		

		this.accel_graph = new AccellGraphView(this, graphXRange, roboStroke);
		this.heart_rate_view = new HeartRateView(this, roboStroke);
		this.stroke_power_graph = new StrokePowerGraphView(this, roboStroke);
		stroke_analysis_graph = new StrokeAnalysisGraphView(this, roboStroke);
		this.stroke_graph = new StrokeGraphView(this, graphXRange, roboStroke);
		this.stroke_power_bar_graph = new StrokePowerBarGraphView(this, roboStroke);
		this.roll_view_group = new RollViewGroup(this, roboStroke);
		
		notificationHelper = new NotificationHelper(this, R.drawable.icon_small322);

		metersDisplayManager = new MetersDisplayManager(this);

		preferencesHelper = new PreferencesHelper(this); // handles preferences -> parameter synchronization

		ErrorReporter.getInstance().addCustomData("uuid", preferencesHelper.getUUID());
		

		roboStroke.setErrorListener(new ErrorListener() {

			@Override
			public void onError(Exception e) {
				notificationHelper.notifyError(ROBOSTROKE_ERROR,
						e.getMessage(), "robostroke error", "robostroke error");
			}
		});
		
		roboStroke.getAccelerationFilter().addSensorDataSink(metersDisplayManager);

		initGraphDisplayToggling();
		
		View.OnClickListener recordingClickListener = new View.OnClickListener() {
			boolean recording;
			
			@Override
			public void onClick(View arg0) {
				if (!isReplaying() && FileHelper.hasExternalStorage()) {
					roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON, !recording);
					recording = !recording;
				}
			}
		};

		findViewById(R.id.distance_meter).setOnClickListener(recordingClickListener);

		Runtime.getRuntime().addShutdownHook(new Thread("cleanTmpDir on exit hook") {
			@Override
			public void run() {
				cleanTmpDir();
			}
		});
		
		start(null);

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		sessionFileHandler.startPreviewIntent(getIntent());		
		super.onPostCreate(savedInstanceState);
	}
	
		
	private void reportError(Throwable throwable, String msg) {
		logger.error(msg, throwable);

		notificationHelper.notifyError(ROBOSTROKE_ERROR, msg + ": " + throwable.getMessage(),
				"robostroke error", "robostroke error");

		notificationHelper
				.toast(msg + ". See error notification");
	}


	private void initGraphDisplayToggling() {

		View[] viewArr = {
				new PreviewFrameLayout(this, R.drawable.graph_accel_400, accel_graph),
				new PreviewFrameLayout(this, R.drawable.graph_power_bar_400, stroke_power_bar_graph), 
				new PreviewFrameLayout(this, R.drawable.graph_analysis_400, stroke_analysis_graph),
				new PreviewFrameLayout(this, R.drawable.graph_power_400, stroke_power_graph), 
				new PreviewFrameLayout(this, R.drawable.graph_stroke_400, stroke_graph), 
				roll_view_group, 
				heart_rate_view
		};
		
		LinkedList<View> views = new LinkedList<View>();
		
		for (View view: viewArr) { // add only non black-listed views
			if (view.getTag() == null || !view.getTag().equals("blackList")) {
				views.add(view);
			}
		}
		
		FrameLayout[] slots = {
			(FrameLayout) findViewById(R.id.graph_frame1),
			(FrameLayout) findViewById(R.id.graph_frame2),
			(FrameLayout) findViewById(R.id.graph_frame3) 
		};
		
		graphPanelDisplayManager = 
			new GraphPanelDisplayManager(this, (LinearLayout) findViewById(R.id.graph_container), slots, views.toArray(new View[views.size()]));
		

		for (final View view: views) {
						
			final GestureDetector gd = new GestureDetector(
					new GestureDetector.SimpleOnGestureListener() {
						
						@Override
						public boolean onFling(MotionEvent e1, MotionEvent e2,
								float velocityX, float velocityY) {
							float vx = Math.abs(velocityX);
							float vy = Math.abs(velocityY);
							if (vx > vy) { // left/right fling: forward/rewind
											// replay
								if (isReplaying()) {
									pendingReset.trigger(); // post graph data flush/reset request
									roboStroke.getDataInput().skipReplayTime(
											velocityX);
								}
							} else if (velocityY > 0) { // fling down
								if (isReplaying())
									togglePause();
							} else { // fling up
								graphPanelDisplayManager.toggleSlotView((FrameLayout) view.getParent());
							}

							return true;
						}

						@Override
						public boolean onDoubleTap(MotionEvent event) {
							graphPanelDisplayManager.toggleSlotCount(view);

							return true;
						};

						@Override
						public boolean onSingleTapConfirmed(MotionEvent event) {
							if (view == roll_view_group) {
								roll_view_group.setMode(null);
								return true;
							}

							return super.onSingleTapConfirmed(event);
						}

						@Override
						public void onLongPress(MotionEvent event) {

							if (view == roll_view_group) {
								if (tiltFreezeOn) {
									showDialog(R.layout.tilt_freeze_dialog);
								} else {
									if (m_AlertDlg != null) {
										m_AlertDlg.cancel();
									}


									m_AlertDlg = new AlertDialog.Builder(RoboStrokeActivity.this)
									.setPositiveButton("Continue", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
											showDialog(R.layout.tilt_freeze_dialog);
										}
									})
									.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.cancel();
										}
									})

									.setMessage(getString(R.string.tilt_freeze_warning).replace("${CALIBRATION_SECONDS}", TILT_FREEZE_CALIBRATION_TIME+""))
											.setTitle("Tilt Freeze")
											.setIcon(R.drawable.icon)
											.setCancelable(true)
											.show();						
								}
							}
						}
					});

			view.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					gd.onTouchEvent(event);
					return true;
				}
			});
		}

		graphPanelDisplayManager.restore();
	}
	private void registerBpmReceiver() {
		IntentFilter filter = new IntentFilter(HRM_SERVICE_ACTION);

		hxmDataReceiver = new HXMDataReceiver(roboStroke.getBus());

		registerReceiver(hxmDataReceiver, filter);

		startService(hrmService);
	}

	private void unregisterBpmReceiver() {
		if (null != hxmDataReceiver) {
			unregisterReceiver(hxmDataReceiver);
			hxmDataReceiver = null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch (id) {
		case R.layout.tilt_freeze_dialog:
			TextView status = (TextView) dialog.findViewById(R.id.tilt_status);
			status.setText("");
			break;
		case R.layout.replay_file_select:
	
			final LinearLayout list = (LinearLayout) dialog.findViewById(R.id.replay_list_view);

			list.removeAllViews();

			ReplayFileList replayList = new ReplayFileList();
			
			final LinearLayout.LayoutParams rowLayout = 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT);
			final LinearLayout.LayoutParams openButtonLayout = 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			final LinearLayout.LayoutParams deleteButtonLayout = 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT, 0f);
			
			openButtonLayout.gravity = Gravity.CENTER_VERTICAL;
			deleteButtonLayout.gravity = Gravity.CENTER_VERTICAL;
			
			for (Pair<File, Date> p : replayList.files) {
				final LinearLayout row = new LinearLayout(this);
				row.setLayoutParams(new LayoutParams(rowLayout));
				row.setOrientation(LinearLayout.HORIZONTAL);
				
				final File f = p.first;
				Date date = p.second;
				Button button = new Button(this);
				button.setLayoutParams(new LayoutParams(openButtonLayout));
				CharSequence datePart = DateFormat.format("yyyy-MM-dd h:mmaa", date);
				String sizePart = makeFileSizeLabel(f.length());
				
				button.setText(String.format("%s %s", datePart, sizePart));
				button.setOnClickListener(new View.OnClickListener() {

					
					@Override
					public void onClick(View v) {
						restart(new SessionFileInfo(f, false));
						dialog.dismiss();
					}
				});
				
				row.addView(button, openButtonLayout);
				
				ImageButton deleteButton  = new ImageButton(this);
				deleteButton.setLayoutParams(new LayoutParams(deleteButtonLayout));
				deleteButton.setImageResource(android.R.drawable.ic_delete);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						f.delete();
						list.removeView(row);
						list.postInvalidate();
					}
				});
				
				row.addView(deleteButton,deleteButtonLayout);
				list.addView(row, rowLayout);
			}

			break;
		}
	}

	private boolean recheckExternalStorage() {
		if (!FileHelper.hasExternalStorage() || FileHelper.getDir(ROBOSTROKE_DATA_DIR) == null) {
			menu.findItem(R.id.menu_replay_start).setEnabled(false);
			menu.findItem(R.id.menu_record_start).setEnabled(false);
			
			notificationHelper.toast("This feature is unavalable, because data storage has been deactivated.");
			
			return false;
		}
		
		return true;
	}

	private String makeFileSizeLabel(long length) {
		
		double megaBytes = length / 1024000.0;
		
		String format;
		
		if (megaBytes < 10) {
			format = "% 2.1fM";
		} else if (megaBytes > 999.9) {
			return "~~~";
		} else {
			format = "% 3.0fM";
		}
		
		
		return String.format(format, megaBytes);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		String dialogTitle;

		switch (id) {
		case R.layout.replay_file_select:
			dialogTitle = "Select File";
			break;
		case R.layout.tilt_freeze_dialog:
			dialogTitle = "Tilt Freeze";
			break;
		default:
			return super.onCreateDialog(id);
		}

		final Dialog settingDialog = new Dialog(this);
		settingDialog.setContentView(id);
		settingDialog.setTitle(dialogTitle);

		if (id == R.layout.tilt_freeze_dialog) {
			final ToggleButton tb = (ToggleButton) settingDialog.findViewById(R.id.tilt_frozen);
			tb.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					
					if (tb.isChecked()) {
						final ProgressDialog progress = ProgressDialog.show (RoboStrokeActivity.this, "", 
								"Calibrating...");
						scheduler.schedule(new Runnable() {
							@Override
							public void run() {
								tiltFreezeOn = true;
								roboStroke.getBus().fireEvent(StrokeEvent.Type.FREEZE_TILT, true);
								progress.dismiss();
							}
						}, TILT_FREEZE_CALIBRATION_TIME, TimeUnit.SECONDS);
					} else {
						roboStroke.getBus().fireEvent(StrokeEvent.Type.FREEZE_TILT, false);
						tiltFreezeOn = false;
					}
					settingDialog.dismiss();
				}
			});
		}

		return settingDialog;
	}

	private synchronized void stop() {

		enableScheduler(false);

		unregisterBpmReceiver();

		roboStroke.stop();
		
		stopped = true;
		
		if (replayFile != null) { // delete preview/ad-hoc session files 
			
			if (replayFile.temporary) {
				replayFile.file.delete();
			}
			
			replayFile = null;
		}
	}

	synchronized void restart(SessionFileInfo replayFile) {
		if (!stopped) {
			stop();
		}
		
		start(replayFile);
	}

	private synchronized boolean[] resetGraphs(boolean halfFlush, boolean[] restoreStates) {
		final DataUpdatable[] arr = {
				accel_graph, stroke_graph, stroke_power_graph, stroke_analysis_graph
		};
		
		final boolean[] states = new boolean[arr.length];
		
		int i = 0;
		for (DataUpdatable graph: arr) {
			if (halfFlush) {
				if (restoreStates == null) {

					states[i++] = graph.isDisabled();
					graph.disableUpdate(true);
				} else {
					graph.reset();
					graph.disableUpdate(restoreStates[i++]);
				}
			} else {				
				graph.reset();
			}
		}

		return states;
	}

	private void enableScheduler(boolean enable) {
		if (enable) {
			if (scheduler != null) {
				throw new AssertionError("scheduler should have been disabled first");
			}

			scheduler = Executors.newScheduledThreadPool(1, new ThreadFactory() {

				private final SimpleLock lock = new SimpleLock();
				private int counter;

				@Override
				public Thread newThread(Runnable r) {
					synchronized (lock) {
						return new Thread(r,
								"RoboStrokeActivity scheduler thread "
								+ (++counter)) {
							{
								setDaemon(true);
							}
						};
					}
				}
			});
		} else {
			scheduler.shutdownNow();
			scheduler = null;
		}
	}

	private synchronized void start(SessionFileInfo replayFile) {

		roboStroke.getParameters().setParam(
				ParamKeys.PARAM_SESSION_RECORDING_ON, false);

		this.replayFile = null;
		
		enableScheduler(true);

		resetGraphs(false, null);

		try {
			roboStroke.setDataLogger(null);

			if (replayFile != null) {
				roboStroke.setFileInput(replayFile.file);
				this.replayFile = replayFile;
			}
		} catch (IOException e) {
			reportError(e, "error opening session file for replay");
		}

		final boolean replay = this.replayFile != null;
		
		if (!replay) {
			roboStroke.setInput(new AndroidSensorDataInput(this));
			registerBpmReceiver();
		}

		metersDisplayManager.reset();

		stopped = false;

		final int padding = replay ? HIGHLIGHT_PADDING_SIZE : 0;
		
		updateRecordingStateIndication(padding, REPLAYING_ON_COLOUR);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final boolean replay = isReplaying();
		
		boolean hasExternalStorage = FileHelper.hasExternalStorage();

		boolean enableStart = hasExternalStorage && !replay && !recordingOn;
		
		menu.findItem(R.id.menu_replay_start).setVisible(enableStart);
		menu.findItem(R.id.menu_record_start).setVisible(enableStart);
		menu.findItem(R.id.menu_replay_stop).setVisible(replay || recordingOn);
		
		final boolean canShareSession = replay && !replayFile.temporary;
		
		menu.findItem(R.id.menu_replay_share).setVisible(canShareSession);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	public boolean isReplaying() {
		return replayFile != null;
	}
	
	public Menu getMenu() {
		return menu;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);

		this.menu = menu;

		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_replay_start:
			
			if (recheckExternalStorage()) {
				showDialog(R.layout.replay_file_select);
			}
			
			return true;

		case R.id.menu_record_start:
			if (recheckExternalStorage()) {
				roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON, true);
			}
			
			break;
			
		case R.id.menu_replay_stop:
			if (isReplaying()) {
				restart(null);
			} else if (recordingOn) {
				roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON, false);
			}

			return true;
		case R.id.menu_replay_share:
			
			sessionFileHandler.shareSession();
			
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(this, Preferences.class));
			break;
		case R.id.menu_about:
			if (m_AlertDlg != null) {
				m_AlertDlg.cancel();
			}
			
			String version = getVersion(this);
			m_AlertDlg = new AlertDialog.Builder(this)
			.setMessage(getString(R.string.about_text).replace("\\n","\n").replace("${VERSION}", version))
			.setTitle("About")
			.setIcon(R.drawable.icon)
			.setCancelable(true)
			.show();
			break;
		}

		return false;
	}

	public static String getVersion(Context context) {		
		try {
			
			PackageInfo packageInfo = context.getPackageManager()
				   .getPackageInfo(context.getPackageName(), 0);
			
			
			return packageInfo.versionName;
			
		} catch(NameNotFoundException ex) {
			return "unknown";
		}
	}	
	
	@Override
	protected void onDestroy() {
		
		stop();

		roboStroke.destroy();
		
		notificationHelper.cancel(ROBOSTROKE_ERROR);
		
		super.onDestroy();

	}
 
	private void cleanTmpDir() {
		if (FileHelper.hasExternalStorage()) {
			
			File tmpDir = FileHelper.getFile(ROBOSTROKE_DATA_DIR, "tmp");

			tmpDir.mkdir();
			
			FileHelper.cleanDir(tmpDir, TimeUnit.SECONDS.toMillis(30));
		}
	}	

	@Override
	protected void onNewIntent(Intent intent) {
		
		if (sessionFileHandler.startPreviewIntent(intent)) {
			
			startActivity(new Intent(this, RoboStrokeActivity.class)); // this is to clear the 'sticky' preview intent 
		}
		
		
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		Intent intent = getIntent();
		
		screenLock.start();
	}

	@Override
	protected void onPause() {
		screenLock.stop();
		super.onPause();
	}


	public void onUpdateGraphSlotCount(int slotCount) {
		metersDisplayManager.onLayoutModeChange(slotCount == 1 ? LayoutMode.EXPANDED : LayoutMode.DEFAULT);		
		
	}

	public MetersDisplayManager getMetersDisplayManager() {
		return metersDisplayManager;
	}

	public void setEnableHrm(boolean enable, boolean resetNextRun) {
		this.heart_rate_view.setTag(enable ? null : "blackList");
		
		if (resetNextRun) {
			graphPanelDisplayManager.resetNextRun();
		}
	}

	public ParameterListenerRegistration[] getListenerRegistrations() {
		return listenerRegistrations;
	}

	private void updateRecordingStateIndication(final int padding, final int color) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				View highlight = findViewById(R.id.record_play_state_highlighter);
				
				highlight.setPadding(padding, 0, padding, 0);
				
				highlight.setBackgroundColor(color);
			}
		});
	}

	public RoboStroke getRoboStroke() {
		return roboStroke;
	}
}