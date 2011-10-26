package org.nargila.robostroke.android.graph;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.RSRect;
import org.nargila.robostroke.ui.StrokeGraph;

import android.view.View;

public class StrokeGraphImpl extends StrokeGraph {

	private final GraphCommon graphCommon;

	public StrokeGraphImpl(View owner, float xRange, RoboStroke roboStroke) {
		super(xRange, roboStroke);
		
		graphCommon = new GraphCommon(owner);
	}

	public RSPath createPath() {
		return graphCommon.createPath();
	}

	public RSRect getClipBounds(Object canvas) {
		return graphCommon.getClipBounds(canvas);
	}

	public void drawPath(Object canvas, RSPath path, RSPaint strokePaint) {
		graphCommon.drawPath(canvas, path, strokePaint);
	}

	public void drawLine(Object canvas, int left, float y, int right, float y2,
			RSPaint gridPaint) {
		graphCommon.drawLine(canvas, left, y, right, y2, gridPaint);
	}

	public void drawRect(Object canvas, float left, int top, float right,
			int bottom, RSPaint paint) {
		graphCommon.drawRect(canvas, left, top, right, bottom, paint);
	}

	public void repaint() {
		graphCommon.repaint();
	}

	public RSPaint createPaint() {
		return graphCommon.createPaint();
	}

	public int getYellowColor() {
		return graphCommon.getYellowColor();
	}

	public int getGreenColor() {
		return graphCommon.getGreenColor();
	}

	public int getRedColor() {
		return graphCommon.getRedColor();
	}
}
