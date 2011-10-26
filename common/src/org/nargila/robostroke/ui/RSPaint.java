package org.nargila.robostroke.ui;

public interface RSPaint {

	public void setAntiAlias(boolean antiAlias);
	
	public void setStrokeWidth(float width);

	public void setARGB(int a, int r, int g, int b);
	
	
	public void setStyle(PaintStyle style);

	public void setColor(int color);

	public void setAlpha(int abs);

}