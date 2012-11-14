/*
 * Main IO Class that is embedded in Max as an MXJ Object
 * 		All other objects talk to Max through CimsMaxIO
 */

package cims;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.*;
import cims.datatypes.BeatTime;
import cims.interfaces.Interface_Controls;
import cims.supervisors.*;
import com.cycling74.max.*;

//comment
public class CimsMaxIO extends MaxObject {
	private SupervisorMidi superMidi;
	private SupervisorOsc superOsc;
	//private SupervisorAudio superAudio;
	
	private Interface_Controls controls;
	
	private int midiData = 0;
	private String[] oscData;
	private byte audioData;
	private BeatTime beatTime;
	
	private String controlKey = "";
	private int controlValue = 0;
	private String[] controlNames = {"metronome","nextPlay","segmentGap","repeatCue","test"};
	
	private static final Logger LOGGER = Logger.getLogger(CimsMaxIO.class.getName());
	
	private static final int MIDI = 0;
	private static final int OSC = 1;
	private static final int AUDIO = 2;
	private static final int CONTROL = 3;
	private static final int TRANSPORT = 4;
	

	public CimsMaxIO() {
		declareIO(1,3); 
		createInfoOutlet(false); // Right most outlet not required	
		controls = new Interface_Controls(this);
		superMidi = new SupervisorMidi(this,controls);
		superOsc = new SupervisorOsc(this,controls);
		LOGGER.setLevel(Level.OFF); //INFO
		textOut("IO Initialized");
		this.interfaceUpdated();
	}

	public void anything(String message, Atom[] args) {
		LOGGER.log(Level.OFF,"Message: "+message+" args: "+args.toString());
		switch(messageCheck(message)) {
		case MIDI:
			this.midiData = args[0].toInt();
			LOGGER.log(Level.OFF,"MIDI: " + midiData);
			superMidi.dataIn();
			break;
		case OSC:
			LOGGER.log(Level.OFF,"OSC: " + args[0].toString());
			int numArgs = args.length;
			oscData = new String[numArgs];
			for(int i=0;i<numArgs;i++) {
				this.oscData[i] = args[i].toString();
			}
			superOsc.dataIn();
			textOut("OSC RECEIVED");
			break;
		case AUDIO:
			System.out.println("AUDIO: " + args[0]);
			break;
		case CONTROL:
			controlKey=controlNames[args[0].toInt()];
			controlValue=args[1].toInt();
			LOGGER.log(Level.INFO, "KEY: "+controlKey+" VALUE: "+controlValue);
			superMidi.controlIn();

			break;			
		case TRANSPORT:
			Integer[] transport = {args[0].toInt(),args[1].toInt(),args[2].toInt(),
					args[3].toInt(),args[4].toInt(),args[5].toInt(),
					args[6].toInt(),args[7].toInt(),args[8].toInt()};
			this.beatTime = new BeatTime(transport);
			//System.out.println("BT: "+beatTime.toString());
			superMidi.beatTimeIn();
			break;
		}
		//this.gc();
	}
	
	private int messageCheck(String message) {
		int returnValue = -1;
		if(message.equalsIgnoreCase("int")) {
			returnValue =  MIDI;
		}
		if(message.equalsIgnoreCase("osc")) {
			returnValue =  OSC;
		}
		if(message.equalsIgnoreCase("controlParams")) {
			returnValue =  CONTROL;
		}
		if(message.equalsIgnoreCase("transport")) {
			returnValue =  TRANSPORT;
		}
		return returnValue;
	}
	
	public int inMidi() {
		return this.midiData;
	}
	public String[] inOsc() {
		return this.oscData;
	}
	public int inAudio() {
		return this.audioData;
	}
	
	public void interfaceUpdated() {
		LOGGER.log(Level.OFF, "CIMSIO: Interface Updated");
		superMidi.interfaceUpdated();
	}
	
	public void sendInterfaceUpdate(String address,ArrayList<?> message) {
		ArrayList<Object> outMessage = new ArrayList<Object>();
		outMessage.add(address);
		Iterator<?> messageIterator = message.iterator();
		while(messageIterator.hasNext()) {
			outMessage.add(messageIterator.next());
		}
		this.outOsc(outMessage);
	}
	
	public void outMidi(int[] midi) {
		LOGGER.log(Level.OFF, "MIDI OUT");
		int messageSize = midi.length+1;
		Atom[] midiOutMessage = new Atom[messageSize];		
		midiOutMessage[0] = Atom.newAtom("midievent");
		for(int i=1;i<messageSize;i++) {
			midiOutMessage[i] = Atom.newAtom(midi[(i-1)]);
		}
		outlet(0,midiOutMessage);
	}
	
	public void outMidiThru(int[] midi) {
		int messageSize = midi.length+1;
		Atom[] midiOutMessage = new Atom[messageSize];		
		midiOutMessage[0] = Atom.newAtom("midievent");
		for(int i=1;i<messageSize;i++) {
			midiOutMessage[i] = Atom.newAtom(midi[(i-1)]);
		}
		outlet(1,midiOutMessage);
	}
	
	public void outOsc(ArrayList<Object> osc) {
		int messageSize = osc.size();
		Atom[] oscOutMessage = new Atom[messageSize];
		Iterator<Object> oscIterator = osc.iterator();
		int i=0;
		while (oscIterator.hasNext()) {
			oscOutMessage[i] = Atom.newAtom(oscIterator.next().toString());
			i++;
		}
		outlet(2,oscOutMessage);
	}
	
	public void outOscSysMessage(String address, String message) {
		Atom[] osc = new Atom[2];
		osc[0] = Atom.newAtom(address);
		osc[1] = Atom.newAtom(message);
		outlet(2,osc);
	}
	public void outAudio(int audio) {
		LOGGER.log(Level.OFF, "AUDIO OUT");
		outlet(3,audio);
	}
	
	public String key() {
		return this.controlKey;
	}
	
	public int value() {
		return this.controlValue;
	}
	
	public void textOut(String text) {
		post(text);
	}

	public BeatTime getBeatTime() {
		return beatTime;
	}

	public void setBeatTime(BeatTime beatTime) {
		this.beatTime = beatTime;
	}	
}
