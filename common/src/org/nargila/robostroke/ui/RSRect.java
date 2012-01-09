package org.nargila.robostroke.ui;

public class RSRect {

	public int top;
	public int bottom;
	public int left;
	public int right;
	
	public RSRect() {
	}
	
	public RSRect(RSRect rect) {
		top = rect.top;
		bottom = rect.bottom;
		left = rect.left;
		right = rect.right;
	}

	public int height() {
		return bottom - top;
	}
	public int width() {
		return right - left;
	}
	
	@Override
	public String toString() {
		return "[RSRect: x=" + left + ", y=" + top + ", width=" + width() + ", height=" + height() + "]";
	}

}
