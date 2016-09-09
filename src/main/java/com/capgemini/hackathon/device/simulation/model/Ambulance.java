package com.capgemini.hackathon.device.simulation.model;

import com.google.gson.JsonObject;

public class Ambulance {

	private static final String VIN = "vin";
	private static final String DEVICE_ID = "deviceID";
	private static final String TYPE_ID = "typeId";

	private String deviceId;
	private String deviceTypeId;
	private transient Emergency emergency;
	private transient Location location;

	public Ambulance(String deviceId, String deviceTypeId) {
		super();
		this.deviceId = deviceId;
		this.deviceTypeId = deviceTypeId;
		this.location = new Location(0, 0);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceTypeId() {
		return deviceTypeId;
	}

	public void setDeviceTypeId(String deviceTypeId) {
		this.deviceTypeId = deviceTypeId;
	}

	public Emergency getEmergency() {
		return emergency;
	}

	public void setEmergency(Emergency emergency) {
		this.emergency = emergency;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public JsonObject asJson() {
		JsonObject json = new JsonObject();
		json.addProperty(VIN, deviceId);
		json.addProperty(DEVICE_ID, deviceId);
		json.addProperty(TYPE_ID, deviceTypeId);
		return json;
	}

	public static Ambulance createAmbulance(JsonObject json) {
		String groupId = json.get(DEVICE_ID).getAsString();
		String typeId = json.get(TYPE_ID).getAsString();

		return new Ambulance(groupId, typeId);
	}

}
