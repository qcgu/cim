package cims.analysers;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiStatistics;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;
//import static cims.supervisors.SupervisorMidi_Globals.sSegmentGapDuration;

public class AnalyseMidi_Stats extends AnalyseMidi {
	
	private MidiStatistics midiStats; 
	private int density;
	private int segmentStart;
	private int segmentEnd;
	public boolean segmentStarted; 

	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new MidiStatistics();
		density = 0;
	}

	@Override
	public void analyse() {

		if (current_message.messageType == MidiMessage.NOTE_ON) { // note on message
			midiStats.addPitch(current_message.pitch);
			midiStats.addOnset(current_message.timeMillis);
			density = midiStats.getOnsetIntervalTrend();
		}

		LOGGER.info("P_CUR: " + midiStats.getCurrent_pitch());
		LOGGER.info("P_MEAN: "+ midiStats.getMeanPitch());
		LOGGER.info("P_SD: "+ midiStats.getDeviationPitch());
		
		//Update static version of midiStats
		sMidiStats = midiStats;
		
		if(this.current_message.canBeSegmentStart()) {

			if(!segmentStarted) { // START OF DENSITY SEGMENT
				LOGGER.info("START DENSITY SEGMENT");
				this.segmentStart = this.current_message.messageNum;
				segmentStarted = true;
			}
		} else if(this.current_message.canBeSegmentEnd()) {
			if(segmentStarted) {
				//This is the first all notes off event - Do we want this here??
				if (density > 1 && density < 5) {
					System.out.println("ANALYSE MIDI STATS: density break detected.");
					this.segmentEnd = this.current_message.messageNum;
					this.densitySegmentBreak();
				}
			}			
		} 

	}
	
	public void densitySegmentBreak()  {
		LOGGER.info("DENSITY BREAK DETECTED >> segmentStart: "+segmentStart+" segmentEnd: "+segmentEnd);		
		//supervisor.addMidiSegment(this.segmentStart, this.segmentEnd);
		segmentStarted = false;
	}
	
	public boolean isUnusual() {
		return false;
	}
	
}
