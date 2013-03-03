package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.data.DataRecord;
import org.nargila.robostroke.data.SessionRecorderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketDataTransport implements DataTransport {
	
	private static final Logger logger = LoggerFactory.getLogger(SocketDataTransport.class);
	
	private ServerSocket socket;
	
	private Socket s;

	private int port;
	
	private Writer recordOut;
	
	private ArrayBlockingQueue<String> recordQueue = new ArrayBlockingQueue<String>(100);	

	public SocketDataTransport(int port) {
		this.port = port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public synchronized void start() throws IOException {
		socket = new ServerSocket(port);
		
		new Thread("SocketDataTransmitterAccept") {
			@Override
			public void run() {
				
				while (!socket.isClosed()) {
					try {

						s = socket.accept();
						
						if (s.isConnected()) {
							
							recordQueue.clear();
							
							recordOut = new OutputStreamWriter(s.getOutputStream());
							
							writeRecord(new DataRecord(DataRecord.Type.LOGFILE_VERSION, -1,
									SessionRecorderConstants.LOGFILE_VERSION).toString());
							while (s.isConnected()) {

								String data = recordQueue.poll(10,
										TimeUnit.MILLISECONDS);

								if (data != null) {
									writeRecord(data);
								}
							}
						}
						

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (s != null) {
							try {
								s.close();
							} catch (IOException e) {
							}
						}
					}
				}
			}

			public void writeRecord(String data) throws InterruptedException, IOException {			
				recordOut.write(data + SessionRecorderConstants.END_OF_RECORD + "\n");
				recordOut.flush();
			}
		}.start();

	}

	@Override
	public synchronized void stop() {
		
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void write(String data) {
		
		if (s != null && !s.isClosed()) {
			while (!recordQueue.offer(data)) {

				logger.warn("queue overflow");

				recordQueue.poll();			
			}
		}
	}
}
