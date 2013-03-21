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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastDataSender extends UDPDataSender {

	private static final Logger logger = LoggerFactory.getLogger(MulticastDataSender.class);
	
	private InetAddress groupAddress;
	private int port;
	private MulticastSocket socket;
	
	MulticastDataSender(String address, int port) throws DataRemoteError {
		super(address, port);
		
		
	}
	


	
	@Override
	protected DatagramSocket createSocket(String address, int port) throws IOException {
		
		this.port = port;
		this.groupAddress = InetAddress.getByName(address);
		
		socket = new MulticastSocket(port);
		
		return socket;
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
		
		logger.info("server join multicast address {}:{}", groupAddress, port);

		SocketAddress sa = new InetSocketAddress(groupAddress, port) ;
		
		if (ifc != null) {
			socket.joinGroup(sa, ifc);
		} else {
			socket.joinGroup(groupAddress);
		}
				
		logger.info("trying to broadcast empty package");

		sendData("");
		
	}




	private void sendData(String s) throws IOException {
		
		byte[] buf = s.getBytes("UTF-8");		

		DatagramPacket packet = new DatagramPacket(buf, buf.length, groupAddress, port);
		
		socket.send(packet);
	}


	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {
		
		String data = getNextItem();

		if (data != null) {
			sendData(data);
		} else {
			Thread.yield();
		}	
	}
}
