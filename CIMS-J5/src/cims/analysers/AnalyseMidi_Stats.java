package cims.analysers;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiStatistics;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;

public class AnalyseMidi_Stats extends AnalyseMidi {
	
	private MidiStatistics midiStats; 
	private int density;
	private int segmentStart;
	private int segmentEnd;
	public boolean segmentStarted; 
	
	public static final Logger LOGGER = Logger.getLogger(AnalyseMidi.class);

	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		
		if(sMidiStats==null) {
			midiStats = new MidiStatistics();
		} else {
			this.midiStats = sMidiStats;
		}
		density = 0;
		LOGGER.setLevel(Level.INFO);
	}

	@Override
	public void analyse() {

		if (current_message.messageType == MidiMessage.NOTE_ON) { // note on message
			LOGGER.debug("pitch: "+current_message.pitch);
			midiStats.addPitch(current_message.pitch);
			midiStats.addOnset(current_message.timeMillis);
			density = midiStats.getOnsetIntervalTrend();
		}

		LOGGER.debug("P_CUR: " + midiStats.getCurrent_pitch());
		LOGGER.debug("P_MEAN: "+ midiStats.getMeanPitch());
		LOGGER.debug("P_SD: "+ midiStats.getDeviationPitch());
		
		//Update static version of midiStats
		sMidiStats = midiStats;
		LOGGER.debug("sMidiStats updated. pitchClass: "+sMidiStats.getPitchClassHistogram()[(sMidiStats.getCurrent_pitch()%12)] + "currentPitch: "+sMidiStats.getCurrent_pitch());
		LOGGER.debug("sMidiStats pitchHistogram: " +sMidiStats.getPitchHistogramAsString());
		if(this.current_message.canBeSegmentStart()) {

			if(!segmentStarted) { // START OF DENSITY SEGMENT
				LOGGER.debug("START DENSITY SEGMENT");
				this.segmentStart = this.current_message.messageNum;
				segmentStarted = true;
			}
		} else if(this.current_message.canBeSegmentEnd()) {
			if(segmentStarted) {
				//This is the first all notes off event - Do we want this here??
				if (density > 1 && density < 5) {
					LOGGER.debug("ANALYSE MIDI STATS: density break detected.");
					this.segmentEnd = this.current_message.messageNum;
					this.densitySegmentBreak();
				}
			}			
		} 
	}
	
	public void densitySegmentBreak()  {
		LOGGER.debug("DENSITY BREAK DETECTED >> segmentStart: "+segmentStart+" segmentEnd: "+segmentEnd);		
		//supervisor.addMidiSegment(this.segmentStart, this.segmentEnd);
		segmentStarted = false;
	}
	
	public boolean isUnusual() {
		return false;
	}
	
}
