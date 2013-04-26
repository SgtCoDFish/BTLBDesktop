package com.sgtcodfish.BTLBDesktop;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl.Type;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * Records sound as it's played and stores it to be sent over bluetooth.
 * @author Ashley Davis (SgtCoDFish)
 */
public class AudioRecorder extends Thread {
	private static final String threadName = "BTLB AudioRecorder thread";
	private static boolean DEBUG = BTLBDesktop.DEBUG;
	
	public AudioRecorder() {
		super(threadName);
	}
	
	Mixer mixer = null;
	TargetDataLine line = null;
	File file = null;
	private boolean record = true;
	
	enum AudioRecorderMode {
		PAUSE,
		RECORD
	}
	
	AudioRecorderMode mode = AudioRecorderMode.RECORD;
	
	@Override
	public void start() {
		if(DEBUG) {
			System.out.println("AudioRecorder: Starting thread: " + this.getName());
		}
		
		mixer = getPrimaryCaptureDriver();
		try {
			mixer.open();
		
			if(mixer.isOpen()) {
				Line.Info infos[] = mixer.getTargetLineInfo();
				
				if(DEBUG) {
					System.out.println("AudioRecorder: " + infos.length + " lines found in primary capture driver.");
					
					for (Line.Info i : infos) {
						System.out.println("AudioRecorder: Line info: " + i.toString());
						
						if(DEBUG) {
							DataLine.Info dataLineInfo = (DataLine.Info)i;
							AudioFormat formats[] = dataLineInfo.getFormats();
							
							for(AudioFormat ff : formats) {
								System.out.println("\tAudioRecorder: Supported format: " + ff);
							}
						}
					}
					
				}
				
				AudioFormat audioFormat = new AudioFormat(Encoding.PCM_SIGNED, 44100.0f, 16, 2, 4, 44100.0f, false);
				DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
				if(!AudioSystem.isLineSupported(dataLineInfo)) {
					System.out.println("AudioRecorder: Unsupported format!");
				} else {
					//line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
					line = (TargetDataLine) mixer.getLine(dataLineInfo);
					
					if(DEBUG) {
						System.out.println("AudioRecorder: Format: " + line.getFormat() + ", rate=" + line.getFormat().getFrameRate());
					}
				}
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		
		if(DEBUG) {
			System.out.println("AudioRecorder: Startup complete!");
		}
		
		super.start();
	}
	
	@Override
	public void run() {
		if(mode == AudioRecorderMode.RECORD) {
			AudioInputStream inputStream = null;
			int framesToRead = 5;
			int frameSizeInBytes = 88200;
			byte buffer[] = new byte[frameSizeInBytes * framesToRead];
			while(record) {
				while(!line.isOpen()) {
					try {
						line.open();
						if(line.isControlSupported(Type.VOLUME)) {
							System.out.println("AudioRecorder: Line supports volume control.");
						}
					} catch(LineUnavailableException lnae) {
						System.out.println("AudioRecorder: Line unavailable.");
						try {
							Thread.sleep(2500);
						} catch (InterruptedException e) {}
					}
					
					line.start();
					if(DEBUG) {
						System.out.println("AudioRecorder: Opened a line.");
					}
				}
				
				if(inputStream == null) {
					inputStream = new AudioInputStream(line);
					if(DEBUG) {
						System.out.println("AudioRecorder: Opened an input stream.");
					}
				}
				
				try {
					int timesRead = 0;
					
					while(timesRead < framesToRead) {
						inputStream.read(buffer, frameSizeInBytes * timesRead, frameSizeInBytes);
						
						//System.out.println("BytesRead=" + inputStream.read(buffer, frameSizeInBytes * timesRead, frameSizeInBytes));
						timesRead++;
					}
				} catch(IOException ioe) {
					System.out.println(ioe);
				}
			}
			
			line.stop();
			line.close();
			mixer.close();
		} else if(mode == AudioRecorderMode.PAUSE) {
			while(record == false) {}
			record = true;
		}
	}
	
	/**
	 * Finds a "primary sound capture driver". May only work in Windows Vista+
	 * @return The Mixer relating to the primary capture driver, or null if no such driver was found.
	 */
	public static Mixer getPrimaryCaptureDriver() {
		//final String searchName = "Microphone (Realtek High Defini";
		//final String searchName = "Primary Sound Capture Driver";
		final String searchName = "Stereo Mix (Realtek High Defini";// this one for loopback
		Mixer.Info infos[] = AudioSystem.getMixerInfo();
		
		for(Mixer.Info i : infos) {
			if(i.getName().equals(searchName)) {
				if(DEBUG) {
					System.out.println("Found primary sound capture driver: " + i);
				}
				
				return AudioSystem.getMixer(i);
			}
		}
		
		if(DEBUG) {
			System.out.println("Failed to find sound capture driver, name=\"" + searchName + "\".");
		}
		return null;
	}
}
