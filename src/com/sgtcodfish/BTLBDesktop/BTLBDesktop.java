package com.sgtcodfish.BTLBDesktop;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.bluetooth.RemoteDevice;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Desktop portion of turning an android device into a bluetooth headset.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBDesktop extends JFrame {
	private static final long serialVersionUID = 10419L;
	public static final boolean DEBUG = true;
	
	public static BTLBDesktop mainInstance = null; // singleton class
	
	private JLabel infoLabel = null;
	private AudioRecorder audioRecorder = null;
	private BluetoothHandler bluetoothHandler = null;
	
	private RemoteDevice selectedDevice = null;
	
	/**
	 * @param args Not used
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(UnsupportedLookAndFeelException ulafe) {
			System.out.println("Unsupported Look & Feel: System");
		} catch(Exception e) {
			System.out.println(e);
		}
		
		if(DEBUG) { 
			printMixers();
			printMixerDetails(AudioSystem.getMixer(getPrimaryCaptureDriver()));
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new BTLBDesktop("BTLB :: BlueToothLoopBack");
            }
        });
	}
	
	/**
	 * Creates a default Swing window.
	 * @param swingName The title of the window.
	 */
	public BTLBDesktop(String swingName) {
		super(swingName);
		
		if(BTLBDesktop.mainInstance != null) {
			System.err.println("Trying to create 2 BTLBDesktops - exiting.");
			System.exit(1);
		} else {
			BTLBDesktop.mainInstance = this;
		}
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		this.setLayout(new GridLayout(3, 1));
		this.setMinimumSize(new Dimension(320, 320));
		infoLabel = new JLabel("Please wait while Bluetooth devices are enumerated...");
		this.add(infoLabel);
		this.pack();
		this.paintAll(getGraphics());
		audioRecorder = new AudioRecorder(); 
		bluetoothHandler = new BluetoothHandler();
		
		audioRecorder.start();
		bluetoothHandler.start();
	}
	
	/**
	 * Takes a finished BTLBDiscoveryListener class with a valid discoveredDevices vector
	 * and creates a window allowing the user to choose a device to connect to.
	 * @param discoveryListener
	 */
	public void addBluetoothDeviceList(BTLBDiscoveryListener discoveryListener) {
		final HashMap<String, RemoteDevice> deviceListCopy = new HashMap<String, RemoteDevice>(discoveryListener.discoveredDevices);
//		for(Component c : this.getComponents()) {
//			if (c instanceof JLabel) {
//				JLabel temp = (JLabel) c;
//				temp.setText("Select a device from the list below to communicate with:");
//				temp.paint(getGraphics());
//				this.
//			}
//		}
		// add a descriptive label, replacing our current label.
		infoLabel.setText("Select a device from the list below to communicate with:");
		
		// populate a list with the available devices
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String rd : deviceListCopy.keySet()) {
			model.addElement(rd);
		}
		
		final JList<String> deviceList = new JList<String>(model);
		
		this.add(deviceList);
		
		// create a confirmation button
		JButton confButton = new JButton("Use the selected device as remote headset.");
		confButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(deviceList.getSelectedValue() != null) {
					selectedDevice = deviceListCopy.get(deviceList.getSelectedValue());
					BTLBDesktop.mainInstance.startStream();
				}
			}
		});
		this.add(confButton);
		this.revalidate();
		this.pack();
	}
	
	/**
	 * Called internally to signify that a bluetooth device has been chosen
	 * and that the music streaming phase should begin. Causes a redraw.
	 */
	private void startStream() {
		if(selectedDevice == null) {
			System.err.println("Call to startStream() with no selected device, exiting.");
			System.exit(1);
		}
		
		System.out.println("Startstream woo!\n" + selectedDevice);
	}

	/**
	 * Prints the details about a specific mixer
	 * @param mixer The mixer whose details are needed
	 */
	public static void printMixerDetails(Mixer mixer) {
		System.out.println(mixer.getLineInfo().toString());
	}
	
	/**
	 * Finds a "primary sound capture driver". May only work in Windows Vista+
	 * @return The Mixer.Info relating to the primary capture driver, or null if no such driver was found.
	 */
	public static Mixer.Info getPrimaryCaptureDriver() {
		final String searchName = "Primary Sound Capture Driver";
		Mixer.Info infos[] = AudioSystem.getMixerInfo();
		
		for(Mixer.Info i : infos) {
			if(i.getName().equals(searchName)) {
				if(DEBUG) {
					System.out.println("Found primary sound capture driver:" + i);
				}
				
				return i;
			}
		}
		
		if(DEBUG) {
			System.out.println("Failed to find sound capture driver, name=\"" + searchName + "\".");
		}
		return null;
	}
	
	/**
	 * Prints a formatted list of all detected mixers and info about each.
	 */
	public static void printMixers() {
		Mixer.Info infos[] = AudioSystem.getMixerInfo();
		int count = 0;
		
		for(Mixer.Info i : infos) {
			count++;
			String title = "Device #" + count + "\n";
			
			int titleLength = title.length() - 1;
			for(int dasher = 0; dasher < titleLength; dasher++) {
				title += "-";
			}
			
			System.out.println(title);
			System.out.println(i + "\n");
		}
	}
}
