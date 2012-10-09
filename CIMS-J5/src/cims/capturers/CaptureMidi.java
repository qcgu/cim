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
import cims.supervisors.SupervisorMidi;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;


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
		if(arg>=MidiMessage.NOTE_OFF) {
			//LOGGER.info("STATUS: "+arg);
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
			//LOGGER.info("DATA: "+arg);
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
				LOGGER.severe("Bad number of data bytes");
				midiByte=0;
					
			}
		}
		
	}

	public void finalMessage() {
		midiMessage.set(midiData);
		this.supervisor.addMidiMessage(midiMessage);
		this.supervisor.dataThru(midiData); //Send Midi message back out thru port (in Max)
		LOGGER.info("CAPTURE TYPE: "+midiMessage.messageType+" DATA: "+midiData[1]+","+midiData[2]);
		midiByte = 0;
	}
}

