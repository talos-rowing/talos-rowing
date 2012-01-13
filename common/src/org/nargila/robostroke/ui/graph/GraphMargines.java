/*
 * Copyright (c) 2011 Tal Shalif
 * 
 * This file is part of Talos-Rowing.
 * 
 * Talos-Rowing is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Talos-Rowing is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Talos-Rowing.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.robostroke.ui.graph;

public class GraphMargines {
	public int left, top, right, bottom;
	
	public GraphMargines(int left, int top, int right, int bottom) {
		set(left, top, right, bottom); 
	}

	public void set(int left, int top, int right, int bottom) {
		this.left = left;
		this.top = top;
		this.right = right; 
		this.bottom = bottom;
	}

	public GraphMargines() {
	}
}
