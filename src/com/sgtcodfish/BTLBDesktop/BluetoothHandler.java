package com.sgtcodfish.BTLBDesktop;

import javax.bluetooth.*;

import com.intel.bluetooth.*;

/**
 * Handles all the bluetooth communications for BTLB.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BluetoothHandler extends Thread {
	private static final String threadName = "BTLB BluetoothHandler thread";
	private boolean DEBUG;
	
	LocalDevice localDevice = null;
	
	DiscoveryListener discoveryListener = null;
	
	public BluetoothHandler() {
		super(threadName);
		init(BTLBDesktop.DEBUG);
	}
	
	private void init(boolean DEBUG) {
		this.DEBUG = DEBUG;
		
		if(DEBUG) {
			System.out.println(BlueCoveImpl.NATIVE_LIB_MS);
		}
		
		discoveryListener = new BTLBDiscoveryListener();
		
		try {
			localDevice = LocalDevice.getLocalDevice();
			if(DEBUG) {
				System.out.println("Local device address: " + localDevice.getBluetoothAddress());
			}
			
			
			localDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discoveryListener);
			
			long startTime = System.currentTimeMillis();
			long absoluteStart = startTime;
			
			System.out.print("Starting Bluetooth discovery.");
			while(!((BTLBDiscoveryListener) discoveryListener).isDone()) {
				long thisTime = System.currentTimeMillis();
				
				if(thisTime - startTime > 2000) {
					System.out.print(".");
					startTime = thisTime;
				}
			}
			
			System.out.println("Bluetooth discovery took " + (System.currentTimeMillis() - absoluteStart) + "ms.");
			BTLBDesktop.mainInstance.addBluetoothDeviceList((BTLBDiscoveryListener) discoveryListener);
			
		} catch(BluetoothStateException bse) {
			System.out.println("Couldn't start Bluetooth inquiry!");
			bse.printStackTrace();
		}
	}
	
	public void start() {
		if(DEBUG) {
			System.out.println("Starting thread: " + this.getName());
		}
	}
	
	public void run() {
		
	}
	
	/**
	 * Attempt to discover services on remoteDevice
	 * 
	 * BROKEN ATM
	 * @param remoteDevice
	 */
	public void discoverServices(RemoteDevice remoteDevice) {
		try {
			int attrIDs[] = new int[] { 0x0100 };
			UUID []uuidSearch = new UUID[] { new UUID(0x1041) };
			localDevice.getDiscoveryAgent().searchServices(attrIDs, uuidSearch, remoteDevice, discoveryListener);
		} catch(BluetoothStateException bse) {
			System.out.println(bse);
		}
	}
}
