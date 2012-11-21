package cims.analysers;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import cims.analysers.AnalyseMidi;
import cims.datatypes.MidiMessage;
import cims.supervisors.SupervisorMidi;
import cims.utilities.SilenceTimer;

import static cims.supervisors.SupervisorMidi_Globals.sSegmentGapDuration;

/*****************************************************************************************
 * Analyses incoming Midi messages waiting for a gap of specified length. Once a gap is
 * detected, a segment
 * @author andrew
 *
 */
public class AnalyseMidi_Silence extends AnalyseMidi {

	private int segmentStart;
	private int segmentEnd;
	public boolean segmentStarted;
	private SilenceTimer silenceTimer;
	
	public static final Logger LOGGER = Logger.getLogger(AnalyseMidi.class);
	
	public AnalyseMidi_Silence(SupervisorMidi supervisor) {
		super(supervisor);
		silenceTimer = new SilenceTimer(this);
		segmentStart = 1;
		segmentStarted = false;	
		LOGGER.setLevel(Level.INFO);
	}
	
	/*****************************************************************************************
	 * The main worker method called by the supervisor. Registers the start of new segments 
	 * and handles the SilenceTimer which in turn calls silent() when it expires indicating
	 * the end of the segment.
	 */
	public void analyse() {	
		LOGGER.debug("Analysing Silence - NotesOnCount:"+MidiMessage.sTotalNotesOn+
				" noteOnOff: "+ this.current_message.noteOnOff + 
				" timeMillis: "+this.current_message.timeMillis+
				" controller: "+this.current_message.controller+ 
				" sustain: "+MidiMessage.sSustainOn);
			if(this.current_message.canBeSegmentStart()) {
				silenceTimer.cancel();
				if(!segmentStarted) {
					LOGGER.debug("START SEGMENT");
					this.segmentStart = this.current_message.messageNum;
					segmentStarted = true;
				}
			} else if(this.current_message.canBeSegmentEnd()) {
				if(segmentStarted) {
					//This is the first all notes off event
					this.segmentEnd = this.current_message.messageNum;
					//Start silence timer
					sSegmentGapDuration = 250;
					LOGGER.debug("START TIMER with gap: "+sSegmentGapDuration);
					silenceTimer.start(sSegmentGapDuration);
				}			
			} 
	}
	
	/*****************************************************************************************
	 * Called by the silence timer when the preset time expires indicating a gap between notes.
	 */
	public void silent() {
		LOGGER.debug("SILENCE DETECTED >> segmentStart: "+segmentStart+" segmentEnd: "+segmentEnd);		
		supervisor.addMidiSegment(this.segmentStart, this.segmentEnd,this.getClass());
		segmentStarted = false;
	}
}
