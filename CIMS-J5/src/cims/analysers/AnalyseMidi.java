package cims.analysers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;

import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;

public abstract class AnalyseMidi {

	protected SupervisorMidi supervisor;
	protected MidiMessage current_message;
	public static final Logger LOGGER = Logger.getLogger(AnalyseMidi.class);
	
	public AnalyseMidi() {
		LOGGER.setLevel(Level.INFO);
	}
	
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
