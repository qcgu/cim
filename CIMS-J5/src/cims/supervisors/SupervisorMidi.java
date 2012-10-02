package cims.supervisors;

import java.util.*;
import java.util.logging.*;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.capturers.CaptureOutput;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
import cims.utilities.Test;
//import cims.v01.DecideMidi_01;
import cims.v02.DecideMidi_02;
import cims.datatypes.*;
import cims.deciders.DecideMidi_SimpleRepeat;
import cims.deciders.DecideMidi_UserControl;

import static cims.supervisors.SupervisorMidi_Globals.*;

/**
 * @author andrew
 *
 */
public class SupervisorMidi implements Supervisor {
	
	private CimsMaxIO io;
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private AnalyseMidi_Stats analyser_stats;
	private DecideMidi_UserControl decider_userControl;
	private DecideMidi_02 decider;
	@SuppressWarnings(value="unused")
	private DecideMidi_SimpleRepeat decider_simpleRepeat;
	private CaptureOutput outputTracker;
	private Test tester;
	
	private static final int MESSAGE_NOTE = 0;
	private static final int MESSAGE_CONTROL = 1;
	private static final int SEGMENT = 2;
	private static final int TEST_MESSAGE_NOTE = 10;
	private static final int TEST_MESSAGE_CONTROL = 11;
	private static final int TEST_SEGMENT = 12;
	
	/**
	 * SupervisorMidi is the primary class handling message flow within the application.
	 * On construction, it takes a single paramater being {@link CimsMaxIO} which is the 
	 * entry class to the application. CimsMaxIO is the mxj object that is included in Max
	 * and handles all IO between the Max environment and the Supervisor.
	 * 
	 * The Supervisor also sets up and coordinates a number of global properties which are
	 * in {@link SupervisorMidi_Globals}. As the name suggests, these are used application wide.
	 *
	 * @param  ioObj  the reference to the CimsMaxIO mxj object embedded in Max
	 * @see		CimsMaxIO
	 * @see		SupervisorMidi_Globals
	 */
	public SupervisorMidi(CimsMaxIO ioObj) {
		this.io = ioObj;
		sLastMidiMessage = new MidiMessage();
		sMidiMessageList = new ArrayList<MidiMessage>();
		sMidiSegment = new MidiSegment();
		sMidiStartTime=0;
		sMidiStats = new MidiStatistics();
		//Capture input and output midi
		capturer = new CaptureMidi(this);
		outputTracker = new CaptureOutput(this);
		//Analyse
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		analyser_stats = new AnalyseMidi_Stats(this);
		//Decide what to do
		decider_userControl = new DecideMidi_UserControl(this);
		decider = new DecideMidi_02(this);
		decider_simpleRepeat = new DecideMidi_SimpleRepeat(this);
		//Test
		tester = new Test(this);
		//Set Log Level for SupervisorMidi Global Logger
		LOGGER.setLevel(Level.WARNING);
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
		if (message==null) {
			LOGGER.warning("NULL MESSAGE FOR DATA OUT!");
		} else {
			LOGGER.info("DATA OUT: "+message[0]);
			this.io.outMidi(message);
		}
		// output capture to stop stuck notes
		//outputTracker.in(message);
	}
	
	public void addMidiMessage(MidiMessage newMessage) {
		sLastMidiMessage = new MidiMessage();
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
		//sMidiStats.clearPitchHistogram();
		this.doNext(SEGMENT);	
	}
	
	//TODO Check with Andrew what this method is for - potentially dangerous to call the silent() method directly
	/*
	public void densitySegmentBreak() {
		analyser_silence.silent();
	}
	*/
	
	public void doNext(int nextType) {
		if (sTestMode) nextType=+10;
		switch(nextType) {
		case MESSAGE_NOTE:
			decider.messageIn(sLastMidiMessage);
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			if(analyser_stats.newMidi()) analyser_stats.analyse();	
			break;
		case MESSAGE_CONTROL:
			if(analyser_controls.newMidi()) analyser_controls.analyse();
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			break;
		case SEGMENT:
			System.gc(); //force garbage collection
			decider.chooseNextAction();
			//decider_simpleRepeat.repeatLastSegment();
			break;
		case TEST_MESSAGE_NOTE:
			LOGGER.info("RUN MIDIMESSAGE TESTS");
			tester.runTests(Test.MESSAGE_TESTS);
			break;
		case TEST_MESSAGE_CONTROL:
			break;
		case TEST_SEGMENT:
			LOGGER.info("RUN SEGMENT TESTS");
			tester.runTests(Test.SEGMENT_TESTS);
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
