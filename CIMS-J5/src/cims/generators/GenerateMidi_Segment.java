/**
 * CIMS - GenerateMIDI - Make decisions based on Analysis and raw data and generate MIDI for playing.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 * @version 120725
 */

package cims.generators;

import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class GenerateMidi_Segment extends GenerateMidi {

	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	private SupervisorMidi supervisor;
	
	public GenerateMidi_Segment(SupervisorMidi supervisor) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
	}
	
	public GenerateMidi_Segment(SupervisorMidi supervisor, MidiSegment segment) {
		super(supervisor);
		this.supervisor = supervisor;
		midiQueue = new OutputQueue(this);
		this.midiSegment = segment;
	}
	
	public void generate() {
		LOGGER.info("generate()");
		midiQueue = new OutputQueue(this);
		midiQueue.addSegment(midiSegment);
		midiQueue.play();
	}
	
	public void stop() {
		midiQueue.cancel();
	}
	
	public void generate(int start) {
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
		//int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		//LOGGER.warning("OUTPUT STATUS: "+ midimessage.status);
		//LOGGER.warning("OUTPUT RAW MESSAGE: "+ midimessage.rawMessage[0]);
		this.supervisor.dataOut(midimessage.rawMessage);
		//supervisor.txtMsg("TC: "+Thread.activeCount());
	}
		
	public synchronized void makeLastSegment () {
		// Play back the last segment
		//midiSegment = supervisor.getLastMidiSegment();
		midiSegment = supervisor.getLastMidiSegment().zeroTiming();
		midiSegment.setChannel(2); // channel is in the parochial range of 1-16
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
	
