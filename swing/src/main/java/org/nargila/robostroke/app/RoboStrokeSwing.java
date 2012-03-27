package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterChangeListener;
import org.nargila.robostroke.param.ParameterService;

public class RoboStrokeSwing {

	private JFrame frmTalosRowing;

	private RoboStroke rs = new RoboStroke();

	private RoboStrokeAppPanel roboStrokeAppPanel;
		
	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RoboStrokeSwing window = new RoboStrokeSwing();
					window.frmTalosRowing.setVisible(true);
					
					if (args.length > 0) {
						File f = new File(args[0]);
						window.roboStrokeAppPanel.start(f);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RoboStrokeSwing() {
		
		initialize();
		
		loadParams(rs.getParameters());
		
		roboStrokeAppPanel.init(rs);
		
	}

	private void loadParams(ParameterService parameters) {
		
		
		final Preferences pref = Preferences.userNodeForPackage(getClass());
		
		for (Entry<String, Parameter<?>> p: parameters.getParamMap().entrySet()) {
			String value = pref.get(p.getKey(), p.getValue().convertToString());
			parameters.setParam(p.getKey(), value);
		}
		
		parameters.addListener("*", new ParameterChangeListener() {
			
			@Override
			public void onParameterChanged(Parameter<?> param) {
				pref.put(param.getId(), param.convertToString());
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmTalosRowing = new JFrame();
		frmTalosRowing.setTitle("Talos Rowing");
		frmTalosRowing.setBounds(100, 100, 450, 300);
		frmTalosRowing.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		roboStrokeAppPanel = new RoboStrokeAppPanel();
		frmTalosRowing.getContentPane().add(roboStrokeAppPanel, BorderLayout.CENTER);
		frmTalosRowing.pack();
	}

}
