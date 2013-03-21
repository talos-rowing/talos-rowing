package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPDataSender extends UDPData implements DataSender {

	private final boolean batchMode = Boolean.getBoolean("org.nargila.robostroke.data.FileDataInput.batchMode");

	private static final Logger logger = LoggerFactory.getLogger(UDPDataSender.class);

	private final ArrayBlockingQueue<String> recordQueue = new ArrayBlockingQueue<String>(30);
	
	private final UnicastDataHelper udh = new UnicastDataHelper();
	

	UDPDataSender(String address, int port) throws DataRemoteError {
		super(address, port);
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
	protected DatagramSocket createSocket(String address, int port) throws IOException {
		return udh.createSocket(address, port);
	}

	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
        
	}
	


	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {
		
		String data = getNextItem();

		if (data != null) {
			byte[] b = data.getBytes();
			DatagramPacket packet = new DatagramPacket(b, b.length, udh.socketAddress);
			socket.send(packet);
		} else {
			Thread.yield();
		}	
	}
}
