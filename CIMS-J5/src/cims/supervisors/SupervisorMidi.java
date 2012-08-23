package cims.supervisors;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.capturers.CaptureOutput;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
import cims.generators.GenerateMidi_Loop;
import cims.generators.GenerateMidi_NoteMirror;
import cims.generators.GenerateMidi_Segment;
import cims.utilities.Test;
import cims.datatypes.*;

import java.util.*;

public class SupervisorMidi implements Supervisor {
	
	public static MidiMessage sLastMidiMessage;
	public static ArrayList<MidiMessage> sMidiMessageList;
	public static long sMidiStartTime;
	public static MidiSegment sMidiSegment;
	
	public static MidiStatistics sMidiStats;
	
	// Static properties set by external control
	public static int sSilenceDelay = 250;
	public static int sRepeatInterval = 0;
	public static boolean sMetronome = false;
	public static int sCurrentBeat = 0;
	public static long[] sBeatList={4,0,0,0,0};
	public static int sTimeBetweenBeats = 0;
	public static int sNextPlay = 0;
	
	private int currentAction = -1;
	private boolean mirroring = false; //Flag used by addMidiMessage()
	private boolean initiating = false;
	private CimsMaxIO io;
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private AnalyseMidi_Stats analyser_stats;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_NoteMirror generator_note;
	private GenerateMidi_Loop generator_loop;
	private Test tester;
	
	//private PlayMidi player;
	private CaptureOutput outputTracker;
	
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		SupervisorMidi.sLastMidiMessage = new MidiMessage();
		SupervisorMidi.sMidiMessageList = new ArrayList<MidiMessage>();
		SupervisorMidi.sMidiStartTime=0;
		
		//Create all necessary instances for complete signal path
		capturer = new CaptureMidi(this);
		outputTracker = new CaptureOutput(this);
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		analyser_stats = new AnalyseMidi_Stats(this);
		generator_segment = new GenerateMidi_Segment(this);
		generator_note = new GenerateMidi_NoteMirror(this);
		generator_loop = new GenerateMidi_Loop(generator_segment);
		tester = new Test(this);
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
		if(this.io.key().equals("metronome")) {
			if(this.io.value()==1) {
				SupervisorMidi.sMetronome = true;
			} else {
				SupervisorMidi.sMetronome = false;
			}
		}
		if(this.io.key().equals("beat") && SupervisorMidi.sMetronome) {
			int beat = this.io.value();
			SupervisorMidi.sCurrentBeat = beat;
			SupervisorMidi.sBeatList[beat] = System.currentTimeMillis();
			int prevBeat = beat-1;
			if (prevBeat<1) prevBeat = (int) SupervisorMidi.sBeatList[0];
			Long timeBetween = (SupervisorMidi.sBeatList[beat] - SupervisorMidi.sBeatList[prevBeat]);
			if (timeBetween>4000) timeBetween = (long) 500; // default 120BPM
			SupervisorMidi.sTimeBetweenBeats = timeBetween.intValue();
			if (SupervisorMidi.sTimeBetweenBeats<1) SupervisorMidi.sTimeBetweenBeats = 0;
			//this.txtMsg("Time between beats: "+SupervisorMidi.sTimeBetweenBeats);
		}
		if(this.io.key().equals("test")) {
			if(this.io.value()==1) {
			this.txtMsg("RUNNING TESTS");
			this.runTests();
			}
		}
		if(this.io.key().equals("nextPlay")) {
			SupervisorMidi.sNextPlay = this.io.value();
		}
	}
	
	public void dataOut(int[] message) {
		//this.txtMsg("dataOut: "+message[0]+"|"+message[1]+"|"+message[2]);
		this.io.outMidi(message);
		// output capture to stop stuck notes
		outputTracker.in(message);
	}
	
	public void txtMsg(String msg) {
		this.io.textOut(msg);
	}
	
	public void addMidiMessage(MidiMessage newMessage) {
		// start with supporting role
		if (currentAction == -1) {
			this.txtMsg("Choosing to SUPPORT");
			currentAction = 2;
			generator_segment.makeSupportSegment(250);
			generator_loop = new GenerateMidi_Loop(generator_segment);
			generator_loop.setInterval(250);
			generator_loop.start();
		}
		// stop generator if in mirror mode
		if (mirroring) {
			generator_loop.stop();
			generator_segment.stop();
			this.txtMsg("Turning off notes");
			turnOffAgentNotes(); // these two calls need to go together to avoid stuck notes
			//mirroring = false;
			//initiating = false;
			// play mirrored note
			this.io.outMidi(new int[] {newMessage.status, newMessage.pitch, newMessage.velocity});
		}
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
		currentAction = (int)(Math.random() * 4);
		switch (currentAction) {
			case 0: // repeat
				this.txtMsg("Choosing to REPEAT");
				mirroring = false;
				//initiating = false;
				generator_loop.stop();
				generator_segment.makeLastSegment();
				generator_segment.generate(); // repeat last segment?
				break;
			case 1: // initiate
				this.txtMsg("Choosing to INITIATE");
				mirroring = false;
				//initiating = true;
				generator_loop.stop(); // stop support
				generator_segment.stop(); // stop prev initiate
				turnOffAgentNotes();
				generator_segment.makeInitiateSegment(250);
				generator_loop = new GenerateMidi_Loop(generator_segment);
				generator_loop.setInterval(4000);
				generator_loop.start();
				break;
			case 2: // support
				this.txtMsg("Choosing to SUPPORT");
				mirroring = false;
				//initiating = false;
				generator_loop.stop();
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
	private void turnOffAgentNotes() {
		int[] pitches = outputTracker.getOnPitches();
		for(int i=0; i<pitches.length; i++) {
			dataOut(new int[] {128, pitches[i], 0});
		}
	}
	public void runTests() {
		GenerateMidi_Segment gm_segment = tester.generateMidi_Segment(false);
		gm_segment.generate(SupervisorMidi.sNextPlay); // 0 immediate, 1 next beat, 2 next bar
	}
}
