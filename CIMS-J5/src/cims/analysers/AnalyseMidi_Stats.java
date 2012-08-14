package cims.analysers;

import cims.supervisors.SupervisorMidi;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.*;

public class AnalyseMidi_Stats extends AnalyseMidi {
	private int pitchMean;
	private int pitchMode;
	private int pitchDeviation;
	private int velocityMean;
	private int velocityMode;
	private int velocityDeviation;
	private int durationMean;
	private int durationMode;
	private int durationDeviation;
	private int current_pitch;
	private int current_velocity;
	private int current_duration;
	
	private DescriptiveStatistics midiStats; 


	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new SynchronizedDescriptiveStatistics();
	}

	@Override
	public void analyse() {
		current_pitch = current_message.pitch;
		current_velocity = current_message.velocity;
		//Update the mean pitch, velocity and duration - save in supervisor
		midiStats.addValue(current_pitch);
		pitchMean = (int) midiStats.getMean();
		pitchDeviation = (int) midiStats.getStandardDeviation();
		this.supervisor.txtMsg("P_CUR: " +current_pitch);
		this.supervisor.txtMsg("P_MEAN: "+pitchMean);
		this.supervisor.txtMsg("P_SD: "+pitchDeviation);
		//Calculate the standard deviation for current pitch, velocity and duration - save in last MidiMessage
		
	}
}
