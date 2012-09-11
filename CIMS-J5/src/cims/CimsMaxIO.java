/*
 * Main IO Class that is embedded in Max as an MXJ Object
 * 		All other objects talk to Max through CimsMaxIO
 */

package cims;

import java.util.logging.*;

import cims.supervisors.*;
import com.cycling74.max.*;

public class CimsMaxIO extends MaxObject {
	private SupervisorMidi superMidi;
	private SupervisorOsc superOsc;
	private SupervisorAudio superAudio;
	
	private int midiData = 0;
	private int	oscData = 0;
	private int audioData = 0;
	
	private String controlKey = "";
	private int controlValue = 0;
	
	private static final Logger LOGGER = Logger.getLogger(CimsMaxIO.class.getName());

	public CimsMaxIO() {
		declareIO(4,4); 
		createInfoOutlet(false); // Right most outlet not required	
		superMidi = new SupervisorMidi(this);
		LOGGER.setLevel(Level.WARNING);
	}
	
	public void controlParams(Atom[] args) {
		controlKey=args[0].toString();
		controlValue=args[1].toInt();
		LOGGER.log(Level.INFO, "KEY: "+controlKey+" VALUE: "+controlValue);
		superMidi.controlIn();
	}
	
	public void inlet(int arg) {	     
		int current_inlet = getInlet();
		switch(current_inlet) {
		case 0:
			LOGGER.log(Level.INFO, "MIDI IN");
			this.midiData = arg;
			superMidi.dataIn();
			break;
		case 1:
			LOGGER.log(Level.INFO, "OSC IN");
			this.oscData = arg;
			superOsc.dataIn();
			break;
		case 2:
			LOGGER.log(Level.INFO, "AUDIO IN");
			this.audioData = arg;
			superAudio.dataIn();
			break;
		case 3:
			LOGGER.log(Level.INFO, "CONTROL IN");
		}
		
	}
	
	public int inMidi() {
		return this.midiData;
	}
	public int inOsc() {
		return this.oscData;
	}
	public int inAudio() {
		return this.audioData;
	}
	
	public void outMidi(int[] midi) {
		LOGGER.log(Level.INFO, "MIDI OUT");
		outlet(0,midi);
	}
	public void outOsc(int osc) {
		LOGGER.log(Level.INFO, "OSC OUT");
		outlet(2,osc);
	}
	public void outAudio(int audio) {
		LOGGER.log(Level.INFO, "CONTROL OUT");
		outlet(2,audio);
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
