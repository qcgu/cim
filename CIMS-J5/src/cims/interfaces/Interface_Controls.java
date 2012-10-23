package cims.interfaces;

//import java.util.ArrayList;
import java.util.HashMap;

import cims.CimsMaxIO;


public class Interface_Controls {
	private CimsMaxIO io;
	private String[] lastOscData;
	private HashMap<String,Float> activityWeights;
	
	public Interface_Controls(CimsMaxIO io) {
		this.io = io;
		activityWeights = new HashMap<String,Float>();
	}
	
	public void updateInterfaceValues(String[] oscData) {
		this.lastOscData = oscData;
		this.parseOscData();
		this.io.interfaceUpdated();
	}
	
	public String[] getLastOscData() {
		return lastOscData;
	}

	public HashMap<String,Float> getActivityWeights() {
		return activityWeights;
	}

	public void setActivityWeights(HashMap<String,Float> activityWeights) {
		this.activityWeights = activityWeights;
		this.io.interfaceUpdated();
	}
	
	public Float getActivityWeightFor(String activityName) {
		return this.activityWeights.get(activityName);
	}
	
	public void setActivityWeightAs(String activityName,Float weight) {
		this.activityWeights.put(activityName, weight);
		this.io.interfaceUpdated();
	}

	private void parseOscData() {
		String controller;
		Float value;
		System.out.println("SPLIT: "+this.lastOscData[0]);
		String[] oscAddress = this.lastOscData[0].split("/");
		if(oscAddress.length==3) {
			controller = oscAddress[2];	
			value = Float.valueOf(this.lastOscData[1]);
			System.out.println("CONTROL: "+controller+" VALUE: "+value);
			if(controller.equalsIgnoreCase("slider1")) setActivityWeightAs("repeatWeight",value);
			
		}
	}
	
	

}
