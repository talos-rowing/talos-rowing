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

package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.common.ClockTime;
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.FileDataInput;
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.data.SensorDataInput;
import org.nargila.robostroke.data.media.ExternalMedia;
import org.nargila.robostroke.data.media.ExternalMedia.EventType;
import org.nargila.robostroke.data.media.ExternalMedia.VideoEffect;
import org.nargila.robostroke.data.media.MediaSynchedFileDataInput;
import org.nargila.robostroke.data.remote.RemoteDataInput;
import org.nargila.robostroke.media.MediaPlayerFactory;
import org.nargila.robostroke.media.SetupExternalMeidaInfoDialog;
import org.nargila.robostroke.oggz.CovertVideoDialog;
import org.nargila.robostroke.ui.graph.swing.AccellGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeAnalysisGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeGraphView;
import org.nargila.robostroke.ui.meters.MetersDisplayManager;
import org.nargila.robostroke.ui.meters.swing.SwingMeterView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoboStrokeAppPanel extends JPanel {

    private static Logger logger = LoggerFactory.getLogger(RoboStrokeAppPanel.class);

    private static final long serialVersionUID = 1L;

    private AccellGraphView accellGraph;
    private StrokeAnalysisGraphView strokeAnalysisGraph;
    private StrokeGraphView strokeGraph;
    private final JPanel accellGraphContainer;
    private final JPanel analysisGraphContainer;
    private final JPanel strokeGraphContainer;
    private final JPanel videoPanel;

    private RoboStroke rs;

    SwingMeterView meterView;

    private MetersDisplayManager metersDisplayManager;

    private final JSlider slider;

    private final AtomicBoolean paused = new AtomicBoolean(true);

    protected ParamEditDialog paramEditDialog;
    private final JMenuItem mntmExport;

    private final JCheckBoxMenuItem chckbxmntmStroke;

    private final JCheckBoxMenuItem chckbxmntmAnalysis;

    private final JCheckBoxMenuItem chckbxmntmAccel;

    private final JSplitPane splitPane;
    private final JLabel lblSlowFast;
    private final JLabel lblBack;

    private final JLabel lblPlayPause;

    private final JMenuItem mntmMediaSetup;
    private final JLabel lblForward;
    private final JLabel lblStep;
    private final JLabel lblMediaTime;

    /**
     * Create the panel.
     */
    public RoboStrokeAppPanel() {
        setLayout(new BorderLayout(0, 0));

        JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmOpen = new JMenuItem("Open (Talos)");
        mntmOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileAction(false);
            }
        });
        mnFile.add(mntmOpen);

        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        mntmExport = new JMenuItem("Export");
        mntmExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchExportWizard();
            }
        });

        JMenuItem mntmOpenRemote = new JMenuItem("Open (Remote)");
        mntmOpenRemote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRemoteAction();
            }
        });
        
        JMenuItem mntmMedia = new JMenuItem("Open (Media)");
        mntmMedia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileAction(true);
            }
        });
        mnFile.add(mntmMedia);
        mnFile.add(mntmOpenRemote);
        mntmExport.setEnabled(false);
        mnFile.add(mntmExport);

        mnFile.add(mntmExit);

        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        JMenuItem mntmEditParams = new JMenuItem("Preferences");
        mntmEditParams.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (paramEditDialog == null) {
                    paramEditDialog = new ParamEditDialog();
                    paramEditDialog.init(rs);
                }

                paramEditDialog.setLocationRelativeTo(RoboStrokeAppPanel.this);
                paramEditDialog.setVisible(true);

            }
        });

        mnEdit.add(mntmEditParams);

        JMenu mnView = new JMenu("View");
        menuBar.add(mnView);

        chckbxmntmAccel = new JCheckBoxMenuItem("Accel");
        chckbxmntmAccel.setSelected(true);
        chckbxmntmAccel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accellGraph.getParent().setVisible(chckbxmntmAccel.isSelected());
            }
        });

        mnView.add(chckbxmntmAccel);

        chckbxmntmStroke = new JCheckBoxMenuItem("Stroke");
        chckbxmntmStroke.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                strokeGraph.getParent().setVisible(chckbxmntmStroke.isSelected());
            }
        });
        mnView.add(chckbxmntmStroke);

        JSeparator separator = new JSeparator();
        mnView.add(separator);

        chckbxmntmAnalysis = new JCheckBoxMenuItem("Analysis");
        chckbxmntmAnalysis.setSelected(true);
        chckbxmntmAnalysis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                strokeAnalysisGraph.getParent().setVisible(chckbxmntmAnalysis.isSelected());
            }
        });

        mnView.add(chckbxmntmAnalysis);

        JMenu mnTools = new JMenu("Tools");
        menuBar.add(mnTools);

        JMenuItem mntmOggConvert = new JMenuItem("OGG Convert");
        mntmOggConvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchVideoConverter();
            }
        });

        mnTools.add(mntmOggConvert);

        mntmMediaSetup = new JMenuItem("Media Setup");
        mnTools.add(mntmMediaSetup);
        mntmMediaSetup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchMediaSetup();
            }
        });

        videoPanel = new JPanel();
        videoPanel.setBackground(Color.BLACK);

        videoPanel.setPreferredSize(new Dimension(0, 0));
        videoPanel.setMinimumSize(new Dimension(0, 0));

        final JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(300, 600));
        panel.setBackground(Color.BLACK);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, videoPanel);
        videoPanel.setLayout(new BorderLayout(0, 0));
        videoPanel.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                videoPanel.setPreferredSize(new Dimension(640, 420));
                splitPane.setDividerSize(10);
                SwingUtilities.getWindowAncestor(splitPane).pack();
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        splitPane.setDividerLocation(-1);
                    }
                });
            }
            @Override
            public void componentRemoved(ContainerEvent e) {
                if (videoPanel.getComponentCount() == 0) {
                    splitPane.setDividerSize(0);
                    videoPanel.setPreferredSize(new Dimension(0, 0));
                    SwingUtilities.getWindowAncestor(splitPane).pack();
                }
            }
        });

        splitPane.setDividerSize(0);
        add(splitPane, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        meterView = new SwingMeterView();
        meterView.setPreferredSize(new Dimension(350, 432));
        panel.add(meterView);

        JPanel panel_2 = new JPanel();
        panel_2.setOpaque(false);
        panel_2.setMaximumSize(new Dimension(32767, 20));
        panel_2.setBackground(Color.WHITE);
        panel.add(panel_2);
                panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));
                
                JPanel panel_3 = new JPanel();
                panel_3.setOpaque(false);
                panel_2.add(panel_3);
                panel_3.setLayout(new BorderLayout(0, 0));
                
                        slider = new JSlider();
                        panel_3.add(slider, BorderLayout.CENTER);
                        slider.setBorder(new EmptyBorder(2, 5, 2, 0));
                        slider.addChangeListener(new ChangeListener() {
                            @Override
                            public void stateChanged(ChangeEvent e) {
                                if (rs != null && slider.getValueIsAdjusting()) {

                                    double progress = slider.getValue() / (double)slider.getMaximum();

                                    RecordDataInput input = (RecordDataInput) rs.getDataInput();

                                    if (input != null) {
                                        input.setPos(progress);
                                    }
                                }
                            }
                        });
                        
                        
                                slider.setBackground(Color.BLACK);
                                slider.setValue(0);
                                
                                lblMediaTime = new JLabel("00:00:00,000");
                                lblMediaTime.setVisible(false);
                                lblMediaTime.setForeground(Color.WHITE);
                                panel_3.add(lblMediaTime, BorderLayout.EAST);
                                lblMediaTime.setBorder(new EmptyBorder(0, 0, 0, 5));
                                lblMediaTime.setFont(new Font("Dialog", Font.BOLD, 8));
        
                JPanel horizontalBox = new JPanel();
                horizontalBox.setOpaque(false);
                horizontalBox.setBorder(new EmptyBorder(1, 0, 1, 0));
                panel_2.add(horizontalBox);
                horizontalBox.setLayout(new GridLayout(0, 5, 1, 0));
                
                        lblPlayPause = new JLabel(">");
                        lblPlayPause.setEnabled(false);
                        lblPlayPause.setOpaque(true);
                        lblPlayPause.setBackground(Color.BLACK);
                        lblPlayPause.setHorizontalAlignment(SwingConstants.CENTER);
                        horizontalBox.add(lblPlayPause);
                        lblPlayPause.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {

                                if (rs != null) {
                                    RecordDataInput input = (RecordDataInput) rs.getDataInput();
                                    if (input != null) {
                                        input.setPaused(!paused.get());
                                    }
                                }
                            }
                        });
                        
                                lblPlayPause.setFont(new Font("Dialog", Font.BOLD, 22));
                                lblPlayPause.setForeground(Color.WHITE);
                                
                                        lblSlowFast = new JLabel("S");
                                        lblSlowFast.setEnabled(false);
                                        lblSlowFast.addMouseListener(new MouseAdapter() {

                                            private boolean slow;

                                            @Override
                                            public void mouseClicked(MouseEvent e) {
                                                if (rs != null) {
                                                    RecordDataInput input = (RecordDataInput) rs.getDataInput();
                                                    if (input instanceof MediaSynchedFileDataInput) {

                                                        ((MediaSynchedFileDataInput)input).setRate(slow ? 1.0 : 0.2);
                                                        slow = !slow;
                                                    }
                                                    lblSlowFast.setText(slow ? "F" : "S");
                                                }
                                            }
                                        });
                                        
                                                lblSlowFast.setBackground(Color.BLACK);
                                                lblSlowFast.setOpaque(true);
                                                lblSlowFast.setHorizontalAlignment(SwingConstants.CENTER);
                                                lblSlowFast.setForeground(Color.WHITE);
                                                lblSlowFast.setFont(new Font("Dialog", Font.BOLD, 22));
                                                horizontalBox.add(lblSlowFast);
                                                
                                                        lblBack = new JLabel("-3");
                                                        lblBack.setEnabled(false);
                                                        lblBack.addMouseListener(new MouseAdapter() {

                                                            @Override
                                                            public void mouseClicked(MouseEvent e) {
                                                                if (rs != null) {
                                                                    RecordDataInput input = (RecordDataInput) rs.getDataInput();
                                                                    if (input instanceof MediaSynchedFileDataInput) {

                                                                        ((MediaSynchedFileDataInput)input).skipTime(-4000);
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        lblBack.setBackground(Color.BLACK);
                                                        lblBack.setOpaque(true);
                                                        lblBack.setHorizontalAlignment(SwingConstants.CENTER);
                                                        lblBack.setFont(new Font("Dialog", Font.BOLD, 22));
                                                        lblBack.setForeground(Color.WHITE);
                                                        horizontalBox.add(lblBack);
                                                        
                                                                lblForward = new JLabel("+3");
                                                                lblForward.setEnabled(false);
                                                                lblForward.addMouseListener(new MouseAdapter() {
                                                                    @Override
                                                                    public void mouseClicked(MouseEvent e) {
                                                                        if (rs != null) {
                                                                            RecordDataInput input = (RecordDataInput) rs.getDataInput();
                                                                            if (input instanceof MediaSynchedFileDataInput) {

                                                                                ((MediaSynchedFileDataInput)input).skipTime(3000);
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                                lblForward.setBackground(Color.BLACK);
                                                                lblForward.setOpaque(true);
                                                                lblForward.setHorizontalAlignment(SwingConstants.CENTER);
                                                                lblForward.setFont(new Font("Dialog", Font.BOLD, 22));
                                                                lblForward.setForeground(Color.WHITE);
                                                                horizontalBox.add(lblForward);
                                                                
                                                                        lblStep = new JLabel("+>");
                                                                        lblStep.setEnabled(false);
                                                                        lblStep.addMouseListener(new MouseAdapter() {
                                                                            @Override
                                                                            public void mouseClicked(MouseEvent e) {
                                                                                if (rs != null) {
                                                                                    RecordDataInput input = (RecordDataInput) rs.getDataInput();
                                                                                    if (input instanceof MediaSynchedFileDataInput) {

                                                                                        ((MediaSynchedFileDataInput)input).step();
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                                        lblStep.setFont(new Font("Dialog", Font.BOLD, 22));
                                                                        lblStep.setHorizontalAlignment(SwingConstants.CENTER);
                                                                        lblStep.setForeground(Color.WHITE);
                                                                        lblStep.setBackground(Color.BLACK);
                                                                        lblStep.setOpaque(true);
                                                                        horizontalBox.add(lblStep);

        JSeparator separator_2 = new JSeparator();
        panel.add(separator_2);

        Box panel_1 = new Box(BoxLayout.Y_AXIS);
        panel_1.setBackground(Color.BLACK);
        panel.add(panel_1);

        accellGraphContainer = new JPanel();
        accellGraphContainer.setPreferredSize(new Dimension(10, 70));
        accellGraphContainer.setBackground(Color.BLACK);
        panel_1.add(accellGraphContainer);
        accellGraphContainer.setLayout(new BorderLayout(0, 0));

        JLabel lblAccelerationGraph = new JLabel("Acceleration Graph");
        lblAccelerationGraph.setHorizontalAlignment(SwingConstants.CENTER);
        accellGraphContainer.add(lblAccelerationGraph, BorderLayout.CENTER);

        JSeparator separator_3 = new JSeparator();
        panel_1.add(separator_3);

        analysisGraphContainer = new JPanel();
        analysisGraphContainer.setPreferredSize(new Dimension(10, 70));
        analysisGraphContainer.setBackground(Color.BLACK);
        panel_1.add(analysisGraphContainer);
        analysisGraphContainer.setLayout(new BorderLayout(0, 0));

        JLabel lblNewLabel = new JLabel("Stroke Analysis Graph");
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        analysisGraphContainer.add(lblNewLabel, BorderLayout.CENTER);

        JSeparator separator_4 = new JSeparator();
        panel_1.add(separator_4);

        strokeGraphContainer = new JPanel();
        strokeGraphContainer.setPreferredSize(new Dimension(10, 70));
        strokeGraphContainer.setBackground(Color.BLACK);
        panel_1.add(strokeGraphContainer);
        strokeGraphContainer.setLayout(new BorderLayout(0, 0));

        JLabel lblNewLabel_1 = new JLabel("Stroke Graph");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
        strokeGraphContainer.add(lblNewLabel_1, BorderLayout.CENTER);

    }
    private void launchMediaSetup() {

        SetupExternalMeidaInfoDialog dialog = new SetupExternalMeidaInfoDialog();

        dialog.setSize(500, 450);

        dialog.setLocationRelativeTo(this);
        
        dialog.setVisible(true);
    }

    private void launchVideoConverter() {

        CovertVideoDialog dialog = new CovertVideoDialog();

        dialog.setSize(500, 300);

        dialog.setLocationRelativeTo(this);

        dialog.setVisible(true);

    }

    private void launchExportWizard() {

        DataExportDialog exportDialog = new DataExportDialog(rs);

        exportDialog.setLocationRelativeTo(this);

        exportDialog.setVisible(true);
    }

    private void openFileAction(final boolean isMedia) {
        JFileChooser fc = new JFileChooser(Settings.getInstance().getLastDir());
        fc.setFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return isMedia ? "Talos Rowing Data/Media conf files" : "Talos Rowing Data Files";
            }

            @Override
            public boolean accept(File f) {

                if (f.isDirectory()) {
                    return true;
                } else {
                    String name = f.getName();
                    return isMedia ? name.endsWith(".trsm") : name.endsWith(".trsd") || name.endsWith(".txt");
                }
            }
        });

        if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {

            File f = fc.getSelectedFile();

            Settings.getInstance().setLastDir(f.getParentFile());

            Properties mediaConf;
            
            if (isMedia) {
                
                try {
                    mediaConf = new Properties();
                    mediaConf.load(new FileReader(f));
                    
                    f = validateFileProperty(mediaConf, MediaSynchedFileDataInput.PROP_TALOS_DATA);
                    validateFileProperty(mediaConf, MediaSynchedFileDataInput.PROP_TALOS_DATA);
                } catch (Exception e) {
                    showError("Media configuration load error", e.getMessage());
                    return;
                }                                                
            } else {
                mediaConf = null;
            }
            
            prepareFile(f, mediaConf);
        }
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private File validateFileProperty(Properties prop, String propName) throws Exception {
        
        String val = prop.getProperty(propName, null);
        
        File f = val == null ? null : new File(val);

        if (f == null) {
            throw new IllegalArgumentException("property " + propName + " must be defined");
        }
        
        if (!f.canRead() || !f.isFile()) {
            throw new IllegalArgumentException(f + " does is not be opened as a file");                    
        }
        
        return f;
    }
    
    private void openRemoteAction() {		

        try {
            RemoteDataInput dataInput = new RemoteDataInput(rs);
            start(dataInput, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }	

    private void prepareFile(final File f, final Properties mediaConf) {
        
        @SuppressWarnings("serial")
        final PrepareFileDialog pfd = new PrepareFileDialog() {
            @Override
            protected void onFinish(File res) {                
                if (res != null) {
                    start(res, mediaConf);
                }
            }
        };

        pfd.setLocationRelativeTo(this);

        pfd.launch(f);
    }

    void start(File f, Properties mediaConf) {
        
        if (f != null) {
            start(null, null);
        }
                
        try {						
            Pair<SensorDataInput, Boolean> input = setInput(f, mediaConf);
            start(input.first, input.second);
        } catch (Exception e) {
            logger.error("error opening file " + f, e);
        }
    }

    void start(SensorDataInput dataInput, boolean canExport) {

        rs.setInput(null);				
        reset();
        rs.setInput(dataInput);				
        mntmExport.setEnabled(canExport);

    }


    private Pair<SensorDataInput,Boolean> setInput(File inputFile, Properties mediaConf) throws Exception {

        SensorDataInput dataInput = null;

        boolean isVideo = mediaConf != null;
        
        if (inputFile != null) {

            if (isVideo) {			
                dataInput = setupSynchedMediaDataInput(inputFile, mediaConf);
            } else {
                dataInput = new FileDataInput(rs, inputFile);
            }
        }
        
        lblBack.setEnabled(isVideo);
        lblSlowFast.setEnabled(isVideo);
        lblForward.setEnabled(isVideo);
        lblStep.setEnabled(isVideo);
        lblMediaTime.setVisible(isVideo);
        
        lblPlayPause.setEnabled(inputFile != null);
        
        return Pair.create(dataInput, !isVideo);
    }
    
    private SensorDataInput setupSynchedMediaDataInput(File inputFile, Properties mediaConf) throws Exception {

        SensorDataInput dataInput;

        for (String key: new String[]{MediaSynchedFileDataInput.PROP_TIME_OFFSET, MediaSynchedFileDataInput.PROP_SYCH_MARK_ID}) {
            if (null == mediaConf.getProperty(key, (String)null)) {
                logger.warn("mandatory property {} is not defined", key);
            }				
        }

        long synchTimeOffset = new Long(mediaConf.getProperty(MediaSynchedFileDataInput.PROP_TIME_OFFSET, "0"));
        int synchMarkId = new Integer(mediaConf.getProperty(MediaSynchedFileDataInput.PROP_SYCH_MARK_ID, "1"));					
        File mediaFile = validateFileProperty(mediaConf, MediaSynchedFileDataInput.PROP_MEDIA_FILE); 

        VideoEffect videoEffect = VideoEffect.valueOf(mediaConf.getProperty(MediaSynchedFileDataInput.PROP_VIDEO_EFFECT, "NONE"));

        ExternalMedia mediaPlayer = MediaPlayerFactory.createMediaPlayer(mediaFile, videoPanel, videoEffect, new ExternalMedia.EventListener() {

            @Override
            public void onEvent(EventType event, Object data) {
                switch (event) {
                    case TIME:
                        long time = (Long) data;
                        lblMediaTime.setText(ClockTime.fromMillis(time).toString());
                        break;                        
                }                
            }
        });

        dataInput = new MediaSynchedFileDataInput(rs, inputFile, mediaPlayer, synchTimeOffset, synchMarkId);

        return dataInput;
    }

    void reset() {
        graphsReset();

        metersDisplayManager.reset();
    }

    private void graphsReset() {
        accellGraph.reset();
        strokeAnalysisGraph.reset();
        strokeGraph.reset();
    }



    void init(RoboStroke rs) {

        this.rs = rs;

        accellGraph = new AccellGraphView(rs);
        accellGraphContainer.removeAll();
        accellGraphContainer.add(accellGraph, BorderLayout.CENTER);

        strokeAnalysisGraph = new StrokeAnalysisGraphView(rs);
        analysisGraphContainer.removeAll();
        analysisGraphContainer.add(strokeAnalysisGraph, BorderLayout.CENTER);

        strokeGraph = new StrokeGraphView(rs);
        strokeGraphContainer.removeAll();
        strokeGraphContainer.add(strokeGraph, BorderLayout.CENTER);	

        metersDisplayManager = new MetersDisplayManager(rs, meterView);

        accellGraph.disableUpdate(false);
        strokeAnalysisGraph.disableUpdate(false);
        strokeGraph.disableUpdate(false);

        strokeGraph.getParent().setVisible(chckbxmntmStroke.isSelected());
        accellGraph.getParent().setVisible(chckbxmntmAccel.isSelected());
        strokeAnalysisGraph.getParent().setVisible(chckbxmntmAnalysis.isSelected());

        rs.getBus().addBusListener(new BusEventListener() {

            @Override
            public void onBusEvent(DataRecord event) {

                switch (event.type) {
                    case REPLAY_PAUSED:
                    case INPUT_STOP:
                        updatePlayPause(true);
                        break;
                    case REPLAY_PLAYING:
                    case INPUT_START:
                        updatePlayPause(false);
                        break;                        
                    case REPLAY_PROGRESS:

                        if (!slider.getValueIsAdjusting()) {
                            double progress = (Double)event.data;

                            slider.setValue((int)(progress * slider.getMaximum()));
                        }

                        break;

                    case REPLAY_SKIPPED:
                        graphsReset();

                        break;
                    default:
                        break;

                }
            }
        });

        splitPane.resetToPreferredSizes();
    }

    private void updatePlayPause(boolean pauseState) {        
        paused.set(pauseState);        
        lblPlayPause.setText(paused.get() ? ">" : "=");
    }
}
