/*
 * Copyright (c) 2012 Tal Shalif
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class DataStreamCopier extends DataConverter<InputStream,OutputStream> {
	
	public DataStreamCopier(InputStream in, OutputStream out, long inputLength) {
		super(in, out, inputLength);
	}

	@Override
	protected int processNext() throws IOException {
		byte[] buff = new byte[4096];

		int i = in.read(buff);

		if (i != -1) {
			out.write(buff, 0, i);
		}		

		return i;
	}
}
