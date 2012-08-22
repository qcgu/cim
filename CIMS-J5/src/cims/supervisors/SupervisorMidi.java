package cims.supervisors;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_NoteMirror;
import cims.generators.GenerateMidi_Segment;
import cims.datatypes.*;

import java.util.*;

public class SupervisorMidi implements Supervisor {
	
	public static MidiMessage sLastMidiMessage;
	public static ArrayList<MidiMessage> sMidiMessageList;
	public static long sMidiStartTime;
	public static MidiSegment sMidiSegment;
	
	public static MidiStatistics sMidiStats;
	
	// Static properties set by external control
	public static int sSilenceDelay;
	public static int sRepeatInterval;
	
	private boolean mirroring = false; //Flag used by addMidiMessage()
	private CimsMaxIO io;
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private AnalyseMidi_Stats analyser_stats;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_NoteMirror generator_note;
	private GenerateMidi_Loop generator_loop;
	//private PlayMidi player;
	
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		SupervisorMidi.sLastMidiMessage = new MidiMessage();
		SupervisorMidi.sMidiMessageList = new ArrayList<MidiMessage>();
		SupervisorMidi.sMidiStartTime=0;
		
		SupervisorMidi.sSilenceDelay = 250;
		SupervisorMidi.sRepeatInterval = 0;
	
		//Create all necessary instances for complete signal path
		capturer = new CaptureMidi(this);
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		analyser_stats = new AnalyseMidi_Stats(this);
		generator_segment = new GenerateMidi_Segment(this);
		generator_note = new GenerateMidi_NoteMirror(this);
		generator_loop = new GenerateMidi_Loop(generator_segment);
		//player = new PlayMidi(this);
		
	}
	
	public void dataIn() {
		int midiData = this.io.inMidi();
		//this.txtMsg("DataIN: "+midiData);
		capturer.in(midiData);
	}
	
	public void controlIn() {
		//this.txtMsg("Super Key: "+this.io.key()+" Super Value: "+this.io.value());
		if(this.io.key().equals("silenceCue")) {
			SupervisorMidi.sSilenceDelay = this.io.value();
			this.txtMsg("Silence detect time set: "+SupervisorMidi.sSilenceDelay+"ms");
		}
		if(this.io.key().equals("repeatCue")) {
			SupervisorMidi.sRepeatInterval = this.io.value();
			this.txtMsg("Repeat interval set: "+SupervisorMidi.sRepeatInterval+"ms");
		}
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
		SupervisorMidi.sLastMidiMessage = newMidiMessage;
		SupervisorMidi.sMidiMessageList.add(newMidiMessage);
		//this.txtMsg("AMM: "+newMessage.messageNum+"/"+MidiMessage.messagesCount+","+newMessage.timeMillis+","+newMessage.status+","+newMessage.pitch+","+newMessage.velocity+","+newMessage.noteOnOff+","+newMessage.channel+" |");
		if (SupervisorMidi.sMidiStartTime==0) SupervisorMidi.sMidiStartTime = System.currentTimeMillis();
		
		// Let the analyser know that there is new midi to analyse
		if (newMidiMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
			// Note messages
			//this.txtMsg("Calling Analyser - Note");
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			if(analyser_stats.newMidi()) analyser_stats.analyse();
			if (mirroring) generator_note.generate();
		} else {
			// Controller messages - call appropriate analyser
			//this.txtMsg("Calling Analyser - Controller");
			if(analyser_controls.newMidi()) analyser_controls.analyse();
		}		
	}
	
	public synchronized void addMidiSegment(int segmentStart, int segmentEnd) {
		SupervisorMidi.sMidiSegment = new MidiSegment(segmentStart-1, segmentEnd);
		//this.txtMsg("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		chooseNextAction();
	}
	
	private void chooseNextAction() {
		generator_loop.stop();
		int chooseAction = (int)(Math.random() * 4);
		switch (chooseAction) {
			case 0: // repeat
				this.txtMsg("Choosing to REPEAT");
				mirroring = false;
				generator_segment.makeLastSegment();
				generator_segment.generate(); // repeat last segment?
				break;
			case 1: // initiate
				this.txtMsg("Choosing to INITIATE");
				mirroring = false;
				generator_segment.makeInitiateSegment(250);
				generator_loop = new GenerateMidi_Loop(generator_segment);
				generator_loop.setInterval(2000);
				generator_loop.start();
				break;
			case 2: // support
				this.txtMsg("Choosing to SUPPORT");
				mirroring = false;
				generator_segment.makeSupportSegment(250);
				generator_loop = new GenerateMidi_Loop(generator_segment);
				generator_loop.setInterval(250);
				generator_loop.start();
				break;
			case 3: // mirror
				this.txtMsg("Choosing to MIRROR");
				mirroring = true;
				break;
		}
	}
	
	public synchronized MidiSegment getLastMidiSegment() {
		return SupervisorMidi.sMidiSegment;
	}
	
	public synchronized MidiMessage getLastMidiMessage() {
		MidiMessage lastMidiMessage = new MidiMessage();
		lastMidiMessage.copy(SupervisorMidi.sLastMidiMessage);
		return lastMidiMessage;
	}
	
}
