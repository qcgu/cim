/**
 * 
 */

/**
 * @author andrew
 *
 */
package cims.datatypes;
//import cims.*;

public class MidiMessage {

	public static final int CHANNEL_01 = 0;
	public static final int CHANNEL_02 = 1;
	public static final int CHANNEL_03 = 2;
	public static final int CHANNEL_04 = 3;
	public static final int CHANNEL_05 = 4;
	public static final int CHANNEL_06 = 5;
	public static final int CHANNEL_07 = 6;
	public static final int CHANNEL_08 = 7;
	public static final int CHANNEL_09 = 8;
	public static final int CHANNEL_10 = 9;
	public static final int CHANNEL_11 = 10;
	public static final int CHANNEL_12 = 11;
	public static final int CHANNEL_13 = 12;
	public static final int CHANNEL_14 = 13;
	public static final int CHANNEL_15 = 14;
	public static final int CHANNEL_16 = 15;
	
	public static final int NOTE_OFF = 128;
	public static final int NOTE_ON = 144;
	public static final int POLY_AFTERTOUCH = 160;
	public static final int CONTROL_CHANGE = 176;
	public static final int PROGRAM_CHANGE = 192;
	public static final int CHANNEL_AFTERTOUCH =208;
	public static final int PITCH_WHEEL = 224;
	
	public static final int SYSEX = 240;
	public static final int MIDI_TIMECODE_QTR_FRAME = 241;
	public static final int SONG_POSITION_POINTER = 242;
	public static final int SONG_SELECT_NUM = 243;
	public static final int TUNE_REQUEST = 246;
	public static final int EOX = 247;
	public static final int TIMING_CLOCK = 248;
	public static final int START = 250;
	public static final int CONTINUE = 251;
	public static final int STOP = 252;
	public static final int ACTIVE_SENSING = 254;
	public static final int SYSTEM_RESET = 255;
	
	
	public static int sMessagesCount;
	public static int sTotalNotesOn;
	
	public int messageNum;
	public int notesOnCount;
	
	public long timeMillis;
	public int status;
	public int pitch;
	public int velocity;
	public int pressure;
	public int noteOnOff;
	public int channel;
	public int controller;
	public int controlData;
	public int otherData1; // e.g. program change, pitch wheel LSB
	public int otherData2; // e.g. pitch wheel MSB
	public int messageType;
	public int dataByteLength;
	
	public MidiMessage() {
		this.clear();
	}
	
	public void set(int[] message) {
		sMessagesCount++;
		this.messageNum = MidiMessage.sMessagesCount;
		
		this.timeMillis = System.currentTimeMillis();
		this.status = message[0];
		switch(detectMessageType(this.status)) {
		case NOTE_ON:
			this.channel = this.status-NOTE_ON-1;
			this.pitch = message[1];
			this.velocity = message[2];
			if(this.velocity>0) {
				this.noteOnOff = 1;
				MidiMessage.sTotalNotesOn++; 
			} else {
				//Note off status in the form of zero velocity
				this.noteOnOff = 0;
				this.messageType = NOTE_OFF;
				MidiMessage.sTotalNotesOn--;
			}
			break;
		case NOTE_OFF:
			this.channel = this.status-NOTE_OFF-1;
			this.pitch = message[1];
			this.velocity = 0;
			this.noteOnOff = 0;
			MidiMessage.sTotalNotesOn--;
			break;
		case POLY_AFTERTOUCH:
			this.channel = this.status-POLY_AFTERTOUCH-1;
			this.pitch = message[1];
			this.pressure = message[2];
			break;
		case CONTROL_CHANGE:
			this.channel = this.status-CONTROL_CHANGE-1;
			this.controller = message[1];
			this.controlData = message[2];
			break;
		case PROGRAM_CHANGE:
			this.channel = this.status-PROGRAM_CHANGE-1;
			this.otherData1 = message[1];
			break;
		case CHANNEL_AFTERTOUCH:
			this.channel = this.status-CHANNEL_AFTERTOUCH-1;
			this.pressure = message[1];
			break;
		case PITCH_WHEEL:
			this.channel = this.status-PITCH_WHEEL-1;
			this.otherData1 = message[1];
			this.otherData2 = message[2];
			break;
			
		default:
			// Other status messages not implemented
			break;
		}
		this.notesOnCount = MidiMessage.sTotalNotesOn;
	}
	
	public int detectMessageType(int statusByte) {
		if(inTheRange(statusByte, NOTE_OFF, NOTE_OFF+CHANNEL_16)) this.messageType = NOTE_OFF;
		if(inTheRange(statusByte, NOTE_ON ,NOTE_ON+CHANNEL_16)) this.messageType = NOTE_ON;
		if(inTheRange(statusByte, POLY_AFTERTOUCH ,POLY_AFTERTOUCH+CHANNEL_16)) this.messageType = POLY_AFTERTOUCH;
		if(inTheRange(statusByte, CONTROL_CHANGE ,CONTROL_CHANGE+CHANNEL_16)) this.messageType = CONTROL_CHANGE;
		if(inTheRange(statusByte, PROGRAM_CHANGE ,PROGRAM_CHANGE+CHANNEL_16)) this.messageType = PROGRAM_CHANGE;
		if(inTheRange(statusByte, CHANNEL_AFTERTOUCH ,CHANNEL_AFTERTOUCH+CHANNEL_16)) this.messageType = CHANNEL_AFTERTOUCH;
		if(inTheRange(statusByte, PITCH_WHEEL ,PITCH_WHEEL+CHANNEL_16)) this.messageType = PITCH_WHEEL;
		
		if(this.messageType==PROGRAM_CHANGE || this.messageType==CHANNEL_AFTERTOUCH) {
			this.dataByteLength = 1;
		} else {
			this.dataByteLength = 2;
		}
		return this.messageType;
	}
	

	
	public void copy(MidiMessage newEvent) {
		this.messageNum = newEvent.messageNum;
		this.timeMillis = newEvent.timeMillis;
		this.status = newEvent.status;
		this.pitch = newEvent.pitch;
		this.velocity = newEvent.velocity;
		this.pressure = newEvent.pressure;
		this.noteOnOff = newEvent.noteOnOff;
		this.channel = newEvent.channel;
		this.controller = newEvent.controller;
		this.controlData = newEvent.controlData;
		this.otherData1 = newEvent.otherData1;
		this.otherData2 = newEvent.otherData2;
		this.messageType = newEvent.messageType;
		this.dataByteLength = newEvent.dataByteLength;
	}
	
	public void clear() {
		this.messageNum = 0;
		this.timeMillis = 0;
		this.status = 0;
		this.pitch = 0;
		this.velocity = 0;
		this.pressure = 0;
		this.noteOnOff = 0;
		this.channel = 0;
		this.controller = 0;
		this.controlData = 0;
		this.otherData1 = 0;
		this.otherData2 = 0;
		this.messageType =0;
		this.dataByteLength = 0;
	}
	
	public static boolean inTheRange(int value, int min, int max)
	{
	  return((value >= min) && (value <= max));
	}

}

