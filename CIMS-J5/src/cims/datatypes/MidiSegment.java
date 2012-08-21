package cims.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import cims.supervisors.SupervisorMidi;

public class MidiSegment {
	private List<MidiMessage> segment;
	private int segmentDuration;
	
	public MidiSegment() { //Empty Segment
		segment = new ArrayList<MidiMessage>();
		segmentDuration = 0;
	}
	
	public MidiSegment(int start, int end) { //Segment as subset of MessageList
		List<MidiMessage> safeList = new CopyOnWriteArrayList<MidiMessage>(SupervisorMidi.sMidiMessageList);
		segment = safeList.subList(start, end);
	}
	
	public void add(MidiMessage message) {
		int duration = (int)message.timeMillis;
		segment.add(message);
		segmentDuration =+ duration;
	}
	
	public int duration() {
		return segmentDuration; // Length of segment in milliseconds
	}
	
	public List<MidiMessage> asList() {
		List<MidiMessage> segmentCopy = new CopyOnWriteArrayList<MidiMessage>(this.segment);
		return segmentCopy;
	}
	
	public MidiMessage lastMessage() {
		MidiMessage message = new MidiMessage();
		return message;
	}

}
