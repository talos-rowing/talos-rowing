package org.nargila.robostroke.app;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFrame;

import org.nargila.robostroke.RoboStroke;

public class RoboStrokeSwing {

	private JFrame frame;

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
					window.frame.setVisible(true);
					
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
		
		roboStrokeAppPanel.init(rs);
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		roboStrokeAppPanel = new RoboStrokeAppPanel();
		frame.getContentPane().add(roboStrokeAppPanel, BorderLayout.CENTER);
		frame.pack();
	}

}
