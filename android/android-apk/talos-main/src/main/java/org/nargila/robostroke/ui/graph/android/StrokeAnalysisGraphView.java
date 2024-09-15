/*
 * Copyright (c) 2024 Tal Shalif
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

package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.android.AndroidUILiaison;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.StrokeAnalysisGraph;

import android.content.Context;
import android.widget.FrameLayout;


/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokeAnalysisGraphView extends FrameLayout implements DataUpdatable {

	private final StrokeAnalysisGraph graph;
	
	public StrokeAnalysisGraphView(Context context, RoboStroke roboStroke) {
		
		super(context);
		
		StrokeAnalysisGraphSingleView g1 = new StrokeAnalysisGraphSingleView(context, roboStroke);
		StrokeAnalysisGraphSingleView g2 = new StrokeAnalysisGraphSingleView(context, roboStroke);
		
		LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		addView(g1, layoutParams);
		addView(g2, layoutParams);
		
		graph = new StrokeAnalysisGraph(new AndroidUILiaison(this), roboStroke, g1.graph, g2.graph);
		
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
	
	@Override
	protected void onAttachedToWindow() {
		disableUpdate(false);
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		disableUpdate(true);
		super.onDetachedFromWindow();
	}	
}