package org.nargila.robostroke.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SwingCanvas implements RSCanvas {

	private final Component canvas;
	
	private final Graphics2D g;
	
	public SwingCanvas(Component canvas, Graphics g) {
		this.canvas = canvas;
		this.g = (Graphics2D) g;
	}
	
	public void drawLine(int left, float y, int right, float y2,
			RSPaint gridPaint) {
		
		SwingPaint gp = (SwingPaint) gridPaint;
		
		g.setColor(gp.color);
		g.setStroke(gp.stroke);

		g.drawLine (left, (int)y, right, (int)y2);
	}

	public void drawPath(RSPath path, RSPaint strokePaint) {
		SwingPaint p = (SwingPaint) strokePaint;
		SwingPath pth = (SwingPath) path;
		g.setColor(p.color);
		g.setStroke(p.stroke);
		g.draw(pth);
	}

	public void drawRect(int left, int top, int right, int bottom,
			RSPaint paint) {
		SwingPaint p = (SwingPaint) paint;
		g.setColor(p.color);
		g.setStroke(p.stroke);

		int width = (right - left);
		int height = (bottom - top);
		
		switch (((SwingPaint)paint).paintStyle) {
		case FILL:
			g.fillRect(left, top, width, height);
			break;
		case FILL_AND_STROKE:
			g.fillRect(left, top, width, height);
			g.drawRect(left, top, width, height);
			break;
		case STROKE:
			g.drawRect(left, top, width, height);
			break;			
		}			
	}

	public RSRect getClipBounds() {
		Rectangle r = canvas.getBounds();
		
		RSRect res = new RSRect();
		
		res.top = r.y;
		res.bottom = r.y + r.height;
		res.left = r.x;
		res.right = r.x + r.width;
		
		return res;
	}

}
