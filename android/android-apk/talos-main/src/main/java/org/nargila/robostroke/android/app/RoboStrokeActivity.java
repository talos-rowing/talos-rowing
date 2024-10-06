/*
 * Copyright (c) 2024 Tal Shalif
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


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.format.DateFormat;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import org.apache.log4j.Level;
import org.nargila.robostroke.ParamKeys;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.android.common.ConfigureLog4J;
import org.nargila.robostroke.android.common.FileHelper;
import org.nargila.robostroke.android.common.NotificationHelper;
import org.nargila.robostroke.android.common.ScreenStayupLock;
import org.nargila.robostroke.android.remote.TalosBroadcastServiceConnector;
import org.nargila.robostroke.android.remote.TalosReceiverServiceConnector;
import org.nargila.robostroke.common.DataStreamCopier;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.common.SimpleLock;
import org.nargila.robostroke.data.*;
import org.nargila.robostroke.data.version.DataVersionConverter;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterListenerOwner;
import org.nargila.robostroke.param.ParameterListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * AndroidRoboStroke application entry point and main activity/
 */
public class RoboStrokeActivity extends Activity implements RoboStrokeConstants, ParameterListenerOwner {

    private static final String MIME_TYPE_ROBOSTROKE_SESSION = "application/vnd.robostroke.session";

    static final String ROBOSTROKE_DATA_DIR = "RoboStroke";

    private static final int RECORDING_ON_COLOUR = Color.argb(255, 255, 0, 0);

    private static final int REPLAYING_ON_COLOUR = Color.argb(255, 0, 255, 0);

    private static final int HIGHLIGHT_PADDING_SIZE = 5;

    private static final Logger logger = LoggerFactory.getLogger(RoboStrokeActivity.class);
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 42;

    private final ParameterListenerRegistration[] listenerRegistrations = {
            new ParameterListenerRegistration(ParamKeys.PARAM_SESSION_RECORDING_ON.getId(), new ParameterChangeListener() {

                @Override
                public void onParameterChanged(Parameter param) {
                    final boolean recording = param.getValue();

                    setRecordingOn(recording);

                    final int padding = recording ? HIGHLIGHT_PADDING_SIZE : 0;

                    updateRecordingStateIndication(padding, RECORDING_ON_COLOUR);
                }
            })
    };

    private boolean recordingOn;
    final Handler handler = new Handler();

    ScheduledExecutorService scheduler;

    final RoboStroke roboStroke =
            new RoboStroke(new AndroidLocationDistanceResolver(), new TalosBroadcastServiceConnector(this));

    private NotificationHelper notificationHelper;


    private final ScreenStayupLock screenLock;
    private Menu menu;
    private boolean replayPaused;

    private HXMDataReceiver hxmDataReceiver;


    PreferencesHelper preferencesHelper;

    private boolean stopped = true;

    GraphPanelDisplayManager graphPanelDisplayManager;

    MetersDisplayManager metersDisplayManager;

    private RecordSyncLeaderDialog recordLeaderDialog;

    static AlertDialog m_AlertDlg;
    private WindowInsetsControllerCompat windowInsetsController;

    private class SessionFileHandler {

        boolean wasRecording;
        Date sessionTimestamp = new Date();

        private String sessionTag() {
            return DateFormat.format("kk:mm:ss", sessionTimestamp) + "";
        }

        private void resetSessionRecording() {


            try {
                if (recordingOn && !isReplaying()) {

                    sessionTimestamp = new Date();

                    File logFile = recordingOn ? FileHelper.getFile(getApplicationContext(), ROBOSTROKE_DATA_DIR, System.currentTimeMillis() + "-dataInput.txt") : null;

                    roboStroke.setDataLogger(logFile);

                    roboStroke.getBus().fireEvent(DataRecord.Type.RECORDING_START, sessionTag());
                    roboStroke.getBus().fireEvent(DataRecord.Type.UUID, preferencesHelper.getUUID());

                    Runnable runAfter = new Runnable() {

                        @Override
                        public void run() {
                        }
                    };


                    if (!showFilmLeaderDialog(runAfter, sessionTag())) {
                        runAfter.run();
                    }

                    wasRecording = true;

                } else {

                    final AtomicReference<IOException> error = new AtomicReference<IOException>();

                    Runnable runAfter = new Runnable() {

                        @Override
                        public void run() {
                            try {
                                roboStroke.setDataLogger(null);
                            } catch (IOException e) {
                                error.set(e);
                            }
                        }
                    };

                    if (!wasRecording || !showFilmLeaderDialog(runAfter, sessionTag())) {
                        runAfter.run();
                    }

                    if (error.get() != null) {
                        throw error.get();
                    }

                    wasRecording = false;
                }

            } catch (IOException e) {
                logger.error("failed to start session data logger", e);
            }
        }

        private boolean showFilmLeaderDialog(Runnable runAfter, String tag) {
            Boolean enabled = preferencesHelper.getPref(ParamKeys.PARAM_SESSION_RECORDING_LEADER_ENABLE.getId(), (Boolean) ParamKeys.PARAM_SESSION_RECORDING_LEADER_ENABLE.getDefaultValue());

            if (enabled) {

                if (recordLeaderDialog == null) {
                    recordLeaderDialog = new RecordSyncLeaderDialog(RoboStrokeActivity.this);
                }

                recordLeaderDialog.setTag(tag);
                recordLeaderDialog.setRunAfter(runAfter);

                recordLeaderDialog.show();
            }

            return enabled;
        }

        private void shareSession() {

            if (dataInputInfo.inputType == DataInputInfo.InputType.FILE) {
                File toShare = dataInputInfo.file;

                if (toShare != null) {

                    Context context = getApplicationContext();

                    if (toShare.length() > 30000000) {
                        if (m_AlertDlg != null) {
                            m_AlertDlg.cancel();
                        }

                        m_AlertDlg = new AlertDialog.Builder(context).setMessage(
                                        getString(
                                                R.string.session_record_upload_size_too_large,
                                                90)).setTitle(
                                        getString(R.string.file_too_large)).setCancelable(true)
                                .show();
                    } else {

                        try {
                            File tmpdir = FileHelper.getFile(context, ROBOSTROKE_DATA_DIR, "tmp");

                            if (tmpdir == null || (!tmpdir.exists() && !tmpdir.mkdir())) {
                                throw new Exception("Could not create temporary file");
                            }

                            final String name = toShare.getName().replaceFirst("txt$", "trsd");

                            File trsd = new File(tmpdir, name);

                            Runnable runnable = new Runnable() {

                                @Override
                                public void run() {

                                    Intent intent = new Intent(Intent.ACTION_SEND);

                                    intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);

                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(
                                            "content://" + SessionContentProvider.AUTHORITY + "/" + name
                                    ));
                                    intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.default_session_record_dispatch_address)});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, String.format("Talos Rowing Session: %s", preferencesHelper.getUUID()));
                                    intent.putExtra(Intent.EXTRA_TEXT, "Description:\n");
                                    intent.setType(MIME_TYPE_ROBOSTROKE_SESSION);

                                    startActivityForResult(Intent.createChooser(intent, "Email:"), 42);
                                }
                            };

                            prepareData(Uri.fromFile(toShare), trsd, 1, runnable);

                        } catch (Exception e) {
                            reportError("error preparing session data for sharing", e);

                        }
                    }
                }
            }
        }

        /**
         * check either intent contains session data to replay
         *
         * @param intent
         * @return true if preview intent was started
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

                } catch (Exception e) {
                    reportError("preview error: failed to create preview file", e);

                    return false;
                }

                final DataInputInfo sessionFileInfo = new DataInputInfo(outFile, true);

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

            final AtomicReference<Future<?>> job = new AtomicReference<>();

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

            final AtomicBoolean res = new AtomicBoolean(true);

            synchronized (res) {
                Runnable jobRunnable = new Runnable() {

                    @Override
                    public void run() {
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

                            DataStreamCopier dsc = new DataStreamCopier(is, os, size) {

                                int lastPos = -1;

                                @Override
                                protected boolean onProgress(double d) {

                                    if (d > 0) {
                                        int pos = (int) (100.0 * d);

                                        progress.setProgress(pos);

                                        if (pos == 100 || pos - lastPos >= 5) {
                                            lastPos = pos;
                                            Thread.yield();
                                        }
                                    }

                                    return !Thread.currentThread().isInterrupted();
                                }
                            };


                            dsc.run();

                            if (!dsc.isGood() && dsc.getError() != null) {
                                throw new MyError("error in data copying processor", dsc.getError());
                            }

                            if (!job.get().isCancelled()) {
                                if (runnable == null) {
                                    res.set(true);
                                } else {
                                    handler.post(runnable);
                                }
                            }
                        } catch (MyError e) {
                            reportError("session data preparation failed: " + e.getMessage(), e.getCause());
                        } finally {
                            progress.dismiss();

                            synchronized (res) {
                                res.notify();
                            }
                        }
                    }
                };

                job.set(scheduler.submit(jobRunnable));

                progress.show();

                if (runnable == null) {
                    try {
                        res.wait();
                    } catch (InterruptedException e) {
                        // ignore exception
                    }
                }
            }
        }

        /**
         * Creates a temporary session output file with the given suffix.
         * The file is created under the tmp directory in ROBOSTROKE_DATA_DIR.
         * The file will be automatically deleted when the JVM exits.
         *
         * @param suffix the suffix of the file
         * @return a File object representing the temporary session output file
         * @throws Exception if the temp directory cannot be created or accessed
         */
        private File createTmpSessionOutputFile(String suffix) throws Exception {
            File tmpdir = FileHelper.getFile(getApplicationContext(), ROBOSTROKE_DATA_DIR, "tmp");

            if (tmpdir == null) {
                throw new Exception("could not get tempdir");
            }

            //noinspection ResultOfMethodCallIgnored
            tmpdir.mkdir();

            File tmp = File.createTempFile("dataInput-", suffix, tmpdir);

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

    private static class MyError extends Throwable {

        public MyError(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

    private static class DataInputInfo {
        enum InputType {
            FILE,
            REMOTE,
            SENSORS
        }

        final InputType inputType;
        final File file;
        final boolean temporary;
        final String host;
        final int port;

        DataInputInfo(File file, boolean temporary) {
            this.inputType = InputType.FILE;
            this.file = file;
            this.temporary = temporary;
            this.host = null;
            this.port = -1;
        }

        DataInputInfo(String host, int port) {
            this.inputType = InputType.REMOTE;
            this.file = null;
            this.temporary = false;
            this.host = host;
            this.port = port;
        }

        DataInputInfo() {
            this.inputType = InputType.SENSORS;
            this.file = null;
            this.temporary = false;
            this.host = null;
            this.port = -1;
        }
    }


    private final SessionFileHandler sessionFileHandler = new SessionFileHandler();

    private DataInputInfo dataInputInfo = new DataInputInfo();

    public RoboStrokeActivity() {

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

    void togglePause() {
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferencesHelper = new PreferencesHelper(this); // handles preferences -> parameter synchronization

        if (preferencesHelper.getPref(PreferencesHelper.PREFERENCE_KEY_PREFERENCES_LOG, false)) {
            ConfigureLog4J.configure(Level.DEBUG, "talos-main");
        } else {
            ConfigureLog4J.configure(null);
        }

        setContentView(R.layout.main);

        notificationHelper = new NotificationHelper(this, R.drawable.icon_small322);


        roboStroke.setErrorListener(new ErrorListener() {

            @Override
            public void onError(Exception e) {
                notificationHelper.notifyError(ROBOSTROKE_ERROR,
                        e.getMessage(), "robostroke error", "robostroke error");
            }
        });

        metersDisplayManager = new MetersDisplayManager(this);

        graphPanelDisplayManager = new GraphPanelDisplayManager(this);

        preferencesHelper.init();

        graphPanelDisplayManager.init();

        roboStroke.getAccelerationSource().addSensorDataSink(metersDisplayManager);

        View.OnClickListener recordingClickListener = new View.OnClickListener() {
            boolean recording;

            @Override
            public void onClick(View arg0) {
                if (!isReplaying() && FileHelper.hasExternalStorage(getApplicationContext())) {
                    roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON.getId(), !recording);
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


        roboStroke.getParameters().setParam(ParamKeys.PARAM_SENSOR_ORIENTATION_LANDSCAPE.getId(),
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        checkLocationPermissionAndStart();

        setupFullscreenMode();
    }

    /**
     * Configures the activity to operate in fullscreen mode by hiding the navigation bars. It sets up an
     * OnApplyWindowInsetsListener to show or hide the action bar based on the visibility of the navigation bars.
     * Additionally, it configures a click listener on the main view to toggle the visibility of the navigation bars
     * and action bar.
     */
    private void setupFullscreenMode() {
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());

        ViewCompat.setOnApplyWindowInsetsListener(
            getWindow().getDecorView(),
            (view, windowInsets) -> {
                if (windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())) {
                    getActionBar().show();
                } else {
                    getActionBar().hide();
                }
                return ViewCompat.onApplyWindowInsets(view, windowInsets);
            }
        );

        View mainView = findViewById(R.id.main_layout); // Make sure to replace with your main view id
        mainView.setOnClickListener((View v) -> {
            if (getActionBar().isShowing()) {
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
            } else {
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars());
            }
        });
    }

    private void checkLocationPermissionAndStart() {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            start(new DataInputInfo());
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            checkLocationPermissionAndStart();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        roboStroke.getParameters().setParam(ParamKeys.PARAM_SENSOR_ORIENTATION_LANDSCAPE.getId(), landscape);

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        sessionFileHandler.startPreviewIntent(getIntent());
        super.onPostCreate(savedInstanceState);
    }

    void showNotification(String msg) {
        notificationHelper.notifyError(ROBOSTROKE_ERROR, msg,
                "robostroke error", "robostroke error");

        notificationHelper
                .toast(msg + ". See error notification");
    }

    void reportError(String msg, Throwable throwable) {
        logger.error(msg, throwable);

        showNotification(msg + (throwable == null ? "" : ": " + throwable.getMessage()));
    }


    private void registerBpmReceiver() {

        hxmDataReceiver = new HXMDataReceiver(roboStroke.getBus());

        for (String intentSpec : HRM_SERVICE_ACTIONS) {
            IntentFilter filter = new IntentFilter(intentSpec);
            registerReceiver(hxmDataReceiver, filter, android.content.Context.RECEIVER_EXPORTED);
        }

    }

    private void unregisterBpmReceiver() {
        if (null != hxmDataReceiver) {
            unregisterReceiver(hxmDataReceiver);
            hxmDataReceiver = null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, final Dialog dialog) {
        if (id == R.layout.tilt_freeze_dialog) {
            TextView status = (TextView) dialog.findViewById(R.id.tilt_status);
            status.setText("");
        } else if (id == R.layout.replay_file_select) {

            final LinearLayout list = (LinearLayout) dialog.findViewById(R.id.replay_list_view);

            list.removeAllViews();

            ReplayFileList replayList = new ReplayFileList(this);

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
                        restart(new DataInputInfo(f, false));
                        dialog.dismiss();
                    }
                });

                row.addView(button, openButtonLayout);

                ImageButton deleteButton = new ImageButton(this);
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

                row.addView(deleteButton, deleteButtonLayout);
                list.addView(row, rowLayout);
            }
        }
    }

    private boolean recheckExternalStorage() {
        Context context = getApplicationContext();
        if (!FileHelper.hasExternalStorage(context) || FileHelper.getDir(context, ROBOSTROKE_DATA_DIR) == null) {
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

        if (id == R.layout.replay_file_select) {
            dialogTitle = "Select File";
        } else if (id == R.layout.tilt_freeze_dialog) {
            dialogTitle = "Tilt Freeze";
        } else {
            return super.onCreateDialog(id);
        }

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(id);
        dialog.setTitle(dialogTitle);

        if (id == R.layout.tilt_freeze_dialog) {
            final ToggleButton tb = (ToggleButton) dialog.findViewById(R.id.tilt_frozen);
            tb.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {


                    if (tb.isChecked()) {
                        final ProgressDialog progress = ProgressDialog.show(RoboStrokeActivity.this, "",
                                "Calibrating...");
                        scheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                graphPanelDisplayManager.tiltFreezeOn = true;
                                roboStroke.getBus().fireEvent(DataRecord.Type.FREEZE_TILT, true);
                                progress.dismiss();
                            }
                        }, TILT_FREEZE_CALIBRATION_TIME, TimeUnit.SECONDS);
                    } else {
                        roboStroke.getBus().fireEvent(DataRecord.Type.FREEZE_TILT, false);
                        graphPanelDisplayManager.tiltFreezeOn = false;
                    }
                    dialog.dismiss();
                }
            });
        }

        return dialog;
    }

    private synchronized void stop() {

        logger.info("stopping input type {}", dataInputInfo.inputType);

        enableScheduler(false);

        unregisterBpmReceiver();

        roboStroke.stop();

        stopped = true;

        if (dataInputInfo.inputType == DataInputInfo.InputType.FILE) { // delete preview/ad-hoc session files

            if (dataInputInfo.temporary) {
                dataInputInfo.file.delete();
            }
        }
    }

    synchronized void restart(DataInputInfo replayFile) {

        logger.info("restarting input type {}", replayFile.inputType);

        if (!stopped) {
            stop();
        }

        start(replayFile);
    }

    private void enableScheduler(boolean enable) {
        if (enable) {

            logger.debug("creating new scheduler");

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
            logger.debug("shutting scheduler");
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    private synchronized void start(DataInputInfo replayFile) {

        logger.info("starting input type {}", replayFile.inputType);

        roboStroke.getParameters().setParam(
                ParamKeys.PARAM_SESSION_RECORDING_ON.getId(), false);

        enableScheduler(true);

        try {
            if (replayFile.inputType == DataInputInfo.InputType.FILE) {

                DataVersionConverter converter = DataVersionConverter.getConvertersFor(replayFile.file);

                if (converter != null) {
                    convertStart(converter, replayFile);
                    return;
                }
            }
        } catch (DataVersionConverter.ConverterError e) {
            reportError("error getting data file converter", e);
        }

        realStart(replayFile);
    }

    private void convertStart(final DataVersionConverter converter, final DataInputInfo replayFile) {

        final ProgressDialog progress = new ProgressDialog(RoboStrokeActivity.this);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMessage("Converting...");

        progress.setMax(100);

        progress.setIndeterminate(true);


        progress.setCancelable(true);

        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                converter.cancel();
                progress.dismiss();
            }
        });

        converter.setProgressListener(new DataVersionConverter.ProgressListener() {

            @Override
            public boolean onProgress(double d) {

                progress.setIndeterminate(false);

                progress.setProgress((int) (100 * d));

                return true;
            }
        });

        progress.show();

        scheduler.submit(new Runnable() {

            @Override
            public void run() {

                DataInputInfo newInfo = new DataInputInfo();

                try {

                    File output = converter.convert(replayFile.file);

                    if (output != null) {
                        newInfo = new DataInputInfo(output, true);
                    }

                    if (replayFile.temporary) {
                        replayFile.file.delete();
                    }

                } catch (Exception e) {
                    reportError("error getting data file converter", e);
                } finally {
                    progress.dismiss();
                    realStart(newInfo);
                }
            }
        });
    }

    private synchronized void realStart(DataInputInfo dataInputInfo) {

        boolean replay = false;
        int padding = 0;
        int recordingIndicatorHilight = REPLAYING_ON_COLOUR;

        this.dataInputInfo = dataInputInfo;

        SensorDataInput dataInput = new AndroidSensorDataInput(this);

        graphPanelDisplayManager.resetGraphs();

        try {
            roboStroke.setDataLogger(null);
        } catch (IOException ignored) {
        }

        try {
            switch (dataInputInfo.inputType) {
                case FILE:
                case REMOTE:
                    SensorDataInput input;

                    if (dataInputInfo.inputType == DataInputInfo.InputType.FILE) {
                        input = new FileDataInput(roboStroke, dataInputInfo.file);
                    } else {
                        input = new TalosReceiverServiceConnector(this, roboStroke, dataInputInfo.host, dataInputInfo.port);
                    }

                    this.dataInputInfo = dataInputInfo;
                    dataInput = input;
                    padding = HIGHLIGHT_PADDING_SIZE;
                    replay = true;
                    recordingIndicatorHilight = REPLAYING_ON_COLOUR;
                    break;
            }
        } catch (Exception e) {
            this.dataInputInfo = new DataInputInfo();
            logger.error("failed to set input to " + dataInputInfo.inputType, e);
        }

        roboStroke.setInput(dataInput);

        if (!replay) {
            registerBpmReceiver();
        }

        metersDisplayManager.reset();

        stopped = false;

        updateRecordingStateIndication(padding, recordingIndicatorHilight);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final boolean replay = isReplaying();

        boolean hasExternalStorage = FileHelper.hasExternalStorage(getApplicationContext());

        boolean enableStart = hasExternalStorage && !replay && !recordingOn;

        boolean isBroadcasting = (Boolean) roboStroke.getParameters().getValue(ParamKeys.PARAM_SESSION_BROADCAST_ON.getId());
        boolean isRemote = dataInputInfo.inputType == DataInputInfo.InputType.REMOTE;

        menu.findItem(R.id.menu_replay_start).setVisible(enableStart);
        menu.findItem(R.id.menu_broadcast_stop).setVisible(isBroadcasting);
        menu.findItem(R.id.menu_broadcast_start).setVisible(!isBroadcasting && !isRemote);
        menu.findItem(R.id.menu_remote_start).setVisible(!replay && !recordingOn && !isBroadcasting);
        menu.findItem(R.id.menu_record_start).setVisible(enableStart);
        menu.findItem(R.id.menu_replay_stop).setVisible(replay || recordingOn);

        final boolean canShareSession = replay && dataInputInfo.inputType == DataInputInfo.InputType.FILE;

        menu.findItem(R.id.menu_replay_share).setVisible(canShareSession);

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean isReplaying() {
        return dataInputInfo.inputType != DataInputInfo.InputType.SENSORS;
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

            case R.id.menu_remote_start:
                int port = Integer.valueOf(preferencesHelper.getPref("org.nargila.talos.rowing.session.broadcast.port", SessionRecorderConstants.BROADCAST_PORT + ""));
                String host = preferencesHelper.getPref("org.nargila.talos.rowing.android.session.remote.host", SessionRecorderConstants.BROADCAST_HOST);
                restart(new DataInputInfo(host, port));
                break;

            case R.id.menu_broadcast_start:
                roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_ON.getId(), true);
                break;
            case R.id.menu_broadcast_stop:
                roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_BROADCAST_ON.getId(), false);
                break;
            case R.id.menu_record_start:
                if (recheckExternalStorage()) {
                    roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON.getId(), true);
                }

                break;

            case R.id.menu_replay_stop:
                if (isReplaying()) {
                    restart(new DataInputInfo());
                } else if (recordingOn) {
                    roboStroke.getParameters().setParam(ParamKeys.PARAM_SESSION_RECORDING_ON.getId(), false);
                }

                return true;
            case R.id.menu_replay_share:

                sessionFileHandler.shareSession();

                return true;
            case R.id.menu_settings:
                startActivity(new Intent(this, Preferences.class));
                break;
            case R.id.menu_about:
                showAbout();
                break;
        }

        return false;
    }

    public void showAbout() {
        if (m_AlertDlg != null) {
            m_AlertDlg.cancel();
        }

        String version = getVersion();
        m_AlertDlg = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.about_text).replace("\\n", "\n").replace("${VERSION}", version))
                .setTitle(R.string.about)
                .setIcon(R.drawable.icon)
                .setCancelable(true)
                .setNegativeButton(R.string.changelog, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String[] v = getVersion().split("\\.");

                        String page = "AndroidRelease";

                        for (int i = 0; i < v.length - 1; ++i) {
                            page += "_" + v[i];
                        }

                        String url = "http://nargila.org/trac/robostroke/wiki/" + page;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                })
                .setPositiveButton(R.string.open_guide, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = getString(R.string.about_dialog_guide_url);
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                })
                .setNeutralButton(R.string.license, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        StringBuffer bf = new StringBuffer();
                        BufferedReader reader = null;

                        try {
                            reader = new BufferedReader(new InputStreamReader(
                                    getAssets().open("gpl-3.0.txt"),
                                    "UTF-8"));
                            String l;
                            while ((l = reader.readLine()) != null) {
                                bf.append(l).append('\n');
                            }

                            new AlertDialog.Builder(RoboStrokeActivity.this)
                                    .setMessage(bf.toString())
                                    .setTitle(R.string.license)
                                    .setCancelable(true)
                                    .show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    // ignore exception
                                }
                            }
                        }
                    }
                })
                .show();
    }

    public String getVersion() {
        try {

            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);


            return packageInfo.versionName;

        } catch (NameNotFoundException ex) {
            return "unknown";
        }
    }

    @Override
    protected void onDestroy() {

        stop();

        roboStroke.destroy();

        notificationHelper.cancel(ROBOSTROKE_ERROR);

        sendLogFile();

        super.onDestroy();

    }

    private void cleanTmpDir() {
        Context context = getApplicationContext();
        if (FileHelper.hasExternalStorage(context)) {

            File tmpDir = FileHelper.getFile(context, ROBOSTROKE_DATA_DIR, "tmp");

            if (tmpDir != null) {
                tmpDir.mkdir();

                FileHelper.cleanDir(tmpDir, TimeUnit.SECONDS.toMillis(30));
            }
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

        screenLock.start();
    }

    @Override
    protected void onPause() {
        screenLock.stop();
        super.onPause();
    }

    public MetersDisplayManager getMetersDisplayManager() {
        return metersDisplayManager;
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

    public void setLandscapeLayout(boolean landscape) {
        setRequestedOrientation(landscape ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void sendLogFile() {

        File log = ConfigureLog4J.getLogFilePath();

        if (log != null) {

            Intent intent = new Intent(Intent.ACTION_SEND);

            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(log));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.default_session_record_dispatch_address)});
            intent.putExtra(Intent.EXTRA_SUBJECT, String.format("Talos Rowing Log: %s", preferencesHelper.getUUID()));
            intent.putExtra(Intent.EXTRA_TEXT, "Description:\n");
            intent.setType("text/plain");

            startActivityForResult(Intent.createChooser(intent, "Email:"), 43);
        }
    }

}
