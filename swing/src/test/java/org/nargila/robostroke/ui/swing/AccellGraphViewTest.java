package org.nargila.robostroke.ui.swing;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AccellGraphViewTest extends GraphTestBase<AccellGraphView> {


	@Test
	public void testEnableDisable() throws Exception {
		
		assertFalse(graph.isDisabled());
		
		graph.disableUpdate(true);
		
		assertTrue(graph.isDisabled());
		
	}

	@Test
	public void test() throws Exception {
		startRs();
	}

	@Override
	protected AccellGraphView createGraph() {
		return new AccellGraphView(rs);
	}


}
