package org.nargila.robostroke.media;

import org.nargila.robostroke.app.Settings;
import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.media.ExternalMedia.VideoEffect;
import org.nargila.robostroke.data.media.MediaSynchedFileDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SetupExternalMediaInfoPanel extends JPanel {

    private static final String MEDIA_CONFIG_FILE_SUFFIX = ".trsm";

    private static final Logger logger = LoggerFactory.getLogger(SetupExternalMediaInfoPanel.class);

    private final JTextField inputOgg;
    private final JTextField inputTalos;
    private final JButton cancelBtn;
    private final JButton saveBtn;
    private final JButton btnSelectMedia;
    private final JButton btnSelectTalos;
    private final JButton btnSelectOutput;
    private final JTextField resultTalos;
    private final JLabel statusLine;
    private final JProgressBar progressBar;
    private JFileChooser fc;
    private final AtomicReference<File> resultTalosConfFile = new AtomicReference<>();
    private final AtomicReference<File> talosFile = new AtomicReference<>();
    private final AtomicReference<File> mediaFile = new AtomicReference<>();

    private final AtomicReference<Exception> error = new AtomicReference<>();

    private final JFormattedTextField textTimeOffset;
    private final JFormattedTextField textMarkId;
    private VideoEffect videoEffect = VideoEffect.NONE;
    private final JButton btnDetect;
    private FindQrMarkPipeline findQr;
    private boolean canceled;
    private final AtomicReference<Pair<Integer, Long>> syncData = new AtomicReference<>();

    private final JComboBox cbxVideoEfects;

    private final JButton btnManual;

    /**
     * Create the panel.
     */
    public SetupExternalMediaInfoPanel() {

        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);

        btnSelectMedia = new JButton("Select Media");
        springLayout.putConstraint(SpringLayout.NORTH, btnSelectMedia, 22, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, btnSelectMedia, 21, SpringLayout.WEST, this);
        btnSelectMedia.addActionListener(e -> chooseMediaFile());
        add(btnSelectMedia);

        inputOgg = new JTextField();
        springLayout.putConstraint(SpringLayout.WEST, inputOgg, 171, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, btnSelectMedia, -18, SpringLayout.WEST, inputOgg);
        springLayout.putConstraint(SpringLayout.NORTH, inputOgg, 22, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.EAST, inputOgg, -25, SpringLayout.EAST, this);
        inputOgg.setEditable(false);
        add(inputOgg);
        inputOgg.setColumns(10);

        btnSelectTalos = new JButton("Select Talos");
        springLayout.putConstraint(SpringLayout.WEST, btnSelectTalos, 0, SpringLayout.WEST, btnSelectMedia);
        springLayout.putConstraint(SpringLayout.EAST, btnSelectTalos, 0, SpringLayout.EAST, btnSelectMedia);
        btnSelectTalos.addActionListener(e -> chooseTalosFile());
        add(btnSelectTalos);

        inputTalos = new JTextField();
        springLayout.putConstraint(SpringLayout.NORTH, inputTalos, 100, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, inputTalos, 150, SpringLayout.WEST, btnSelectMedia);
        springLayout.putConstraint(SpringLayout.EAST, inputTalos, -25, SpringLayout.EAST, this);
        inputTalos.setEditable(false);
        add(inputTalos);
        inputTalos.setColumns(10);

        cancelBtn = new JButton("Cancel");
        springLayout.putConstraint(SpringLayout.WEST, cancelBtn, 100, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, cancelBtn, 181, SpringLayout.WEST, this);
        cancelBtn.addActionListener(e -> cancel());
        springLayout.putConstraint(SpringLayout.NORTH, cancelBtn, -64, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.SOUTH, cancelBtn, -31, SpringLayout.SOUTH, this);
        add(cancelBtn);

        saveBtn = new JButton("Save");
        springLayout.putConstraint(SpringLayout.SOUTH, saveBtn, -31, SpringLayout.SOUTH, this);
        saveBtn.setEnabled(false);
        saveBtn.addActionListener(e -> onSave());
        springLayout.putConstraint(SpringLayout.WEST, saveBtn, -148, SpringLayout.EAST, this);
        springLayout.putConstraint(SpringLayout.EAST, saveBtn, -67, SpringLayout.EAST, this);
        add(saveBtn);

        btnSelectOutput = new JButton("Output");
        springLayout.putConstraint(SpringLayout.NORTH, btnSelectOutput, 132, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.SOUTH, btnSelectTalos, -6, SpringLayout.NORTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.SOUTH, inputTalos, -6, SpringLayout.NORTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.WEST, btnSelectOutput, 21, SpringLayout.WEST, this);
        btnSelectOutput.addActionListener(e -> chooseOutputFile());
        add(btnSelectOutput);

        resultTalos = new JTextField();
        springLayout.putConstraint(SpringLayout.EAST, btnSelectOutput, -18, SpringLayout.WEST, resultTalos);
        springLayout.putConstraint(SpringLayout.WEST, resultTalos, 171, SpringLayout.WEST, this);
        resultTalos.setEditable(false);
        springLayout.putConstraint(SpringLayout.NORTH, resultTalos, 0, SpringLayout.NORTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.SOUTH, resultTalos, 0, SpringLayout.SOUTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.EAST, resultTalos, -25, SpringLayout.EAST, this);
        add(resultTalos);
        resultTalos.setColumns(10);

        progressBar = new JProgressBar();
        springLayout.putConstraint(SpringLayout.SOUTH, progressBar, -22, SpringLayout.NORTH, cancelBtn);
        springLayout.putConstraint(SpringLayout.NORTH, saveBtn, 22, SpringLayout.SOUTH, progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        springLayout.putConstraint(SpringLayout.WEST, progressBar, 21, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, progressBar, -25, SpringLayout.EAST, this);
        add(progressBar);

        statusLine = new JLabel("");
        springLayout.putConstraint(SpringLayout.SOUTH, statusLine, -117, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.NORTH, progressBar, 6, SpringLayout.SOUTH, statusLine);
        springLayout.putConstraint(SpringLayout.WEST, statusLine, 21, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, statusLine, -25, SpringLayout.EAST, this);
        statusLine.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusLine);

        JLabel lblDataStartTime = new JLabel("Data Start Time Offset");
        springLayout.putConstraint(SpringLayout.NORTH, lblDataStartTime, 35, SpringLayout.SOUTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.WEST, lblDataStartTime, 21, SpringLayout.WEST, this);
        add(lblDataStartTime);

        JLabel lblDataStartSynch = new JLabel("Data Start Synch Mark ID");
        springLayout.putConstraint(SpringLayout.WEST, lblDataStartSynch, 21, SpringLayout.WEST, this);
        add(lblDataStartSynch);

        textTimeOffset = new JFormattedTextField();
        springLayout.putConstraint(SpringLayout.EAST, textTimeOffset, -246, SpringLayout.EAST, this);
        textTimeOffset.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textTimeOffset.selectAll();
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                setSynchData(Pair.create((Integer) textMarkId.getValue(), (Long) textTimeOffset.getValue()));
            }
        });
        textTimeOffset.setValue(-1L);
        springLayout.putConstraint(SpringLayout.NORTH, lblDataStartSynch, 6, SpringLayout.SOUTH, textTimeOffset);
        springLayout.putConstraint(SpringLayout.NORTH, textTimeOffset, 6, SpringLayout.SOUTH, lblDataStartTime);
        springLayout.putConstraint(SpringLayout.WEST, textTimeOffset, 21, SpringLayout.WEST, this);
        add(textTimeOffset);
        textTimeOffset.setColumns(10);

        textMarkId = new JFormattedTextField();
        springLayout.putConstraint(SpringLayout.WEST, textMarkId, 21, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, textMarkId, -246, SpringLayout.EAST, this);
        textMarkId.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textMarkId.selectAll();
                    }
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                setSynchData(Pair.create((Integer) textMarkId.getValue(), (Long) textTimeOffset.getValue()));
            }
        });
        textMarkId.setValue(1);
        springLayout.putConstraint(SpringLayout.NORTH, textMarkId, 6, SpringLayout.SOUTH, lblDataStartSynch);
        add(textMarkId);
        textMarkId.setColumns(10);

        btnDetect = new JButton("Detect");
        springLayout.putConstraint(SpringLayout.NORTH, btnDetect, 0, SpringLayout.NORTH, lblDataStartSynch);
        springLayout.putConstraint(SpringLayout.WEST, btnDetect, 24, SpringLayout.EAST, textMarkId);
        springLayout.putConstraint(SpringLayout.SOUTH, btnDetect, 0, SpringLayout.SOUTH, textMarkId);
        springLayout.putConstraint(SpringLayout.EAST, btnDetect, -110, SpringLayout.EAST, this);
        btnDetect.setEnabled(false);
        btnDetect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDetect();
            }
        });
        add(btnDetect);

        lblVideoEvects = new JLabel("Video Evects");
        lblVideoEvects.setEnabled(false);
        lblVideoEvects.setHorizontalAlignment(SwingConstants.RIGHT);
        springLayout.putConstraint(SpringLayout.SOUTH, btnSelectMedia, -10, SpringLayout.NORTH, lblVideoEvects);
        springLayout.putConstraint(SpringLayout.WEST, lblVideoEvects, 0, SpringLayout.WEST, btnSelectMedia);
        springLayout.putConstraint(SpringLayout.EAST, lblVideoEvects, 0, SpringLayout.EAST, btnSelectMedia);
        add(lblVideoEvects);

        cbxVideoEfects = new JComboBox();
        cbxVideoEfects.setEnabled(false);
        cbxVideoEfects.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                videoEffect = (VideoEffect) e.getItem();
            }
        });
        springLayout.putConstraint(SpringLayout.WEST, cbxVideoEfects, 18, SpringLayout.EAST, lblVideoEvects);
        springLayout.putConstraint(SpringLayout.EAST, cbxVideoEfects, -25, SpringLayout.EAST, this);
        springLayout.putConstraint(SpringLayout.NORTH, lblVideoEvects, 4, SpringLayout.NORTH, cbxVideoEfects);
        springLayout.putConstraint(SpringLayout.NORTH, cbxVideoEfects, 54, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.SOUTH, inputOgg, -6, SpringLayout.NORTH, cbxVideoEfects);
        add(cbxVideoEfects);

        btnManual = new JButton("Manual");
        btnManual.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onManualMarkTime();
            }
        });
        btnManual.setEnabled(false);
        springLayout.putConstraint(SpringLayout.NORTH, btnManual, 0, SpringLayout.NORTH, lblDataStartTime);
        springLayout.putConstraint(SpringLayout.WEST, btnManual, 24, SpringLayout.EAST, textTimeOffset);
        springLayout.putConstraint(SpringLayout.SOUTH, btnManual, 0, SpringLayout.SOUTH, textTimeOffset);
        springLayout.putConstraint(SpringLayout.EAST, btnManual, -110, SpringLayout.EAST, this);
        add(btnManual);

        for (VideoEffect e : VideoEffect.values()) {
            cbxVideoEfects.addItem(e);
        }
    }


    private void onManualMarkTime() {
        SteppingPlayerDialog steppingPlayer = new SteppingPlayerDialog();
        Pair<String, ClockTime> syncTime = steppingPlayer.launch(mediaFile.get().getAbsolutePath());

        if (syncTime != null) {

            int markId = syncTime.first == null ? 1 : Integer.parseInt(syncTime.first.split(":")[1]);

            setSynchData(Pair.create(markId, syncTime.second.toMillis()));
        }
    }


    private void cancel() {

        canceled = true;

        if (findQr != null) {
            findQr.stop();
        }

        onClose();
    }

    private void onDetect() {

        textTimeOffset.setEditable(false);
        textMarkId.setEditable(false);

        btnSelectMedia.setEnabled(false);
        btnSelectTalos.setEnabled(false);
        btnSelectOutput.setEnabled(false);

        progressBar.setVisible(true);

        saveBtn.setEnabled(false);

        final SetupExternalMediaInfoPanel self = this;

        try {
            findQr = MediaPlayerFactory.createFindQrMarkPipeline(mediaFile.get());
        } catch (Exception e) {
            error.set(e);
        }

        new Thread("DetectQrMark") {
            @Override
            public void run() {

                final AtomicReference<Pair<Integer, Long>> res = new AtomicReference<>();

                try {
                    if (error.get() != null) {
                        throw error.get();
                    }

                    res.set(findQr.findMark(60));
                } catch (Exception e) {
                    error.set(e);
                } finally {

                    if (!canceled && error.get() != null) {
                        JOptionPane.showMessageDialog(self, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
                    }

                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setVisible(false);
                            textTimeOffset.setEditable(true);
                            textMarkId.setEditable(true);
                            btnSelectMedia.setEnabled(true);
                            btnSelectTalos.setEnabled(true);
                            btnSelectOutput.setEnabled(true);
                            cancelBtn.setSelected(true);

                            if (res.get() != null) {
                                textTimeOffset.setText(res.get().second + "");
                                textMarkId.setText(res.get().first + "");
                                setSynchData(res.get());
                            }
                        }
                    });
                }
            }
        }.start();

    }

    private void setSynchData(final Pair<Integer, Long> res) {

        if (res != null && res.first >= -1 && res.second > -1) {
            syncData.set(res);
        } else {
            syncData.set(null);
        }

        textMarkId.setValue(syncData.get() == null ? -1 : syncData.get().first);
        textTimeOffset.setValue(syncData.get() == null ? -1L : syncData.get().second);

        updateState();
    }

    private void onSave() {

        Properties props = new Properties();

        props.setProperty(MediaSynchedFileDataInput.PROP_MEDIA_FILE, mediaFile.get().getAbsolutePath());
        props.setProperty(MediaSynchedFileDataInput.PROP_TALOS_DATA, talosFile.get().getAbsolutePath());
        props.setProperty(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, syncData.get().first.toString());
        props.setProperty(MediaSynchedFileDataInput.PROP_TIME_OFFSET, syncData.get().second.toString());
        props.setProperty(MediaSynchedFileDataInput.PROP_VIDEO_EFFECT, videoEffect.name());

        try {
            props.store(new FileWriter(resultTalosConfFile.get()), null);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, error.get().getMessage(), error.get().getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }

        onClose();

    }

    protected abstract void onClose();

    private String chooseFile(FileFilter fileFilter, boolean saveFile, File suggestFile) {

        if (fc == null) {
            fc = new JFileChooser(Settings.getInstance().getLastDir());
        }

        fc.setFileFilter(fileFilter);

        fc.setSelectedFile(suggestFile);

        int status = saveFile ? fc.showSaveDialog(this) : fc.showOpenDialog(this);

        if (JFileChooser.APPROVE_OPTION == status) {
            File f = fc.getSelectedFile();
            Settings.getInstance().setLastDir(f.getParentFile());
            return f.getAbsolutePath();
        }

        return null;
    }


    protected void chooseOutputFile() {

        File suggestedFile = mediaFile.get() == null ? null : new File(mediaFile.get().getAbsolutePath() + MEDIA_CONFIG_FILE_SUFFIX);

        String path = chooseFile(TALOS_MEDIA_INFO_FILES_FILTER, true, suggestedFile);

        setOutfile(path);
    }

    private void chooseTalosFile() {

        String path = chooseFile(new FileFilter() {

            @Override
            public String getDescription() {
                return "Talos Rowing Data Files";
            }

            @Override
            public boolean accept(File f) {

                if (f.isDirectory()) {
                    return true;
                } else {
                    String name = f.getName();
                    return name.endsWith(".trsd") || name.endsWith(".txt");
                }
            }
        }, false, null);

        setPathItem(path, talosFile, inputTalos, true);

    }

    private void setPathItem(String path, AtomicReference<File> value, JTextComponent text, boolean checkExists) {

        if (path != null) {
            File res = new File(path);
            if (!checkExists || res.exists()) {
                text.setText(res.getAbsolutePath());
                value.set(res);
            }
        }

        updateState();
    }


    private void updateState() {

        boolean haveMedia = mediaFile.get() != null;
        boolean haveTalosFile = talosFile.get() != null;
        boolean haveConfFile = resultTalosConfFile.get() != null;
        boolean haveSyncData = syncData.get() != null;
        boolean enable = haveMedia && haveTalosFile && haveConfFile && haveSyncData;

        logger.info("can save: {} (mediaFile: {}, talosFile: {}, resultTalosConfFile:{}, syncData: {})",
                enable, haveMedia, haveTalosFile, haveConfFile, haveSyncData);

        btnDetect.setEnabled(haveMedia);
        btnManual.setEnabled(haveMedia);
        lblVideoEvects.setEnabled(haveMedia);
        cbxVideoEfects.setEnabled(haveMedia);

        saveBtn.setEnabled(enable);

        if (enable) {
            cancelBtn.setSelected(true);
        }
    }

    protected void chooseMediaFile() {

        String path = chooseFile(ALL_FILES_FILTER, false, null);

        File media = mediaFile.get();

        setPathItem(path, mediaFile, inputOgg, true);

        if (mediaFile.get() != null && !mediaFile.get().equals(media)) {

            setOutfile(mediaFile.get().getAbsolutePath() + MEDIA_CONFIG_FILE_SUFFIX);

            if (talosFile.get() == null) {
                setPathItem(mediaFile.get().getAbsolutePath().replaceFirst("\\.[a-zA-Z0-9]+$", ".txt"), talosFile, inputTalos, true);
            }
        }
    }

    private void setOutfile(String path) {

        File confFile = resultTalosConfFile.get();
        setPathItem(path, resultTalosConfFile, resultTalos, false);

        if (resultTalosConfFile.get().exists() && !resultTalosConfFile.get().equals(confFile)) {

            Properties props = new Properties();

            try {

                props.load(new FileReader(resultTalosConfFile.get()));

                cbxVideoEfects.setSelectedItem(VideoEffect.valueOf(props.getProperty(MediaSynchedFileDataInput.PROP_VIDEO_EFFECT, VideoEffect.NONE.name())));

                setSynchData(Pair.create(
                        Integer.valueOf(props.getProperty(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, "-1")),
                        Long.valueOf(props.getProperty(MediaSynchedFileDataInput.PROP_TIME_OFFSET, "-1"))));


                setPathItem(props.getProperty(MediaSynchedFileDataInput.PROP_TALOS_DATA, null), talosFile, inputTalos, true);
            } catch (IOException e) {
                logger.error("failed to load config file " + path, e);
            }
        }
    }

    private static final FileFilter ALL_FILES_FILTER = new FileFilter() {

        @Override
        public String getDescription() {
            return "All files";
        }

        @Override
        public boolean accept(File f) {
            return !f.isDirectory();
        }
    };

    private static final FileFilter TALOS_MEDIA_INFO_FILES_FILTER = new FileFilter() {

        @Override
        public String getDescription() {
            return "Talos Media Info files";
        }

        @Override
        public boolean accept(File f) {

            if (f.isDirectory()) {
                return true;
            } else {
                return f.getName().endsWith(MEDIA_CONFIG_FILE_SUFFIX);
            }
        }
    };

    private final JLabel lblVideoEvects;
}
