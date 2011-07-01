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

package org.nargila.robostroke.common.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Low pass filter implementation.
 * Low pass filter allows filtered data amplitude to grow by as
 * much as <code>filteringFactor</code> between each call. It is
 * used as data damper/smoother.
 * @author tshalif
 *
 */
public class LowpassFilter extends PassFilterBase {
	
	/**
	 * construct with initial filtering factor
	 * @param kFilteringFactor
	 */
	public LowpassFilter(float kFilteringFactor) {
		super(kFilteringFactor);
	}
	
	public LowpassFilter() {
	}
	
	@Override
	protected float[] doFilter(float[] values) {
		for (int i = 0; i < values.length; ++i) {
			filteredValues[i] = (values[i] * filteringFactor) + (filteredValues[i] * (1.0f - filteringFactor));
		}
		
		return filteredValues;
	}
	
	public static void main(String[] args) throws Exception {
		final File file = new File(args[0]);

		BufferedReader in = new BufferedReader(new FileReader(file));

		PassFilterBase filter = new LowpassFilter();

		String l;

		while ((l = in.readLine()) != null && !l.equals("")) {
			String[] vals = l.split(" +");
			int x = new Integer(vals[0]);
			float[] yy = {new Float(vals[1])};

			float y = filter.filter(yy)[0];
			System.out.println("" + x + " " + y);
		}
	}
}
