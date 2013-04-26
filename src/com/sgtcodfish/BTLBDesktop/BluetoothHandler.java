package com.sgtcodfish.BTLBDesktop;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;

import com.intel.bluetooth.BlueCoveImpl;

/**
 * Handles all the bluetooth communications for BTLB.
 * @author Ashley Davis (SgtCoDFish)
 */
@Deprecated
public class BluetoothHandler extends Thread {
	private static final String threadName = "BTLB BluetoothHandler thread";
	public static final UUID BTLB_UUID = new UUID("08abb94d2b1644e9ae95e2d18b614496", false);
	private boolean DEBUG = false;
	
	public LocalDevice localDevice = null;
	
	public DiscoveryListener discoveryListener = null;
	
	enum BTHandlerMode {
		DISCOVER,
		TRANSMIT
	}
	BTHandlerMode mode = BTHandlerMode.DISCOVER;
	
	public BluetoothHandler() {
		super(threadName);
		
		this.DEBUG = BTLBDesktop.DEBUG;
		
		if(DEBUG) {
			System.out.println(BlueCoveImpl.NATIVE_LIB_MS);
		}
		
		discoveryListener = new BTLBDiscoveryListener();
	}
	
	@Override
	public void start() {
		super.start();
		if(DEBUG) {
			System.out.println("Starting thread: " + this.getName());
		}
	}
	
	@Override
	public void run() {
		if(mode == BTHandlerMode.DISCOVER) {
			try {
				localDevice = LocalDevice.getLocalDevice();
				if(DEBUG) {
					System.out.println("BTHandler: Local device address: " + localDevice.getBluetoothAddress());
				}
				
				
				localDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discoveryListener);
				
				long absoluteStart = System.currentTimeMillis();
				
				System.out.print("BTHandler: Starting Bluetooth discovery.");
				while(!((BTLBDiscoveryListener) discoveryListener).isDone()) {
				}
				
				System.out.println("BTHandler: Bluetooth discovery took " + (System.currentTimeMillis() - absoluteStart) + "ms.");
				//BTLBDesktop.mainInstance.addBluetoothDeviceList((BTLBDiscoveryListener) discoveryListener);
				
				mode = BTHandlerMode.TRANSMIT;
			} catch(BluetoothStateException bse) {
				System.out.println("Couldn't start Bluetooth inquiry!");
				bse.printStackTrace();
			}
		} else if(mode == BTHandlerMode.TRANSMIT) {
			
		}
	}
	
	/**
	 * Attempt to discover services on remoteDevice, stores in the discoveryListener
	 * @param remoteDevice The device whose services we're querying
	 * @return A string containing a service url for the first service.
	 */
	public String discoverServices(RemoteDevice remoteDevice) {
		try {
			int attrIDs[] = new int[] { 0x0100 };
			UUID []uuidSearch = new UUID[] { BTLB_UUID };
			if(DEBUG) {
				System.out.println("UUID(0x10419): " + uuidSearch[0].toString());
			}
			
			localDevice.getDiscoveryAgent().searchServices(attrIDs, uuidSearch, remoteDevice, discoveryListener);
			while(((BTLBDiscoveryListener)(discoveryListener)).discoveredServices.size() == 0) {
				
			}
			int serviceSize = ((BTLBDiscoveryListener) discoveryListener).discoveredServices.size();
			System.out.println("servicesize=" + serviceSize);
			if(serviceSize > 0) {
				System.out.println("URL:::= " + ((BTLBDiscoveryListener)discoveryListener).discoveredServices.lastElement());
				return ((BTLBDiscoveryListener)discoveryListener).discoveredServices.lastElement();
			}
		} catch(BluetoothStateException bse) {
			System.out.println(bse);
		}
		
		return "";
	}
}
