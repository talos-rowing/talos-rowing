package org.nargila.robostroke.android.graph;

import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.RSRect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.View;

class GraphCommon {
	private final View owner;
	
	GraphCommon(View owner) {
		this.owner = owner;
	}

	public RSPath createPath() {
		return new RSPathProxy();
	}

	public RSRect getClipBounds(Object canvas) {
		Rect r = ((Canvas)canvas).getClipBounds();
		
		RSRect res = new RSRect();
		
		res.bottom = r.bottom;
		res.top = r.top;
		res.left = r.left;
		res.right = r.right;
		
		return res;
	}

	public void drawPath(Object canvas, RSPath path, RSPaint strokePaint) {
		((Canvas)canvas).drawPath((Path)path, (Paint)strokePaint);
	}

	public void drawLine(Object canvas, int left, float y, int right,
			float y2, RSPaint gridPaint) {
		((Canvas)canvas).drawLine(left, y, right, y2, (Paint) gridPaint);
		
	}

	public void drawRect(Object canvas, float left, int top, float right,
			int bottom, RSPaint paint) {
		((Canvas)canvas).drawRect(left, top, right, bottom, (Paint)paint);
	}
	
	public void repaint() {
		owner.postInvalidate();			
	}

	public RSPaint createPaint() {
		return new RSPaintProxy();
	}	
	public int getYellowColor() {
		return Color.YELLOW;
	}
	
	public int getGreenColor() {
		return Color.GREEN;
	}
	
	public int getRedColor() {
		return Color.RED;
	}
}
