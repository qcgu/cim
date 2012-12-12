package cims;

import java.util.ArrayList;
import java.util.Iterator;

import com.cycling74.max.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
//import org.apache.log4j.PropertyConfigurator;

import cims.supervisors.*;
import cims.datatypes.BeatTime;
import cims.interfaces.Interface_Controls;

/****
 * Provides the initialisation of CIMS and handles all I/O between CIMS and Max.
 * @author Andrew Gibson a.gibson@griffith.edu.au
 *
 */
public class CimsMaxIO extends MaxObject {
	
	//Primary Handler Classes for various I/O data
	private SupervisorMidi superMidi;
	private SupervisorOsc superOsc;
	//private SupervisorAudio superAudio;
	private Interface_Controls controls;
	
	//Temporary storage of incoming data
	private int midiData = 0;
	private String[] oscData;
	private byte audioData;
	private BeatTime beatTime;	
	private String controlKey = "";
	private int controlValue = 0;
	private double dataCounter = 0;
	
	//Names used by this class
	private static String[] controlNames = {"metronome","nextPlay","segmentGap","repeatCue","test"};
	private static final int MIDI = 0;
	private static final int OSC = 1;
	private static final int AUDIO = 2;
	private static final int CONTROL = 3;
	private static final int TRANSPORT = 4;
	
	//Logging localised to this class
	private static Logger LOGGER = Logger.getLogger(CimsMaxIO.class);
	
	
	/*******************************************************************************************
	 * 
	 */
	
	public CimsMaxIO() {
		declareIO(1,1); //Inlets and Outlets available to Max
		createInfoOutlet(false); // Right most outlet not required	
		
		//Init objects and set initial values
		controls = new Interface_Controls(this);
		superMidi = new SupervisorMidi(this,controls);
		superOsc = new SupervisorOsc(this,controls);
		beatTime = new BeatTime();
		beatTime.recalcDefaultTimings();
		this.interfaceUpdated();
		
		BasicConfigurator.configure();
		LOGGER.setLevel(Level.WARN);
		LOGGER.info("IO Initialized");		
	}

	/*******************************************************************************************
	 * Anything captures all input coming from the Max environment. It processes this input by
	 * data type based on the first argument in the list. The remainder of the arguments are
	 * then processed according to type.
	 * @param message - A string containing the message type from Max (always a list for this app)
	 * @param args  - A List from Max (Atom allows for mixed types)
	 */
	
	public void anything(String message, Atom[] args) {
		
		switch(args[0].toInt()) {
		
		case MIDI:
			this.midiData = args[1].toInt();
			LOGGER.debug("MIDI: " + midiData);
			superMidi.midiIn();
			break;
			
		case OSC:
			LOGGER.debug("anything OSC: " + args[1].toString());
			int numArgs = args.length;
			this.oscData = new String[numArgs];
			for(int i=1;i<numArgs;i++) {
				this.oscData[i-1] = args[i].toString();
			}
			superOsc.dataIn();
			break;
			
		case AUDIO:
			LOGGER.debug("AUDIO: " + args[1]);
			break;
			
		case CONTROL:
			controlKey=controlNames[args[1].toInt()];
			controlValue=args[2].toInt();
			LOGGER.debug("KEY: "+controlKey+" VALUE: "+controlValue);
			superMidi.controlIn();
			break;
			
		case TRANSPORT:
			Integer[] transport = {args[1].toInt(),args[2].toInt(),args[3].toInt(),
					args[4].toInt(),args[5].toInt(),args[6].toInt(),
					args[7].toInt(),args[8].toInt(),args[9].toInt()};
			this.beatTime = new BeatTime(transport);
			LOGGER.debug("BT: "+beatTime.toString());
			superMidi.beatTimeIn();
			break;
		}
		//Force garbage collection regularly
		dataCounter++;
		if (dataCounter>1000) {
			LOGGER.info("GARBAGE COLLECTION");
			this.gc();
			dataCounter = 0;
		}
	}
	
	/*******************************************************************************************
	 * Handle MIDI I/O
	 */
	
	public int inMidi() {
		LOGGER.debug("MIDI IN");
		return this.midiData;
	}
	
	public void outMidi(int[] midi) {
		LOGGER.debug("MIDI OUT");
		int messageSize = midi.length+2;
		Atom[] midiOutMessage = new Atom[messageSize];		
		midiOutMessage[0] = Atom.newAtom(0);
		midiOutMessage[1] = Atom.newAtom("midievent");
		for(int i=2;i<messageSize;i++) {
			midiOutMessage[i] = Atom.newAtom(midi[(i-2)]);
		}
		LOGGER.debug("OUT: "+midiOutMessage[2]+" "+midiOutMessage[3]);
		outlet(0,midiOutMessage);
	}
	
	public void outMidiThru(int[] midi) {
		LOGGER.debug("MIDI THRU");
		int messageSize = midi.length+2;
		Atom[] midiOutMessage = new Atom[messageSize];		
		midiOutMessage[0] = Atom.newAtom(1);
		midiOutMessage[1] = Atom.newAtom("midievent");
		for(int i=2;i<messageSize;i++) {
			midiOutMessage[i] = Atom.newAtom(midi[(i-2)]);
		}
		outlet(0,midiOutMessage);
	}
	
	/*******************************************************************************************
	 *  HANDLE OSC I/O
	 */
	
	public String[] inOsc() {
		return this.oscData;
	}
	
	public void outOsc(ArrayList<Object> osc) {
		LOGGER.debug("OSC OUT");
		int messageSize = osc.size()+1;
		Atom[] oscOutMessage = new Atom[messageSize];
		oscOutMessage[0] = Atom.newAtom(2);
		Iterator<Object> oscIterator = osc.iterator();
		int i=1;
		while (oscIterator.hasNext()) {
			oscOutMessage[i] = Atom.newAtom(oscIterator.next().toString());
			i++;
		}
		outlet(0,oscOutMessage);
	}
	
	public void outOscSysMessage(String address, String message) {
		LOGGER.debug("OSC SYS MESSAGE");
		ArrayList<Object> outMessage = new ArrayList<Object>();
		outMessage.add(address);
		outMessage.add(message);
		this.outOsc(outMessage);
	}
	
	public void sendInterfaceUpdate(String address,ArrayList<?> message) {
		LOGGER.debug("OSC INTERFACE UPDATE");
		ArrayList<Object> outMessage = new ArrayList<Object>();
		outMessage.add(address);
		Iterator<?> messageIterator = message.iterator();
		while(messageIterator.hasNext()) {
			outMessage.add(messageIterator.next());
		}
		this.outOsc(outMessage);
	}
	
	/*******************************************************************************************
	 * HANDLE AUDIO I/O
	 */
	public int inAudio() {
		LOGGER.debug("AUDIO IN");
		return this.audioData;
	}
	
	public void outAudio(int audio) {
		LOGGER.debug("AUDIO OUT");
	}
	
	/*******************************************************************************************
	 * GENERAL UTILITY METHODS
	 */
	
	public void interfaceUpdated() {
		LOGGER.debug("Interface Updated");
		superMidi.interfaceUpdated();
	}
	
	public void textOut(String text) {
		post(text);
	}
	
	
	/*******************************************************************************************
	 * GENERAL GETTERS AND SETTERS
	 */
	
	public String getControlkey() {
		return this.controlKey;
	}
	
	public int getControlValue() {
		return this.controlValue;
	}
	
	public BeatTime getBeatTime() {
		return beatTime;
	}

	public void setBeatTime(BeatTime beatTime) {
		this.beatTime = beatTime;
	}	
}
