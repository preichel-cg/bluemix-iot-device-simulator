package com.capgemini.hackathon.device.simulation.bo;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Emergency;
import com.capgemini.hackathon.device.simulation.model.Emergency.Status;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.capgemini.hackathon.device.simulation.routing.RouteCalculator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.graphhopper.GHResponse;
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

	public static final String HOSPITAL_ID = "Hospital_Kings_Cross";

	private List<Emergency> emergencies = new LinkedList<Emergency>();

	public Hospital(DeviceClientConfig deviceClientConfig, Object id) {
		super(deviceClientConfig, HOSPITAL_ID);
	}

	@Override
	protected void configureDeviceClient(DeviceClient deviceClient) {
		deviceClient.setCommandCallback(new EmergencyDeviceCommand());

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
				ambulance.sendToEmergency(emergency);
				emergency.setAmbulanceVin(ambulance.getId().toString());
				emergency.setStatus(Emergency.Status.ONGING);
			}
		} else if (emergency.getStatus() == Emergency.Status.SOLVED) {
			synchronized (emergencies) {
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
		List<Ambulance> ambulances = BORegistry.getInstance().getAmbulances();

		Ambulance nearestAmbulance = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Ambulance ambulance : ambulances) {

			if (ambulance.isFree()) {
				GHResponse response = RouteCalculator.getInstance().calculateRoute(
						ambulance.getCurrentLocation().getLatitude(), ambulance.getCurrentLocation().getLongitude(),
						emergency.getLocation().getLatitude(), emergency.getLocation().getLongitude());

				if (response.getDistance() < nearestDistance) {
					nearestAmbulance = ambulance;
				}
			}
		}
		return nearestAmbulance;
	}

	public void solveEmergency(Emergency solvedEmergency) {
		synchronized (emergencies) {
			for (Emergency emergency : emergencies) {
				if (emergency.getEmergencyId().equals(solvedEmergency.getEmergencyId())) {
					emergency.setStatus(Status.SOLVED);
					System.out.println("Hospital: Emergency " + emergency.getEmergencyId() + " solved");

				}
			}
		}

	}

	private class EmergencyDeviceCommand implements CommandCallback {

		@Override
		public void processCommand(Command command) {
			JsonObject jsonCmd = new JsonParser().parse(command.getPayload()).getAsJsonObject().get("d")
					.getAsJsonObject();
			String latitude = jsonCmd.get("latitude").getAsString();
			String longtitude = jsonCmd.get("longitude").getAsString();

			handleNewEmergency(latitude, longtitude);

		}

		private void handleNewEmergency(String latitude, String longtitude) {
			// new emergency
			Emergency emergency = new Emergency();
			emergency.setEmergencyId(UUID.randomUUID().toString());
			emergency.setLocation(new Location(Double.valueOf(latitude), Double.valueOf(longtitude)));

			synchronized (emergencies) {
				emergencies.add(emergency);
				System.out.println("Hospital: Emergency " + emergency.getEmergencyId() + " happend");

			}

		}

	}
}
