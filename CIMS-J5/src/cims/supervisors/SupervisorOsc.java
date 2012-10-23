package cims.supervisors;

import static cims.supervisors.SupervisorMidi_Globals.LOGGER;

import java.util.logging.Level;

import cims.CimsMaxIO;
//import cims.interfaces.Interface_Controls;
import cims.interfaces.Interface_Controls;

public class SupervisorOsc  {
	private CimsMaxIO io;
	private Interface_Controls controls;
	
	public SupervisorOsc(CimsMaxIO io) {
		this.io = io;
		controls = new Interface_Controls(io);
	}
	
	public SupervisorOsc(CimsMaxIO io, Interface_Controls controls) {
		this.io = io;
		this.controls = controls;
	}
	
	public void dataIn() {
		String[] oscData = this.io.inOsc();
		controls.updateInterfaceValues(oscData);
		LOGGER.log(Level.OFF, "SupervisorOsc DataIN: "+oscData[0]);
		//System.out.println("OSC: " + oscData[0] +" value: " + oscData[1]);
		//this.dataOut(oscData);
	}
	
	public void dataOut(String[] data) {
		this.io.outOsc(data);
	}
	
	public void setInterfaceControls(Interface_Controls newControls) {
		this.controls = newControls;
	}

}
