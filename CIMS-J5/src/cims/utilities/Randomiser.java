package cims.utilities;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.sActivityWeights;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

public class Randomiser {
	
	public Randomiser() {
		
	}
	
	public int positiveInteger(int maxValue) {
		int value =  (int) (Math.random() * (maxValue+1));
		return value;
	}
	
	public int getRandomPitchClass() {
		int pchLength = sMidiStats.getPitchClassHistogram().length; //Fixed at 12
		int maxCnt = 0;
		for (int i=0; i<pchLength; i++) {
			//System.out.println("pitchClass " + i + " = " + sMidiStats.getPitchClass(i));
			maxCnt += sMidiStats.getPitchClass(i);
		}
		int rnd = (int)(Math.random() * maxCnt);
		LOGGER.info("mxCnt = " + maxCnt + " rnd = " + rnd);
		
		int pitchClass = 0;
		int val = sMidiStats.getPitchClass(pitchClass);
		while(val < rnd) {
			pitchClass++;
			val += sMidiStats.getPitchClass(pitchClass);
		}
		//System.out.println("pitchClass returned is " + (pitchClass));
		return pitchClass;
	}
	
	public int weightedActivityChoice() {
		Float repeatWeight = sActivityWeights.get("repeatWeight");
		System.out.println("REPEAT WEIGHT SET: "+repeatWeight.toString());
		double initiateWeight = 0.5;
		double supportWeight = 0.5;
		double mirrorWeight = 0.5;
		double silenceWeight = 0.5;
		double totalWeight = repeatWeight + initiateWeight + supportWeight + mirrorWeight + silenceWeight;
		double rnd = Math.random() * totalWeight;
		int returnVal = 0;
		if (rnd < repeatWeight) {
			returnVal = 0;
		} else if (rnd < (repeatWeight + initiateWeight)) {
			returnVal = 1;
		} else if (rnd < (repeatWeight + initiateWeight + supportWeight)) {
			returnVal = 2;
		} else if (rnd < (repeatWeight + initiateWeight + supportWeight + mirrorWeight)) {
			returnVal = 3;
		} else {
			returnVal = 4;
		}
		return returnVal;
	}
	
}
