package org.nargila.robostroke.data.remote;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MulticastDataHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(MulticastDataHelper.class);
	
	MulticastSocket socket;

	InetSocketAddress socketAddress;

	MulticastSocket createSocket(String address, int port) throws IOException {
				
		socketAddress = new InetSocketAddress(address, port) ;
		
		socket = new MulticastSocket(port);
		
		return socket;
	}
	
	void initConnection() throws IOException {				

		NetworkInterface ifc = null;

		{
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();


			while (enumeration.hasMoreElements()) {

				NetworkInterface i = enumeration.nextElement();

				logger.debug("interface {}:  lo={}, ppp={}, up={}, virt={}, multicast={} - {}", new Object[]{i.getDisplayName(), i.isLoopback(), i.isPointToPoint(), i.isUp(), i.isVirtual(), i.supportsMulticast(), i});

				if (i.isUp() && !i.isLoopback() && !i.isPointToPoint() && i.supportsMulticast()) {
					if (ifc == null) {
						logger.info("using interface {}", i);
						ifc = i;
					}
				}
			}
		} 
		 
		logger.info("client join multicast address {}", socketAddress);

		
		if (ifc != null) {
			socket.joinGroup(socketAddress, ifc);
		} else {
			socket.joinGroup(socketAddress.getAddress());
		}
        
	}
	
	void sendData(String s) throws IOException {
		UnicastDataHelper.sendData(socket, socketAddress, s);
	}
}
