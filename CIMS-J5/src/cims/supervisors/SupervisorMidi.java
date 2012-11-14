package cims.supervisors;

import java.util.*;
import java.util.logging.*;

import cims.CimsMaxIO;
import cims.capturers.CaptureMidi;
import cims.capturers.CaptureOutput;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
import cims.players.PlayMidi_BeatTime;
import cims.utilities.Test;
//import cims.v01.DecideMidi_01;
import cims.v02.DecideMidi_02;
import cims.datatypes.*;
import cims.deciders.DecideMidi_SimpleRepeat;
import cims.deciders.DecideMidi_UserControl;
import cims.interfaces.Interface_Controls;

import static cims.supervisors.SupervisorMidi_Globals.*;

/**
 * @author andrew
 *
 */
public class SupervisorMidi implements Supervisor {
	
	private CimsMaxIO io;
	private Interface_Controls controls;
	
	private CaptureMidi capturer;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private AnalyseMidi_Stats analyser_stats;
	private DecideMidi_UserControl decider_userControl;
	private DecideMidi_02 decider;
	@SuppressWarnings(value="unused")
	private DecideMidi_SimpleRepeat decider_simpleRepeat;
	private PlayMidi_BeatTime player_beatTime;
	private CaptureOutput outputTracker;
	private Test tester;
	private boolean firstMessage = true;
	
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
	public SupervisorMidi(CimsMaxIO ioObj,Interface_Controls controls) {
		this.io = ioObj;
		this.controls = controls;
		sLastMidiMessage = new MidiMessage();
		sMidiMessageList = new ArrayList<MidiMessage>();
		sLastMidiControlMessage = new MidiControlMessage();
		sMidiControlMessageTable = new MidiControlMessageTable();
		sCurrentBeatTime = new BeatTime();
		
		sMidiSegment = new MidiSegment();
		sMidiSegmentTable = new MidiSegmentTable();
		
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
		player_beatTime = new PlayMidi_BeatTime(this);
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
	
	public void beatTimeIn() {
		sCurrentBeatTime = this.io.getBeatTime();
		this.player_beatTime.beatTimeIn();
	}
	
	public void interfaceUpdated() {
		sActivityWeights = controls.getActivityWeights();
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
	
	public void dataThru(int[] message) {
		if (message==null) {
			LOGGER.warning("NULL MESSAGE FOR DATA THRU!");
		} else {
			LOGGER.info("DATA THRU: "+message[0]);
			this.io.outMidiThru(message);
		}
	}
		
	public void addMidiMessage(MidiMessage newMessage) {
		sLastMidiMessage = new MidiMessage();
		sLastMidiMessage.copy(newMessage);
		sMidiMessageList.add(sLastMidiMessage);

		if (newMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
			LOGGER.info("addMidiMessage: NOTE");
			if(firstMessage) {
				decider.firstAction(newMessage);
				firstMessage = false;
			}
			this.doNext(MESSAGE_NOTE);
		} else {
			LOGGER.info("addMidiMessage: CONTROL");
			sLastMidiControlMessage.addMessage(sLastMidiMessage);
			sMidiControlMessageTable.add(sLastMidiControlMessage);
			this.doNext(MESSAGE_CONTROL);
			//LOGGER.info("allControlsMap: " + sMidiControlMessageTable.getAllControlMessages());
		}
		//System.out.println("MidiMessage.beatTime: "+sLastMidiMessage.getBeatTimeAsString()+" newMessage: "+newMessage.beatTime.toString());
	}

	public void addMidiSegment(int segmentStart, int segmentEnd, Class<?> creatorClass) {
		sMidiSegment = new MidiSegment(segmentStart-1, segmentEnd);
		sMidiSegment.setCreatorClass(creatorClass);
		//add the segment to the MidiSegmentTable
		sMidiSegmentTable.add(sMidiSegment);
		LOGGER.info("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		//sMidiStats.clearPitchHistogram();
		this.doNext(SEGMENT);	
	}
	
	//TODO Sometimes we don't know start and end of segments, but want to add a point that will define a segment later on
	public void addSegmentBreakPoint(long breakPoint, Class<?> creatorClass) {
		
	}
	
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
		//outputTracker.allNotesOff();
	}
	
	public void txtMsg(String msg) {
		this.io.textOut(msg);
	}
	
	public void oscSysMsg(String msg) {
		this.controls.setSysMessage(msg);
		this.controls.sendSysMessageToInterface();
	}
	
	public void displayNextAction(int nextAction,boolean now) {
		//System.out.println("DISPLAY NEXT ACTION");
		double value = 0.01;
		String activity = "Next";
		if(now) {
			activity = "Now";
		} 
		//Turn all off -very low
		this.controls.sendControlMessageToInterface("activity"+activity+"Red", new ArrayList<Object>(Collections.singletonList(value)));
		this.controls.sendControlMessageToInterface("activity"+activity+"Orange", new ArrayList<Object>(Collections.singletonList(value)));
		this.controls.sendControlMessageToInterface("activity"+activity+"Purple", new ArrayList<Object>(Collections.singletonList(value)));
		this.controls.sendControlMessageToInterface("activity"+activity+"Green", new ArrayList<Object>(Collections.singletonList(value)));
		this.controls.sendControlMessageToInterface("activity"+activity+"Yellow", new ArrayList<Object>(Collections.singletonList(value)));
		if(now) {
			value = 1;
			
		} else {
			value = 0.5;
		}
		switch(nextAction) {
		case 0:
			this.controls.sendControlMessageToInterface("activity"+activity+"Red", new ArrayList<Object>(Collections.singletonList(value)));
			break;
		case 1:
			this.controls.sendControlMessageToInterface("activity"+activity+"Orange", new ArrayList<Object>(Collections.singletonList(value)));
			break;
		case 2:
			this.controls.sendControlMessageToInterface("activity"+activity+"Purple", new ArrayList<Object>(Collections.singletonList(value)));
			break;
		case 3:
			this.controls.sendControlMessageToInterface("activity"+activity+"Green", new ArrayList<Object>(Collections.singletonList(value)));
			break;
		case 4:
			this.controls.sendControlMessageToInterface("activity"+activity+"Yellow", new ArrayList<Object>(Collections.singletonList(value)));
			break;		
		}
	}
	
	public PlayMidi_BeatTime currentPlayer() {
		return this.player_beatTime;
	}

}
