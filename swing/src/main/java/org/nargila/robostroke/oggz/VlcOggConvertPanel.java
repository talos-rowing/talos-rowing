package org.nargila.robostroke.oggz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public abstract class VlcOggConvertPanel extends JPanel {

    private static final String[] VLC_EXE_PATH_LINUX = {
            "/usr/bin/vlc"
    };

    private static final String[] VLC_EXE_PATH_WIN32 = {
            "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
            "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe"
    };

    private static final Logger logger = LoggerFactory.getLogger(VlcOggConvertPanel.class);
    private JTextField inputVideo;
    private JTextField inputVlcExecutable;
    private JButton cancelBtn;
    private JButton startBtn;
    private JButton btnSelectVideo;
    private JButton btnSelectVlc;
    private JButton btnSelectOutput;
    private JTextField outputOgg;
    private JFileChooser fc;
    private File outfile;
    private File video;
    private File vlcExecutable;

    /**
     * Create the panel.
     */
    public VlcOggConvertPanel() {
        SpringLayout springLayout = new SpringLayout();
        setLayout(springLayout);

        btnSelectVideo = new JButton("Select Video");
        btnSelectVideo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseVideoFile();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, btnSelectVideo, 38, SpringLayout.NORTH, this);
        springLayout.putConstraint(SpringLayout.WEST, btnSelectVideo, 21, SpringLayout.WEST, this);
        add(btnSelectVideo);

        inputVideo = new JTextField();
        inputVideo.setEditable(false);
        springLayout.putConstraint(SpringLayout.NORTH, inputVideo, 0, SpringLayout.NORTH, btnSelectVideo);
        springLayout.putConstraint(SpringLayout.WEST, inputVideo, 18, SpringLayout.EAST, btnSelectVideo);
        springLayout.putConstraint(SpringLayout.SOUTH, inputVideo, 0, SpringLayout.SOUTH, btnSelectVideo);
        springLayout.putConstraint(SpringLayout.EAST, inputVideo, -25, SpringLayout.EAST, this);
        add(inputVideo);
        inputVideo.setColumns(10);

        btnSelectVlc = new JButton("Select Vlc");
        springLayout.putConstraint(SpringLayout.WEST, btnSelectVlc, 21, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, btnSelectVlc, -297, SpringLayout.EAST, this);
        btnSelectVlc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseVlcExecutable();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, btnSelectVlc, 21, SpringLayout.SOUTH, btnSelectVideo);
        springLayout.putConstraint(SpringLayout.EAST, btnSelectVideo, 0, SpringLayout.EAST, btnSelectVlc);
        add(btnSelectVlc);

        inputVlcExecutable = new JTextField();
        inputVlcExecutable.setEditable(false);
        springLayout.putConstraint(SpringLayout.NORTH, inputVlcExecutable, 0, SpringLayout.NORTH, btnSelectVlc);
        springLayout.putConstraint(SpringLayout.WEST, inputVlcExecutable, 18, SpringLayout.EAST, btnSelectVlc);
        springLayout.putConstraint(SpringLayout.SOUTH, inputVlcExecutable, 0, SpringLayout.SOUTH, btnSelectVlc);
        springLayout.putConstraint(SpringLayout.EAST, inputVlcExecutable, -25, SpringLayout.EAST, this);
        add(inputVlcExecutable);
        inputVlcExecutable.setColumns(10);

        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, cancelBtn, -64, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.SOUTH, cancelBtn, -31, SpringLayout.SOUTH, this);
        springLayout.putConstraint(SpringLayout.EAST, cancelBtn, -269, SpringLayout.EAST, this);
        add(cancelBtn);

        startBtn = new JButton("Start");
        startBtn.setEnabled(false);
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStart();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, startBtn, 0, SpringLayout.NORTH, cancelBtn);
        springLayout.putConstraint(SpringLayout.WEST, startBtn, -148, SpringLayout.EAST, this);
        springLayout.putConstraint(SpringLayout.SOUTH, startBtn, 0, SpringLayout.SOUTH, cancelBtn);
        springLayout.putConstraint(SpringLayout.EAST, startBtn, -67, SpringLayout.EAST, this);
        add(startBtn);

        btnSelectOutput = new JButton("Output");
        btnSelectOutput.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseOutputFile();
            }
        });
        springLayout.putConstraint(SpringLayout.NORTH, btnSelectOutput, 21, SpringLayout.SOUTH, btnSelectVlc);
        springLayout.putConstraint(SpringLayout.WEST, btnSelectOutput, 21, SpringLayout.WEST, this);
        springLayout.putConstraint(SpringLayout.EAST, btnSelectOutput, 0, SpringLayout.EAST, btnSelectVideo);
        add(btnSelectOutput);

        outputOgg = new JTextField();
        outputOgg.setEditable(false);
        springLayout.putConstraint(SpringLayout.NORTH, outputOgg, 0, SpringLayout.NORTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.WEST, outputOgg, 18, SpringLayout.EAST, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.SOUTH, outputOgg, 0, SpringLayout.SOUTH, btnSelectOutput);
        springLayout.putConstraint(SpringLayout.EAST, outputOgg, -25, SpringLayout.EAST, this);
        add(outputOgg);
        outputOgg.setColumns(10);

        resolveVlcExecutable();
    }

    private void resolveVlcExecutable() {

        String[] execPaths = CheckOS.isWindows() ? VLC_EXE_PATH_WIN32 : VLC_EXE_PATH_LINUX;


        for (String path : execPaths) {

            File vlc = new File(path);

            if (validateVlcExecutable(vlc)) {
                break;
            }
        }
    }

    private void cancel() {

        onClose();
    }

    private void onStart() {

        try {
            ExecHelper.exec(false, inputVlcExecutable.getText(), video.getAbsolutePath(), "--sout",
                    String.format("#transcode{vcodec=theo,vb=800,scale=1,acodec=vorb,ab=128,channels=2,samplerate=44100}:file{dst=%s}", ExecHelper.fixPath(outfile)));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
        }

        onClose();

    }

    protected abstract void onClose();

    private File chooseFile(FileFilter fileFilter, boolean saveFile) {

        if (fc == null) {
            fc = new JFileChooser();
        }

        fc.setFileFilter(fileFilter);


        int status = saveFile ? fc.showSaveDialog(this) : fc.showOpenDialog(this);


        return JFileChooser.APPROVE_OPTION == status ? fc.getSelectedFile() : null;
    }


    protected void chooseOutputFile() {

        outfile = chooseFile(new FileFilter() {

            @Override
            public String getDescription() {
                return "OGG Video files";
            }

            @Override
            public boolean accept(File f) {

                if (f.isDirectory()) {
                    return true;
                } else {
                    String name = f.getName();
                    return name.endsWith(".ogg");
                }
            }
        }, true);


        if (outfile != null) {
            outputOgg.setText(outfile.getAbsolutePath());
        }

        updateState();
    }

    protected void chooseVlcExecutable() {

        File vlc = chooseFile(new FileFilter() {

            @Override
            public String getDescription() {
                return "Executable Files";
            }

            @Override
            public boolean accept(File f) {

                if (f.isDirectory()) {
                    return true;
                } else {
                    String name = f.getName();
                    return name.endsWith(".exe");
                }
            }
        }, false);

        validateVlcExecutable(vlc);

        updateState();
    }

    protected boolean validateVlcExecutable(File vlc) {

        boolean canExecute = vlc == null ? false : vlc.canExecute();

        logger.info("checking for vlc executable in {}: canExecute: {}", vlc, canExecute);

        if (canExecute) {
            vlcExecutable = vlc;
            inputVlcExecutable.setText(vlcExecutable.getAbsolutePath());
        }

        return canExecute;
    }

    private void updateState() {

        boolean enable = video != null && vlcExecutable != null && outfile != null;

        startBtn.setEnabled(enable);

        if (enable) {
            cancelBtn.setSelected(true);
        }
    }

    protected void chooseVideoFile() {
        video = chooseFile(new FileFilter() {

            @Override
            public String getDescription() {
                return "Video files";
            }

            @Override
            public boolean accept(File f) {

                if (f.isDirectory()) {
                    return true;
                } else {
                    return true;
                }
            }
        }, false);

        if (video != null) {
            inputVideo.setText(video.getAbsolutePath());

            if (outfile == null) {
                outfile = new File(video.getAbsolutePath().replaceFirst("(\\.[\\.]+)?$", ".ogg"));
                outputOgg.setText(outfile.getAbsolutePath());
            }
        }
        updateState();
    }

}
