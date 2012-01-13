package org.nargila.robostroke.ui.swing;


import org.junit.Test;

public class StrokeGraphViewTest extends GraphTestBase<StrokeGraphView> {


	@Test
	public void test() throws Exception {
		startRs();
	}

	@Override
	protected StrokeGraphView createGraph() {
		return new StrokeGraphView(rs);
	}


}
