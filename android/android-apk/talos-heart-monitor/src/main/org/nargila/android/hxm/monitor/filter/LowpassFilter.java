/* 
 * Copyright (C) 2010 Tal Shalif
 * 
 * This file is part of robostroke HRM.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.nargila.android.hxm.monitor.filter;

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
