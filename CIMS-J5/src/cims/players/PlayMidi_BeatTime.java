package cims.players;

//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;
import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeatTime;

import java.util.TreeMap;
import java.util.logging.Logger;

import cims.datatypes.BeatTime;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
//import cims.generators.GenerateMidi;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;

public class PlayMidi_BeatTime {
	private SupervisorMidi supervisor;
	private TreeMap<Double,MidiMessage> beatMessages;
	@SuppressWarnings("unused")
	private volatile MidiMessage currentMessage;
	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	
	public static final Logger LOGGER = Logger.getLogger(PlayMidi_BeatTime.class.getName());
	
	public PlayMidi_BeatTime(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.currentMessage = new MidiMessage();
	}
	
	public void beatTimeIn() {
		//Check bar/beat
		@SuppressWarnings("unused")
		BeatTime currentBT = sCurrentBeatTime;
		//See what messages are in the queue to be played
		//Send them to the output queue
		
	}
	
	public Double add(MidiMessage message) {
		Double key = (double) 0;
		BeatTime messageBeatTime = message.beatTime;
		int barNum = messageBeatTime.getValueFor("bar");
		int beat = messageBeatTime.getValueFor("beat");
		int subBeat = messageBeatTime.getValueFor("unit");
		//Create a key for this midi message
		key = this.makeKey(barNum, beat, subBeat);
		System.out.println("New BeatTime Message: "+key+"> "+barNum+"|"+beat+"|"+subBeat);
		//Add the message to the treemap of upcoming midimessages (can come in any order, will always be sorted by bar/beat
		beatMessages.put(key, message);
		return key;
	}
	
	private Double makeKey(int barNum, int beat, int subBeat) {
		return 1000000 + (barNum * 100) + beat + (subBeat * 0.001);
	}
	
	public Double schedule(MidiMessage message, Integer bar, Integer beat, Integer unit) {
		Double key = (double) 0;
		return key;
	}
	
	public void remove(Double key) {
		//Remove the midimessage with key from the treemap
	}
	
	public MidiMessage getMessage(Double key) {
		MidiMessage message = new MidiMessage();
		// Get the midiMessage from the treemap that matches key
		return message;
	}
	public void replace(Double key,MidiMessage newMessage) {
		//Replace the midimessage from the treemap that matches key with newMessage
		//
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
		this.supervisor.midiOut(midimessage.rawMessage);
	}
	

}
