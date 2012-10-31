package cims.interfaces;

import java.util.HashMap;

import cims.CimsMaxIO;

import java.util.ArrayList;
import java.util.HashMap;

import cims.CimsMaxIO;
import java.util.HashMap;

import cims.CimsMaxIO;


public class Interface_Controls {
	private CimsMaxIO io;
	private String[] lastOscData;
	private HashMap<String,Double> activityWeights;
	private static String[] activityWeightNames = {"repeatWeight","initiateWeight","supportWeight","mirrorWeight","silenceWeight"};
	private String oscDeviceAddress;
	private String sysMessage;
	
	public Interface_Controls(CimsMaxIO io) {
		this.io = io;
		activityWeights = new HashMap<String,Double>();
		initialiseControls();
	}
	
	public void initialiseControls() {
		setOscDeviceAddress("CIMiPad1");
		for(int i=0;i<activityWeightNames.length;i++) {
			this.activityWeights.put(activityWeightNames[i], 0.5);
		}
		setSysMessage("Welcome to CIM");
		sendSysMessageToInterface();
	}
	
	public void updateInterfaceValues(String[] oscData) {
		this.lastOscData = oscData;
		this.parseOscData();
		this.io.interfaceUpdated();
	}
	
	public void sendSysMessageToInterface() {
		String address = "/"+getOscDeviceAddress()+"/sysMessage";
		this.io.outOscSysMessage(address,getSysMessage());
	}
	
	public void sendControlMessageToInterface(String controller,ArrayList<Object> params) {
		String address = "/"+getOscDeviceAddress()+"/"+controller;
		this.io.sendInterfaceUpdate(address, params);
	}
	
	public String[] getLastOscData() {
		return lastOscData;
	}

	public String getSysMessage() {
		return this.sysMessage;
	}
	
	public void setSysMessage(String message) {
		this.sysMessage = message;
	}
	
	public HashMap<String,Double> getActivityWeights() {
		return activityWeights;
	}

	public void setActivityWeights(HashMap<String,Double> activityWeights) {
		this.activityWeights = activityWeights;
		//this.io.interfaceUpdated();
	}
	
	public Double getActivityWeightFor(String activityName) {
		return this.activityWeights.get(activityName);
	}
	
	public void setActivityWeightAs(String activityName,Double weight) {
		this.activityWeights.put(activityName, weight);
		this.io.interfaceUpdated();
	}

	private void parseOscData() {
		String controller = "";
		System.out.println("SPLIT: "+this.lastOscData[0]);
		String[] oscAddress = this.lastOscData[0].split("multifader1/");
		if(oscAddress.length>1) {
			controller = activityWeightNames[(Integer.valueOf(oscAddress[1])-1)];
			setActivityWeightAs(controller,Double.valueOf(this.lastOscData[1]));
		}
		String weightChange = " FADER: "+controller+" VALUE: "+this.lastOscData[1];
		setSysMessage(weightChange);
		sendSysMessageToInterface();
	}

	public String getOscDeviceAddress() {
		return oscDeviceAddress;
	}

	public void setOscDeviceAddress(String oscDeviceAddress) {
		this.oscDeviceAddress = oscDeviceAddress;
	}
	
	public String[] getActivityWeightNames() {
		return activityWeightNames;
	}

}
