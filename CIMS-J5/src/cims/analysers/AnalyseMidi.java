package cims.analysers;

//import java.util.*;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;

import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;

public abstract class AnalyseMidi {

	protected SupervisorMidi supervisor;
	protected MidiMessage current_message;
	
	
	public AnalyseMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.current_message = new MidiMessage();
	}
	
	public boolean newMidi() {
		boolean returnValue = false;
		if (sMidiMessageList.size() > 0) {
			this.current_message = this.supervisor.getLastMidiMessage();
			returnValue = true;
		} 
		return returnValue;
	}
	abstract void analyse();

}
