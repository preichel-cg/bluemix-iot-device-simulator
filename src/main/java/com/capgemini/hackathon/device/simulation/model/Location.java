package com.capgemini.hackathon.device.simulation.model;

import com.capgemini.hackathon.device.simulation.routing.MapCoordinatePoint;
import com.google.gson.JsonObject;

public class Location {
	private static final String LONGTITUDE = "longitude";
	private static final String LATITUDE = "latitude";

	private double latitude;
	private double longitude;

	public Location(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public JsonObject asJson() {
		JsonObject json = new JsonObject();
		json.addProperty(LATITUDE, latitude);
		json.addProperty(LONGTITUDE, longitude);
		return json;
	}

	public static Location createRandomLocation() {
		return new Location(
				MapCoordinatePoint.pointDownRightLatitude + (Math.random() * MapCoordinatePoint.distanceToTop),
				MapCoordinatePoint.pointDownRightLongitude + (Math.random() * -MapCoordinatePoint.distanceToLeft));

	}

	public static Location createLocation(JsonObject json) {
		return new Location(json.get(LATITUDE).getAsDouble(), json.get(LONGTITUDE).getAsDouble());
	}

}