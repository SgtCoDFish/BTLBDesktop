package com.sgtcodfish.BTLBDesktop;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBAudioReader extends Thread {
	private BTLBDesktop parent = null;
	private BTLBTransmitter transmitter = null;
	
	private byte []buffer = null;
	private boolean read = false;
	
	private int bufferSize = -1; 
	
	public BTLBAudioReader(BTLBDesktop parent, BTLBTransmitter transmitter) {
		this.parent = parent;
		this.transmitter = transmitter;
	}
	
	@Override
	public void run() {
		// First get an audio input stream
		BTLBFormat format = parent.getSelectedAudioDevice();
		
		TargetDataLine line = format.line;
		
		AudioInputStream inputStream = null;
		
		try {
			line.open();
		} catch (LineUnavailableException e1) {
			BTLBDesktop.debugMessage("Selected line unavilable", e1);
		}
		line.start();
		inputStream = new AudioInputStream(line);
		bufferSize = inputStream.getFormat().getFrameSize() * 100;
		BTLBDesktop.debugMessage("BTLBTransmitter: Buffer size: " + bufferSize);
		buffer = new byte[bufferSize];
		transmitter.setBuffer(buffer);
		
		while(read) {
			try {
				synchronized(buffer) {
					inputStream.read(buffer, 0, buffer.length);
				}
			} catch (IOException e) {
				BTLBDesktop.debugMessage("Exception in audioReader", e);
			}
		}
	}
	
	public void cease() {
		read = false;
	}
}
