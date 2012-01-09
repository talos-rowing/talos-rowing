package org.nargila.robostroke.ui.graph.android;

import java.util.LinkedList;

import org.nargila.robostroke.common.Pair;
import org.nargila.robostroke.ui.RSPath;

public class SwingPath implements RSPath {

	LinkedList<Pair<LinkedList<Integer>, LinkedList<Integer>>> lines = new LinkedList<Pair<LinkedList<Integer>,LinkedList<Integer>>>();
	
	LinkedList<Integer> xs;
	LinkedList<Integer> ys;
	
	public SwingPath() {
		initLine();
	}
	
	public void lineTo(float x, float y) {
		addPoint(x, y);
	}

	private void addPoint(float x, float y) {
		xs.add((int) x);
		ys.add((int) y);
	}
	
	private void initLine() {
		xs = new LinkedList<Integer>();
		ys = new LinkedList<Integer>();
	}

	public void moveTo(float x, float y) {
		if (xs.size() > 0) {
			lines.add(Pair.create(xs, ys));
			initLine();
		}
		
		addPoint(x, y);
	}
}
