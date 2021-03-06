package cims.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;

//import cims.datatypes.BeatTime;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
//import cims.generators.GenerateMidi;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_Segment;
//import cims.players.PlayMidi_BeatTime;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class Test {
	
	private SupervisorMidi sm;
	
	public static final int SEGMENT_TESTS = 0;
	public static final int MESSAGE_TESTS = 1;
	public static final int BEATTIME_TESTS = 2;
	
	public static final Logger LOGGER = Logger.getLogger(Test.class.getName());

	public Test(SupervisorMidi supervisor) {
		sm = supervisor;
		LOGGER.setLevel(Level.ALL);
	}
	
	public void runTests(int testType) {
		switch(testType) {
		case SEGMENT_TESTS:
			GenerateMidi_Segment gm_segment = this.generateMidi_Segment_repeatLast();
			gm_segment.generate(sNextPlay); // 0 immediate, 1 next beat, 2 next bar
			LOGGER.warning("Segment Active Threads: "+this.activeThreadCount());
			break;
		case MESSAGE_TESTS:
			LOGGER.warning("Message Active Threads: "+this.activeThreadCount());
			break;
		case BEATTIME_TESTS:
			//PlayMidi_BeatTime pm = this.playMidi_beatTime();
			LOGGER.warning("Message Active Threads: "+this.activeThreadCount());
			break;
		}
	}
	/*
	public PlayMidi_BeatTime playMidi_beatTime() {
		LOGGER.warning("TST: playMidi_beatTime");
		PlayMidi_BeatTime pm = sm.currentPlayer();
		MidiMessage noteOn = this.midiMessage(1);
		//Set BeatTime info
		//private String[] transportNames = {"bar","beat","unit","ppq","tempo","beatsPerBar","beatType","state","ticks"};
		Integer[] transportOn = {2,1,0,0,0,0,0,0,0};
		BeatTime btOn = new BeatTime(transportOn);
		noteOn.beatTime = btOn;
		pm.add(noteOn);
		MidiMessage noteOff = this.midiMessage(0);
		//Set BeatTime info
		Integer[] transportOff = {3,4,0,0,0,0,0,0,0};
		BeatTime btOff = new BeatTime(transportOff);
		noteOff.beatTime = btOff;
		pm.add(noteOff);
		return pm;
	}
	*/
	public GenerateMidi_Loop generateMidi_Loop() {
		LOGGER.warning("TST: generateMidi_Loop");
		GenerateMidi_Segment gm_segment = this.generateMidi_Segment(false);
		GenerateMidi_Loop gm_loop = new GenerateMidi_Loop(gm_segment);
		return gm_loop;
	}
	
	public GenerateMidi_Segment generateMidi_Segment(boolean generate) {
		LOGGER.warning("TST: generateMidi_Segment");
		MidiSegment segment = this.midiSegment();
		GenerateMidi_Segment gm_segment = new GenerateMidi_Segment(sm,segment);
		if(generate) {
			gm_segment.generate();
		}
		return gm_segment;
	}
	
	public GenerateMidi_Segment generateMidi_Segment_repeatLast() {
		LOGGER.warning("TST: generateMidi_Segment");
		MidiSegment segment = sm.getLastMidiSegment();
		GenerateMidi_Segment gm_segment = new GenerateMidi_Segment(sm,segment);
		return gm_segment;
	}
	
	public MidiSegment midiSegment() {
		LOGGER.warning("TST: MidiSegment");
		MidiSegment segment = new MidiSegment();
		
		MidiMessage noteOn = this.midiMessage(1);
		MidiMessage noteOff = this.midiMessage(0);
		
		segment.add(noteOn);
		segment.add(noteOff);
		
		return segment;
	}
	
	public MidiMessage midiMessage(int onOff) {
		LOGGER.warning("TST: MidiMessage");
		MidiMessage message = new MidiMessage();
		switch(onOff) {
		case 1:
			int[] noteOn = {MidiMessage.NOTE_ON,64,100};
			message.set(noteOn, false);
			message.timeMillis = 0;
			break;
		case 0:
			int[] noteOff = {MidiMessage.NOTE_OFF,64,0};
			message.set(noteOff, false);
			message.timeMillis = 2000;
			break;
			default:
				// do nothing
		}
		return message;
	}
	
	public int activeThreadCount() {
		return Thread.activeCount();
	}
}
