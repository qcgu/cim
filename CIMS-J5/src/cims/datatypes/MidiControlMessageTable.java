package cims.datatypes;

import java.util.HashMap;
import java.util.TreeMap;
//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class MidiControlMessageTable {
	//private TreeMap<Long, MidiControlMessage> singleControlMap;
	private HashMap<Integer,TreeMap<Long, MidiControlMessage>> allControlsMap;

	public MidiControlMessageTable() {
		//singleControlMap = new TreeMap<Long, MidiControlMessage>();
		allControlsMap = new HashMap<Integer,TreeMap<Long, MidiControlMessage>>();
	}
	
	public void add(MidiControlMessage message) {
		int controlType = message.getControlType();

		if (allControlsMap.containsKey(controlType)) {
			this.allControlsMap.get(controlType).put(message.getOriginalMessage().timeMillis, message);
		} else {
			TreeMap<Long, MidiControlMessage> newTypeControlMap = new TreeMap<Long, MidiControlMessage>();
			newTypeControlMap.put(message.getOriginalMessage().timeMillis, message);
			this.allControlsMap.put(controlType, newTypeControlMap);
		}	
	}
	
	public HashMap<Integer,TreeMap<Long, MidiControlMessage>> getAllControlMessages() {
		return this.allControlsMap;
	}
	
	public TreeMap<Long, MidiControlMessage> getControlMessagesForType(int controlType) {
		return this.allControlsMap.get(controlType);
	}
	
	public MidiControlMessage getControlMessage (int controlType, long timestamp) {
		return this.allControlsMap.get(controlType).get(timestamp);
	}

}
