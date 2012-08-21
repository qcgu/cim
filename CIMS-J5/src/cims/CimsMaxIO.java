/*
 * Main IO Class that is embedded in Max as an MXJ Object
 * 		All other objects talk to Max through CimsMaxIO
 */

package cims;

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

	public CimsMaxIO() {
		//declareIO(4,4);
		declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL,DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL,DataTypes.ALL});
        
		createInfoOutlet(false); // Right most outlet not required	
		superMidi = new SupervisorMidi(this);
	}
	
	public void controlParams(Atom[] args) {
		controlKey=args[0].toString();
		controlValue=args[1].toInt();
		//this.textOut("K: "+controlKey+" V: "+controlValue);
		superMidi.controlIn();
	}
	
	public void inlet(int arg) {	     
		int current_inlet = getInlet();
		switch(current_inlet) {
		case 0:
			this.midiData = arg;
			superMidi.dataIn();
			break;
		case 1:
			this.oscData = arg;
			superOsc.dataIn();
			break;
		case 2:
			this.audioData = arg;
			superAudio.dataIn();
			break;
		case 3:
			//this.textOut(">>CONTROL");
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
		outlet(0,midi);
	}
	public void outOsc(int osc) {
		outlet(2,osc);
	}
	public void outAudio(int audio) {
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
