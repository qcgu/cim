/**
 * CIMS - CaptureMIDI - Take raw MIDI data and capture in a form that can be interpreted and reproduced.
 * 
 */

package cims.capturers;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;

/*****************************************************************************************
 * Provides a mechanism to capture raw incoming midi data and form it into MidiMessage
 * objects
 */
public class CaptureMidi {

	private SupervisorMidi supervisor;
	
	private int[] midiData;
	private int midiByte;
	private MidiMessage midiMessage;
	
	public static Logger LOGGER = Logger.getLogger(CaptureMidi.class);
	
	public CaptureMidi(SupervisorMidi supervisor) {
		this.supervisor = supervisor;
		this.midiData = new int[3];
		this.midiByte = 0;
		this.midiMessage = new MidiMessage();
		LOGGER.setLevel(Level.INFO);
	}
	
	public void in(int arg) {
		LOGGER.debug("CaptureMidi - Data In");
		if(arg>=MidiMessage.NOTE_OFF) {
			LOGGER.debug("STATUS: "+arg);
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
			LOGGER.debug("DATA: "+arg);
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
				LOGGER.error("Bad number of data bytes");
				midiByte=0;
					
			}
		}
		
	}

	public void finalMessage() {
		midiMessage.set(midiData);
		LOGGER.debug("CAPTURE TYPE: "+midiMessage.messageType+" DATA: "+midiData[1]+","+midiData[2]);
		this.supervisor.addMidiMessage(midiMessage);
		this.supervisor.midiThru(midiData); //Send raw Midi data back out thru port (in Max via supervisor and CimsMaxIO)
		midiByte = 0;
	}
}

