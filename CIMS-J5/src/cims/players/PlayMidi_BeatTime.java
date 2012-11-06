package cims.players;

import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

import java.util.TreeMap;

import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class PlayMidi_BeatTime {
	private SupervisorMidi supervisor;
	private TreeMap<Double,MidiMessage> beatMessages;
	private volatile MidiMessage currentMessage;
	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	
	public PlayMidi_BeatTime(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.currentMessage = new MidiMessage();
	}
	
	public void beatTimeIn() {
		//Check bar/beat
		//See what messages are in the queue to be played
		//Send them to the output queue
		
	}
	
	public void add(MidiMessage message) {
		//Create a key for this midi message
		// KEY = 1000,000 + (BarNumber x 100) + Beat + (SubBeat x 0.001)
		//Double key = m
		//Add the message to the treemap of upcoming midimessages (can come in any order, will always be sorted by bar/beat
		
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
