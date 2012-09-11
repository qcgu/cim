package cims.analysers;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiMessage;
import cims.datatypes.MidiStatistics;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class AnalyseMidi_Stats extends AnalyseMidi {
	
	
	private MidiStatistics midiStats; 
	private int[] pitchHistogram = new int[12];

	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new MidiStatistics();
	}

	@Override
	public void analyse() {
		
		if (current_message.messageType == MidiMessage.NOTE_ON) { // note on message
			pitchHistogram[current_message.pitch % 12]++;
		}
		
		midiStats.addPitch(current_message.pitch);
		LOGGER.info("P_CUR: " + midiStats.current_pitch);
		LOGGER.info("P_MEAN: "+ midiStats.meanPitch);
		LOGGER.info("P_SD: "+ midiStats.deviationPitch);
		
		//Update static version of midiStats
		sMidiStats = midiStats;

	}
	
	public boolean isUnusual() {
		return false;
	}
	
	// choose a pitch from the histogram with weighted probability
	public int getRandomPitchValue() {
		int maxCnt = 0;
		for (int i=0; i< pitchHistogram.length; i++) {
			maxCnt += pitchHistogram[i];
		}
		int rnd = (int)(Math.random() * maxCnt);
		//LOGGER.info("mxCnt = " + maxCnt + " rnd = " + rnd);
		int val = 0;
		int pClass = -1;
		while (rnd >= val) {
			pClass++;
			val += pitchHistogram[pClass];
		} 
		return pClass;
	}
	
	public void clearPitchHistogram() {
		pitchHistogram = new int[12];
	}
}
