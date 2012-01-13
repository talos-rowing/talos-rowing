package org.nargila.robostroke.ui.swing;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.graph.UpdatableGraphBase;

public abstract class GraphTestBase<T extends SwingGraphViewBase<? extends UpdatableGraphBase>> {

	protected T graph;	
	private JFrame frame;
	protected RoboStroke rs;

	protected abstract T createGraph();
	
	@Before
	public void setUp() throws Exception {
		rs = new RoboStroke();
		graph = createGraph();
		graph.disableUpdate(false);
		graph.setPreferredSize(new Dimension(400, 200));
		frame = new JFrame();
		frame.add(graph, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		

	}
	
	@After
	public void tearDown() {
		frame.dispose();
	}
		
	protected void startRs() throws Exception {
		rs.setFileInput(new File("/home/tshalif/src/ws/talos-rowing/swing/src/test/java/org/nargila/robostroke/ui/swing/1288448996868-dataInput.txt"));		

		Thread.sleep(1000000);
	}


}
