package com.sgtcodfish.BTLBDesktop;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 * Desktop portion of turning an android device into a bluetooth headset.
 * 
 * This class is the base UI window. When this window is closed, the app should
 * close.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBDesktop {
	public static final boolean DEBUG = true;
	public static final String defaultTitle = "BTLB :: BlueToothLoopBack";
	public static final String defaultPropertiesFileName = "./BTLB.properties";	
	public static BTLBDesktop mainInstance = null; // singleton class
	public static final BTLBAudioDeviceManager audioDeviceManager = new BTLBAudioDeviceManager();
	
	private BTLBFormat selectedAudioDevice = null;
	private RemoteDevice selectedBluetoothDevice = null;
	
	private Properties properties = null;
	
	BTLBTransmitter transmitter = null;
	boolean connected = false;
	
	// Swing Components
	private JFrame frame = null; // the root frame
	
	private JMenuBar menuBar = null; // the menu bar of the root frame
	private JMenu settingsMenu = null;
	private JMenu helpMenu = null;
	private JCheckBoxMenuItem saveCheckboxMenuItem = null; // member variable for global access
	
	private JLabel btDeviceLabel = null;
	private final String btDeviceLabelText = "Selected Bluetooth Device:\n";
	
	private JLabel audioDeviceLabel = null;
	private final String audioDeviceLabelText = "Selected Audio Device:\n";
	
	private JButton connectButton = null;
	private final String buttonTextConnect = "Connect!";
	private final String buttonTextDisconnect = "Disconnect!";

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
		
		JDialog.setDefaultLookAndFeelDecorated(true);
		
		SwingUtilities.invokeLater(new Runnable() {
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
		// ensure we run this class as a singleton
		if(BTLBDesktop.mainInstance != null) {
			System.err.println("Trying to create 2 BTLBDesktops - exiting.");
			System.exit(1);
		} else {
			BTLBDesktop.mainInstance = this;
		}
		
		setupFrame(BTLBDesktop.defaultTitle);
		setupMenu();
		
		//load the properties file if one exists, create one if it doesn't
		openPropertiesFile();
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
		frame.setLayout(new GridLayout(4, 1));
		frame.setMinimumSize(new Dimension(320, 320));
		
		btDeviceLabel = new JLabel(btDeviceLabelText);
		btDeviceLabel.setHorizontalAlignment(JLabel.CENTER);
		audioDeviceLabel = new JLabel(audioDeviceLabelText);
		audioDeviceLabel.setHorizontalAlignment(JLabel.CENTER);
		
		connectButton = new JButton("Connect!");
		connectButton.setEnabled(false);
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doConnectButton();
			}
		});
		
		frame.add(btDeviceLabel);
		frame.add(audioDeviceLabel);
		frame.add(connectButton);
		frame.pack();
	}
	
	/**
	 * Called when the user presses the "Connect"/"Disconnect" button
	 */
	protected void doConnectButton() {
		if(!connected) {
			if(selectedBluetoothDevice != null && selectedAudioDevice != null) {
				transmitter = new BTLBTransmitter(this);
				
				transmitter.start();
				connected = true;
			}
			
			connectButton.setText(buttonTextDisconnect);
		} else {
			if(transmitter != null) {
				transmitter.ceaseTransmission();
				transmitter = null;
			}
			connectButton.setText(buttonTextConnect);
			connected = false;
			
			if(selectedBluetoothDevice == null || selectedAudioDevice == null) {
				connectButton.setEnabled(false);
			}
		}
	}

	/**
	 * Allows the user to choose a remote bluetooth device to connect to.
	 * Shouldn't be enabled if we're already connected to a remote device.
	 */
	public void chooseBluetoothDevice() {
		BTLBBluetoothDeviceSelectDialog btDialog = new BTLBBluetoothDeviceSelectDialog(this, "BTLB :: Select a Bluetooth Device");
		btDialog.populate();
	}
	
	/**
	 * Allows the user to choose the audio device to stream from.
	 * Shouldn't be enabled if we're already connected to a remote device.
	 */
	public void chooseAudioDevice() {
		BTLBMixerSelectDialog msDialog = new BTLBMixerSelectDialog(this, "BTLB :: Select an Audio Device");
		msDialog.populate();
	}
	
	/**
	 * Called when the user clicks Help>About in the menu.
	 */
	public void aboutDialog() {
		JOptionPane.showMessageDialog(this.frame,
				"BTLBDesktop by Ashley Davis (SgtCoDFish).\nhttps://github.com/SgtCoDFish/BTLBDesktop\nhttp://sgtcodfish.com",
				"BTLB :: About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Called by {@link BTLBBluetoothDeviceSelectDialog} and {@link BTLBMixerSelectDialog}
	 * to allow the root window to handle a user dialog selection of a device.
	 * @param id An ID representing the dialog that called this function
	 * @param selection The selection the user made. Should be type-cast.
	 */
	public void handleDeviceSelectDialog(String id, Object selection) {
		boolean success = false;
		
		if(id == BTLBMixerSelectDialog.MixerSelectDialogID) {
			BTLBFormat format = BTLBFormat.fromString((String) selection);
			if(format == null) {
				debugMessage("format == null in handleDSD:\n" + (String)selection);
				return;
			}
			
			if(!AudioSystem.isLineSupported(format.line.getLineInfo())) {
				debugMessage("Invalid line chosen. This shouldn't happen.");
				System.exit(1);
			} else {
				changeAudioDevice(format);
				success = true;
			}
		} else if(id == BTLBBluetoothDeviceSelectDialog.BluetoothSelectDialogID) {
			changeBluetoothDevice((RemoteDevice)selection);
			success = true;
		}
		
		if(success) {
			String message = "Device selected successfully.";
			if(saveCheckboxMenuItem.isSelected()) {
				message += "\nYour chosen devices were saved as the defaults.\nTo prevent this in future, uncheck the checkbox in the settings menu.";
				saveProperties();
			}
			
			JOptionPane.showMessageDialog(frame, message, "Device Selection Successful", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Creates the default menu bar for this app.
	 */
	public void setupMenu() {
		menuBar = new JMenuBar();
		settingsMenu = new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);
		
		JMenuItem chooseBluetoothMenuItem = new JMenuItem("Choose Remote Bluetooth Device");
		chooseBluetoothMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseBluetoothDevice();
			}
		});
		settingsMenu.add(chooseBluetoothMenuItem);
		
		JMenuItem chooseAudioDeviceMenuItem = new JMenuItem("Choose Audio Device");
		chooseAudioDeviceMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooseAudioDevice();
			}
		});
		settingsMenu.add(chooseAudioDeviceMenuItem);
		
		settingsMenu.addSeparator();
		saveCheckboxMenuItem = new JCheckBoxMenuItem("Save chosen devices as defaults");
		saveCheckboxMenuItem.setSelected(true);
		settingsMenu.add(saveCheckboxMenuItem);
		
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aboutDialog();
			}
		});
		helpMenu.add(aboutMenuItem);
		
		menuBar.add(settingsMenu);
		menuBar.add(helpMenu);
		
		frame.setJMenuBar(menuBar);
	}
	
	/**
	 * Should be called to change the bluetooth device. The device should never be directly assigned.
	 */
	public void changeBluetoothDevice(RemoteDevice rd) {
		btDeviceLabel.setText(btDeviceLabelText + rd);
		selectedBluetoothDevice = rd;
		
		if(selectedBluetoothDevice != null && selectedAudioDevice != null) {
			connectButton.setEnabled(true);
		} else {
			connectButton.setEnabled(false);
		}
	}
	
	/**
	 * Should be called to change the audio device. The device should never be directly assigned.
	 */
	public void changeAudioDevice(BTLBFormat format) {
		audioDeviceLabel.setText(audioDeviceLabelText + format.mixer.getMixerInfo().getName());
		selectedAudioDevice = format;
		
		if(selectedBluetoothDevice != null && selectedAudioDevice != null) {
			connectButton.setEnabled(true);
		} else {
			connectButton.setEnabled(false);
		}
	}
	
	/**
	 * Initialises this.properties by loading with a default name
	 */
	public void openPropertiesFile() {
		FileInputStream in = null;
		properties = new Properties();
		
		try {
			in = new FileInputStream(defaultPropertiesFileName);
		} catch (FileNotFoundException e) {
			debugMessage("Properties file not found, creating one.");
			try {
				File infile = new File(defaultPropertiesFileName);
				infile.createNewFile();
				infile = null;
				
				in = new FileInputStream(defaultPropertiesFileName);
			} catch(Exception fnfe) {
				debugMessage("Couldn't create properties file, exiting.", fnfe);
				System.exit(1);
			}
		}
		
		try {
			properties.load(in);
			in.close();
		} catch (IOException e) {
			debugMessage("IOE while loading properties", e);
		}
		
		String defaultBluetoothDevice = properties.getProperty("btlb.bluetooth_device", "");
		String defaultAudioDevice = properties.getProperty("btlb.audio_device", ""); 
		
		loadBluetoothDeviceFromProperty(defaultBluetoothDevice);
		loadAudioDeviceFromProperty(defaultAudioDevice);
	}
	
	/**
	 * Helper function called by loadProperties(). Searches the list of available devices for the device and sets it up if it exists.
	 * @param device The device to search for.
	 */
	private void loadBluetoothDeviceFromProperty(String device) {
		if(device.equals("")) { return; }
		try {
			RemoteDevice rdevs[] = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
			
			for (RemoteDevice rd : rdevs) {
				if(rd.getBluetoothAddress().equals(device)) {
					// if the device is found we're done
					changeBluetoothDevice(rd);
					debugMessage("Loaded bluetooth device property: " + selectedBluetoothDevice);
					return;
				}
			}
			
			// if we reach here, the device wasn't in the preknown list and
			// therefore the properties file is invalid and needs to be killed.
			killProperties();
		} catch (BluetoothStateException bse) {
			debugMessage("Bluetooth state exception.", bse);
		}
	}
	
	/**
	 * Helper function called by loadProperties(). Searches the list of available mixers for an appropriate line and sets it up if it exists.
	 * @param device The device to search for.
	 */
	private void loadAudioDeviceFromProperty(String device) {
		if(device.equals("")) { return; }
		BTLBFormat format = BTLBFormat.fromString(device);
		
		if(format == null) {
			// we have an invalid file and need to kill the properties file
			killProperties();
		} else {
			changeAudioDevice(format);
			debugMessage("Loaded audio device property: " + selectedAudioDevice);
		}
	}
	
	public void killProperties() {
		debugMessage("Killing invalid property list:\n" + "bluetooth_device: " +
				properties.getProperty("btlb.bluetooth_device", "NO_BT_DEVICE") + "\naudio_device: " + 
				properties.getProperty("btlb.audio_device","NO_AU_DEVICE"));
		properties.clear();
		saveProperties(); // save the empty list.
	}
	
	/**
	 * If the user chooses a different device (bluetooth or audio) and has the
	 * "Save as defaults" menu checkbox chosen, this function is called to save
	 * the choice as default.
	 */
	private void saveProperties() {
		if(properties == null) {
			debugMessage("Call to saveProperties with null properties object!");
			return;
		}
		
		FileOutputStream os = null;
		
		try {
			os = new FileOutputStream(defaultPropertiesFileName);
			
		} catch (FileNotFoundException e) {
			debugMessage("Couldn't save properties.", e);
			return;
		}
		
		try {
			String btProperty = (selectedBluetoothDevice == null ? "" : selectedBluetoothDevice.getBluetoothAddress());
			String auProperty = (selectedAudioDevice == null ? "" : selectedAudioDevice.toString());
			
			properties.setProperty("btlb.bluetooth_device", btProperty);
			properties.setProperty("btlb.audio_device", auProperty);
			
			properties.store(os, "BTLB Properties");
			debugMessage("Properties saved to " + defaultPropertiesFileName);
			os.close();
		} catch (IOException e) {
			debugMessage("Coudldn't write to properties file.", e);
		}
	}
	
	/**
	 * @return The main root frame of this app.
	 */
	public JFrame getFrame() {
		return frame;
	}
	
	/**
	 * @return The selected RemoteDevice
	 */
	public synchronized RemoteDevice getSelectedBluetoothDevice() {
		return selectedBluetoothDevice;
	}
	
	/**
	 * @return The selected audio device.
	 */
	public synchronized BTLBFormat getSelectedAudioDevice() {
		return selectedAudioDevice;
	}
	
	// --------------------------------------------------------------------- //
	// ----- DEBUG FUNCTIONS ----- //
	// --------------------------------------------------------------------- //
	
	/**
	 * Prints a debug message if DEBUG is enabled.
	 * @param msg The message to print.
	 */
	public static void debugMessage(String msg) {
		if(DEBUG) {
			System.out.println("BTLBDesktop Debug: " + msg);
		}
	}
	
	/**
	 * Prints a debug message and an exception's stack trace if DEBUG is enabled.
	 * @param msg The message to print.
	 * @param e The exception whose stack trace we're printing.
	 */
	public static void debugMessage(String msg, Exception e) {
		if(DEBUG) {
			debugMessage(msg);
			e.printStackTrace();
		}
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
	
//	/**
//	 * Takes a finished BTLBDiscoveryListener class with a valid discoveredDevices vector
//	 * and creates a window allowing the user to choose a device to connect to.
//	 * @param discoveryListener
//	 */
//	@Deprecated
//	public void addBluetoothDeviceList(BTLBDiscoveryListener discoveryListener) {
//		final HashMap<String, RemoteDevice> deviceListCopy = new HashMap<String, RemoteDevice>(discoveryListener.discoveredDevices);
//		
//		// add a descriptive label, replacing our current label.
//		infoLabel.setText("Select a device from the list below to communicate with:");
//		
//		// populate a list with the available devices
//		DefaultListModel<String> model = new DefaultListModel<String>();
//		for(String rd : deviceListCopy.keySet()) {
//			model.addElement(rd);
//		}
//		
//		final JList<String> deviceList = new JList<String>(model);
//		
//		frame.add(deviceList);
//		
//		// create a confirmation button
//		JButton confButton = new JButton("Use the selected device as remote headset.");
//		confButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if(deviceList.getSelectedValue() != null) {
//					selectedDevice = deviceListCopy.get(deviceList.getSelectedValue());
//					BTLBDesktop.mainInstance.startStream();
//				}
//			}
//		});
//		frame.add(confButton);
//		frame.pack();
//	}
	
//	/**
//	 * Called internally to signify that a bluetooth device has been chosen
//	 * and that the music streaming phase should begin. Causes a redraw.
//	 */
//	@Deprecated
//	private void startStream() {
//		if(selectedDevice == null) {
//			System.err.println("Call to startStream() with no selected device, exiting.");
//			System.exit(1);
//		}
//		
//		setupFrame(BTLBDesktop.defaultTitle);
//		JButton discoverButton = new JButton("Search for services!");
//		discoverButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				bluetoothHandler.discoverServices(selectedDevice);
//			}
//		});
//		
//		
//		JButton testButton = new JButton("Send test data!");
//		testButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				sendTestData();
//			}
//		});
//		
//		
//		frame.add(discoverButton);
//		frame.add(testButton);
//		frame.pack();
//		
//	}
	
//	@Deprecated
//	public void sendTestData() {
//		String url = bluetoothHandler.discoverServices(selectedDevice);
//		System.out.println("SendTestData: URL: " + url);
//		System.out.println("SendTestData: Sending...");
//		if(!url.equals("")) {
//			try {
//				if(DEBUG) {
//					System.out.println("Connecting to " + url);
//				}
//				StreamConnection conn = (StreamConnection)Connector.open(url);
//				java.io.OutputStream ops = conn.openOutputStream();
//				ops.write(new byte[] {1, 0, 4, 1, 9});
//				//conn.close();
//				if(DEBUG) {
//					System.out.println("SendTestData: Wrote to stream.");
//				}
//			} catch (IOException e) {
//				System.err.println("SendTestData: IO Error in StreamConnectionNotifier");
//				e.printStackTrace();
//			}
//		}
//	}
}
