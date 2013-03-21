package org.nargila.robostroke.data.remote;


public interface DataReceiver extends DataRemote {
	
	public interface Listener {
		public void onDataReceived(String s);
	}
	/**
	 * set listener to receive data asynchronously 
	 */	
	public void setListener(Listener listener);
}
