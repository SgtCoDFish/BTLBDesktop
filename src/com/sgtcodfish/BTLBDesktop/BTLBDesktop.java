package com.sgtcodfish.BTLBDesktop;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Desktop portion of turning an android device into a bluetooth headset.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBDesktop {
	private JFrame frame = null;
	public static final boolean DEBUG = true;
	
	public static final String defaultTitle = "BTLB :: BlueToothLoopBack";
	
	
	public static BTLBDesktop mainInstance = null; // singleton class
	private JLabel infoLabel = null;
	//infoLabel is used before device select to display a progress message,
	//during device select for instructions
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
		}
		
		//System.setProperty("bluecove.jsr82.psm_minimum_off", "true");
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new BTLBDesktop(BTLBDesktop.defaultTitle);
            }
        });
	}
	
	/**
	 * Creates a default Swing window.
	 * @param swingName The title of the window.
	 */
	public BTLBDesktop(String swingName) {
		if(BTLBDesktop.mainInstance != null) {
			System.err.println("Trying to create 2 BTLBDesktops - exiting.");
			System.exit(1);
		} else {
			BTLBDesktop.mainInstance = this;
		}
		
		setupFrame(swingName);
		
		infoLabel = new JLabel("Please wait while Bluetooth devices are enumerated...");
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		frame.add(infoLabel);
		frame.pack();
		frame.paintAll(frame.getGraphics());
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
		
		// add a descriptive label, replacing our current label.
		infoLabel.setText("Select a device from the list below to communicate with:");
		
		// populate a list with the available devices
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String rd : deviceListCopy.keySet()) {
			model.addElement(rd);
		}
		
		final JList<String> deviceList = new JList<String>(model);
		
		frame.add(deviceList);
		
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
		frame.add(confButton);
		//frame.revalidate();
		frame.pack();
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
		
		setupFrame(BTLBDesktop.defaultTitle);
		JButton discoverButton = new JButton("Search for services!");
		discoverButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bluetoothHandler.discoverServices(selectedDevice);
			}
		});
		
		
		JButton testButton = new JButton("Send test data!");
		testButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendTestData();
			}
		});
		
		
		frame.add(discoverButton);
		frame.add(testButton);
		frame.pack();
		
	}
	
	public void sendTestData() {
		System.out.println("In sendTestData()");
		String url = bluetoothHandler.discoverServices(selectedDevice);
		System.out.println("aaaa" + url);
		System.out.println("commencing sending");
		if(!url.equals("")) {
			try {
				if(DEBUG) {
					System.out.println("Connecting to " + url);
				}
				StreamConnection conn = (StreamConnection)Connector.open(url);
				java.io.OutputStream ops = conn.openOutputStream();
				ops.write(new byte[] {1, 0, 4, 1, 9});
				//conn.close();
				if(DEBUG) {
					System.out.println("Wrote to stream.");
				}
			} catch (IOException e) {
				System.err.println("IO Error in StreamConnectionNotifier");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Sets up a default frame for use.
	 */
	private void setupFrame(String title) {
		if(frame != null) {
			frame.removeAll();
			frame.dispose();
			frame = null;
		}
		
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setLayout(new GridLayout(3, 1));
		frame.setMinimumSize(new Dimension(320, 320));
	}

	/**
	 * Prints the details about a specific mixer
	 * @param mixer The mixer whose details are needed
	 */
	public static void printMixerDetails(Mixer mixer) {
		System.out.println(mixer.getLineInfo().toString());
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
			
			Line.Info lineInfos[] = AudioSystem.getMixer(i).getTargetLineInfo();
			System.out.println(lineInfos.length + " lines detected:");
			for(Line.Info lineIn : lineInfos) {
				System.out.println("    " + lineIn);
				if(!i.getName().contains("Port")) {
					DataLine.Info dlInfo = (DataLine.Info)lineIn;
					AudioFormat afs[] = dlInfo.getFormats();
					for (AudioFormat af : afs) {
						System.out.println("        " + af);
					}
				}
			}
			
			System.out.println();
		}
	}
}
