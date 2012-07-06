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
package org.nargila.robostroke.ui.swing;


import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.nargila.robostroke.data.SensorDataSink;
import org.nargila.robostroke.ui.graph.CyclicArrayXYSeries;
import org.nargila.robostroke.ui.graph.XYSeries;
import org.nargila.robostroke.ui.graph.XYSeries.XMode;
import org.nargila.robostroke.ui.graph.swing.LineGraphView;

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
