package cims.datatypes;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

public class MidiStatistics {
	
	public int current_pitch;
	public int current_velocity;
	public int current_duration;
	
	public int meanPitch;
	public int meanVelocity;
	public int meanDuration;
	
	public int deviationPitch;
	public int deviationVelocity;
	public int deviationDuration;

	
	private int[] pitchClassHistogram = new int[12];

	private DescriptiveStatistics midiStats_Pitch;
	private DescriptiveStatistics midiStats_Velocity;
	private DescriptiveStatistics midiStats_Duration;
	
	
	public MidiStatistics() {
		this.midiStats_Pitch = new SynchronizedDescriptiveStatistics();
		this.midiStats_Velocity = new SynchronizedDescriptiveStatistics();
		this.midiStats_Duration = new SynchronizedDescriptiveStatistics();
		// initalise pitch class histgram
		for (int i=0; i<pitchClassHistogram.length; i++) {
			pitchClassHistogram[i] = 0;
		}
	}

	public void addPitch(int newPitch) {
		current_pitch = newPitch;
		midiStats_Pitch.addValue(newPitch);
		meanPitch = (int) midiStats_Pitch.getMean();
		deviationPitch = (int) midiStats_Pitch.getStandardDeviation();
		// update pitch class histogram
		int pitch_Class = newPitch%12;
		pitchClassHistogram[pitch_Class]++;
	}
	
	public void addVelocity(int newVelocity) {
		current_velocity = newVelocity;
		midiStats_Velocity.addValue(newVelocity);
		meanVelocity = (int) midiStats_Velocity.getMean();
		deviationVelocity = (int) midiStats_Velocity.getStandardDeviation();
	}
	
	public void addDuration(int newDuration) {
		current_velocity = newDuration;
		midiStats_Duration.addValue(newDuration);
		meanDuration = (int) midiStats_Velocity.getMean();
		deviationDuration = (int) midiStats_Velocity.getStandardDeviation();
	}
	
	// make a weighted selection of a pitch class from the histogram
	public int getRandomPitchClass() {
		int max = 0;
		for (int i=0; i<pitchClassHistogram.length; i++) {
			max += pitchClassHistogram[i];
		}
		int rnd = (int)(Math.random() * max);
		int val = 0;
		int i = 0;
		while(val < rnd) {
			val += pitchClassHistogram[i++];
		}
		return i;
	}
}
