package cims.players;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import static cims.supervisors.SupervisorMidi_Globals.sCurrentBeatTime;

import java.util.TreeMap;

import cims.datatypes.BeatTime;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.supervisors.SupervisorMidi;
import cims.utilities.OutputQueue;


public class PlayMidi_BeatTime {
	private SupervisorMidi supervisor;
	private TreeMap<Double,MidiMessage> beatMessages;
	@SuppressWarnings("unused")
	private volatile MidiMessage currentMessage;
	protected volatile MidiSegment midiSegment;
	protected volatile OutputQueue midiQueue;
	
	public static final Logger LOGGER = Logger.getLogger(PlayMidi_BeatTime.class);
	
	public PlayMidi_BeatTime(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.beatMessages = new TreeMap<Double,MidiMessage>();
		this.currentMessage = new MidiMessage();
		this.midiSegment = new MidiSegment();
		LOGGER.setLevel(Level.INFO);
		//this.makeTest();
	}
	
	
	
	public void beatTimeIn() {
		boolean somethingToPlay = true;
		MidiMessage messageToPlay = new MidiMessage();
		//Check bar/beat
		//@SuppressWarnings("unused")
		BeatTime currentBT = sCurrentBeatTime;
		//Create a key range to look for
		Double currentKey = currentBT.getKey();
		Double startKey = currentKey - Double.valueOf(0.15); // (1/32 of a beat)
		Double endKey = currentKey + Double.valueOf(0.15);	
		LOGGER.debug("key: "+currentKey+" s: "+startKey+" e: "+endKey);
		//See what messages are in the queue to be played
		if(!(this.beatMessages==null)) {
		while(somethingToPlay) {
			if(this.beatMessages.size()==0) {
				LOGGER.debug("No first entry");
				somethingToPlay = false;
			} else {
				currentKey = this.beatMessages.firstKey();
				if(currentKey < startKey) { //Time has gone, remove it
					LOGGER.debug("Time has passed - remove it");
					this.beatMessages.remove(this.beatMessages.firstKey());
				} else if (currentKey <= endKey) {
					//play this one
					LOGGER.debug("Message to play");
					messageToPlay = this.beatMessages.get(currentKey);
					this.midiSegment.add(messageToPlay);
					this.play();
					//then remove it
					this.beatMessages.remove(this.beatMessages.firstKey());
				} else {
					somethingToPlay = false;
				}
			}
			
		}
		}
	}
	
	public Double add(MidiMessage message) {
		Double key = (double) 0;
		if(message==null) {
			LOGGER.error("No message!");
		} else {
		BeatTime messageBeatTime = new BeatTime();
		messageBeatTime = message.beatTime;
		int barNum = messageBeatTime.getValueFor("bar");
		int beat = messageBeatTime.getValueFor("beat");
		int subBeat = messageBeatTime.getValueFor("unit");
		//Create a key for this midi message
		key = messageBeatTime.getKey();
		LOGGER.debug("New BeatTime Message: "+key+"> "+barNum+"|"+beat+"|"+subBeat);
		//Add the message to the treemap of upcoming midimessages (can come in any order, will always be sorted by bar/beat
		beatMessages.put(key, message);
		}
		return key;
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
		midiQueue.addSegment(this.midiSegment);
		midiQueue.play();
	}
	
	public void stop() {
		midiQueue.cancel();
	}
	
	public void output(MidiMessage midimessage) {
		this.supervisor.midiOut(midimessage.rawMessage);
	}
	
	public void makeTest() {
		MidiMessage m1 = new MidiMessage();
		int[] d1 = {144,60,120};
		m1.set(d1,false);
		Integer[] b1 = {3,1,0,0,120,4,4,0,0}; // {"bar","beat","unit","ppq","tempo","beatsPerBar","beatType","state","ticks"};
		m1.beatTime = new BeatTime(b1);
		this.add(m1);
		MidiMessage m2 = new MidiMessage();
		int[] d2 = {128,60,120};
		m2.set(d2,false);
		Integer[] b2 = {3,4,460,0,120,4,4,0,0};
		m2.beatTime = new BeatTime(b2);
		this.add(m2);
	}

}
