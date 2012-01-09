package org.nargila.robostroke.ui;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

public class LineGraphTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private LineGraphView graph;	
	private JFrame frame;
	private RoboStroke rs;

	@Before
	public void setUp() throws Exception {
		rs = new RoboStroke();
		graph = new LineGraphView(TimeUnit.SECONDS.toNanos(8), XYSeries.XMode.ROLLING, 8, 1);
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
	
	
	@Test
	public void testEnableDisable() throws Exception {
		
		assertFalse(graph.isDisabled());
		
		graph.disableUpdate(true);
		
		assertTrue(graph.isDisabled());
		
	}

	@Test
	public void test() throws Exception {
		graph.disableUpdate(false);
		
		final XYSeries xy = graph.addSeries(new CyclicArrayXYSeries(XMode.ROLLING, new XYSeries.Renderer(new SwingPaint())));

		rs.getAccelerationFilter().addSensorDataSink(new SensorDataSink() {		
			@Override
			public void onSensorData(long timestamp, Object value) {
				float[] values = (float[]) value;
				xy.add(timestamp, values[0]);
			}
		});
				
		rs.setFileInput(new File("/home/tshalif/src/ws/talos-rowing/swing/src/test/java/org/nargila/robostroke/ui/1288448996868-dataInput.txt"));
		Thread.sleep(1000000);
	}


}
