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

package org.nargila.robostroke.android.app.roll;

import org.nargila.robostroke.android.graph.RSPaintProxy;
import org.nargila.robostroke.android.graph.RSPathProxy;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.RSRect;
import org.nargila.robostroke.ui.RollGraph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

/**
 * subclass of LineGraphView for setting stroke specific parameters
 */
public class RollGraphView extends View {

	private class RollGraphImpl extends RollGraph {

		public RollGraphImpl(double xRange) {
			super(xRange);
		}

		@Override
		protected int getRedColor() {
			return Color.RED;
		}

		@Override
		protected int getGreenColor() {
			return Color.GREEN;
		}

		@Override
		protected void drawRect(Object canvas, float left, int top,
				float right, int bottom, RSPaint paint) {

			((Canvas)canvas).drawRect(left, top, right, bottom, (Paint) paint);
		}

		@Override
		protected RSPaint createPaint() {
			return new RSPaintProxy();
		}

		@Override
		protected RSPath createPath() {
			return new RSPathProxy();
		}

		@Override
		protected RSRect getClipBounds(Object canvas) {
			Rect r = ((Canvas)canvas).getClipBounds();
			RSRect res = new RSRect();
			res.bottom = r.bottom;
			res.top = r.top;
			res.right = r.right;
			res.left = r.left;
			
			return res;
		}

		@Override
		protected void drawPath(Object canvas, RSPath path, RSPaint strokePaint) {
			((Canvas)canvas).drawPath((Path)path, (Paint)strokePaint);
		}

		@Override
		protected void drawLine(Object canvas, int left, float y, int right,
				float y2, RSPaint gridPaint) {
			((Canvas)canvas).drawLine(left, y, right, y2, (Paint) gridPaint);
			
		}

		@Override
		public void repaint() {
			postInvalidate();			
		}
	}
	
	private final RollGraphImpl impl;
	
	public RollGraphView(Context context, final double xRange) { 
		super(context);
		
		impl = new RollGraphImpl(xRange);

	}

	public void reset() {
		impl.reset();
	}
}