/**
 * CIMS - CaptureOutput - Keep track of MIDI messages generated by the agent.
 * 
 */

package cims.capturers;

import java.util.ArrayList;
import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class CaptureOutput {

	private ArrayList<Integer> onList = new ArrayList<Integer>();
	private SupervisorMidi supervisor;
	
	public CaptureOutput(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
	}
	
	public void in(int[] midiData) {
		new MidiMessage();
		
		if (MidiMessage.isNoteOn(midiData[0])) {
			//LOGGER.info("Message Type: ON " + message.pitch);
			//onList.add(midiData[1]);
		}
		
		if (MidiMessage.isNoteOff(midiData[0])) {
			//this.supervisor.txtMsg("Message Type: OFF");
			//deleteMatchingNoteOn(midiData[1]);
		}
	}
	
	private void deleteMatchingNoteOn(int offPitch) {
		int size = onList.size();
		for (int i=0; i<size; i++) {
			if(offPitch == onList.get(i).intValue()) {
				onList.remove(i);
				//this.supervisor.txtMsg("Removed message from onList for pitch " + mess.pitch);
				i = size;
			}		
		}
	}
	
	// return an array of the pitches currently turned on but not yet off
	public int[] getOnPitches() {
		int size = onList.size();
		int[] pitches = new int[size];
		for (int i=0; i<size; i++) {
			pitches[i] = onList.get(i);
		}
		return pitches;
	}
	
	public void allNotesOff() {
		//int[] pitches = getOnPitches();
		for(int i=0; i<onList.size(); i++) {
			//supervisor.txtMsg("pitch  in on pitches " + pitches[i]);
			supervisor.dataOut(new int[] {128, onList.get(i), 0});
		}
	}
}

