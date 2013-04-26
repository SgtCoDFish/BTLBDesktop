package com.sgtcodfish.BTLBDesktop;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

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
	public boolean done = false;
	public boolean serviceDone = false;
	public HashMap<String, RemoteDevice> discoveredDevices = new HashMap<String, RemoteDevice>();
	public Vector<String> discoveredServices = new Vector<String>();
	
	public BTLBDiscoveryListener() {
		this.DEBUG = BTLBDesktop.DEBUG;
	}
	
	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		String friendlyName = null;
		
		try {
			friendlyName = btDevice.getFriendlyName(false);
		} catch(IOException ioe) {
			System.err.println("BTLBDL: Couldn't get friendly name for: " + btDevice.getBluetoothAddress() + "\n" + ioe);
		}
		
		if(DEBUG) {
			System.out.println("BTLBDL: Device discovered: " + btDevice.getBluetoothAddress() + " : " + friendlyName);
		}
		
		discoveredDevices.put(friendlyName + ": " + btDevice.getBluetoothAddress(), btDevice);
	}
	
	@Override
	public void inquiryCompleted(int discType) {
		if(DEBUG) {
			System.out.println("BTLBDL: Inquiry complete!");
		}
		
		done = true;
	}
	
	//http://homepages.ius.edu/RWISMAN/C490/html/JavaandBluetooth.htm - reference
	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		for(int i = 0; i < servRecord.length; i++) {
			String url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			if(url == null) continue;
			discoveredServices.add(url);
			DataElement serviceName = servRecord[i].getAttributeValue(0x0100);
			if(serviceName != null) {
				System.out.println("BTLBDL: Service " + serviceName.getValue() + " found " + url);
			} else {
				System.out.println("BTLBDL: Service found " + url);
			}
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		done = true;
		if(respCode == SERVICE_SEARCH_NO_RECORDS) {
			System.out.println("BTLBDL: Info: Transaction (" + transID + ") returned SERVICE_SEARCH_NO_RECORDS.");
		} else {
			System.out.println("BTLBDL: Info: Transaction (" + transID + ") returned " + discoveredServices.size() + " services.");
		}
	}


	
	public boolean isDone() {
		return done;
	}

}
