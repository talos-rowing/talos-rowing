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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MulticastDataReceiver extends MulticasData implements DataReceiver {

	private static final Logger logger = LoggerFactory.getLogger(MulticastDataReceiver.class);
	
	private Listener dataListener;
	
	public MulticastDataReceiver(String address, int port, Listener dataListener) {
		super(address, port);
		
		this.dataListener = dataListener;
	}
	
	
	@Override
	public void setListener(Listener dataListener) {
		this.dataListener = dataListener;
	}


	private void onItemReceived(String received) {
		if (dataListener != null) {
			dataListener.onDataReceived(received);
		}
	}
	
	@Override
	protected DatagramSocket createSocket(String address, int port) throws IOException {
		return new DatagramSocket();
	}
	
	@Override
	protected void initConnection(String address, int port, byte[] buf) throws IOException {
		
		InetAddress serverAddr = InetAddress.getByName(address);
		
        byte[] b = "Hello".getBytes();
        
        DatagramPacket packet = new DatagramPacket(b, b.length, serverAddr, port);
        
        logger.info("sending hello packet to server");
        
        socket.send(packet);
   
        // get response
        packet = new DatagramPacket(buf, buf.length);
        
        socket.receive(packet);
 
        // display response
        String received = getData(packet);
        
        logger.info("received from server: '{}'", received);
        
	}
	
	@Override
	protected void processNextItem(DatagramSocket socket, byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		String received = getData(packet);
		onItemReceived(received);
	}
}
