package org.nargila.robostroke.ui.swing;


import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.nargila.robostroke.input.SensorDataSink;
import org.nargila.robostroke.ui.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;

public class LineGraphTest extends GraphTestBase<LineGraphView> {


	@Test
	public void test() throws Exception {
		startRs();
	}

	@Override
	protected LineGraphView createGraph() {
		
		LineGraphView res = new LineGraphView(TimeUnit.SECONDS.toNanos(8), XYSeries.XMode.ROLLING, 8, 1);
		final XYSeries xy = res.addSeries(new CyclicArrayXYSeries(XMode.ROLLING, new XYSeries.Renderer(new SwingPaint())));
		
		rs.getAccelerationFilter().addSensorDataSink(new SensorDataSink() {		
			@Override
			public void onSensorData(long timestamp, Object value) {
				float[] values = (float[]) value;
				xy.add(timestamp, values[0]);
			}
		});
		
		return res;
	}


}
