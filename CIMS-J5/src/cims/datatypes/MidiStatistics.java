package cims.datatypes;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

public class MidiStatistics {
	
	private int current_pitch;
	private int current_velocity;
	private int current_duration;
	
	private int meanPitch;
	private int meanVelocity;
	private int meanDuration;
	
	private int deviationPitch;
	private int deviationVelocity;
	private int deviationDuration;

	private int[] pitchClassHistogram;

	private DescriptiveStatistics midiStats_Pitch;
	private DescriptiveStatistics midiStats_Velocity;
	private DescriptiveStatistics midiStats_Duration;
	
	
	public MidiStatistics() {
		this.midiStats_Pitch = new SynchronizedDescriptiveStatistics();
		this.midiStats_Velocity = new SynchronizedDescriptiveStatistics();
		this.midiStats_Duration = new SynchronizedDescriptiveStatistics();
		// initialise pitch class histogram
		this.pitchClassHistogram = new int[12];
		this.clearPitchHistogram();
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
	
	public int getPitchClass(int pitch) {
		pitch -= 1; //Allow for 1-12
		return pitchClassHistogram[pitch];
	}
	
	public void clearPitchHistogram() {
		for (int i=0; i<12; i++) {
			pitchClassHistogram[i] = 0;
		}
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

	public int getCurrent_pitch() {
		return current_pitch;
	}

	public void setCurrent_pitch(int current_pitch) {
		this.current_pitch = current_pitch;
	}

	public int getCurrent_velocity() {
		return current_velocity;
	}

	public void setCurrent_velocity(int current_velocity) {
		this.current_velocity = current_velocity;
	}

	public int getCurrent_duration() {
		return current_duration;
	}

	public void setCurrent_duration(int current_duration) {
		this.current_duration = current_duration;
	}

	public int getMeanPitch() {
		return meanPitch;
	}

	public void setMeanPitch(int meanPitch) {
		this.meanPitch = meanPitch;
	}

	public int getMeanVelocity() {
		return meanVelocity;
	}

	public void setMeanVelocity(int meanVelocity) {
		this.meanVelocity = meanVelocity;
	}

	public int getMeanDuration() {
		return meanDuration;
	}

	public void setMeanDuration(int meanDuration) {
		this.meanDuration = meanDuration;
	}

	public int getDeviationPitch() {
		return deviationPitch;
	}

	public void setDeviationPitch(int deviationPitch) {
		this.deviationPitch = deviationPitch;
	}

	public int getDeviationVelocity() {
		return deviationVelocity;
	}

	public void setDeviationVelocity(int deviationVelocity) {
		this.deviationVelocity = deviationVelocity;
	}

	public int getDeviationDuration() {
		return deviationDuration;
	}

	public void setDeviationDuration(int deviationDuration) {
		this.deviationDuration = deviationDuration;
	}

	public int[] getPitchClassHistogram() {
		return pitchClassHistogram;
	}

	public void setPitchClassHistogram(int[] pitchClassHistogram) {
		this.pitchClassHistogram = pitchClassHistogram;
	}

	public DescriptiveStatistics getMidiStats_Pitch() {
		return midiStats_Pitch;
	}

	public void setMidiStats_Pitch(DescriptiveStatistics midiStats_Pitch) {
		this.midiStats_Pitch = midiStats_Pitch;
	}

	public DescriptiveStatistics getMidiStats_Velocity() {
		return midiStats_Velocity;
	}

	public void setMidiStats_Velocity(DescriptiveStatistics midiStats_Velocity) {
		this.midiStats_Velocity = midiStats_Velocity;
	}

	public DescriptiveStatistics getMidiStats_Duration() {
		return midiStats_Duration;
	}

	public void setMidiStats_Duration(DescriptiveStatistics midiStats_Duration) {
		this.midiStats_Duration = midiStats_Duration;
	}	
}
