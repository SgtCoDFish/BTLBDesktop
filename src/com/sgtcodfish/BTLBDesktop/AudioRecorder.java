package com.sgtcodfish.BTLBDesktop;

/**
 * Records sound as it's played and stores it to be sent over bluetooth.
 * @author Ashley Davis (SgtCoDFish)
 */
public class AudioRecorder extends Thread {
	private static final String threadName = "BTLB AudioRecorder thread";
	private boolean DEBUG;
	
	public AudioRecorder() {
		super(threadName);
		DEBUG = BTLBDesktop.DEBUG;
	}
	
	@Override
	public void start() {
		if(DEBUG) {
			System.out.println("Starting thread: " + this.getName());
		}
	}
	
	@Override
	public void run() {
		
	}
}
