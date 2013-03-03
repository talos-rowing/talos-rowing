package org.nargila.robostroke.data.remote;


public interface DataTransport {
	public void write(String data);
	void start() throws Exception;
	void stop();
	void setPort(int port);
}
