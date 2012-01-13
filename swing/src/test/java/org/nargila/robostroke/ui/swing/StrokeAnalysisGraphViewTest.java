package org.nargila.robostroke.ui.swing;


import org.junit.Test;

public class StrokeAnalysisGraphViewTest extends GraphTestBase<StrokeAnalysisGraphView> {


	@Override
	protected StrokeAnalysisGraphView createGraph() {
		return new StrokeAnalysisGraphView(rs); 
	}

	@Test
	public void test() throws Exception {
		startRs();
	}
}
