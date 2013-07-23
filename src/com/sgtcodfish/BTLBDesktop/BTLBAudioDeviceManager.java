package com.sgtcodfish.BTLBDesktop;

import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;


/**
 * This class maintains a list of all supported mixers and audio devices
 * to simplify use of javax.sound
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBAudioDeviceManager {
	public HashMap<String, BTLBFormat> formatList = null;
	
	public BTLBAudioDeviceManager() {
		formatList = new HashMap<String, BTLBFormat>();
		Mixer.Info mixerInfos[] = AudioSystem.getMixerInfo();
		
		for(Mixer.Info mi : mixerInfos) {
			Line.Info lineInfos[] = AudioSystem.getMixer(mi).getTargetLineInfo();
			for(Line.Info li : lineInfos) {
				if(li.getLineClass() == Port.class) {
					continue;
				}
				AudioFormat formats[] = ((DataLine.Info)li).getFormats();
				for(AudioFormat af : formats) {
					try {
						BTLBFormat newFormat = new BTLBFormat(AudioSystem.getMixer(mi),
															(TargetDataLine)AudioSystem.getLine(li),
															af);
						
						formatList.put(newFormat.toString(), newFormat);
					} catch (LineUnavailableException e) {
						BTLBDesktop.debugMessage("Line unavailable: " + li, e);
					}
				}
			}
		}
	}
}
