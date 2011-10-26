package org.nargila.robostroke.ui;

public class RSRect {

	public int top;
	public int bottom;
	public int left;
	public int right;
	
	public int height() {
		return bottom - top;
	}
	public int width() {
		return right - left;
	}

}
