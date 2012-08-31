package cims.supervisors;

import java.util.*;

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
import cims.deciders.DecideMidi_01;
import cims.deciders.DecideMidi_UserControl;

import static cims.supervisors.SupervisorMidi_Globals.*;


public class SupervisorMidi implements Supervisor {
	
	private CimsMaxIO io;
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	public AnalyseMidi_Stats analyser_stats;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_NoteMirror generator_note;
	private DecideMidi_UserControl decider_userControl;
	private DecideMidi_01 decider_01;
	private Test tester;
	private MidiSegment sMidiSegment;
	
	private static final int SEGMENT_TESTS = 0;
	//private static final int MESSAGE_TESTS = 1;
	//private PlayMidi player;
	private CaptureOutput outputTracker;
	
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		sLastMidiMessage = new MidiMessage();
		sMidiMessageList = new ArrayList<MidiMessage>();
		sMidiStartTime=0;
		sMidiSegment = new MidiSegment();
		
		//Capture input and output midi
		capturer = new CaptureMidi(this);
		outputTracker = new CaptureOutput(this);
		//Analyse
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		analyser_stats = new AnalyseMidi_Stats(this);
		//Decide what to do
		decider_userControl = new DecideMidi_UserControl(this);
		decider_01 = new DecideMidi_01(this);
		//Generate output
		generator_segment = new GenerateMidi_Segment(this);
		decider_01.addGenerator(generator_segment);
		generator_note = new GenerateMidi_NoteMirror(this);
		
		//Test
		tester = new Test(this);
	}
	
	public void dataIn() {
		int midiData = this.io.inMidi();
		//this.txtMsg("DataIN: "+midiData);
		capturer.in(midiData);
	}
	
	public void controlIn() {
		decider_userControl.input(this.io.key(), this.io.value());	
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
		if(sTestMode) {
			// Run Tests
		} else {
			// Let the analyser know that there is new midi to analyse
			if (newMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
				// Note messages
				// Run appropriate decider
				sMidiSegment.add(newMessage);
				this.txtMsg("addMidiMessage: " + newMessage.pitch + " " + newMessage.velocity);
				decider_01.messageIn(newMessage);
				//this.txtMsg("Calling Analyser - Note");
				if(analyser_silence.newMidi()) analyser_silence.analyse();
				if(analyser_stats.newMidi()) analyser_stats.analyse();
				if (decider_01.isMirroring()) generator_note.generate();
			} else {
				// Controller messages - call appropriate analyser
				//this.txtMsg("Calling Analyser - Controller");
				if(analyser_controls.newMidi()) analyser_controls.analyse();
			}
		}
	}

	public synchronized void addMidiSegment(int segmentStart, int segmentEnd) {	
		//this.txtMsg("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		System.gc();
		if(sTestMode) {
			tester.runTests(SEGMENT_TESTS);
		} else {
			decider_01.chooseNextAction();
		}
		//this.txtMsg("in SupervisorMidi sMidiSegment duration is " + sMidiSegment.size());
		sMidiSegment = new MidiSegment(); //segmentStart-1, segmentEnd);
	}	
	
	
	
	public synchronized MidiSegment getLastMidiSegment() {
		return sMidiSegment;
	}
	
	public synchronized MidiMessage getLastMidiMessage() {
		MidiMessage lastMidiMessage = new MidiMessage();
		lastMidiMessage.copy(sLastMidiMessage);
		return lastMidiMessage;
	}
	
	public void allNotesOff() {
		outputTracker.allNotesOff();
	}
		
}
