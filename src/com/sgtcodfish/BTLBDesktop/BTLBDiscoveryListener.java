package com.sgtcodfish.BTLBDesktop;

import java.io.IOException;
import java.util.HashMap;

import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBDiscoveryListener implements DiscoveryListener {
	private boolean DEBUG;
	//public Vector<RemoteDevice> discoveredDevices = new Vector<RemoteDevice>();
	private boolean done = false;
	public HashMap<String, RemoteDevice> discoveredDevices = new HashMap<String, RemoteDevice>();
	
	public BTLBDiscoveryListener() {
		this.DEBUG = BTLBDesktop.DEBUG;
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		String friendlyName = null;
		
		try {
			friendlyName = btDevice.getFriendlyName(false);
		} catch(IOException ioe) {
			System.err.println("Couldn't get friendly name for: " + btDevice.getBluetoothAddress() + "\n" + ioe);
		}
		
		if(DEBUG) {
			System.out.println("\nDevice discovered: " + btDevice.getBluetoothAddress());
			System.out.println("Friendly name: " + friendlyName);
		}
		
		discoveredDevices.put(friendlyName + ": " + btDevice.getBluetoothAddress(), btDevice);
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for(ServiceRecord sr : servRecord) {
			if(DEBUG) {
				System.out.println("Service: " + sr.getConnectionURL(0, false));
			}
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
	}

	@Override
	public void inquiryCompleted(int discType) {
		if(DEBUG) {
			System.out.println("Inquiry complete!");
		}
		
		done = true;
	}
	
	public boolean isDone() {
		return done;
	}

}
