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
import static cims.supervisors.SupervisorMidi_Globals.sPitchClassSet;
public class GenerateMidi_Note extends GenerateMidi {
	
	public static final int PITCH_SHIFT = 0;
	public static final int LOWER_TRIADIC = 1;
	public static final int PARALLEL_INTERVAL = 2;
	
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
			//System.out.println("lower triadic method " + pitch + " " + newPitch);
			this.currentMessage.pitch = newPitch;
			break;
		//play in 3rds 6ths etc above the performed note, transform value is number of scale degree steps (i.e., 3 = 3rd)
		case PARALLEL_INTERVAL: 
			pitch = this.currentMessage.pitch;
			int currScaleDegree = getScaleDegree(pitch);
			this.currentMessage.pitch = pitchAboveFromScaleDegree(pitch, (currScaleDegree + transformValue - 1)%sPitchClassSet.length);
			System.out.println("Parallel " + currScaleDegree + " " + pitch +  " " + this.currentMessage.pitch);
			break;
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
	
	public int getScaleDegree(int p) {
		int result = 0;
		for (int i=0; i < sPitchClassSet.length; i++) {
			if (p%12 == sPitchClassSet[i]) result = i;
		}
		return result;
	}
	
	public int pitchAboveFromScaleDegree(int p, int degree) {
		int newPitch = sPitchClassSet[degree];
		while (newPitch < p) {
			newPitch +=12;
		}
		return newPitch;
	}
}
	