package cims.analysers;

//import java.util.*;
import cims.analysers.AnalyseMidi;
import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import cims.utilities.SilenceTimer;

public class AnalyseMidi_Silence extends AnalyseMidi {

	private int segmentStart;
	private int segmentEnd;
	private int notesCount;
	private boolean segmentStarted;
	private SilenceTimer silenceTimer;
	
	public AnalyseMidi_Silence(SupervisorMidi supervisor) {
		super(supervisor);
		silenceTimer = new SilenceTimer(this);
		segmentStart = 1;
		segmentStarted = false;
	}
	
	public void analyse() {
		notesCount = MidiMessage.sTotalNotesOn;
		//supervisor.txtMsg("Analysing Silence - NotesOnCount:"+notesCount);
			if(this.current_message.noteOnOff==1) {
				// Event is note on
				// Cancel any running silence timer
				silenceTimer.cancel();
				if(!segmentStarted) {
					//This is the first note of the segment
					this.segmentStart = this.current_message.messageNum;
					segmentStarted = true;
				}
			} else if(notesCount==0) {
				// All notes are off
				if(segmentStarted) {
					//This is the first all notes off event
					this.segmentEnd = this.current_message.messageNum;
					//Start silence timer
					silenceTimer.start(SupervisorMidi.sSilenceDelay);
				}			
			} 
	}
	
	public void silent() {
		//supervisor.txtMsg("SEGMENT START: "+segmentStart+" END: "+MidiMessage.messagesCount);		
		supervisor.addMidiSegment(this.segmentStart, this.segmentEnd);
		segmentStarted = false;
	}
}
