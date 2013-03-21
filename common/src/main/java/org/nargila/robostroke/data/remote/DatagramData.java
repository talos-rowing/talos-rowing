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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.nargila.robostroke.data.remote.DataRemote.DataRemoteError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DatagramData {
	
	private static final Logger logger = LoggerFactory.getLogger(DatagramData.class);
	
	private String address;
	
	private int port;
		
	private Thread connectionThread;
	
	private boolean stopRequested;
		
	final DatagramSocketHelper dsh = new DatagramSocketHelper();
	
	private DatagramSocket socket;
	
	private final DatagramSocketType type;
	
	protected DatagramData(DatagramSocketType type, String address, int port) throws DataRemoteError {
		this.type = type;
		this.address = address;
		this.port = port;
	}

	public synchronized void stop() {
				
		stopRequested = true;
		
		if (socket != null) {
			socket.close();
		}
		
		if (connectionThread != null) {
			connectionThread.interrupt();
			
			try {
				connectionThread.join();
			} catch (InterruptedException e) {
			}
						
			socket = null;			
		}
		
		stopRequested = false;
	}

	public boolean isConnected() {
		return !stopRequested && socket != null;
	}
	

	public synchronized void start() {
		

		final String name = getClass().getSimpleName();
		final Object startSync = this;
		
		connectionThread = new Thread(name) {
			public void run() {					
								
				while (!stopRequested) {
					try {
						
						if (socket == null) {
							synchronized (startSync) {
								try {

									socket = dsh.createSocket(type, address, port);
									
								} finally {
									startSync.notifyAll();
								}
							}							
						}
						
						
						processNextItem(dsh);
						
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
	
	protected abstract void processNextItem(DatagramSocketHelper dsh) throws IOException;	
	
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
	
	public boolean isMulticast() throws DataRemoteError {
		try {
			return InetAddress.getByName(address).isMulticastAddress();
		} catch (UnknownHostException e) {
			throw new DataRemoteError(e);
		}
	}
}
