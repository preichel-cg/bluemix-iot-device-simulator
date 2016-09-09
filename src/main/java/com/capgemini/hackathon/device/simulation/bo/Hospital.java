package com.capgemini.hackathon.device.simulation.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.capgemini.hackathon.device.service.DeviceConfig;
import com.capgemini.hackathon.device.simulation.ApplicationClientConfig;
import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Ambulance;
import com.capgemini.hackathon.device.simulation.model.Emergency;
import com.capgemini.hackathon.device.simulation.model.Emergency.Status;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.capgemini.hackathon.device.simulation.model.VehicleLocation;
import com.capgemini.hackathon.device.simulation.routing.RouteCalculator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.graphhopper.GHResponse;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.app.Event;
import com.ibm.iotf.client.app.EventCallback;
import com.ibm.iotf.client.device.Command;
import com.ibm.iotf.client.device.CommandCallback;
import com.ibm.iotf.client.device.DeviceClient;

/**
 * Hospital simulator class which handles emergencies, and notfies next
 * ambulance.
 * 
 * To notify the hospital about an emergency a json command with following
 * structure must be send to the hospital device:
 * 
 *
 */
public class Hospital extends Simulation {

	private List<Emergency> emergencies = new LinkedList<Emergency>();
	private HashMap<String, List<Ambulance>> ambulances = new HashMap<String, List<Ambulance>>();

	public Hospital(DeviceClientConfig deviceClientConfig, ApplicationClientConfig appClientConfig, String metadata) {
		super(deviceClientConfig, appClientConfig);

		configureHospitalFleet(metadata);
	}

	private void configureHospitalFleet(String metadata) {
		JsonArray fleet = new JsonParser().parse(metadata).getAsJsonArray();
		for (JsonElement jsonAmbulance : fleet) {
			JsonObject jsonObject = jsonAmbulance.getAsJsonObject();
			String deviceId = jsonObject.get(DeviceConfig.DEVICE_ID).getAsString();
			String typeId = jsonObject.get(DeviceConfig.TYPE_ID).getAsString();
			String group = typeId.split("-")[1];

			Ambulance ambulance = new Ambulance(deviceId, typeId);
			if (!ambulances.containsKey(group)) {
				ambulances.put(group, new ArrayList<Ambulance>());
			}
			ambulances.get(group).add(ambulance);
			System.out.println("Ambulance added: " + group + " -> " + ambulance.getDeviceId());
		}

	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {
		deviceClient.setCommandCallback(new DeviceCommandHandler(new EmergencyDeviceCommandStrategy(),
				new EmergencySolvedDeviceCommandStrategy()));

	}

	@Override
	protected void configureAppplicationClient(ApplicationClient applicationClient) {
		applicationClient.setEventCallback(new AmbulanceEventHandler());
		applicationClient.subscribeToDeviceEvents();

	}

	@Override
	protected void process() {
		while (true) {
			try {
				Thread.sleep(2000);
				synchronized (emergencies) {
					for (Emergency emergency : emergencies) {
						publishEmergency(emergency);
						handleEmergency(emergency);

					}
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleEmergency(Emergency emergency) {
		// find ambulance to solve emergency
		if (emergency.getStatus() == Emergency.Status.OPEN) {
			Ambulance ambulance = findNearestAmbulance(emergency);
			if (ambulance != null) {
				ambulance.setEmergency(emergency);
				emergency.setAmbulance(ambulance);
				emergency.setStatus(Emergency.Status.ONGING);
			}
		} else if (emergency.getStatus() == Emergency.Status.SOLVED) {
			synchronized (emergencies) {
				emergency.getAmbulance().setEmergency(null);
				emergencies.remove(emergency);
			}
		}
	}

	private void publishEmergency(Emergency emergency) {
		try {
			// Publish event to IoT
			getDeviceClient().publishEvent(Emergency.EVENT_LOCATION, emergency.asJson());
			System.out.println("Hospital: emergency " + emergency.getEmergencyId() + " ongoing ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method finds which ambulance is the closest ambulance to the
	 * emergency location
	 * 
	 * @param emergencyLatitude
	 *            the latitude of the emergency location
	 * 
	 * @param emergencyLongitude
	 *            the longitude of the emergency location
	 */

	public Ambulance findNearestAmbulance(Emergency emergency) {
		List<Ambulance> ambulances = this.ambulances.get(emergency.getGroupId());

		Ambulance nearestAmbulance = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Ambulance ambulance : ambulances) {

			if (ambulance.getEmergency() == null) {
				GHResponse response = RouteCalculator.getInstance().calculateRoute(
						ambulance.getLocation().getLatitude(), ambulance.getLocation().getLongitude(),
						emergency.getLocation().getLatitude(), emergency.getLocation().getLongitude());

				if (response.getDistance() < nearestDistance) {
					nearestAmbulance = ambulance;
				}
			}
		}
		return nearestAmbulance;
	}

	private class AmbulanceEventHandler implements EventCallback {

		@Override
		public void processCommand(com.ibm.iotf.client.app.Command cmd) {

		}

		@Override
		public void processEvent(Event e) {

			if (!e.getEvent().equals("location")) {
				return;
			}
			if (!e.getDeviceId().startsWith("ambulance")) {
				return;
			}

			JsonObject data = new JsonParser().parse(e.getPayload()).getAsJsonObject().get("d").getAsJsonObject();
			VehicleLocation vl = VehicleLocation.createVehicleLocation(data);

			for (Ambulance ambulance : ambulances.get(vl.getGroupId())) {
				if (ambulance.getDeviceId().equals(vl.getVin())) {
					ambulance.getLocation().setLatitude(vl.getLatitude());
					ambulance.getLocation().setLongitude(vl.getLongitude());
					break;
				}
			}

		}
	}

	private class DeviceCommandHandler implements CommandCallback {

		private List<DeviceCommandStrategy> strategies = new ArrayList<DeviceCommandStrategy>();

		public DeviceCommandHandler(DeviceCommandStrategy... strategies) {
			this.strategies = Arrays.asList(strategies);
		}

		@Override
		public void processCommand(Command cmd) {
			for (DeviceCommandStrategy strategy : strategies) {
				if (strategy.getCommand().equals(cmd.getCommand())) {
					strategy.processCommand(cmd);
				}
			}
		}

	}

	private interface DeviceCommandStrategy {
		public String getCommand();

		public void processCommand(Command command);
	}

	private class EmergencyDeviceCommandStrategy implements DeviceCommandStrategy {

		@Override
		public String getCommand() {
			return "emergency";
		}

		@Override
		public void processCommand(Command command) {
			JsonObject jsonCmd = new JsonParser().parse(command.getPayload()).getAsJsonObject().get("d")
					.getAsJsonObject();
			String groupId = jsonCmd.get("groupId").getAsString();
			String latitude = jsonCmd.get("latitude").getAsString();
			String longtitude = jsonCmd.get("longitude").getAsString();

			handleNewEmergency(groupId, latitude, longtitude);

		}

		private void handleNewEmergency(String groupId, String latitude, String longtitude) {
			// new emergency
			Emergency emergency = new Emergency();
			emergency.setEmergencyId(UUID.randomUUID().toString());
			emergency.setGroupId(groupId);
			emergency.setLocation(new Location(Double.valueOf(latitude), Double.valueOf(longtitude)));

			synchronized (emergencies) {
				emergencies.add(emergency);
				System.out.println("Hospital: Emergency " + emergency.getEmergencyId() + " happend");

			}

		}

	}

	private class EmergencySolvedDeviceCommandStrategy implements DeviceCommandStrategy {

		@Override
		public String getCommand() {
			return "emergency-solved";
		}

		@Override
		public void processCommand(Command command) {
			JsonObject jsonCmd = new JsonParser().parse(command.getPayload()).getAsJsonObject().get("d")
					.getAsJsonObject();
			Emergency solved = Emergency.createEmergency(jsonCmd);
			synchronized (emergencies) {
				for (Emergency emergency : emergencies) {
					if (emergency.getEmergencyId().equals(solved.getEmergencyId())) {
						emergency.getAmbulance().setEmergency(null);
						emergency.setStatus(Status.SOLVED);
						System.out.println("Hospital: Emergency " + emergency.getEmergencyId() + " solved");

					}
				}
			}
		}

	}

}
