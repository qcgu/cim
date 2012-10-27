package cims.deciders;

import static cims.supervisors.SupervisorMidi_Globals.sNextPlay;
import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

import cims.generators.GenerateMidi_Segment;
import cims.supervisors.SupervisorMidi;

public class DecideMidi_SimpleRepeat {
	
	private SupervisorMidi supervisor;
	private GenerateMidi_Segment generator_segment;
	
	public DecideMidi_SimpleRepeat(SupervisorMidi supervisor) {
		//LOGGER.info("simpleRepeat");
		this.supervisor = supervisor;
		generator_segment = new GenerateMidi_Segment(supervisor);
	}
	
	public void repeatLastSegment() {
		//supervisor.txtMsg("Simple Repeat");
		generator_segment.makeLastSegment();
		generator_segment.generate(sNextPlay); 
		
	}

}
