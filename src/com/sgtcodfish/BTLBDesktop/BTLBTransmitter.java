package com.sgtcodfish.BTLBDesktop;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBTransmitter extends Thread {
	// constant for BTLB audio transmission identification
	public static final UUID BTLB_UUID = new UUID("08abb94d2b1644e9ae95e2d18b614496", false);
	
	private BTLBDesktop parent = null;
	private boolean go = true;
	
	public BTLBTransmitter(BTLBDesktop parent) {
		super();
		this.parent = parent;
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
	@Override
	public void run() {
		// First get an audio input stream
		BTLBFormat format = parent.getSelectedAudioDevice();
		
		TargetDataLine line = format.line;
		
		AudioInputStream inputStream = null;
		
		try {
			line.open();
		} catch (LineUnavailableException e1) {
			BTLBDesktop.debugMessage("Selected line unavilable", e1);
		}
		line.start();
		inputStream = new AudioInputStream(line);
		
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
		
		byte buffer[] = new byte[88200*10]; // enough space to store 100 frames
		while(go) {
			try {
				int read = inputStream.read(buffer, 88200, 88200);
				BTLBDesktop.debugMessage("Read " + read + "B");
				
				outputStream.write(buffer, 0, 88200);
				outputStream.flush();
			} catch(IOException ioe) {
				BTLBDesktop.debugMessage("BTLBTransmitter: Exception during transmit: ", ioe);
			}
		}
	}
	
	public void ceaseTransmission() {
		go = false;
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
			while(discoveryListener.isDone() == false) { }
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
}
