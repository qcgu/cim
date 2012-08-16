/**
 * CIMS - GenerateMIDI - Make decisions based on Analysis and raw data and generate MIDI for playing.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 * @version 120725
 */

package cims.generators;

import java.util.ArrayList;
import java.util.List;

import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class GenerateMidi_Segment extends GenerateMidi {

	private volatile List<MidiMessage> midiSegment;
	private volatile OutputQueue midiQueue;
	
	public GenerateMidi_Segment(SupervisorMidi supervisor) {
		super(supervisor);
		midiQueue = new OutputQueue(this);
	}
	
	public void generate() {
		midiQueue.addSegment(this.midiSegment);
		midiQueue.play();
	}
	
	public void output(MidiMessage midimessage) {
		int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		this.supervisor.dataOut(message);
	}
		
	public synchronized void makeLastSegment () {
		// Play back the last segment
		this.midiSegment = supervisor.getLastMidiSegment();	
	}
	
	public synchronized void makeNewSegment(int duration) {
		midiSegment = new ArrayList<MidiMessage>();
		MidiMessage noteOn = new MidiMessage();
		int[] onMessage = {MidiMessage.NOTE_ON,64,100};
		noteOn.set(onMessage);
		noteOn.timeMillis = 0;
		MidiMessage noteOff = new MidiMessage();
		int[] offMessage = {MidiMessage.NOTE_OFF,64,0};
		noteOff.set(offMessage);
		noteOff.timeMillis = duration;
		this.midiSegment.add(noteOn);
		this.midiSegment.add(noteOff);
	}
	
	
}
	