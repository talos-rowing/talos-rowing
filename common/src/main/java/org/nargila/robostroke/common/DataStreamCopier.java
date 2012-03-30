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
