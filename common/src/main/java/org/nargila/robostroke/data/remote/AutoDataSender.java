package org.nargila.robostroke.data.remote;

public class AutoDataSender extends AutoData implements DataSender {


	public AutoDataSender(String address, int port) throws DataRemoteError {
		super(address);

		impl = multicast ? 
				new MulticastDataSender(address, port) :
					new UDPDataSender(address, port);
	}
	
	public void write(String data) {
		((DataSender)impl).write(data);
	}

}
