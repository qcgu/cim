package cims.analysers;

//import java.util.*;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;


public abstract class AnalyseMidi {

	protected SupervisorMidi supervisor;
	protected MidiMessage current_message;
	
	
	public AnalyseMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.current_message = new MidiMessage();
	}
	
	public boolean newMidi() {
		boolean returnValue = false;
			if (SupervisorMidi.sMidiMessageList.size() > 0) {
				this.current_message.copy(SupervisorMidi.sMidiMessageList.get(MidiMessage.sMessagesCount - 1));
				returnValue = true;
			} 
			return returnValue;
	}
	abstract void analyse();

}
