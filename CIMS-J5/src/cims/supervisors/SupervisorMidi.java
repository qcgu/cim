package cims.supervisors;

import java.util.*;
import java.util.logging.*;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.capturers.CaptureOutput;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
//import cims.generators.GenerateMidi_Loop;
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
	private AnalyseMidi_Stats analyser_stats;
	private GenerateMidi_Segment generator_segment;
	private GenerateMidi_NoteMirror generator_note;
	private DecideMidi_UserControl decider_userControl;
	private DecideMidi_01 decider_01;
	private CaptureOutput outputTracker;
	private Test tester;
	
	private static final int MESSAGE_NOTE = 0;
	private static final int MESSAGE_CONTROL = 1;
	private static final int SEGMENT = 2;
	private static final int TEST_MESSAGE_NOTE = 10;
	private static final int TEST_MESSAGE_CONTROL = 11;
	private static final int TEST_SEGMENT = 12;
	
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		sLastMidiMessage = new MidiMessage();
		sMidiMessageList = new ArrayList<MidiMessage>();
		sMidiSegment = new MidiSegment();
		sMidiStartTime=0;
		
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
		//Set Log Level for SupervisorMidi Global Logger
		LOGGER.setLevel(Level.INFO);
	}
	
	public void dataIn() {
		int midiData = this.io.inMidi();
		LOGGER.info("DataIN: "+midiData);
		capturer.in(midiData);
	}
	
	public void controlIn() {
		LOGGER.info("ControlIN");
		decider_userControl.input(this.io.key(), this.io.value());	
	}
	
	public void dataOut(int[] message) {
		LOGGER.info("dataOut: "+message[0]+"|"+message[1]+"|"+message[2]);
		this.io.outMidi(message);
		// output capture to stop stuck notes
		outputTracker.in(message);
	}
	
	public void addMidiMessage(MidiMessage newMessage) {
		sLastMidiMessage.copy(newMessage);
		sMidiMessageList.add(sLastMidiMessage);
		if (newMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
			LOGGER.info("addMidiMessage: NOTE");
			this.doNext(MESSAGE_NOTE);
		} else {
			LOGGER.info("addMidiMessage: CONTROL");
			this.doNext(MESSAGE_CONTROL);
		}
	}

	public void addMidiSegment(int segmentStart, int segmentEnd) {
		sMidiSegment = new MidiSegment(segmentStart-1, segmentEnd);
		LOGGER.info("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		this.doNext(SEGMENT);	
	}
	
	public void doNext(int nextType) {
		if (sTestMode) nextType=+10;
		switch(nextType) {
		case MESSAGE_NOTE:
			decider_01.messageIn(sLastMidiMessage);
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			if(analyser_stats.newMidi()) analyser_stats.analyse();	
			if (decider_01.isMirroring()) generator_note.generate();
			break;
		case MESSAGE_CONTROL:
			if(analyser_controls.newMidi()) analyser_controls.analyse();
			break;
		case SEGMENT:
			System.gc(); //force garbage collection
			decider_01.chooseNextAction();
			break;
		case TEST_MESSAGE_NOTE:
			LOGGER.info("RUN MIDIMESSAGE TESTS");
			//tester.runTests(Test.MESSAGE_TESTS);
			break;
		case TEST_MESSAGE_CONTROL:
			break;
		case TEST_SEGMENT:
			LOGGER.info("RUN SEGMENT TESTS");
			//tester.runTests(Test.SEGMENT_TESTS);
			break;
		}
	}
	
	
	
	public synchronized MidiSegment getLastMidiSegment() {
		return sMidiSegment;
	}
	
	public synchronized MidiMessage getLastMidiMessage() {
		return sLastMidiMessage;
	}
	
	public void allNotesOff() {
		outputTracker.allNotesOff();
	}
	
	public void txtMsg(String msg) {
		this.io.textOut(msg);
	}
}
