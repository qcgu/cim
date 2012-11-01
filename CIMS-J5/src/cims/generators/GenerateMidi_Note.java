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
import static cims.supervisors.SupervisorMidi_Globals.sCurrentChord;
public class GenerateMidi_Note extends GenerateMidi {
	
	public static final int PITCH_SHIFT = 0;
	public static final int LOWER_TRIADIC = 1;
	
	private volatile MidiMessage currentMessage;
	
	public GenerateMidi_Note(SupervisorMidi supervisor) {
		super(supervisor);
		this.currentMessage = new MidiMessage();
	}
	
	public void generate() {
		this.currentMessage = this.supervisor.getLastMidiMessage();
		//this.supervisor.txtMsg("MIRROR: "+ this.currentMessage.pitch);
		this.transform(PITCH_SHIFT, -12);
		this.output();
	}
	
	public void setMessage(MidiMessage newMessage) {
		this.currentMessage = newMessage;
	}
	
	public void output(MidiMessage newMessage) {
		this.setMessage(newMessage);
		this.output();
	}
	
	public void output() {
		int[] message = {this.currentMessage.status,this.currentMessage.pitch,this.currentMessage.velocity};
		this.supervisor.dataOut(message);
	}
	
	// this method should likely be moved to GenerateMidi_Note_02 class
	public synchronized void transform(int transformType, int transformValue) {
		switch(transformType) {
		case PITCH_SHIFT:
			int pitch = this.currentMessage.pitch;
			pitch = pitch+transformValue;
			if (pitch>127) pitch = 127; 
			if (pitch<0) pitch = 0;
			this.currentMessage.pitch = pitch;
			break;
		case LOWER_TRIADIC: //transform type argument is ignored
			pitch = this.currentMessage.pitch;
			int newPitch = pitch - 1;
			while (!isInCurrentChord(newPitch)) {
				newPitch--;
			}
			System.out.println("lower triadic method " + pitch + " " + newPitch);
			this.currentMessage.pitch = newPitch;
		default:
			// do nothing
			break;
		}		
	}
	
	// this method probably should go into a new utilities class call PitchClassUtil
	public boolean isInCurrentChord(int p) {
		boolean result = false;
		for (int i=0; i < sCurrentChord.length; i++) {
			if (p%12 == sCurrentChord[i]) result = true;
		}
		return result;
	}
}
	