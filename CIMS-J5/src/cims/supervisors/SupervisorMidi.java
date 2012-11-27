package cims.supervisors;

import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.CimsMaxIO;
import cims.interfaces.Interface_Controls;
import cims.capturers.CaptureMidi;
import cims.analysers.AnalyseMidi_Silence;
import cims.analysers.AnalyseMidi_Controls;
import cims.analysers.AnalyseMidi_Stats;
import cims.v03.DecideMidi_03;
import cims.deciders.DecideMidi_SimpleRepeat;
import cims.players.PlayMidi_BeatTime;
import cims.utilities.*;
import cims.datatypes.*;

import static cims.supervisors.SupervisorMidi_Globals.*;

/*****************************************************************************************
 * SupervisorMidi is the primary class handling midi message flow within the system.
 * It is the hub between multiple worker classes and provides access to I/O and 
 * UI elements.
 * @author Andrew Gibson a.gibson@griffith.edu.au
 *
 */
public class SupervisorMidi implements Supervisor {
	
	// IO and UI
	private CimsMaxIO io;
	private Interface_Controls controls;
	
	// Manage incoming data
	private CaptureMidi capturer;
	private PlayMidi_BeatTime player_beatTime;
	private AnalyseMidi_Silence analyser_silence;
	private AnalyseMidi_Controls analyser_controls;
	private AnalyseMidi_Stats analyser_stats;
	
	// Primary decision making
	private DecideMidi_03 decider;
	@SuppressWarnings("unused")
	private DecideMidi_SimpleRepeat decider_simpleRepeat;
	private Test tester;
	private boolean firstMessage = true;
	
	// Data type labels
	private static final int MESSAGE_NOTE = 0;
	private static final int MESSAGE_CONTROL = 1;
	private static final int SEGMENT = 2;
	private static final int TEST_MESSAGE_NOTE = 10;
	private static final int TEST_MESSAGE_CONTROL = 11;
	private static final int TEST_SEGMENT = 12;
	
	public static Logger LOGGER = Logger.getLogger(SupervisorMidi.class);
	
	
	/*****************************************************************************************
	 * On construction, it takes a single paramater being {@link CimsMaxIO} which is the 
	 * entry class to the application. CimsMaxIO is the mxj object that is included in Max
	 * and handles all IO between the Max environment and the Supervisor.
	 * 
	 * The Supervisor also sets up and coordinates a number of global properties which are
	 * in {@link SupervisorMidi_Globals}. As the name suggests, these are used application wide.
	 *
	 * @param  ioObj  the reference to the CimsMaxIO mxj object embedded in Max
	 * @param controls the reference to ui controls
	 * @see		CimsMaxIO
	 * @see		SupervisorMidi_Globals
	 */
	public SupervisorMidi(CimsMaxIO ioObj,Interface_Controls controls) {
		this.io = ioObj;
		this.controls = controls;
		
		//Global objects init
		sLastMidiMessage = new MidiMessage();
		sMidiMessageList = new ArrayList<MidiMessage>();
		sLastMidiControlMessage = new MidiControlMessage();
		sMidiControlMessageTable = new MidiControlMessageTable();
		sCurrentBeatTime = new BeatTime();
		sMidiSegment = new MidiSegment();
		sMidiSegmentTable = new MidiSegmentTable();
		sMidiStartTime=0;
		sMidiStats = new MidiStatistics();
		
		//Key worker objects init
		capturer = new CaptureMidi(this);
		player_beatTime = new PlayMidi_BeatTime(this);
		analyser_silence = new AnalyseMidi_Silence(this);
		analyser_controls = new AnalyseMidi_Controls(this);
		analyser_stats = new AnalyseMidi_Stats(this);
		decider = new DecideMidi_03(this);
		decider_simpleRepeat = new DecideMidi_SimpleRepeat(this);
		tester = new Test(this);

		LOGGER.setLevel(Level.INFO);
	}
	
	/*****************************************************************************************
	 * PRIMARY DECISION MAKING METHODS
	 */
	
	/*****************************************************************************************
	 * Handles the very first MidiMessage through the system which requires special
	 * consideration by the Decider to ensure system responds immediately. Once first messge
	 * is handled the property firstMessage is set to false, and this method is not called
	 * again.
	 * 
	 * @param newMessage
	 */
	private void firstMessageIn(MidiMessage newMessage) {
		decider.firstAction(newMessage);
		firstMessage = false;
	}
	
	/*****************************************************************************************
	 * The primary decision loop of the supervisor, making calls appropriate to incoming data.
	 * Analysers are called to analyse incoming data
	 * Deciders are called to determine what to do next based on analysed data
	 * Deciders also create and call Generators to generate outgoing data
	 * 
	 * @param nextType
	 */
	
	private void doNext(int nextType) {
		if (sTestMode) nextType=+10;
		switch(nextType) {
		case MESSAGE_NOTE:
			LOGGER.debug("MIDI MESSAGE - NOTE");
			decider.messageIn(sLastMidiMessage);	// THIS IS THE MAIN CALL TO THE DECIDER WHEN A MESSAGE IS CREATED
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			if(analyser_stats.newMidi()) analyser_stats.analyse();	
			break;
		case MESSAGE_CONTROL:
			LOGGER.debug("MIDI MESSAGE - CONTROL");
			if(analyser_controls.newMidi()) analyser_controls.analyse();
			if(analyser_silence.newMidi()) analyser_silence.analyse();
			break;
		case SEGMENT:
			LOGGER.debug("MIDI SEGMENT");
			sMidiStats.clearPitchHistogram();
			decider.segmentCreated(sMidiSegment);   // THIS IS THE MAIN CALL TO THE DECIDER WHEN A SEGMENT IS CREATED
			//decider_simpleRepeat.segmentCreated();
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
	
	/*****************************************************************************************
	 * Handle User Controls from the interface
	 */
	
	private void controlHandler() {
		String key = this.io.getControlkey();
		int value =  this.io.getControlValue();
		LOGGER.debug("KEY: "+key+" VALUE: "+value);
		if(key.equals("segmentGap")) {
			sSegmentGap = value;
			sCurrentBeatTime.recalcDefaultTimings();
		}
		if(key.equals("repeatCue")) {
			sRepeatInterval = value;
			LOGGER.info("Repeat interval set: "+sRepeatInterval+"ms");
		}
		if(key.equals("metronome")) {
			if(value==1) {
				sMetronome = true;
				LOGGER.info("METRONOME ON");
			} else {
				sMetronome = false;
				LOGGER.info("METRONOME OFF");
			}
		}
		
		if(key.equals("test")) {
			if(value==1) {
				LOGGER.info("TEST MODE ON");
				sTestMode = true;
				//this.runTests();
			} else {
				LOGGER.info("TEST MODE OFF");
				sTestMode = false;
			}
		}
		if(key.equals("nextPlay")) {
			LOGGER.info("NEXTPLAY: "+value);
			sNextPlay = value;
		}
	}
	
	/*****************************************************************************************
	 * HANDLE MIDI DATA
	 */
	
	/*****************************************************************************************
	 * Get the current midi data from CimsMaxIO and process it to create complete
	 * MIDI messages
	 */
	public void midiIn() {
		int midiData = this.io.inMidi();
		LOGGER.debug("DataIN: "+midiData);
		capturer.in(midiData); //process raw midi data into midi messages
	}
	
	/*****************************************************************************************
	 * Send a pre-formatted MIDI message out through CimsMaxIO MIDI OUT.
	 * @param pre-formatted int array midi message
	 */
	public void midiOut(int[] message) {
		if (message==null) {
			LOGGER.warn("NULL MESSAGE FOR MIDI OUT!");
		} else {
			LOGGER.debug("MIDI OUT (STATUS): "+message[0]);
			this.io.outMidi(message);
		}
	}
	
	/*****************************************************************************************
	 * Send a pre-formatted MIDI message out through CimsMaxIO MIDI THRU.
	 * @param pre-formatted int array midi message
	 */
	public void midiThru(int[] message) {
		if (message==null) {
			LOGGER.warn("NULL MESSAGE FOR MIDI THRU!");
		} else {
			LOGGER.debug("MIDI THRU (STATUS): "+message[0]);
			this.io.outMidiThru(message);
		}
	}
	
	/*****************************************************************************************
	 * Called by Capturer when a new MidiMessage object is created from raw MIDI data.
	 * This method is not intended to be called by other classes as it assumes the message
	 * is coming in through the I/O
	 */
	
	public void addMidiMessage(MidiMessage newMessage) {
		sLastMidiMessage = new MidiMessage();
		sLastMidiMessage.copy(newMessage);
		sMidiMessageList.add(sLastMidiMessage);

		if (newMessage.messageType<MidiMessage.POLY_AFTERTOUCH){
			LOGGER.debug("addMidiMessage: NOTE");
			if(firstMessage) firstMessageIn(newMessage);
			this.doNext(MESSAGE_NOTE);
		} else {
			LOGGER.debug("addMidiMessage: CONTROL");
			sLastMidiControlMessage.addMessage(sLastMidiMessage);
			sMidiControlMessageTable.add(sLastMidiControlMessage);
			this.doNext(MESSAGE_CONTROL);
			LOGGER.debug("allControlsMap: " + sMidiControlMessageTable.getAllControlMessages());
		}
		LOGGER.debug("MidiMessage.beatTime: "+sLastMidiMessage.getBeatTimeAsString()+" newMessage: "+newMessage.beatTime.toString());
	}

	/*****************************************************************************************
	 * 
	 */
	public void addMidiSegment(int segmentStart, int segmentEnd, Class<?> creatorClass) {
		sMidiSegment = new MidiSegment(segmentStart-1, segmentEnd);
		sMidiSegment.setCreatorClass(creatorClass);
		//add the segment to the MidiSegmentTable
		sMidiSegmentTable.add(sMidiSegment);
		LOGGER.debug("SEGMENT ADDED: "+segmentStart+" - "+segmentEnd);
		this.doNext(SEGMENT);	
	}
	
	/*****************************************************************************************
	 * TODO Sometimes we don't know start and end of segments, but want to add a point that 
	 * will define a segment later on
	 */
	public void addSegmentBreakPoint(long breakPoint, Class<?> creatorClass) {
			
	}
	
	
	/*****************************************************************************************
	 * HANDLE USER INTERFACE DATA - PRIMARILY VIA OSC
	 */
	
	public void oscSysMsg(String msg) {
		this.controls.setSysMessage(msg);
		this.controls.sendSysMessageToInterface();
	}
	
	public void displayNextAction(int nextAction,boolean now) {
		LOGGER.debug("DISPLAY NEXT ACTION");
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
	
	/*****************************************************************************************
	 * HANDLE OTHER NON MIDI DATA
	 */
	
	
	public void controlIn() {
		this.controlHandler();	
	}
	
	public void beatTimeIn() {
		sCurrentBeatTime = this.io.getBeatTime();
		this.player_beatTime.beatTimeIn();
	}
	
	public void interfaceUpdated() {
		sActivityWeights = controls.getActivityWeights();
	}
	
	public void txtMsg(String msg) {
		this.io.textOut(msg);
	}
	
	
	/*****************************************************************************************
	 * GENERAL GETTERS AND SETTERS
	 */
	
	public PlayMidi_BeatTime currentPlayer() {
		return this.player_beatTime;
	}
	
	public synchronized MidiSegment getLastMidiSegment() {
		return sMidiSegment;
	}
	
	public synchronized MidiMessage getLastMidiMessage() {
		return sLastMidiMessage;
	}

}
