package org.nargila.robostroke.ui;

public interface RSCanvas {
	public void drawRect(int left, int top, int right, int bottom, RSPaint paint);
	public RSRect getClipBounds();
	public void drawPath(RSPath path, RSPaint strokePaint);
	public void drawLine(int left, float y, int right, float y2, RSPaint gridPaint);
}
