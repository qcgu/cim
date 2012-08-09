/**
 * CIMS - CaptureMIDI - Take raw MIDI data and capture in a form that can be interpreted and reproduced.
 * 
 */

/**
 * @author Andrew Gibson andrew@gibsons.id.au
 *
 */
package cims.capturers;

import cims.datatypes.MidiMessage;
//import cims.*;
//import cims.datatypes.*;
import cims.supervisors.SupervisorMidi;


public class CaptureMidi {

	private SupervisorMidi supervisor;
	
	private int[] midiData;
	private int midiByte;
	private MidiMessage midiMessage;
	
	public CaptureMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.midiData = new int[3];
		this.midiByte = 0;
		this.midiMessage = new MidiMessage();
	}
	
	public void in(int arg) {
		//this.supervisor.txtMsg("ARG: "+arg);
		if(arg>=MidiMessage.NOTE_OFF) {
			//Status byte 
			//this.supervisor.txtMsg("STATUS: "+arg);
			midiMessage.detectMessageType(arg);
			midiData[0] = arg;
			if(midiMessage.dataByteLength>0) {
				midiByte = 1;
			} else {
				midiData[1] = 0;
				midiData[2] = 0;
				this.finalMessage();
			}
		} else {
			//Data byte
			//this.supervisor.txtMsg("DATA: "+arg);
			switch(midiByte) {
			case 1:
				midiData[1] = arg;
				if(midiMessage.dataByteLength==1) {
					midiData[2] = 0;
					this.finalMessage();
				} else {
					midiByte = 2;
				}
				break;
			case 2:
				midiData[2] = arg;
				this.finalMessage();
				break;
			default:
				this.supervisor.txtMsg("ERROR: Bad number of data bytes");
				midiByte=0;
					
			}
		}
		
	}

	public void finalMessage() {
		
		midiMessage.set(midiData);
		this.supervisor.addMidiMessage(midiMessage);
		//this.supervisor.txtMsg("TYPE: "+midiMessage.messageType+" DATA: "+midiData[1]+","+midiData[2]);
		midiByte = 0;
	}
}

