package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
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

import org.nargila.robostroke.BusEvent;
import org.nargila.robostroke.BusEventListener;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.input.FileSensorDataInput;
import org.nargila.robostroke.ui.graph.swing.AccellGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeAnalysisGraphView;
import org.nargila.robostroke.ui.graph.swing.StrokeGraphView;
import org.nargila.robostroke.ui.meters.MetersDisplayManager;
import org.nargila.robostroke.ui.meters.swing.SwingMeterView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoboStrokeAppPanel extends JPanel {

	private static Logger logger = LoggerFactory.getLogger(RoboStrokeAppPanel.class);
	
	private static final long serialVersionUID = 1L;

	private AccellGraphView accellGraph;
	private StrokeAnalysisGraphView strokeAnalysisGraph;
	private StrokeGraphView strokeGraph;
	private JPanel accellGraphContainer;
	private JPanel analysisGraphContainer;
	private JPanel strokeGraphContainer;
	
	private RoboStroke rs;

	SwingMeterView meterView;

	private MetersDisplayManager metersDisplayManager;

	private JSlider slider;

	protected boolean paused;

	protected ParamEditDialog paramEditDialog;

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
				openFileAction();
			}
		});
		mnFile.add(mntmOpen);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
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
				
				paramEditDialog.setVisible(true);
				
			}
		});
		
		mnEdit.add(mntmEditParams);
		
		JMenu mnView = new JMenu("View");
		menuBar.add(mnView);
		
		final JCheckBoxMenuItem chckbxmntmAccel = new JCheckBoxMenuItem("Accel");
		chckbxmntmAccel.setSelected(true);
		chckbxmntmAccel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accellGraph.getParent().setVisible(chckbxmntmAccel.isSelected());
			}
		});
		
		mnView.add(chckbxmntmAccel);
		
		final JCheckBoxMenuItem chckbxmntmStroke = new JCheckBoxMenuItem("Stroke");
		chckbxmntmStroke.setSelected(true);
		chckbxmntmStroke.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				strokeGraph.getParent().setVisible(chckbxmntmStroke.isSelected());
			}
		});
		mnView.add(chckbxmntmStroke);
		
		JSeparator separator = new JSeparator();
		mnView.add(separator);
		
		final JCheckBoxMenuItem chckbxmntmAnalysis = new JCheckBoxMenuItem("Analysis");
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
		meterView.setPreferredSize(new Dimension(370, 350));
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

			    	FileSensorDataInput input = (FileSensorDataInput) rs.getDataInput();
			    	
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
					FileSensorDataInput input = (FileSensorDataInput) rs.getDataInput();
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
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.BLACK);
		panel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		
		accellGraphContainer = new JPanel();
		accellGraphContainer.setPreferredSize(new Dimension(10, 70));
		accellGraphContainer.setBackground(Color.BLACK);
		panel_1.add(accellGraphContainer);
		accellGraphContainer.setLayout(new BorderLayout(0, 0));
		
		JSeparator separator_3 = new JSeparator();
		panel_1.add(separator_3);
		
		analysisGraphContainer = new JPanel();
		analysisGraphContainer.setPreferredSize(new Dimension(10, 70));
		analysisGraphContainer.setBackground(Color.BLACK);
		panel_1.add(analysisGraphContainer);
		analysisGraphContainer.setLayout(new BorderLayout(0, 0));
		
		JSeparator separator_4 = new JSeparator();
		panel_1.add(separator_4);
		
		strokeGraphContainer = new JPanel();
		strokeGraphContainer.setPreferredSize(new Dimension(10, 70));
		strokeGraphContainer.setBackground(Color.BLACK);
		panel_1.add(strokeGraphContainer);
		strokeGraphContainer.setLayout(new BorderLayout(0, 0));
		
	}

	protected void openFileAction() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "Talos Rowing Data File";
			}
			
			@Override
			public boolean accept(File f) {
				return true;
			}
		});
		
		if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(this)) {

			File f = fc.getSelectedFile();

			start(f);
		}
	}

	void start(File f) {
		
		paused = false;
		
		try {
			rs.setFileInput(f);
			reset();
		} catch (IOException e) {
			logger.error("error opening file " + f, e);
		}
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
		accellGraphContainer.add(accellGraph, BorderLayout.CENTER);
		strokeAnalysisGraph = new StrokeAnalysisGraphView(rs);
		analysisGraphContainer.add(strokeAnalysisGraph, BorderLayout.CENTER);
		strokeGraph = new StrokeGraphView(rs);
		strokeGraphContainer.add(strokeGraph, BorderLayout.CENTER);	
		
		metersDisplayManager = new MetersDisplayManager(rs, meterView);

		accellGraph.disableUpdate(false);
		strokeAnalysisGraph.disableUpdate(false);
		strokeGraph.disableUpdate(false);
		
		rs.getBus().addBusListener(new BusEventListener() {
			
			@Override
			public void onBusEvent(BusEvent event) {
				
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
}
