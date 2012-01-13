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
package org.nargila.robostroke.ui.graph.android;

import org.nargila.robostroke.ui.RSCanvas;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.RSRect;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class RSCanvasImpl implements RSCanvas {

	private final Canvas canvas;
	
	
	public RSCanvasImpl(Canvas canvas) {
		this.canvas = canvas;
	}

	@Override
	public void drawLine(int left, float y, int right, float y2,
			RSPaint gridPaint) {
		canvas.drawLine(left, y, right, y2, (Paint) gridPaint);

	}

	@Override
	public void drawPath(RSPath path, RSPaint strokePaint) {
		canvas.drawPath((Path) path, (Paint) strokePaint);

	}

	@Override
	public void drawRect(int left, int top, int right, int bottom,
			RSPaint paint) {
		canvas.drawRect(left, top, right, bottom, (Paint) paint);

	}

	@Override
	public RSRect getClipBounds() {
		Rect r = canvas.getClipBounds();
		
		RSRect res = new RSRect();
		
		res.bottom = r.bottom;
		res.top = r.top;
		res.left = r.left;
		res.right = r.right;
		
		return res;
	}
}
