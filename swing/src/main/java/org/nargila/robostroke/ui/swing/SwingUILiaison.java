package org.nargila.robostroke.ui.swing;

import java.awt.Color;
import java.awt.Component;

import org.nargila.robostroke.ui.RSPaint;
import org.nargila.robostroke.ui.RSPath;
import org.nargila.robostroke.ui.UILiaison;

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
	
	@Override
	public void setVisible(boolean visible) {
		canvas.setVisible(visible);		
	}
}
