package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UDPDataSender extends UDPData implements DataSender {

	private final boolean batchMode = Boolean.getBoolean("org.nargila.robostroke.data.FileDataInput.batchMode");

	private static final Logger logger = LoggerFactory.getLogger(UDPDataSender.class);

	private final ArrayBlockingQueue<String> recordQueue = new ArrayBlockingQueue<String>(100);

	private InetAddress sendAddress;

	private int sendPort; 
	
	public UDPDataSender(String address, int port) {
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
						logger.warn("queue overflow");
						
						recordQueue.poll();			
					}
				}
			} while (!inserted);
		}	
	}
	

	private String getNextItem() {
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
		return new DatagramSocket(port);
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		logger.info("accepting connection on port {}", port);
		
		socket.receive(packet);
		
		InetAddress sendAddress = packet.getAddress();
		
		int sendPort = packet.getPort();

		String fromClient = getData(packet);
		
		logger.info("accept connection from {}:{} (says: {})", new Object[]{sendAddress, sendPort, fromClient});
		
		buf = "Hello back".getBytes();
		
		logger.info("sending back hello to {}:{}", new Object[]{sendAddress, sendPort});

		packet = new DatagramPacket(buf, buf.length, sendAddress, sendPort);
		
		socket.send(packet);
		
		this.sendAddress = sendAddress;
		this.sendPort = sendPort;
		
		
	}


	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {
		
		String data = getNextItem();

		if (data != null) {
			byte[] b = data.getBytes();
			DatagramPacket packet = new DatagramPacket(b, b.length, sendAddress, sendPort);
			socket.send(packet);
		} else {
			Thread.yield();
		}	
	}
}
