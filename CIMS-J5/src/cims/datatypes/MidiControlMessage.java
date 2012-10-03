package cims.datatypes;
import cims.datatypes.MidiMessage;
//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class MidiControlMessage {
	private int controlType;
	private MidiMessage originalMessage;
	
	public MidiControlMessage() {
		
	}
	
	public MidiControlMessage(MidiMessage message) {
		this.addMessage(message);
	}
	
	public void addMessage(MidiMessage message) {
		this.originalMessage = message;
		this.controlType = message.messageType;
	}
	
	public int getControlType() {
		return this.controlType;
	}
	
	public MidiMessage getOriginalMessage() {
		return this.originalMessage;
	}

}
