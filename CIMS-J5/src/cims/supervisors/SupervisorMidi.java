package cims.supervisors;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.generators.GenerateMidi_SegmentRepeat;
import cims.datatypes.*;

import java.util.concurrent.*;
import java.util.*;

public class SupervisorMidi implements Supervisor {
	
	//public static MidiMessage sMidiMessage;
	public static ArrayList<MidiMessage> sMidiMessageList;
	public static long sMidiStartTime;
	public static List<MidiMessage> sMidiSegment;
	public static int sSilenceDelay;
	
	private CimsMaxIO io;
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private GenerateMidi_SegmentRepeat generator_segment;
	//private PlayMidi player;
	
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		SupervisorMidi.sMidiMessageList = new ArrayList<MidiMessage>();
		SupervisorMidi.sMidiStartTime=0;
	
		//Create all necessary instances for complete signal path
		capturer = new CaptureMidi(this);
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		generator_segment = new GenerateMidi_SegmentRepeat(this);
		//player = new PlayMidi(this);
		
	}
	
	public void dataIn() {
		int midiData = this.io.inMidi();
		//this.txtMsg("DataIN: "+midiData);
		capturer.in(midiData);
	}
	
	public void dataOut(int[] message) {
		//this.txtMsg("dataOut: "+message[0]+"|"+message[1]+"|"+message[2]);
		this.io.outMidi(message);
		
	}
	
	public void txtMsg(String msg) {
		this.io.textOut(msg);
	}
	
	public void addMidiMessage(MidiMessage newMessage) {
		MidiMessage newMidiMessage = new MidiMessage();
		newMidiMessage.copy(newMessage);
		SupervisorMidi.sMidiMessageList.add(newMidiMessage);
		//this.txtMsg("AMM: "+newMessage.messageNum+"/"+MidiMessage.messagesCount+","+newMessage.timeMillis+","+newMessage.status+","+newMessage.pitch+","+newMessage.velocity+","+newMessage.noteOnOff+","+newMessage.channel+" |");
		if (SupervisorMidi.sMidiStartTime==0) SupervisorMidi.sMidiStartTime = System.currentTimeMillis();
		
		// Let the analyser know that there is new midi to analyse
		if (newMidiMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
			// Note messages
			//this.txtMsg("Calling Analyser - Note");
			if(analyser_silence.newMidi()) analyser_silence.analyse();
		} else {
			// Controller messages - call appropriate analyser
			//this.txtMsg("Calling Analyser - Controller");
			if(analyser_controls.newMidi()) analyser_controls.analyse();
		}
		
			
	}
	
	public synchronized void addMidiSegment(int segmentStart, int segmentEnd) {
		List<MidiMessage> safeList = new CopyOnWriteArrayList<MidiMessage>(SupervisorMidi.sMidiMessageList);
		SupervisorMidi.sMidiSegment = safeList.subList(segmentStart-1, segmentEnd);
		//this.txtMsg("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		generator_segment.generate();
		/* Print out segment pitches
		Iterator<MidiMessage> segmentIterator = SupervisorMidi.sMidiSegment.iterator();
		while (segmentIterator.hasNext()) {
			SupervisorMidi.sDebug = SupervisorMidi.sDebug + "P: "+ segmentIterator.next().pitch +"\n";
		} */
	}
	
	public synchronized List<MidiMessage> getLastMidiSegment() {
		List<MidiMessage> lastMidiSegment = new CopyOnWriteArrayList<MidiMessage>(SupervisorMidi.sMidiSegment);
		return lastMidiSegment;
	}
	
}
