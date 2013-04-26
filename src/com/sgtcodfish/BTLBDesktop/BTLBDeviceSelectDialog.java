package com.sgtcodfish.BTLBDesktop;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A basic dialog box which has a label, list box of devices and a submit
 * button.
 * @author Ashley Davis (SgtCoDFish)
 */
abstract public class BTLBDeviceSelectDialog<DeviceType> {
	protected JDialog dialog = null;
	
	protected JLabel infoLabel = null;
	protected JList<DeviceType> deviceList = null;
	protected JButton submitButton = null;
	
	protected BTLBDesktop parent = null;
	
	public BTLBDeviceSelectDialog(BTLBDesktop parent, String title) {
		this.parent = parent;
		
		dialog = new JDialog(parent.getFrame(), title);
		//dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setLayout(new GridLayout(3, 1));
		dialog.setMinimumSize(new Dimension(320, 320));
		dialog.setMaximumSize(new Dimension(640, 640));
		dialog.setResizable(false);
		dialog.setVisible(true);
		dialog.setLocation(parent.getFrame().getLocation().x + 5, parent.getFrame().getLocation().y + 5);
		
		int minWidth = dialog.getMinimumSize().width;
		
		infoLabel = new JLabel("Please wait while devices are enumerated...");
		infoLabel.setMinimumSize(new Dimension(minWidth, infoLabel.getPreferredSize().height));
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		/* -----   ADD CALLS   ----- */
		// Grouped so it's easier to visualise where they'll go.
		// Only info at first so the user sees a message telling them to wait
		
		dialog.add(infoLabel);
		dialog.pack();
		
		/* ----- ADD CALLS END ----- */
		
		dialog.validate();
		dialog.repaint();
	}
	
	/**
	 * Should create a new DefaultListModel and populate it with appropriate devices.
	 * @return A model to use.
	 */
	abstract DefaultListModel<DeviceType> populateList();
	
	/**
	 * Should be called after the constructor is finished to populate the list and show it and the button
	 */
	public void populate() {
		submitButton = new JButton("Select this Device");
		submitButton.setEnabled(false);
		submitButton.setMaximumSize(submitButton.getPreferredSize());
		submitButton.setHorizontalAlignment(SwingConstants.CENTER);
		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deviceSelected();
				dialog.dispose();
			}
		});
		
		DefaultListModel<DeviceType> listModel = populateList();
		if(listModel.size() == 0) {
			// no devices found, show error and wait for the user to quit.
			infoLabel.setText("No devices found!<br />" + getNoDeviceFoundMessage());
			submitButton.setText("Press to Close this Dialog");
			submitButton.setEnabled(true);
		} else {
			infoLabel.setText("Choose a device from the list below:");
			deviceList = new JList<DeviceType>(listModel);
			int deviceListMinHeight = dialog.getMinimumSize().height - infoLabel.getMinimumSize().height - submitButton.getMinimumSize().height;
			deviceList.setMinimumSize(new Dimension(dialog.getMinimumSize().width, deviceListMinHeight));
			deviceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			// this will enable the button if something is selected and disable if there is nothing selected
			deviceList.addListSelectionListener(new ListSelectionListener() {
				// suppress the unchecked warning because of the strange cast
				@SuppressWarnings("unchecked")
				@Override
				public void valueChanged(ListSelectionEvent e) {
					submitButton.setEnabled(((JList<DeviceType>) e.getSource()).getSelectedValue() != null); 
				}
			});
		}
		
		/* -----   ADD CALLS   ----- */
		// Grouped so it's easier to visualise where they'll go.
		if(deviceList != null) { dialog.add(deviceList); }
		dialog.add(submitButton);
		dialog.pack();
		/* ----- ADD CALLS END ----- */
	}
	
	/**
	 * Should notify the parent object that a device was selected.
	 * @return The object the user chose.
	 */
	protected void deviceSelected() {
		if(deviceList != null) {
			if(deviceList.getSelectedValue() != null) {
				parent.handleDeviceSelectDialog(getID(), deviceList.getSelectedValue());
			}
		}
	}
	
	/**
	 * Returns a string identifying this BTLBDeviceSelectDialog uniquely from others
	 * @return The ID string
	 */
	abstract String getID();
	
	/**
	 * @return A string giving the user more information about what to do since no devices were found.
	 */
	abstract String getNoDeviceFoundMessage();
}
