package com.capgemini.hackathon.device.simulation.bo;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Emergency;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.device.DeviceClient;

public class Ambulance extends Vehicle {

	private EmergencyCommandHandler commandHandler = new EmergencyCommandHandler();

	public Ambulance(DeviceClientConfig deviceClientConfig, Object id) {
		super(deviceClientConfig, id);
	}

	@Override
	public void process() {
		while (true) {

			if (!commandHandler.interrupt()) {
				driveToDestination(Location.createRandomLocation(), commandHandler);
			} else {
				commandHandler.setInterrupt(false);
				System.out.println(
						"Ambulance " + getId() + ": Emergency" + commandHandler.getEmergency().getEmergencyId());
				driveToDestination(commandHandler.getEmergency().getLocation());
				System.out.println("Ambulance " + getId() + ": Emergency"
						+ commandHandler.getEmergency().getEmergencyId() + " reached");
				solveEmergency();
			}

		}

	}

	@Override
	protected void addMetainformationWhenPublishLocation(JsonObject event) {
		event.addProperty("isFree", String.valueOf(commandHandler.isFree()));
	}

	private void solveEmergency() {
		Emergency emergency = commandHandler.getEmergency();
		commandHandler.reset();
		BORegistry.getInstance().getBoById(Hospital.HOSPITAL_ID, Hospital.class).solveEmergency(emergency);
		System.out.println("Ambulance " + getId() + ": Emergency" + emergency.getEmergencyId() + " solved");
	}

	public void sendToEmergency(Emergency emergency) {
		if (commandHandler.isFree()) {
			commandHandler.emergency(emergency);
		}
	}

	public boolean isFree() {
		return commandHandler.isFree();
	}

	private class EmergencyCommandHandler implements Interruption {

		private Emergency emergency;
		private boolean interrupt = false;

		@Override
		public boolean interrupt() {
			if(interrupt){
				//interrupt = false;
				return true;
			}
			return false;
		}

		public Emergency getEmergency() {
			return emergency;
		}

		public void setInterrupt(boolean interrupt){
			this.interrupt = interrupt;
		}
		
		
		public void reset() {
			emergency = null;
		}

		public void emergency(Emergency emergency) {
			this.emergency = emergency;
			this.interrupt = true;
		}

		public boolean isFree() {
			return this.emergency == null;
		}

	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {

	}

}
