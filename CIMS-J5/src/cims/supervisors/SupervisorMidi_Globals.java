package cims.supervisors;

import java.util.ArrayList;
import java.util.logging.Logger;

import cims.datatypes.MidiControlMessage;
import cims.datatypes.MidiControlMessageTable;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiSegment;
import cims.datatypes.MidiSegmentTable;
import cims.datatypes.MidiStatistics;

public class SupervisorMidi_Globals {
	
	public static MidiMessage sLastMidiMessage;
	public static ArrayList<MidiMessage> sMidiMessageList;
	public static MidiControlMessage sLastMidiControlMessage;
	public static MidiControlMessageTable sMidiControlMessageTable;
	
	public static MidiSegment sMidiSegment;
	public static MidiSegmentTable sMidiSegmentTable;
	public static MidiStatistics sMidiStats;
	public static long sMidiStartTime=0;
	public static int sSegmentGapDuration = 390;
	public static int sDefaultDuration = 250;
	
	// Static properties set by external control
	public static int sSegmentGap = 3;
	public static int sRepeatInterval = 0;
	public static boolean sMetronome = false;
	public static int sCurrentBeat = 0;
	public static long[] sBeatList={4,0,0,0,0,0,0,0,0,0,0,0,0};
	public static int sBeatsPerMinute = 120;
	public static int sTimeBetweenBeats = 0;
	public static int sNextPlay = 0;
	
	public static boolean sTestMode = false;
	
	public static final Logger LOGGER = Logger.getLogger(SupervisorMidi.class.getName());
	public static final int ON = 1;
	public static final int OFF = 0;
	
	public SupervisorMidi_Globals() {
		// TODO Auto-generated constructor stub
	}

}
