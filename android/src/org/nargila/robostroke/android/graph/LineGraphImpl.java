package org.nargila.robostroke.android.graph;

import org.nargila.robostroke.ui.LineGraph;
import org.nargila.robostroke.ui.MultiXYSeries;
import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.RSRect;

import android.view.View;

public class LineGraphImpl extends LineGraph {

	private final GraphCommon graphCommon;
	
	public LineGraphImpl(View owner, double yRange, double yGridInterval,
			MultiXYSeries multiSeries) {
		super(yRange, yGridInterval, multiSeries);
		

		graphCommon = new GraphCommon(owner);
		
	}
	
	@Override
	protected RSPath createPath() {
		return graphCommon.createPath();
	}
	
	@Override
	protected RSRect getClipBounds(Object canvas) {
		return graphCommon.getClipBounds(canvas);
	}
	
	@Override
	protected void drawPath(Object canvas, RSPath path, RSPaint strokePaint) {
		graphCommon.drawPath(canvas, path, strokePaint);
	}
	
	@Override
	protected void drawLine(Object canvas, int left, float y, int right,
			float y2, RSPaint gridPaint) {
		graphCommon.drawLine(canvas, left, y, right, y2, gridPaint);
		
	}
	
	@Override
	public void repaint() {
		graphCommon.repaint();	
	}
	
	@Override
	protected RSPaint createPaint() {
		return graphCommon.createPaint();
	}		
		
	@Override
	protected int getRedColor() {
		return graphCommon.getRedColor();
	}

	@Override
	protected int getGreenColor() {
		return graphCommon.getGreenColor();
	}

	@Override
	protected void drawRect(Object canvas, float left, int top, float right,
			int bottom, RSPaint paint) {
		graphCommon.drawRect(canvas, left, top, right, bottom, paint);
		
	}
}
