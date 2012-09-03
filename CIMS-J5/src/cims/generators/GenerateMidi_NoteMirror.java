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
import cims.supervisors.SupervisorMidi;

public class GenerateMidi_NoteMirror extends GenerateMidi {
	
	private static final int PITCH_SHIFT = 0;
	
	private volatile MidiMessage currentMessage;
	
	public GenerateMidi_NoteMirror(SupervisorMidi supervisor) {
		super(supervisor);
		this.currentMessage = new MidiMessage();
	}
	
	public void generate() {
		this.currentMessage = this.supervisor.getLastMidiMessage();
		//this.supervisor.txtMsg("MIRROR: "+ this.currentMessage.pitch);
		this.transform(PITCH_SHIFT, -12);
		this.output(this.currentMessage);
	}
	
	public void output(MidiMessage midimessage) {
		int[] message = {midimessage.status,midimessage.pitch,midimessage.velocity};
		this.supervisor.dataOut(message);
	}
	
	public synchronized void transform(int transformType, int transformValue) {
		switch(transformType) {
		case PITCH_SHIFT:
			int pitch = this.currentMessage.pitch;
			pitch = pitch+transformValue;
			if (pitch>127) pitch = 127; 
			if (pitch<0) pitch = 0;
			this.currentMessage.pitch = pitch;
			break;
		default:
			// do nothing
			break;
		}		
	}
	
	
}
	