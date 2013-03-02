package org.nargila.robostroke.data.remote;

import java.io.IOException;

public interface DataTransport {
	public void write(String data);
	void start() throws IOException;
	void stop();
}
