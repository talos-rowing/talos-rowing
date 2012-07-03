package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.input.DataRecord;
import org.nargila.robostroke.input.FileDataInput;
import org.nargila.robostroke.input.RecordDataInput;
import org.nargila.robostroke.input.SensorDataInput;
import org.nargila.robostroke.jst.TalosPipeline;
import org.nargila.robostroke.ui.graph.swing.AccellGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeAnalysisGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeGraphView;
import org.nargila.robostroke.ui.meters.MetersDisplayManager;
import org.nargila.robostroke.ui.meters.swing.SwingMeterView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fluendo.jst.Pad;
import com.fluendo.jst.PadListener;
import com.fluendo.jst.Pipeline;

import javax.swing.SwingConstants;

public class RoboStrokeAppPanel extends JPanel {

	private static Logger logger = LoggerFactory.getLogger(RoboStrokeAppPanel.class);
	
	private static final long serialVersionUID = 1L;

	private AccellGraphView accellGraph;
	private StrokeAnalysisGraphView strokeAnalysisGraph;
	private StrokeGraphView strokeGraph;
	private JPanel accellGraphContainer;
	private JPanel analysisGraphContainer;
	private JPanel strokeGraphContainer;
	private TalosPipeline jst;
	private JFrame videoFrame;
	private Canvas videoCanvas;
	
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
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.BLACK);
		add(panel, BorderLayout.CENTER);
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
		
		JLabel label = new JLabel("=");
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (rs != null) {
					RecordDataInput input = (RecordDataInput) rs.getDataInput();
					if (input != null) {
						input.setPaused(!paused);
						paused = !paused;
					}
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

	private void launchExportWizard() {
		
		DataExportDialog exportDialog = new DataExportDialog(rs);
		
		exportDialog.setLocationRelativeTo(this);
		
		exportDialog.setVisible(true);
	}

	private void openFileAction(final boolean ogg) {
		JFileChooser fc = new JFileChooser();
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
					String name = f.getName();
					return ogg ? name.endsWith(".ogg") : (name.endsWith(".trsd") || name.endsWith(".txt"));
				}
			}
		});
		
		if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {

			File f = fc.getSelectedFile();

			if (ogg) {
				start(f);
			} else {
				prepareFile(f);
			}
		}
	}

	private void prepareFile(final File f) {
		
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
			boolean canExport = setInput(f);
			reset();
			mntmExport.setEnabled(canExport);
		} catch (IOException e) {
			logger.error("error opening file " + f, e);
		}
	}

	private boolean setInput(File f) throws IOException {	
		
		boolean ogg = f.getName().toLowerCase().endsWith(".ogg");
		SensorDataInput dataInput;
		
		if (jst != null) {
			jst.setState(Pipeline.NONE);
			jst = null;
			
			videoFrame.setVisible(false);
		}
		
		if (ogg) {
			
			dataInput = new RecordDataInput(rs.getBus()) {
				
				@Override
				public void stop() {
					videoFrame.setVisible(false);
				}
				
				@Override
				public void start() {
					jst.setState(Pipeline.PLAY);
				}
				
				@Override
				public void skipReplayTime(float velocityX) {
					jst.setState(Pipeline.NONE);
				}
				
				@Override
				public void setPaused(boolean pause) {
					jst.setState(pause ? Pipeline.PAUSE : Pipeline.PLAY);
				}
				
				@Override
				public void setPos(double pos) {
					
					if (pos < 0 || pos > 1.0) {
						throw new IllegalArgumentException("pos must be a float between 0 and 1.0");
					}

					jst.setPos(pos);
				}
			};
			
			if (videoFrame == null) {
				videoFrame = new JFrame();
				videoCanvas = new Canvas();
				videoCanvas.setSize(500, 400);
				videoFrame.getContentPane().add(videoCanvas);
				videoFrame.pack();
				videoFrame.setLocationRelativeTo(this);
			}
			
			jst = new TalosPipeline();
			jst.setComponent(videoCanvas);
			jst.setTalosRecordPlayer((RecordDataInput) dataInput);
			jst.setUrl(f.toURI().toURL().toString());

			jst.addPadListener(new PadListener() {
				
				@Override
				public void padRemoved(Pad pad) {					
				}
				
				@Override
				public void padAdded(Pad pad) {
				}
				
				@Override
				public void noMorePads() {	
					videoFrame.setVisible(jst.hasVideo());
				}
			});
						
		} else {
			dataInput = new FileDataInput(rs.getBus(), f);
		}
		
		rs.setInput(dataInput);

		return !ogg;
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

					double progress = (Double)event.data;
					
					slider.setValue((int)(progress * slider.getMaximum()));
					
					break;
					
				case REPLAY_SKIPPED:
					graphsReset();
					
					break;
					
				}
			}
		});
	}
	public JMenuItem getMntmExport() {
		return mntmExport;
	}
}
