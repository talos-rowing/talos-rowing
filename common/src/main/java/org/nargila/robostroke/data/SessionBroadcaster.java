package org.nargila.robostroke.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nargila.robostroke.RoboStroke;
import org.nargila.robostroke.SensorBinder;
import org.nargila.robostroke.data.DataRecord.Type;
import org.nargila.robostroke.param.Parameter;
import org.nargila.robostroke.param.ParameterBusEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SessionBroadcaster extends SensorBinder {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionBroadcaster.class);
	
	private ServerSocket socket;
	
	private Socket s;
	
	private Writer recordOut;
		
	private ArrayBlockingQueue<DataRecord> recordQueue = new ArrayBlockingQueue<DataRecord>(100);	
	
	private final Runnable acceptJob  = new Runnable() {
				
		public void run() {
			
			while (!socket.isClosed()) {
				try {

					s = socket.accept();
					
					if (s.isConnected()) {
						
						recordQueue.clear();
						
						recordOut = new OutputStreamWriter(s.getOutputStream());
						
						writeRecord(new DataRecord(Type.LOGFILE_VERSION, -1,
								SessionRecorderConstants.LOGFILE_VERSION));
						
						for (Parameter<?> param: roboStroke.getParameters().getParamMap().values()) {
							
							if (!s.isConnected()) {
								break;
							}
							
							writeRecord(DataRecord.create(DataRecord.Type.SESSION_PARAMETER, -1, 
									new ParameterBusEventData(param.getId() + "|" + param.convertToString())));
						}

						while (s.isConnected()) {

							DataRecord record = recordQueue.poll(10,
									TimeUnit.MILLISECONDS);

							if (record != null) {
								writeRecord(record);
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

		public void writeRecord(DataRecord record) throws InterruptedException, IOException {			
			recordOut.write(record.toString() + SessionRecorderConstants.END_OF_RECORD + "\n");
			recordOut.flush();
		}
	};
	

	private int port = SessionRecorderConstants.BROADCAST_PORT;

	private boolean broadcast;
	
	public SessionBroadcaster(RoboStroke roboStroke) {
		super(roboStroke);
	}

	public void setBoradcast(boolean broadcast) {
				
		if (this.broadcast != broadcast) {
			if (broadcast) {
				connect();
			} else {
				disconnect();
			}
			this.broadcast = broadcast;
		}
	}
	
	@Override
	protected synchronized void connect() {
		
		super.connect();					
		
		try {
			socket = new ServerSocket(port);
			new Thread(acceptJob, "SessionBroadcaster accept").start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected synchronized void disconnect() {
		
		super.disconnect();
		
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
	
	@Override
	protected void onSensorData(DataRecord record) {		
		queueRecord(record);		
	}


	@Override
	public void onBusEvent(DataRecord record) {
		
		if (record.type.isExportableEvent) {
			queueRecord(record);
		}
	}

	public void queueRecord(DataRecord record) {
		
		if (s != null && !s.isClosed()) {
			while (!recordQueue.offer(record)) {

				logger.warn("queue overflow");

				recordQueue.poll();			
			}
		}
	}

	public void setPort(int port) {
		this.port = port;		
	}
}
