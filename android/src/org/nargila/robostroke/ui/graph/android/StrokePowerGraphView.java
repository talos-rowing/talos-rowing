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

package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.graph.DataUpdatable;
import org.nargila.robostroke.ui.graph.StrokePowerGraph;

import android.content.Context;
import android.view.View;

/**
 * subclass of LineGraphView for setting acceleration specific parameters
 */
public class StrokePowerGraphView extends View implements DataUpdatable {
	
	private final StrokePowerGraph impl;
	
	public StrokePowerGraphView(Context context, RoboStroke roboStroke) { 
		super(context);
		
		impl = new StrokePowerGraph(new UILiaisonViewImpl(this), roboStroke);
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
	
	@Override
	public boolean isDisabled() {
		return impl.isDisabled();		
	}
	
	public void disableUpdate(boolean disable) {
		impl.disableUpdate(disable);
	}
	
	@Override
	public void reset() {
		impl.reset();
	}
}