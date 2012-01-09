package org.nargila.robostroke.ui;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nargila.robostroke.RoboStroke;

public class AccellGraphViewTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private AccellGraphView graph;
	private JFrame frame;
	private RoboStroke rs;

	@Before
	public void setUp() throws Exception {
		rs = new RoboStroke();
		
		graph = new AccellGraphView(rs); 
		graph.setPreferredSize(new Dimension(400, 200));
		frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(graph, BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		

	}
	
	@After
	public void tearDown() {
		frame.dispose();
	}
	
	
	@Test
	public void testEnableDisable() throws Exception {
		
		assertFalse(graph.isDisabled());
		
		graph.disableUpdate(true);
		
		assertTrue(graph.isDisabled());
		
	}

	@Test
	public void test() throws Exception {
		graph.disableUpdate(false);
		rs.setFileInput(new File("/home/tshalif/src/ws/talos-rowing/swing/src/test/java/org/nargila/robostroke/ui/1288448996868-dataInput.txt"));
		
		Thread.sleep(1000000);
	}


}
