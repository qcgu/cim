/**
 * CIMS - GenerateMIDI - Make decisions based on Analysis and raw data and generate MIDI for playing.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 * @version 120725
 */

package cims.generators;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class GenerateMidi_Segment extends GenerateMidi {

	private volatile MidiSegment midiSegment;
	private volatile OutputQueue midiQueue;
	
	public GenerateMidi_Segment(SupervisorMidi supervisor) {
		super(supervisor);
		midiQueue = new OutputQueue(this);
	}
	
	public void generate() {
		midiQueue.addSegment(midiSegment);
		midiQueue.play();
	}
	
	public void output(MidiMessage midimessage) {
		int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		this.supervisor.dataOut(message);
	}
		
	public synchronized void makeLastSegment () {
		// Play back the last segment
		midiSegment = supervisor.getLastMidiSegment();	
	}
	
	public synchronized void makeNewSegment(int duration) {
		//Test Comment
		midiSegment = new MidiSegment();
		
	public synchronized void makeInitiateSegment(int duration) {
		this.midiSegment = new ArrayList<MidiMessage>();
		int[] pitches = {72, 74, 76, 79, 81, 84};
		addNote(i*duration, pitches[0], (int)(Math.random() * 30) + 80, duration);
		for(int i=0; i<7; i++) {
			addNote(i*duration, pitches[(int)(Math.random() * pitches.length)], (int)(Math.random() * 30) + 80, duration);
		}
	}
	
	public synchronized void makeSupportSegment(int duration) {
		this.midiSegment = new ArrayList<MidiMessage>();
		addNote(0, 48, (int)(Math.random() * 30) + 80, duration);
	}
	
	private void addNote(int startTime, int pitch, int velocity, int duration) {
		MidiMessage noteOn = new MidiMessage();
		int[] onMessage = {MidiMessage.NOTE_ON,pitch,velocity};
		noteOn.set(onMessage, false);
		noteOn.timeMillis = startTime;

		MidiMessage noteOff = new MidiMessage();
		int[] offMessage = {MidiMessage.NOTE_OFF,pitch,0};
		noteOff.set(offMessage, false);
		noteOff.timeMillis = (int)(startTime + duration * 0.8);
		this.midiSegment.add(noteOn);
		this.midiSegment.add(noteOff);
	}
	
	
}
	