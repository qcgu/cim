package cims.datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//import cims.analysers.AnalyseMidi;


import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;
//import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class MidiSegment {
	private List<MidiMessage> segment;
	private int segmentDuration;
	private Class<?> creatorClass;
	
	public static Logger LOGGER = Logger.getLogger(MidiSegment.class);

	public MidiSegment() { //Empty Segment
		segment = new ArrayList<MidiMessage>();
		segmentDuration = 0;
		LOGGER.setLevel(Level.INFO);
	}
	
	public MidiSegment(int start, int end) { //Segment as subset of MessageList
		List<MidiMessage> safeList = new CopyOnWriteArrayList<MidiMessage>(sMidiMessageList);
		segment = safeList.subList(start, end);
	}
	
	public MidiSegment(List<MidiMessage> listOfMessages) {
		segment = listOfMessages;
	}

	public Class<?> getCreatorClass() {
		return creatorClass;
	}

	public void setCreatorClass(Class<?> creatorClass) {
		this.creatorClass = creatorClass;
	}
	
	public void add(MidiMessage message) {
		MidiMessage mess = new MidiMessage();
		mess.copy(message);
		mess.pitch = message.pitch;
		segment.add(mess);
		segmentDuration = (int) (message.timeMillis - segment.get(0).timeMillis);
		if (segmentDuration<1) {
			LOGGER.debug("Segment Duration too short: "+segmentDuration+"ms");
			segmentDuration = 0;
		} else {
			LOGGER.debug("SEGMENT DURATION: "+segmentDuration+"ms");
		}
	}
	
	public int duration() {
		return segmentDuration; // Length of segment in milliseconds
	}
	
	private int calcDuration() {
		segmentDuration = (int) (this.lastMessage().timeMillis - this.firstMessage().timeMillis);
		return segmentDuration;
	}
	
	public List<MidiMessage> asList() {
		List<MidiMessage> segmentCopy = new CopyOnWriteArrayList<MidiMessage>(this.segment);
		return segmentCopy;
	}
	
	public MidiSegment copy() {
		MidiSegment newSegment = new MidiSegment();
		newSegment.segment = this.asList();
		newSegment.calcDuration();
		return newSegment;
	}
	
	public MidiMessage lastMessage() {
		return (MidiMessage)(segment.get(segment.size()-1));
	}
	
	public MidiMessage firstMessage() {
		return (MidiMessage)(segment.get(0));
	}
	
	public int size() {
		return segment.size();
	}
	
	public boolean isEmpty() {
		return segment.isEmpty();
	}
	
	public MidiSegment zeroTiming() {
		MidiSegment newSegment = this.copy();
		long startTime = this.firstMessage().timeMillis;
		Iterator<MidiMessage> segmentIterator = newSegment.asList().iterator();
		while(segmentIterator.hasNext()) {
			MidiMessage message = segmentIterator.next();
			message.timeMillis -= startTime;
		}
		return newSegment;
	}
	
	public void setChannel(int channel) {
		Iterator<MidiMessage> segmentIterator = this.asList().iterator();
		while(segmentIterator.hasNext()) {
			segmentIterator.next().changeChannel(channel);
		}
	}
	
	

}
