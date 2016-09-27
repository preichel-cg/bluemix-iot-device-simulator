package com.capgemini.hackathon.device.simulation.model;

import com.google.gson.JsonObject;

public class Emergency {
	public static final String EVENT_LOCATION = "emergency";
	public static final String CMD_SEND_AMBULANCE = "send_ambulance";
	private static final String GROUP_ID = "groupId";
	private static final String STATUS = "status";
	private static final String LONGITUDE = "longitude";
	private static final String LATITUDE = "latitude";
	private static final String EMERGENCY_ID = "emergencyId";
	private static final String AMBULANCE = "vin";

	private Status status = Status.OPEN;
	private String ambulanceVin;
	private String emergencyId;
	private String groupId;
	private Location location;

	public String getAmbulanceVin() {
		return ambulanceVin;
	}

	public void setAmbulanceVin(String ambulanceVin) {
		this.ambulanceVin = ambulanceVin;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getEmergencyId() {
		return emergencyId;
	}

	public void setEmergencyId(String emergencyId) {
		this.emergencyId = emergencyId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupdId) {
		this.groupId = groupdId;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public JsonObject asJson() {
		JsonObject json = new JsonObject();
		json.addProperty(EMERGENCY_ID, this.emergencyId);
		json.addProperty(LATITUDE, this.location.getLatitude());
		json.addProperty(LONGITUDE, this.location.getLongitude());
		json.addProperty(STATUS, status.toString());
		json.addProperty(GROUP_ID, this.groupId);
		json.addProperty(AMBULANCE, this.ambulanceVin);

		return json;
	}

	public static Emergency createEmergency(JsonObject json) {
		Emergency emergency = new Emergency();
		emergency.setEmergencyId(json.get(EMERGENCY_ID).getAsString());
		emergency.setLocation(new Location(json.get(LATITUDE).getAsDouble(), json.get(LONGITUDE).getAsDouble()));
		emergency.setGroupId(json.get(GROUP_ID).getAsString());
		emergency.setStatus(Status.valueOf(json.get(STATUS).getAsString()));

		if (emergency.getStatus() != Status.OPEN) {
			emergency.setAmbulanceVin(json.get(AMBULANCE).getAsString());
		}

		return emergency;
	}

	public enum Status {
		OPEN, ONGING, SOLVED
	}

}
