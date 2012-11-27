package cims.generators;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;


/*****************************************************************************************
 * The main generator for generating MIDI segments. Usually instantiated and called by
 * a decider.
 * 
 * @author Andrew Gibson a.gibson@griffith.edu.au
 * @author Andrew Brown
 *
 */
public class GenerateMidi_Segment extends GenerateMidi {

	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	private SupervisorMidi supervisor;
	
	public static Logger LOGGER = Logger.getLogger(GenerateMidi_Segment.class);
	
	public GenerateMidi_Segment(SupervisorMidi supervisor) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
		LOGGER.setLevel(Level.INFO);
	}
	
	public GenerateMidi_Segment(SupervisorMidi supervisor, MidiSegment segment) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
		this.midiSegment = segment;
		LOGGER.setLevel(Level.INFO);
	}
	
	public void generate() {
		LOGGER.debug("generate() called. Creating OutputQueue, adding segment, then play");
		midiQueue = new OutputQueue(this);
		midiQueue.addSegment(midiSegment);
		midiQueue.play();
	}
	
	public void stop() {
		midiQueue.cancel();
	}
	
	public void generate(int start) {
		LOGGER.debug("generate(int start) called. Creating OutputQueue, adding segment, then play according to start");
		midiQueue = new OutputQueue(this);
		switch(start) {
		case 0:
			midiQueue.startOnPlay();
			break;
		case 1:
			midiQueue.startOnNextBeat();
			break;
		case 2:
			midiQueue.startOnNextBar();
		}
		midiQueue.addSegment(midiSegment);
		midiQueue.play();
	}
	
	public MidiSegment getMidiSegment() {
		return this.midiSegment;
	}
	public void output(MidiMessage midimessage) {
		LOGGER.debug("OUTPUT MidiMessage Status: "+ midimessage.status);
		this.supervisor.midiOut(midimessage.rawMessage);
		LOGGER.debug("Thread Count: "+Thread.activeCount());
	}
		
	public synchronized void makeLastSegment () {
		LOGGER.debug("Playing back last segement");
		midiSegment = supervisor.getLastMidiSegment().zeroTiming();
		midiSegment.setChannel(2); // channel is midi channel 1-16
	}
	
	public void makeEmptySegment() {
		this.midiSegment = new MidiSegment();
	}
	
	public void makeNoteSegment(int startTime, int pitch, int velocity, int duration ) {
		this.makeEmptySegment();
		this.addNote(startTime,pitch,velocity,duration);
	}
	
	public void addNote(int startTime, int pitch, int velocity, int duration) {
		MidiMessage noteOn = new MidiMessage();
		// + 1 is to move the data to MIDI channel 2
		int[] onMessage = {MidiMessage.NOTE_ON + 1, pitch, velocity};
		noteOn.set(onMessage, false);
		noteOn.timeMillis = startTime;
		MidiMessage noteOff = new MidiMessage();
		int[] offMessage = {MidiMessage.NOTE_OFF + 1, pitch, 0};
		noteOff.set(offMessage, false);
		noteOff.timeMillis = (int)(startTime + duration * 0.8);
		
		this.midiSegment.add(noteOn);
		this.midiSegment.add(noteOff);
	}
	
}
	
