package com.sgtcodfish.BTLBDesktop;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;


/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBTransmitter extends Thread {
	// constant for BTLB audio transmission identification
	public static final UUID BTLB_UUID = new UUID("08abb94d2b1644e9ae95e2d18b614496", false);
	
	private BTLBDesktop parent = null;
	private boolean go = true;
	
	private byte buffer[] = null;
	private BTLBAudioReader audioReader = null;
	
	public BTLBTransmitter(BTLBDesktop parent) {
		super();
		this.parent = parent;
		BTLBDesktop.debugMessage("Created BTLBTransmitter instance, thread ID = " + this.getId());
	}
	
	@Override
	public void run() {
		BTLBDesktop.debugMessage("In BTLBTransmitter.run()");
		audioReader = new BTLBAudioReader(parent, this);
		audioReader.start();
		
		// Now get a stream to the android device
		RemoteDevice remote = parent.getSelectedBluetoothDevice();
		
		String url = getRemoteURL(remote);
		StreamConnection streamConnection = null;
		DataOutputStream outputStream = null;
		
		try {
			streamConnection = (StreamConnection)Connector.open(url);
			outputStream = streamConnection.openDataOutputStream();
		} catch (IOException e) {
			BTLBDesktop.debugMessage("BTLBTransmitter: IOException: ", e);
		}
		
		
		while(go) {
			//BTLBDesktop.debugMessage("BTLBTransmitter: In goLoop.");
			try {
				synchronized(buffer) {
					outputStream.write(buffer, 0, buffer.length);
				}
				//BTLBDesktop.debugMessage("BTLBTransmitter: buffer.length = " + buffer.length);
				//outputStream.flush();
				//BTLBDesktop.debugMessage("BTLBTransmitter: Wrote to stream");
			} catch(IOException ioe) {
				BTLBDesktop.debugMessage("BTLBTransmitter: Exception during transmit: ", ioe);
				ceaseTransmission();
			}
		}
	}
	
	public synchronized void setBuffer(byte []nbuffer) {
		buffer = nbuffer;
	}
	
	public void ceaseTransmission() {
		BTLBDesktop.debugMessage("BTLBTransmitter: Transmission ceased.");
		go = false;
		audioReader.cease();
		audioReader = null;
	}
	
	/**
	 * Finds a url for the remote device with a BTLB UUID
	 * @param remoteDevice The remote device to use.
	 * @return A URL identifying the device, or "" if it couldn't be obtained.
	 */
	public String getRemoteURL(RemoteDevice remoteDevice) {
		try {
			int attrIDs[] = new int[] { 0x0100 };
			UUID []uuidSearch = new UUID[] { BTLB_UUID };
			
			BTLBDiscoveryListener discoveryListener = new BTLBDiscoveryListener();
			
			LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, uuidSearch, remoteDevice, discoveryListener);
			while(discoveryListener.discoveredServices.size() <= 0) { }
			int serviceSize = ((BTLBDiscoveryListener) discoveryListener).discoveredServices.size();
			System.out.println("BTLBTransmitter: Discovered " + serviceSize + " BTLB service(s).");
			if(serviceSize > 0) {
				String URL = ((BTLBDiscoveryListener)discoveryListener).discoveredServices.lastElement();
				System.out.println("BTLBTransmitter: Connecting to URL: " + URL);
				return URL;
			}
		} catch(BluetoothStateException bse) {
			System.out.println(bse);
		}
		
		return "";
	}
	
//	@Deprecated
//	public void sendTestData() {
//		String url = bluetoothHandler.discoverServices(selectedDevice);
//		System.out.println("SendTestData: URL: " + url);
//		System.out.println("SendTestData: Sending...");
//		if(!url.equals("")) {
//			try {
//				if(DEBUG) {
//					System.out.println("Connecting to " + url);
//				}
//				StreamConnection conn = (StreamConnection)Connector.open(url);
//				java.io.OutputStream ops = conn.openOutputStream();
//				ops.write(new byte[] {1, 0, 4, 1, 9});
//				//conn.close();
//				if(DEBUG) {
//					System.out.println("SendTestData: Wrote to stream.");
//				}
//			} catch (IOException e) {
//				System.err.println("SendTestData: IO Error in StreamConnectionNotifier");
//				e.printStackTrace();
//			}
//		}
//	}
}
