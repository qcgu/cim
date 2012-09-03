package cims.analysers;

import cims.supervisors.SupervisorMidi;
import cims.datatypes.MidiStatistics;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;


public class AnalyseMidi_Stats extends AnalyseMidi {
	
	
	private MidiStatistics midiStats; 
	private int[] pitchHistogram = new int[12];

	public AnalyseMidi_Stats(SupervisorMidi supervisor) {
		super(supervisor);
		midiStats = new MidiStatistics();
	}

	@Override
	public void analyse() {
		midiStats.addPitch(current_message.pitch);
		if (current_message.messageType == 144) { // note on message
			pitchHistogram[current_message.pitch % 12]++;
		}
		//this.supervisor.txtMsg("Rnd Pitcht = " + getRandomPitch());
		//this.supervisor.txtMsg("P_CUR: " + midiStats.current_pitch);
		//this.supervisor.txtMsg("P_MEAN: "+ midiStats.meanPitch);
		//this.supervisor.txtMsg("P_SD: "+ midiStats.deviationPitch);
		
		//Update static version of midiStats
		sMidiStats = midiStats;

	}
	
	public boolean isUnusual() {
		return false;
	}
	
	// choose a pitch from the histogram with weighted probability
	public int getRandomPitchClass() {
		int maxCnt = 0;
		for (int i=0; i< pitchHistogram.length; i++) {
			maxCnt += pitchHistogram[i];
		}
		int rnd = (int)(Math.random() * maxCnt);
		//this.supervisor.txtMsg("mxCnt = " + maxCnt + " rnd = " + rnd);
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
