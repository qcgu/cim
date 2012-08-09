/**
 * CIMS - GenerateMIDI - Make decisions based on Analysis and raw data and generate MIDI for playing.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 * @version 120725
 */

package cims.generators;

import java.util.List;

import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class GenerateMidi_SegmentRepeat extends GenerateMidi {

	private volatile List<MidiMessage> midiSegment;
	private volatile OutputQueue midiQueue;
	
	public GenerateMidi_SegmentRepeat(SupervisorMidi supervisor) {
		super(supervisor);
		midiQueue = new OutputQueue(this);
	}
	
	public void generate() {
		this.makeSegment();
	}
	
	public void output(MidiMessage midimessage) {
		int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		this.supervisor.dataOut(message);
	}
	
	public synchronized void makeSegment() {
					// Play back the last segment
					midiSegment = supervisor.getLastMidiSegment();
					midiQueue.addSegment(midiSegment);
					midiQueue.play();
	}
	
	
}
	