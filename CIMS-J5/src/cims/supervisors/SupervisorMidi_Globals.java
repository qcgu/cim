package cims.supervisors;

import java.util.ArrayList;
import java.util.HashMap;

import cims.datatypes.BeatTime;
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
	public static HashMap<String,Double> sActivityWeights;
	public static BeatTime sCurrentBeatTime;
	
	public static final int ON = 1;
	public static final int OFF = 0;
	
	
	/****
	 *  The following properties should be returned by methods, as their implementation may change over time
	 *  They should be considered temporary!!
	 */
	public static long sMidiStartTime=0;
	public static int sSegmentGapDuration = 390;
	public static int sSegmentGap = 3;
	public static int sRepeatInterval = 0;
	public static int sNextPlay = 0;
	public static int[] sPitchClassSet = {0,2,4,5,7,9,11}; // scale, root note first in list offset by sRootPitch
	public static int sRootPitch = 0; // offset to the scale
	public static int[] sCurrentChord = {0, 4, 7}; // chromatic pitch class set, no offset - absolute pitch classes

	public static boolean sMetronome = false;
	public static boolean sTestMode = false;
	
}
