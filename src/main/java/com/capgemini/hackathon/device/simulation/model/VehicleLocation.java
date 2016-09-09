package com.capgemini.hackathon.device.simulation.model;

import com.google.gson.JsonObject;

public class VehicleLocation extends Location {

	public  static final String EVENT = "location";
	private static final String VIN = "vin";
	private static final String GROUP = "group";
	private String groupId;
	private String vin;

	public VehicleLocation(double latitude, double longtitude, String groupId, String vin) {
		super(latitude, longtitude);
		this.groupId = groupId;
		this.vin = vin;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public static VehicleLocation createVehicleLocation(JsonObject json) {
		Location location = Location.createLocation(json);
		String vin = json.get(VIN).getAsString();
		String group = json.get(GROUP).getAsString();
		return new VehicleLocation(location.getLatitude(), location.getLongitude(), group, vin);
	}

	public JsonObject asJson() {
		JsonObject json = super.asJson();
		json.addProperty(VIN, vin);
		json.addProperty(GROUP, groupId);
		return json;
	}

}
