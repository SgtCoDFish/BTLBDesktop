package com.sgtcodfish.BTLBDesktop;

import javax.sound.sampled.*;

/**
 * Desktop portion of turning an android device into a bluetooth headset.
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBDesktop {

	/**
	 * @param args Not used
	 */
	public static void main(String[] args) {
		printMixers();
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
			System.out.println(i.getName());
			System.out.println(i.getDescription());
			System.out.println();
		}
	}
}
