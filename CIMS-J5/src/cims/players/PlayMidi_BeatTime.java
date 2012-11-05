package cims.players;

import static cims.supervisors.SupervisorMidi_Globals.LOGGER;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class PlayMidi_BeatTime {
	private SupervisorMidi supervisor;
	private volatile MidiMessage currentMessage;
	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	
	public PlayMidi_BeatTime(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.currentMessage = new MidiMessage();
	}
	
	public void beatTimeIn() {
		
	}
	
	public void add(MidiMessage message) {
		
	}
	
	
	public void play() {
		LOGGER.info("generate()");
		midiQueue = new OutputQueue(this);
		midiQueue.addSegment(midiSegment);
		midiQueue.play();
	}
	
	public void stop() {
		midiQueue.cancel();
	}
	
	public void output(MidiMessage midimessage) {
		this.supervisor.dataOut(midimessage.rawMessage);
	}
	

}
