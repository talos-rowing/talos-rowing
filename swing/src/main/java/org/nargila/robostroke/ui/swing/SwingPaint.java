package org.nargila.robostroke.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.nargila.robostroke.ui.PaintStyle;
import org.nargila.robostroke.ui.RSPaint;

public class SwingPaint implements RSPaint {

	boolean antiAlias;
	
	Color color = Color.BLACK;

	Stroke stroke = new BasicStroke(1);

	PaintStyle paintStyle;
	
	public void setARGB(int a, int r, int g, int b) {
		
		if (a < 0 || a > 255) {
			throw new IllegalArgumentException("HDIGH!");			
		}
		
		color = new Color(r, g, b, a);		
	}

	public void setAlpha(int abs) {
		setARGB(abs, color.getRed(), color.getGreen(), color.getBlue());
	}

	public void setAntiAlias(boolean antiAlias) {
		this.antiAlias = antiAlias;
	}

	public void setColor(int color) {
		this.color = new Color(color);
	}

	public void setStrokeWidth(float width) {
		this.stroke = new BasicStroke(width);

	}

	public Stroke getStroke() {
		return stroke;
	}
	
	public void setStyle(PaintStyle style) {
		this.paintStyle = style;

	}

}
