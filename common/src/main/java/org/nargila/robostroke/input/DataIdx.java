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

package org.nargila.robostroke.input;

/**
 * Static array index ID holder.
 * The index IDs can help keeping the code more readable by using e.g.
 * <code>values[DataIdx.ACCEL_Y]</code> rather than <code>values[1]</code> inside the code
 */
public final class DataIdx {
	public static final int ACCEL_X = 0;
	public static final int ACCEL_Y = 1;
	public static final int ACCEL_Z = 2;
	public static final int ORIENT_AZIMUTH = 0;
	public static final int ORIENT_PITCH = 1;
	public static final int ORIENT_ROLL = 2;
	public static final int GPS_LAT = 0;
	public static final int GPS_LONG = 1;
	public static final int GPS_ALT = 2;
	public static final int GPS_SPEED = 3;
	public static final int GPS_BEARING = 4;
	public static final int GPS_ACCURACY = 5;
	public static final int GPS_ITEM_COUNT_ = 6;	
}
