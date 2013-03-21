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

package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.nargila.robostroke.data.remote.DataRemote.DataRemoteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UDPData {
	
	private static final Logger logger = LoggerFactory.getLogger(UDPData.class);

	private static final int MAX_PACKET_SIZE = 1400;

	protected DatagramSocket socket;
	
	private String address;
	
	private int port;
		
	private Thread connectionThread;
	
	private boolean connected;
	
	private boolean stopRequested;

	protected NetworkInterface ifc;
		
	protected UDPData( String address, int port) throws DataRemoteError {		
		this.address = address;
		this.port = port;
		

		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

			NetworkInterface i = null;

			while (enumeration.hasMoreElements()) {

				i = enumeration.nextElement();

				logger.info("interface {}:  lo={}, ppp={}, up={}, virt={}, multicast={} - {}", new Object[]{i.getDisplayName(), i.isLoopback(), i.isPointToPoint(), i.isUp(), i.isVirtual(), i.supportsMulticast(), i});

				if (i.isUp() && !i.isLoopback() && !i.isPointToPoint() && i.supportsMulticast()) {
					ifc = i;
					break;
				}
			}
		} catch (SocketException e) {
			throw new DataRemoteError(e);
		}
	}

	public synchronized void stop() {
		
		connected = false;
		
		stopRequested = true;
		
		if (socket != null) {

			socket.close();
			
			connectionThread.interrupt();
			
			try {
				connectionThread.join();
			} catch (InterruptedException e) {
			}
						
			socket = null;			
		}
		
		stopRequested = false;
	}

	public boolean isRunning() {
		return socket != null;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public synchronized void start() {
		

		final String name = getClass().getSimpleName();
		final Object startSync = this;
		
		connectionThread = new Thread(name) {
			public void run() {					
				
				final byte[] packetBuffer = new byte[MAX_PACKET_SIZE];
				
				while (!stopRequested) {
					try {
						
						if (socket == null) {
							synchronized (startSync) {
								try {

									socket = createSocket(address, port);
									
								} finally {
									startSync.notifyAll();
								}
							}
							
							
							try {
								initConnection(address, port, packetBuffer);
								connected = true;
							} finally {
								if (!connected) {
									socket.close();
									socket = null;
								}
							}
						}
						
						
						processNextItem(socket, packetBuffer);
						
					} catch (Exception e) {
						if (!stopRequested) {
							logger.warn("remote data reading error - receiver loop continues", e);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {
							}
						}
					}
				}
			}
		};
		
		synchronized (startSync) {
			
			connectionThread.start();
			
			try {
				startSync.wait();
			} catch (InterruptedException e) {

			}
		}
	}

	protected abstract DatagramSocket createSocket(String address, int port) throws IOException;
	
	protected abstract void initConnection(String address, int port, byte[] buf) throws IOException;

	protected abstract void processNextItem(DatagramSocket socket, byte[] buf) throws IOException;	

	
	private synchronized void restart() {
		
		boolean alreadyStarted = socket != null;
		
		if (alreadyStarted) {
			stop();
			start();
		}
	}

	public void setPort(int port) {		
		this.port = port;		
		restart();		
	}

	public void setAddress(String address) {
		this.address = address;
		restart();
	}

	protected String getData(DatagramPacket packet) throws UnsupportedEncodingException {
		return new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
	}
}
