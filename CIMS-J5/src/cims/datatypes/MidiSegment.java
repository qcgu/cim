package cims.datatypes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static cims.supervisors.SupervisorMidi_Globals.sMidiMessageList;

public class MidiSegment {
	private List<MidiMessage> segment;
	private int segmentDuration;
	
	public MidiSegment() { //Empty Segment
		segment = new ArrayList<MidiMessage>();
		segmentDuration = 0;
	}
	
	public MidiSegment(int start, int end) { //Segment as subset of MessageList
		List<MidiMessage> safeList = new CopyOnWriteArrayList<MidiMessage>(sMidiMessageList);
		segment = safeList.subList(start, end);
	}
	
	public void add(MidiMessage message) {
		MidiMessage mess = new MidiMessage();
		mess.pitch = message.pitch;
		mess.velocity = message.velocity;
		mess.channel = message.channel;
		mess.timeMillis = message.timeMillis;
		mess.messageType = message.messageType;
		mess.status = message.status;
		segment.add(mess);
		segmentDuration = (int)message.timeMillis;
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
	
	public MidiMessage getFirstMessage() {
		return (MidiMessage)(segment.get(0));
	}
	
	public int size() {
		return segment.size();
	}
	
	public void zeroTiming() {
		long startTime = getFirstMessage().timeMillis;
		for(int i=0; i< segment.size(); i++) {
			long currTime = ((MidiMessage)(segment.get(i))).timeMillis;
			((MidiMessage)(segment.get(i))).timeMillis = currTime - startTime;
		}
	}
	
	// works for note on and off only at present
	public void setChannel(int channel) {
		for(int i=0; i< segment.size(); i++) {
			int currChan = ((MidiMessage)(segment.get(i))).status;
			//System.out.println("MidiSegment " + currChan);
			if (currChan >= 144 && currChan <= 159) {
				((MidiMessage)(segment.get(i))).status = 144 + channel - 1;
			}
			if (currChan >= 128 && currChan <= 143) {
				((MidiMessage)(segment.get(i))).status = 128 + channel - 1;
			}
		}
	}

}
