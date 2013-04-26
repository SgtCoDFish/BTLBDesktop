package com.sgtcodfish.BTLBDesktop;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;

/**
 * Provides a dialog with which the user can select an Android bluetooth device
 * to which they wish to send audio.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBBluetoothDeviceSelectDialog extends BTLBDeviceSelectDialog<RemoteDevice> {
	public static final String BluetoothSelectDialogID = "BluetoothSelectDialogID";
	
	public BTLBDiscoveryListener discoveryListener = null;
	
	public BTLBBluetoothDeviceSelectDialog(BTLBDesktop parent, String title) {
		super(parent, title);
		discoveryListener = new BTLBDiscoveryListener();
	}

	@Override
	DefaultListModel<RemoteDevice> populateList() {
		DefaultListModel<RemoteDevice> retVal = new DefaultListModel<RemoteDevice>();
		
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, discoveryListener);
				} catch (BluetoothStateException e) {
					BTLBDesktop.debugMessage("BTState Exception:", e);
				}
			}
		})).start();
		
		
		// add a little animated thing to keep people interested
		// since bluetooth discovery can take a while
		long lastTime = System.currentTimeMillis();
		
		String anim[] = {">", ">-", ">-|", ">-|-", ">-|-<", "-|-<","|-<", "-<", "<"};
		int animDir = 1; // 1 = right, -1 = left
		int animCurr = 0;
		JLabel animLabel = new JLabel(anim[0]);
		dialog.add(animLabel);
		dialog.validate();
		dialog.repaint();
		while(!discoveryListener.isDone()) {
			long thisTime = System.currentTimeMillis();
			if(thisTime - lastTime > 200) {
				lastTime = thisTime;
				animCurr += animDir;
				if(animCurr >= anim.length || animCurr < 0) {
					animCurr = anim.length - 1;
					animDir = -animDir;
				}
				animLabel.setText(anim[animCurr]);
				dialog.repaint();
			}
		}
		
		// since the discoveryListener must be done if we're here, kill the
		// animation label and list the found devices
		animLabel.setVisible(false);
		dialog.remove(animLabel);
		
		for(RemoteDevice rd : discoveryListener.discoveredDevices.values()) {
			retVal.addElement(rd);
		}
		return retVal;
	}

	@Override
	String getID() {
		return BluetoothSelectDialogID;
	}

	@Override
	String getNoDeviceFoundMessage() {
		return "Please check that you have bluetooth enabled on the device you wish to discover.<br />" +
				"You may wish to use the Operating System bluetooth pairing function before running BTLB again.";
	}
	
}
