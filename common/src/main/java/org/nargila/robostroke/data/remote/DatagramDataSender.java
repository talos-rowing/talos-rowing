package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatagramDataSender extends DatagramData implements DataSender {

	private final boolean batchMode = Boolean.getBoolean("org.nargila.robostroke.data.FileDataInput.batchMode");

	private static final Logger logger = LoggerFactory.getLogger(DatagramDataSender.class);

	private final ArrayBlockingQueue<String> recordQueue = new ArrayBlockingQueue<String>(30);
		

	public DatagramDataSender(String address, int port) throws DataRemoteError {
		super(DatagramSocketType.SENDER, address, port);
	}
	

	@Override
	public void write(String o) {		
		
		if (isConnected()) {
			
			if (o == null) {
				throw new IllegalArgumentException("null queue object not allowed");
			}
			
			boolean inserted;
			
			do {
				if (batchMode) {
					try {
						inserted = recordQueue.offer(o, 200, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				} else {
					
					inserted = recordQueue.offer(o);
					
					if (!inserted) {
						recordQueue.poll();			
					}
				}
			} while (!inserted);
		}	
	}
	

	protected String getNextItem() {
		try {
			return recordQueue.poll(10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return null;
		}		
	}
	
	
	@Override
	public synchronized void stop() {
		super.stop();
		recordQueue.clear();
	}

	@Override
	protected void processNextItem(DatagramSocketHelper dsh) throws IOException {
		
		String data = getNextItem();
		
		if (data != null) {
			dsh.sendData(data);
		} else {
			Thread.yield();
		}	
	}
}
