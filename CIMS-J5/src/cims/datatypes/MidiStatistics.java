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

	
	private DescriptiveStatistics midiStats_Pitch;
	private DescriptiveStatistics midiStats_Velocity;
	private DescriptiveStatistics midiStats_Duration;
	
	
	public MidiStatistics() {
		this.midiStats_Pitch = new SynchronizedDescriptiveStatistics();
		this.midiStats_Velocity = new SynchronizedDescriptiveStatistics();
		this.midiStats_Duration = new SynchronizedDescriptiveStatistics();
	}

	public void addPitch(int newPitch) {
		current_pitch = newPitch;
		midiStats_Pitch.addValue(newPitch);
		meanPitch = (int) midiStats_Pitch.getMean();
		deviationPitch = (int) midiStats_Pitch.getStandardDeviation();
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
	
}
