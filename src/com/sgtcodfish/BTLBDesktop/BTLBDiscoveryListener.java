package com.sgtcodfish.BTLBDesktop;

import java.io.IOException;
import java.util.HashMap;

import javax.bluetooth.DataElement;
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
	public HashMap<String, ServiceRecord> discoveredServices = new HashMap<String, ServiceRecord>();
	
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
			System.out.println("\nDevice discovered: " + btDevice.getBluetoothAddress() + " : " + friendlyName);
		}
		
		discoveredDevices.put(friendlyName + ": " + btDevice.getBluetoothAddress(), btDevice);
	}
	
	//http://homepages.ius.edu/RWISMAN/C490/html/JavaandBluetooth.htm - reference
	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		System.out.println("Services discovered: ID = " + transID);
		for(int i = 0; i < servRecord.length; i++) {
			String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			if(url == null) continue;
			discoveredServices.put(url, servRecord[i]);
			DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
			if(serviceName != null) {
				System.out.println("Service " + serviceName.getValue() + " found " + url);
			} else {
				System.out.println("Service found " + url);
			}
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		if(respCode == SERVICE_SEARCH_NO_RECORDS) {
			System.out.println("Info: Transaction (" + transID + ") returned SERVICE_SEARCH_NO_RECORDS.");
		}
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
