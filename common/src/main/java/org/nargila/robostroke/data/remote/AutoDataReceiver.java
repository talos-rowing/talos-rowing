package org.nargila.robostroke.data.remote;


public class AutoDataReceiver extends AutoData implements DataReceiver {


	public AutoDataReceiver(String address, int port, Listener dataListener) throws DataRemoteError {
		super(address);
		
		impl = multicast ? 
				new MulticastDataReceiver(address, port, dataListener) :
					new UDPDataReceiver(address, port, dataListener);
	}
	public void setListener(Listener listener) {
		((DataReceiver)impl).setListener(listener);
	}
}
