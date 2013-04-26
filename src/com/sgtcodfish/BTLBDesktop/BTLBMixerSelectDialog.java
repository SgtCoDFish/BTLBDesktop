package com.sgtcodfish.BTLBDesktop;

import javax.swing.DefaultListModel;

/**
 * Provides a dialog box which the user can use to select the Mixer whose audio
 * they wish to transmit.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBMixerSelectDialog extends BTLBDeviceSelectDialog<String> {
	public static final String MixerSelectDialogID = "MixerSelectDialogID";
	public static BTLBAudioDeviceManager audioDeviceManager = BTLBDesktop.audioDeviceManager;
	
	public BTLBMixerSelectDialog(BTLBDesktop parent, String title) {
		super(parent, title);
	}
	
	public BTLBMixerSelectDialog(BTLBDesktop parent, String title, BTLBAudioDeviceManager deviceManager) {
		super(parent, title);
	}

	@Override
	DefaultListModel<String> populateList() {
		DefaultListModel<String> retVal = new DefaultListModel<String>();
		if(BTLBMixerSelectDialog.audioDeviceManager == null) {
			BTLBMixerSelectDialog.audioDeviceManager = new BTLBAudioDeviceManager();
		}
		for(String f : BTLBMixerSelectDialog.audioDeviceManager.formatList.keySet()) {
			retVal.addElement(f);
		}
		return retVal;
	}

	@Override
	String getID() {
		return MixerSelectDialogID;
	}

	@Override
	String getNoDeviceFoundMessage() {
		return "Couldn't find any audio lines from which sound can be recorded.\n" +
				"You may wish to check the installation of your sound card,\n" +
				"or you may need a new sound system for BTLB to work.";
	}

}
