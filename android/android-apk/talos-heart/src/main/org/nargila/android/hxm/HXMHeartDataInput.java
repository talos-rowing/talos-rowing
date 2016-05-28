package org.nargila.android.hxm;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;

public class HXMHeartDataInput implements HXMConstants {
		
	interface BPMListener {
		void onBPMUpdate(Bundle data);
		void onError(String deviceName, HRMError e);
		void onConnect(String deviceName);
	}
	

    private static final UUID SBP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final int STX = 0x02;
	public static final int ETX = 0x03;
	public static final int MSG_ID = 0x26;
	public static final int DLC = 0x37;
	
	private class ConnectionThread extends Thread {
		private final BluetoothSocket socket;
		private boolean shutdown;
		private DataInputStream din;
		private final String deviceName;
		
		public ConnectionThread(BluetoothDevice device) throws IOException {
			super("HXM data thread");
			setDaemon(true);
			
			deviceName = device.getName();
			socket = device.createRfcommSocketToServiceRecord(SBP_UUID);
		}
		
		int readLittleEndian(DataInputStream din, int bytecount) throws IOException {
			int[] vals = new int[bytecount];
			
			for (int i = 0; i < bytecount; ++i) {
				vals[i] = din.readUnsignedByte(); 
			}
			int val = 0;
			
			for (int i = 0; i < bytecount; ++i) {
				val |= vals[i] << (8 * i); 
			}
			
			return val;
		}
		@Override
		public void run() {
			try {
				try {
					try {
						socket.connect();
					} catch (IOException e) {
						throw new HRMError(ErrorCode.DeviceConnectionError, String.format("failed to connect to heart monitor %s", deviceName), e);
					}
					
					din =  new DataInputStream(socket.getInputStream());

					bpmListener.onConnect(deviceName);
					
					do {
						if (din.readUnsignedByte() != ETX) continue;
						if (din.readUnsignedByte() != STX) continue;
						if (din.readUnsignedByte() != MSG_ID) continue;
						if (din.readUnsignedByte() != DLC) continue;

						int firmwareId = readLittleEndian(din, 2); 
						int firmwareVersion = readLittleEndian(din, 2); 
						int hardwareId = readLittleEndian(din, 2); 
						int hardwareVersion = readLittleEndian(din, 2); 
						int batteryCharge = din.readUnsignedByte();
						int heartRate = din.readUnsignedByte();
						int hbn = din.readUnsignedByte();
						int timestamp = readLittleEndian(din, 2);

						if (D) {
							Log.d(TAG, String.format("firmwareId: 0x%x, firmwareVersion: 0x%x, battery: %d, heart rate: %d, #%d, timestamp: %d", firmwareId, firmwareVersion, batteryCharge, heartRate, hbn, timestamp));
						}
						
						Bundle data = new Bundle();
						
						data.putInt("bpm", heartRate);
						data.putInt("battery", batteryCharge);
						data.putInt("beatNumber", hbn);
						data.putString("deviceName", deviceName);
						
						
						bpmListener.onBPMUpdate(data);
						
					} while (true);
				} finally {
					socket.close();
				}
			} catch (Throwable e) {
				if (!shutdown) {
					HRMError err;
					if (e instanceof HRMError) {
						err = (HRMError) e;
					} else {
						 err = new HRMError(ErrorCode.DeviceDataError, "error in HXM data input thread", e);
					}
					
					Log.e(TAG, err.getMessage(), e);
					bpmListener.onError(deviceName, err);
				}
			}
		}
		
		public void shutdown() {
			this.shutdown = true;
			if (din != null) {
				try {
					din.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
			
			interrupt();
		}
	}

	private ConnectionThread connectionThread;
	
	private final BPMListener bpmListener;

	
	public HXMHeartDataInput(BPMListener bpmListener) {
		this.bpmListener = bpmListener;
	}
	
	public synchronized void start() throws HRMError {
		if (D) Log.d(TAG, "getting BluetoothAdapter");
       
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (null == adapter) {
        	if (D) Log.d(TAG, "Bluetooth service is not available");
        	throw new HRMError(ErrorCode.AdaptorNotAvailable, "Bluetooth service is not available");
        }

        if (D) Log.d(TAG, "got BluetoothAdapter");
		
		if (!adapter.isEnabled()) {
        	throw new HRMError(ErrorCode.AdaptorDisabled, "Bluetooth service disabled");
		}

		Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

		if (D) Log.d(TAG, String.format("got %d paired Bluetooth devices", pairedDevices.size()));
		
		boolean hasPairedDevice = false;
        for (BluetoothDevice device: pairedDevices) {

        	if (device.getName().startsWith("HXM")) {

        		hasPairedDevice = true;

        		if (D) Log.d(TAG, String.format("found HXM devices %s", device.getName()));
        		
        		try {
        			if (D) Log.d(TAG, "trying to connect to " + device.getName());
            		 
            		connectionThread = new ConnectionThread(device);
				} catch (IOException e) {
					continue; // paired device turned off or out of range, etc. 
				}

				if (D) Log.d(TAG, "starting Bluetooth connection thread");
        		 
        		connectionThread.start();

        		return;
        	}
        }
        
        if (hasPairedDevice) {
        	throw new HRMError(ErrorCode.DeviceConnectionError,"could not connect to Zephyr HXM device");
        } else {
        	throw new HRMError(ErrorCode.DeviceNotAvailable,"could not find any Zephyr HXM device");
        }
    }
    
    
    public synchronized void stop() {
    	if (null != connectionThread) {
    		Log.d(TAG, "HXM data input thread: shuttdown");
    		connectionThread.shutdown();
    		try {
        		Log.d(TAG, "HXM data input thread: joining");
				connectionThread.join();
        		Log.d(TAG, "HXM data input thread: joined");
			} catch (InterruptedException e) {
			}
    		connectionThread = null;
    	}
    }
}
