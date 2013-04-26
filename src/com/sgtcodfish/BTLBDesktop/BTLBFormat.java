package com.sgtcodfish.BTLBDesktop;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

/**
 * Holds a unique line from javax.sound.sampled
 * @author Ashley Davis (SgtCoDFish)
 */
public class BTLBFormat {
	public static final char sepChar = ':';
	
	public Mixer mixer = null;
	public TargetDataLine line = null;
	public AudioFormat format = null;
	
	public BTLBFormat(Mixer mixer, TargetDataLine line, AudioFormat format) {
		this.mixer = mixer;
		this.line = line;
		this.format = format;
	}
	
	@Override
	public String toString() {
		return "BTLBF" + mixer.getMixerInfo().getName() + sepChar + line.getLineInfo().toString() + sepChar + format.toString();
	}
	
	/**
	 * Uses the way the BTLBFormat.toString function works to create a format from a string description.
	 * @return
	 */
	public static BTLBFormat fromString(String s) {
		Mixer m = null;
		TargetDataLine tdl = null;
		AudioFormat audioFormat = null;
		
		if(!s.startsWith("BTLBF")) {
			return null;
		}
		
		String mixerName = s.substring(5, s.indexOf(sepChar));
		for (Mixer.Info mi : AudioSystem.getMixerInfo()) {
			if(mi.getName().equals(mixerName)) {
				m = AudioSystem.getMixer(mi);
				break;
			}
		}
		
		if(m == null) {
			return null;
		}
		
		// substring from char after the first sepChar to the char before the second £
		String lineInfo = s.substring(s.indexOf(sepChar) + 1, s.lastIndexOf(sepChar));
		for (Line.Info li : m.getTargetLineInfo()) {
			if(li.toString().equals(lineInfo)) {
				try {
					tdl = (TargetDataLine)AudioSystem.getLine(li);
				} catch (LineUnavailableException e) {
					BTLBDesktop.debugMessage("Line unavailable: " + li, e);
				}
			}
		}
		
		if(tdl == null) {
			return null;
		}
		
		String formatStr = s.substring(s.lastIndexOf(sepChar)+1);
		for (AudioFormat af : ((DataLine.Info)(tdl.getLineInfo())).getFormats()) {
			if(af.toString().equals(formatStr)) {
				audioFormat = af;
			}
		}
		
		if(audioFormat == null) {
			return null;
		}
		
		return new BTLBFormat(m, tdl, audioFormat);
	}
}
