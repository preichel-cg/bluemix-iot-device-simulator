package com.capgemini.hackathon.device.simulation.model;

import com.google.gson.JsonObject;

public class VehicleLocation extends Location {

	public static final String EVENT = "location";
	private static final String VIN = "vin";
	private String vin;

	public VehicleLocation(double latitude, double longtitude, String vin) {
		super(latitude, longtitude);
		this.vin = vin;
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
		return new VehicleLocation(location.getLatitude(), location.getLongitude(), vin);
	}

	public JsonObject asJson() {
		JsonObject json = super.asJson();
		json.addProperty(VIN, vin);
		return json;
	}

}
