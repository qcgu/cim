/*
 * Main IO Class that is embedded in Max as an MXJ Object
 * 		All other objects talk to Max through CimsMaxIO
 */

package cims;

import java.util.logging.*;

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
	private byte audioData;;
	
	private String controlKey = "";
	private int controlValue = 0;
	
	private static final Logger LOGGER = Logger.getLogger(CimsMaxIO.class.getName());
	
	private static final int MIDI = 0;
	private static final int OSC = 1;
	private static final int AUDIO = 2;
	private static final int CONTROL = 3;
	

	public CimsMaxIO() {
		declareIO(1,3); 
		createInfoOutlet(false); // Right most outlet not required	
		controls = new Interface_Controls(this);
		superMidi = new SupervisorMidi(this,controls);
		superOsc = new SupervisorOsc(this,controls);
		LOGGER.setLevel(Level.OFF); //INFO
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
			break;
		case AUDIO:
			System.out.println("AUDIO: " + args[0]);
			break;
		case CONTROL:
			controlKey=args[0].toString();
			controlValue=args[1].toInt();
			LOGGER.log(Level.OFF, "KEY: "+controlKey+" VALUE: "+controlValue);
			superMidi.controlIn();
			break;			
		}
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
	public void outOsc(String[] osc) {
		LOGGER.log(Level.INFO, osc[0]+osc[1]);
		Atom[] oscOutMessage = new Atom[2];
		oscOutMessage[0] = Atom.newAtom(osc[0]);
		oscOutMessage[1] = Atom.newAtom(Float.valueOf(osc[1]));
		outlet(2,oscOutMessage);
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
}
