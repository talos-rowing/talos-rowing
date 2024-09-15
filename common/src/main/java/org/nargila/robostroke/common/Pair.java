/*
 * Copyright (c) 2024 Tal Shalif
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
package org.nargila.robostroke.common;

public class Pair<T1, T2> {
	public final T1 first;
	public final T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public static <T1, T2> Pair<T1, T2> create(T1 first, T2 second) {
		return new Pair<T1, T2>(first,second);
	}
	public static <T1, T2> Pair<T1, T2> make_pair(T1 first, T2 second) {
		return create(first,second);
	}
}
