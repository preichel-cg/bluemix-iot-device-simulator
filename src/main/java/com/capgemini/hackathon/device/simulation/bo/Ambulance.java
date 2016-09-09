package com.capgemini.hackathon.device.simulation.bo;

import com.capgemini.hackathon.device.service.DeviceConfig;
import com.capgemini.hackathon.device.simulation.ApplicationClientConfig;
import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Emergency;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.device.DeviceClient;

public class Ambulance extends Vehicle {

	private EmergencyCommandHandler commandHandler = new EmergencyCommandHandler();
	private String hospitalDeviceId;
	private String hopstialDeviceType;

	public Ambulance(DeviceClientConfig deviceClientConfig, ApplicationClientConfig appClientConfig, String metadata) {
		super(deviceClientConfig, appClientConfig);

		JsonObject json = new JsonParser().parse(metadata).getAsJsonObject();
		hospitalDeviceId = json.get(DeviceConfig.DEVICE_ID).getAsString();
		hopstialDeviceType = json.get(DeviceConfig.TYPE_ID).getAsString();

	}

	@Override
	protected void configureAppplicationClient(ApplicationClient applicationClient) {
		applicationClient.setEventCallback(commandHandler);
		applicationClient.subscribeToDeviceEvents();
	}

	@Override
	public void process() {
		while (true) {

			if (!commandHandler.interrupt()) {
				driveToDestination(Location.createRandomLocation(), commandHandler);
			} else {
				System.out.println(
						"Ambulance " + getVin() + ": Emergency" + commandHandler.getEmergency().getEmergencyId());
				driveToDestination(commandHandler.getEmergency().getLocation());
				System.out.println("Ambulance " + getVin() + ": Emergency"
						+ commandHandler.getEmergency().getEmergencyId() + " reached");
				solveEmergency();
			}

		}

	}

	@Override
	protected void addMetainformationWhenPublishLocation(JsonObject event) {
		event.addProperty("isFree", String.valueOf(!commandHandler.interrupt()));
	}

	private void solveEmergency() {
		Emergency emergency = commandHandler.getEmergency();
		commandHandler.reset();
		getApplicationClient().publishCommand(hopstialDeviceType, hospitalDeviceId, "emergency-solved",
				emergency.asJson());
		System.out.println("Ambulance " + getVin() + ": Emergency" + emergency.getEmergencyId() + " solved");

	}

	private class EmergencyCommandHandler implements EventCallback, Interruption {

		private Emergency emergency;

		@Override
		public boolean interrupt() {
			return this.emergency != null;
		}

		public Emergency getEmergency() {
			return emergency;
		}

		public void reset() {
			emergency = null;
		}

		@Override
		public void processCommand(com.ibm.iotf.client.app.Command cmd) {

		}

		@Override
		public void processEvent(Event event) {
			if (this.emergency == null && event.getEvent().equals(Emergency.EVENT_LOCATION)) {
				JsonObject json = new JsonParser().parse(event.getPayload()).getAsJsonObject().get("d")
						.getAsJsonObject();
				Emergency emergency = Emergency.createEmergency(json);
				if (emergency.getAmbulance() != null && emergency.getAmbulance().getDeviceId().equals(getVin())) {
					this.emergency = emergency;
					System.out.println("got emergency" + emergency.getEmergencyId());

				}
			}
		}

	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {

	}

}
