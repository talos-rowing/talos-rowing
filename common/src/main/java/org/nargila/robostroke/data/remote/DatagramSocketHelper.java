package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatagramSocketHelper {
		
	private static final Logger logger = LoggerFactory.getLogger(DatagramSocketHelper.class);
	
	private static final int MAX_PACKET_SIZE = 1400;

	final byte[] buf = new byte[MAX_PACKET_SIZE];

	DatagramSocket socket;

	InetSocketAddress socketAddress;
	
	/**
	 * create bound server socket or unbound client socket, or unbound multicast socket - depending on value of address
	 * @param port port number to use for communications
	 * @param address recipient address to send packets to - if null, then this is the receiver - a bound server socket is created. 
	 * If address is a multicast address, MulticastSocket is created and later joined to 
	 * @return socket
	 * 
	 * @throws IOException
	 */
	DatagramSocket createSocket(DatagramSocketType type, String address, int port) throws IOException {
		socketAddress = new InetSocketAddress(address, port);

		if (socketAddress.getAddress().isMulticastAddress()) {
			MulticastSocket s; 
			socket = s = new MulticastSocket(port);

			try {
				joinMulticastGroup(s);
			} catch (IOException e) {
				socket.close();
				socket = null;
				throw e;
			}

		} else {
			switch (type) {
			case RECEIVER:
				socket = new DatagramSocket(port);
				break;
			case SENDER:
				socket = new DatagramSocket();
				break;
			default:
				throw new AssertionError("HDIGH!");
			}
		}
		
		return socket;
	}

	private void joinMulticastGroup(MulticastSocket s) throws IOException {
		NetworkInterface ifc = null;

		{
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();


			while (enumeration.hasMoreElements()) {

				NetworkInterface i = enumeration.nextElement();

				logger.debug("interface {}:  lo={}, ppp={}, up={}, virt={}, multicast={} - {}", new Object[]{i.getDisplayName(), i.isLoopback(), i.isPointToPoint(), i.isUp(), i.isVirtual(), i.supportsMulticast(), i});

				if (i.isUp() && !i.isLoopback() && !i.isPointToPoint() && i.supportsMulticast()) {
					logger.info("using interface {}", i);
					ifc = i;
					break;
				}
			}
		} 
		 
		logger.info("client join multicast address {}", socketAddress);

		
		if (ifc != null) {
			s.joinGroup(socketAddress, ifc);
		} else {
			s.joinGroup(socketAddress.getAddress());
		}
	}
	
	void sendData(String s) throws IOException {
		sendData(socket, socketAddress, s);
	}	

	String receiveData()  throws IOException {

		DatagramPacket packet = receivePacket(socket, buf);
		
		return new String(packet.getData(), packet.getOffset(), packet.getLength());
	}
	
	static void sendData(DatagramSocket socket, InetSocketAddress socketAddress, String s) throws IOException {
		
		logger.debug("sending {} to {}", s, socketAddress);
		
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
