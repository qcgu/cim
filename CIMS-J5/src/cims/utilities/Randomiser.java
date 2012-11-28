package cims.utilities;

import static cims.supervisors.SupervisorMidi_Globals.sMidiStats;
import static cims.supervisors.SupervisorMidi_Globals.sActivityWeights;

import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

//import cims.generators.GenerateMidi;

public class Randomiser {
	public static final Logger LOGGER = Logger.getLogger(Randomiser.class.getName());
	
	public Randomiser() {
		LOGGER.setLevel(Level.DEBUG);
	}
	
	public int positiveInteger(int maxValue) {
		int value =  (int) (Math.random() * (maxValue+1));
		return value;
	}
	
	public int getRandomPitchClass() {
		int pchLength = sMidiStats.getPitchClassHistogram().length; //Fixed at 12
		int maxCnt = 0;
		for (int i=0; i<pchLength; i++) {
			LOGGER.debug("pitchClass " + i + " = " + sMidiStats.getPitchClass(i));
			maxCnt += sMidiStats.getPitchClass(i);
		}
		int rnd = (int)(Math.random() * maxCnt);
		LOGGER.debug("mxCnt = " + maxCnt + " rnd = " + rnd);
		
		int pitchClass = 0;
		int val = sMidiStats.getPitchClass(pitchClass);
		while(val < rnd) {
			pitchClass++;
			val += sMidiStats.getPitchClass(pitchClass);
		}
		LOGGER.debug("pitchClass returned is " + (pitchClass));
		return pitchClass;
	}
	

	public int weightedActivityChoice() {
		Double repeatWeight = sActivityWeights.get("repeatWeight");
		//System.out.println("repeatWeight SET: "+repeatWeight.toString());
		Double initiateWeight = sActivityWeights.get("initiateWeight");
		//System.out.println("initiateWeight SET: "+initiateWeight.toString());
		Double supportWeight = sActivityWeights.get("supportWeight");
		//System.out.println("supportWeight SET: "+supportWeight.toString());
		Double mirrorWeight = sActivityWeights.get("mirrorWeight");
		//System.out.println("mirrorWeight SET: "+mirrorWeight.toString());
		Double silenceWeight = sActivityWeights.get("silenceWeight");
		//System.out.println("silenceWeight SET: "+silenceWeight.toString());
		
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
	
	// note! This might be bodgy!!
	public double gaussian(double mean, double sd) {
		Random rand = new Random();
		double val = rand.nextGaussian();
		return val * sd + mean;
	}
	
}
