package cims.generators;

import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;

public abstract class GenerateMidi {
	protected SupervisorMidi supervisor;

	
	public GenerateMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
	}
	
	abstract void generate();
			
	abstract void output(MidiMessage midimessage);
}
	