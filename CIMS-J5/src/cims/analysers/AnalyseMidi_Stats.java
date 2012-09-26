package cims.analysers;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiStatistics;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class AnalyseMidi_Stats extends AnalyseMidi {
	
	
	private MidiStatistics midiStats; 
	

	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new MidiStatistics();
	}

	@Override
	public void analyse() {

		if (current_message.messageType == MidiMessage.NOTE_ON) { // note on message
			midiStats.addPitch(current_message.pitch);
			midiStats.addOnset(current_message.timeMillis);
			int density = midiStats.getOnsetIntervalTrend();
			if (density > 1 && density < 5) {
				System.out.println("ANALYSE MIDI STATS: density break detected.");
				supervisor.densitySegmentBreak();
			}
		}

		LOGGER.info("P_CUR: " + midiStats.getCurrent_pitch());
		LOGGER.info("P_MEAN: "+ midiStats.getMeanPitch());
		LOGGER.info("P_SD: "+ midiStats.getDeviationPitch());
		
		//Update static version of midiStats
		sMidiStats = midiStats;

	}
	
	public boolean isUnusual() {
		return false;
	}
	
}
