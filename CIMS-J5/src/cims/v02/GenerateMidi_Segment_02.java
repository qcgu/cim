package cims.v02;

import static cims.supervisors.SupervisorMidi_Globals.sDefaultDuration;
import cims.datatypes.MidiSegment;
import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.Randomiser;

public class GenerateMidi_Segment_02 extends GenerateMidi_Segment {
	private SupervisorMidi supervisor;
	private Randomiser randomiser;
	
	public GenerateMidi_Segment_02(SupervisorMidi supervisor) {
		super(supervisor);
		this.supervisor = supervisor;
		this.randomiser = new Randomiser();
	}

	public GenerateMidi_Segment_02(SupervisorMidi supervisor, MidiSegment segment) {
		super(supervisor, segment);
		this.supervisor = supervisor;
		this.randomiser = new Randomiser();
	}
	
	public int supportSegment() {
		this.makeEmptySegment();
		int beatsInBar = 4;
		int duration = sDefaultDuration;
		for(int i=0;i<beatsInBar;i++) {
			this.addNote((duration*i),supervisor.getLastMidiSegment().firstMessage().pitch, randomiser.positiveInteger(40) + 80, duration);
		}
		return (duration*beatsInBar);
		
	}
	
	public int initiateSegment() {
		this.makeEmptySegment();
		int duration = sDefaultDuration;
		int accumTime = 0;
		int segmentLength = 0;
		this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration);
		accumTime += duration;
		for(int i=1; i<8; i++) {
			int dur = duration;
			if (Math.random() < 0.5) dur = duration / 2;
			this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, dur);
			accumTime += dur;
		}
		this.addNote(accumTime,randomiser.getRandomPitchClass() + 72, randomiser.positiveInteger(40) + 80, duration*2);
		segmentLength = accumTime + duration * 2 - 20; // slight reduction to avoid overshoot assuming quantise is on
		return segmentLength;
	}

}
