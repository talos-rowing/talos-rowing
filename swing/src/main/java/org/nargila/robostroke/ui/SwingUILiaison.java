package org.nargila.robostroke.ui;

import java.awt.Color;
import java.awt.Component;

public class SwingUILiaison implements UILiaison {

	private final Component canvas;
	
	public SwingUILiaison(Component canvas) {
		this.canvas = canvas;
	}
	
		
	public RSPaint createPaint() {
		return new SwingPaint();
	}

	public RSPath createPath() {
		return new SwingPath();
	}

	public Object getComponent() {
		return canvas;
	}

	public int getGreenColor() {
		return Color.GREEN.getRGB();
	}

	public int getRedColor() {
		return Color.RED.getRGB();
	}

	public int getYellowColor() {
		return Color.YELLOW.getRGB();
	}

	public void repaint() {
		canvas.repaint();
	}
}
