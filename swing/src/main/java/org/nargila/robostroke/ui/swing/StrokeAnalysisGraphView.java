/*
 * Copyright (c) 2011 Tal Shalif
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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.OverlayLayout;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.graph.StrokeAnalysisGraph;


/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokeAnalysisGraphView extends SwingGraphViewBase<StrokeAnalysisGraph>  {

	private static final long serialVersionUID = 1L;
	private final StrokeAnalysisGraph graph;
	
	public StrokeAnalysisGraphView(RoboStroke roboStroke) {
		
		super(false);
		
		setLayout(new OverlayLayout(this));
		
		
		StrokeAnalysisGraphSingleView g1 = new StrokeAnalysisGraphSingleView(roboStroke);
		StrokeAnalysisGraphSingleView g2 = new StrokeAnalysisGraphSingleView(roboStroke);
		
		add(g1);
		add(g2);
		
		graph = new StrokeAnalysisGraph(new SwingUILiaison(this), roboStroke, g1.graph, g2.graph);
		
		addComponentListener(new ComponentListener() {
			
			public void componentShown(ComponentEvent e) {
				graph.disableUpdate(false);
			}
			
			public void componentResized(ComponentEvent e) {
			}
			
			public void componentMoved(ComponentEvent e) {
			}
			
			public void componentHidden(ComponentEvent e) {
				graph.disableUpdate(true);
			}
		});
	}

	@Override
	public boolean isDisabled() {
		return graph.isDisabled();
	}

	@Override
	public void disableUpdate(boolean disable) {
		graph.disableUpdate(disable);
	}

	@Override
	public void reset() {
		graph.reset();
	}
}