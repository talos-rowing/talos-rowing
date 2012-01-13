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

package org.nargila.robostroke.ui.graph.swing;

import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.graph.AccellGraph;
import org.nargila.robostroke.ui.swing.SwingUILiaison;


/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class AccellGraphView extends SwingGraphViewBase<AccellGraph> {
	private static final long serialVersionUID = 1L;

	private final static float XRANGE = TimeUnit.SECONDS.toNanos(8);
	
	public AccellGraphView(RoboStroke roboStroke) {
		this(XRANGE, roboStroke);
	}
	
	public AccellGraphView(float xRange, RoboStroke roboStroke) {
		setGraph(new AccellGraph(new SwingUILiaison(this), xRange, roboStroke));
	}
}