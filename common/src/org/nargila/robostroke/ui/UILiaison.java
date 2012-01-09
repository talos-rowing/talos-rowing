package org.nargila.robostroke.ui;


public interface UILiaison {

	abstract int getRedColor();

	abstract int getGreenColor();

	abstract int getYellowColor();

	abstract RSPaint createPaint();

	abstract RSPath createPath();
	
	public void repaint(); /* {
	view.postInvalidate();
} */
	
	public Object getComponent();

}
