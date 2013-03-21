package org.nargila.robostroke.data.remote;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AutoData implements DataRemote {

	protected DataRemote impl;
	protected boolean multicast;

	public AutoData(String address) throws DataRemoteError {
		
		InetAddress addr;
		
		try {
			addr = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new DataRemoteError(e);
		}
		
		multicast = addr.isMulticastAddress();
		

	}
	


	public void start() throws DataRemoteError {
		impl.start();
	}

	public void stop() {
		impl.stop();
	}

	public void setPort(int port) {
		impl.setPort(port);
	}

	public void setAddress(String address) {
		impl.setAddress(address);
	}	

	public boolean isMulticast() {
		return multicast;
	}
}
