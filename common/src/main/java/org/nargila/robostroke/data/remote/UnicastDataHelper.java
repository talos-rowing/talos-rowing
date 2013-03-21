package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

class UnicastDataHelper {
		
	DatagramSocket socket;

	InetSocketAddress socketAddress;

	/**
	 * create bound server socket or unbound client socket 
	 * @param port port number to use for communications
	 * @param address recipient address to send packets to - if null, then I am the server, a bound socket is created and I only receive packets
	 * @return socket
	 * 
	 * @throws IOException
	 */
	DatagramSocket createSocket(String address, int port) throws IOException {
		
		if (address != null) {
			socket = new DatagramSocket();
			socketAddress = new InetSocketAddress(address, port);
		} else {
			socket = new DatagramSocket(port);
		}
		
		return socket;
	}
	
	void sendData(String s) throws IOException {
		sendData(socket, socketAddress, s);
	}	

	static void sendData(DatagramSocket socket, InetSocketAddress socketAddress, String s) throws IOException {
		
		byte[] buf = s.getBytes("UTF-8");		

		DatagramPacket packet = new DatagramPacket(buf, buf.length, socketAddress);
		
		socket.send(packet);
	}	

	static DatagramPacket receivePacket(DatagramSocket socket, byte[] buf) throws IOException {
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
	
		return packet;
	}
}
