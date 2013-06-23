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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.FileDataInput;
import org.nargila.robostroke.data.RecordDataInput;
import org.nargila.robostroke.data.SensorDataInput;
import org.nargila.robostroke.data.remote.RemoteDataInput;
import org.nargila.robostroke.oggz.CovertVideoDialog;
import org.nargila.robostroke.oggz.MergeTalosOggDialog;
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
	private JPanel accellGraphContainer;
	private JPanel analysisGraphContainer;
	private JPanel strokeGraphContainer;
	private JPanel videoPanel;
	
	private RoboStroke rs;

	SwingMeterView meterView;

	private MetersDisplayManager metersDisplayManager;

	private JSlider slider;

	protected boolean paused;

	protected ParamEditDialog paramEditDialog;
	private JMenuItem mntmExport;

	private JCheckBoxMenuItem chckbxmntmStroke;

	private JCheckBoxMenuItem chckbxmntmAnalysis;

	private JCheckBoxMenuItem chckbxmntmAccel;

	private JSplitPane splitPane;
	
	/**
	 * Create the panel.
	 */
	public RoboStrokeAppPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileAction(false);
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmOpenOgg = new JMenuItem("Open (OGG)");
		mntmOpenOgg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileAction(true);
			}
		});
		mnFile.add(mntmOpenOgg);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		mntmExport = new JMenuItem("Export");
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchExportWizard();
			}
		});
		
		JMenuItem mntmOpenRemote = new JMenuItem("Open (Remote)");
		mntmOpenRemote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openRemoteAction();
			}
		});
		mnFile.add(mntmOpenRemote);
		mntmExport.setEnabled(false);
		mnFile.add(mntmExport);
		
		mnFile.add(mntmExit);
		
		JMenu mnEdit = new JMenu("Edit");
		menuBar.add(mnEdit);
		
		JMenuItem mntmEditParams = new JMenuItem("Preferences");
		mntmEditParams.addActionListener(new ActionListener() {
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
			public void actionPerformed(ActionEvent e) {
				accellGraph.getParent().setVisible(chckbxmntmAccel.isSelected());
			}
		});
		
		mnView.add(chckbxmntmAccel);
		
		chckbxmntmStroke = new JCheckBoxMenuItem("Stroke");
		chckbxmntmStroke.addActionListener(new ActionListener() {
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
			public void actionPerformed(ActionEvent e) {
				strokeAnalysisGraph.getParent().setVisible(chckbxmntmAnalysis.isSelected());
			}
		});
		
		mnView.add(chckbxmntmAnalysis);
		
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		
		JMenuItem mntmOggConvert = new JMenuItem("OGG Convert");
		mntmOggConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchVideoConverter();
			}
		});
		mnTools.add(mntmOggConvert);
		
		JMenuItem mntmMergeVideo = new JMenuItem("Merge Video");
		mnTools.add(mntmMergeVideo);
		mntmMergeVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				launchVideoMerger();
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
				splitPane.setDividerSize(0);
				videoPanel.setPreferredSize(new Dimension(0, 0));
				SwingUtilities.getWindowAncestor(splitPane).pack();
			}
		});
		
		splitPane.setDividerSize(0);
		add(splitPane, BorderLayout.CENTER);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		meterView = new SwingMeterView();
		meterView.setPreferredSize(new Dimension(350, 432));
		panel.add(meterView);
		
		JPanel panel_2 = new JPanel();
		panel_2.setMaximumSize(new Dimension(32767, 20));
		panel_2.setBackground(Color.BLACK);
		panel_2.setBorder(new EmptyBorder(0, 5, 0, 5));
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		slider = new JSlider();
		slider.setBorder(new EmptyBorder(0, 5, 0, 0));
		slider.addChangeListener(new ChangeListener() {
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
		panel_2.add(slider);
		slider.setValue(0);
		
		final JLabel label = new JLabel("=");
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (rs != null) {
					RecordDataInput input = (RecordDataInput) rs.getDataInput();
					if (input != null) {
						input.setPaused(!paused);
						paused = !paused;
					}
					label.setText(paused ? ">" : "=");
				}
			}
		});
		label.setFont(new Font("Dialog", Font.BOLD, 18));
		label.setForeground(Color.WHITE);
		panel_2.add(label, BorderLayout.WEST);
		
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
	private void launchVideoMerger() {
		
		MergeTalosOggDialog dialog = new MergeTalosOggDialog();
		
		dialog.setSize(500, 350);
		
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

	private void openFileAction(final boolean ogg) {
		JFileChooser fc = new JFileChooser(Settings.getInstance().getLastDir());
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return ogg ? "Talos Rowing Data Files (OGG)" : "Talos Rowing Data Files";
			}
			
			@Override
			public boolean accept(File f) {
				
				if (f.isDirectory()) {
					return true;
				} else {
//					String name = f.getName();
//					return ogg ? name.endsWith(".ogg") : (name.endsWith(".trsd") || name.endsWith(".txt"));
					return true;
				}
			}
		});
		
		if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {

			File f = fc.getSelectedFile();
			
			Settings.getInstance().setLastDir(f.getParentFile());
			
			if (ogg) {
				start(f);
			} else {
				prepareFile(f);
			}
		}
	}

	private void openRemoteAction() {		

		try {
			RemoteDataInput dataInput = new RemoteDataInput(rs);
			start(dataInput, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	private void prepareFile(final File f) {
		
		@SuppressWarnings("serial")
		final PrepareFileDialog pfd = new PrepareFileDialog() {
			protected void onFinish(File res) {
				
				setVisible(false);
				
				if (res != null) {
					start(res);
				}
			}
		};
		
		pfd.setLocationRelativeTo(this);
		
		new Thread("RoboStrokeAppPanel prepareFile") {
			public void run() {
				pfd.launch(f);
			}
		}.start();	
		
		pfd.setVisible(true);
	}

	void start(File f) {
		
		paused = false;
		
		try {						
			Pair<SensorDataInput, Boolean> input = setInput(f);
			start(input.first, input.second);
		} catch (Exception e) {
			logger.error("error opening file " + f, e);
		}
	}

	void start(SensorDataInput dataInput, boolean canExport) {
				
		rs.setInput(null);				
		reset();
		paused = false;
		rs.setInput(dataInput);				
		mntmExport.setEnabled(canExport);

	}

	
	private Pair<SensorDataInput,Boolean> setInput(File videoFile) throws Exception {
		
		boolean isVideo = !videoFile.getName().toLowerCase().matches(".*\\.(txt|trsd)$");
		
		SensorDataInput dataInput;
		
		if (isVideo) {
	        File[] srtFiles = new File[] {
	        		new File(String.format("%s.srt", videoFile.getAbsolutePath())),
	        		new File(videoFile.getAbsolutePath().replaceFirst("\\.[a-zA-Z0-9]+$", ".srt"))
	        };
	        
	        File srtFile = null;
			for (File f: srtFiles) {
	        	        	
	        	if (f.exists()) {
	        		srtFile = f;
	        		break;
	        	}
	        }
	                
	        if (srtFile != null) {
	        	dataInput = new GstExternalDataInput(videoFile, srtFile, rs, videoPanel);
	        } else if (videoFile.getName().toLowerCase().matches(".*\\.(ogg|mkv)$")) {
	        	if (false && videoFile.getName().toLowerCase().endsWith(".ogg")) {
	        		dataInput = new OggDataInput(videoFile, rs, videoPanel);	        		
	        	} else {
	        		dataInput = new GstDataInput(videoFile, rs, videoPanel);
	        	}
	        } else {
	        	throw new IllegalArgumentException(String.format("can't find Talos Data file (either %s or %s)", srtFiles[0], srtFiles[1]));
	        }
		} else {
			dataInput = new FileDataInput(rs, videoFile);
		}
		
		
		return Pair.create(dataInput, !isVideo);
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
				case REPLAY_PROGRESS:

					if (!slider.getValueIsAdjusting()) {
						double progress = (Double)event.data;

						slider.setValue((int)(progress * slider.getMaximum()));
					}
					
					break;
					
				case REPLAY_SKIPPED:
					graphsReset();
					
					break;
					
				}
			}
		});
		
		splitPane.resetToPreferredSizes();
	}
	public JMenuItem getMntmExport() {
		return mntmExport;
	}
}
