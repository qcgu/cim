package cims;

import cims.supervisors.*;

import com.cycling74.max.*;

public class CimsMaxIO extends MaxObject {
	private SupervisorMidi superMidi;
	private SupervisorOsc superOsc;
	private SupervisorAudio superAudio;
	
	private int midiData;
	private int	oscData;
	private int audioData;

	public CimsMaxIO() {
		declareIO(4,4);
		createInfoOutlet(false); // Right most outlet not required	
		superMidi = new SupervisorMidi(this);
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
			SupervisorMidi.sSilenceDelay = arg;
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
	
	public void textOut(String text) {
		post(text);
	}
	


}
