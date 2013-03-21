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
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MulticastDataReceiver extends UDPDataReceiver {

	private static final Logger logger = LoggerFactory.getLogger(MulticastDataReceiver.class);
		
	private InetAddress groupAddress;

	private MulticastSocket socket;
	
	MulticastDataReceiver(String address, int port, Listener dataListener) throws DataRemoteError {
		super(address, port, dataListener);
	}
	
	
	
	@Override
	protected DatagramSocket createSocket(String address, int port) throws IOException {
		
		groupAddress = InetAddress.getByName(address);
		
		socket = new MulticastSocket(port);
		
		return socket;
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
		
		logger.info("client join multicast address {}:{}", groupAddress, port);

		SocketAddress sa = new InetSocketAddress(groupAddress, port) ;
		
		if (ifc != null) {
			socket.joinGroup(sa, ifc);
		} else {
			socket.joinGroup(groupAddress);
		}
        
	}
}
