package cims.analysers;

//import java.util.*;
import cims.analysers.AnalyseMidi;
import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import cims.utilities.SilenceTimer;

import static cims.supervisors.SupervisorMidi_Globals.sSegmentGapDuration;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;
import static cims.supervisors.SupervisorMidi_Globals.ON;
//import static cims.supervisors.SupervisorMidi_Globals.OFF;

public class AnalyseMidi_Silence extends AnalyseMidi {

	private int segmentStart;
	private int segmentEnd;
	private int notesCount;
	public boolean segmentStarted;
	private SilenceTimer silenceTimer;
	
	public AnalyseMidi_Silence(SupervisorMidi supervisor) {
		super(supervisor);
		silenceTimer = new SilenceTimer(this);
		segmentStart = 1;
		segmentStarted = false;
	}
	
	public void analyse() {
		notesCount = MidiMessage.sTotalNotesOn;	
		LOGGER.info("Analysing Silence - NotesOnCount:"+notesCount+" noteOnOff: "+ this.current_message.noteOnOff);
			if(this.current_message.noteOnOff==ON) {
				silenceTimer.cancel();
				if(!segmentStarted) {
					LOGGER.info("START SEGMENT");
					this.segmentStart = this.current_message.messageNum;
					segmentStarted = true;
				}
			} else if(notesCount==0) {
				if(segmentStarted) {
					//This is the first all notes off event
					this.segmentEnd = this.current_message.messageNum;
					//Start silence timer
					silenceTimer.start(sSegmentGapDuration);
				}			
			} 
	}
	
	public void silent() {
		LOGGER.info("SILENCE DETECTED >> segmentStart: "+segmentStart+" segmentEnd: "+segmentEnd);		
		supervisor.addMidiSegment(this.segmentStart, this.segmentEnd);
		segmentStarted = false;
	}
}
