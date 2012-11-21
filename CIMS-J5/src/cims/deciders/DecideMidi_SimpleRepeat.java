package cims.deciders;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;

/*****************************************************************************************
 * This Decider is used mainly for testing purposes and simply repeats the last segment
 * @author Andrew Gibson a.gibson@griffith.edu.au
 *
 */
public class DecideMidi_SimpleRepeat {
	
	private GenerateMidi_Segment generator_segment;
	public static Logger LOGGER = Logger.getLogger(DecideMidi_SimpleRepeat.class);
	
	public DecideMidi_SimpleRepeat(SupervisorMidi supervisor) {
		LOGGER.setLevel(Level.INFO);
		generator_segment = new GenerateMidi_Segment(supervisor);
		
	}
	
	public void segmentCreated() {
		LOGGER.debug("Simple Repeat");
		generator_segment.makeLastSegment();
		generator_segment.generate(sNextPlay); 
		
	}

}
