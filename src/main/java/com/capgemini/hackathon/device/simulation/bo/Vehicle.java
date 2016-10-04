package com.capgemini.hackathon.device.simulation.bo;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.capgemini.hackathon.device.simulation.model.VehicleLocation;
import com.capgemini.hackathon.device.simulation.routing.RouteCalculator;
import com.google.gson.JsonObject;
import com.graphhopper.GHResponse;

public abstract class Vehicle extends Simulation {

	private static final Interruption FALSE_INTERRUPTION = new Vehicle.FalseInterruption();

	// How close the vehicles reach their destination
	private static final double DIST_LAT_LONG = 0.00005;
	private static final double DEFAULT_SPEED = 0.00005;
	// The steps driving the vehicles per iteration
	private final static double SPEED = Vehicle.getSpeed();

	// current Location
	private Location currentLocation;
	// destination Location
	private Location destination;

	public Vehicle(DeviceClientConfig deviceClientConfig, Location location, Object id) {
		super(deviceClientConfig, id);
		this.currentLocation = location;
	}

	public Vehicle(DeviceClientConfig deviceClientConfig, Object id) {
		this(deviceClientConfig, Location.createRandomLocation(), id);
	}

	protected void publishLocation() {
		try {

			VehicleLocation vl = new VehicleLocation(currentLocation.getLatitude(), currentLocation.getLongitude(),
					getId().toString());
			JsonObject event = vl.asJson();
			addMetainformationWhenPublishLocation(event);
			// Publish event to IoT
			getDeviceClient().publishEvent(VehicleLocation.EVENT, event, 1);
			//System.out.println(event);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public Location getDestination() {
		return destination;
	}

	protected abstract void addMetainformationWhenPublishLocation(JsonObject event);

	protected void driveToDestination(Location destination) {
		this.driveToDestination(destination, FALSE_INTERRUPTION);
	}

	protected void driveToDestination(Location destination, Interruption interruption) {
		// Calculate the route between the current and destination location
		GHResponse response = RouteCalculator.getInstance().calculateRoute(currentLocation.getLatitude(),
				currentLocation.getLongitude(), destination.getLatitude(), destination.getLongitude());

		int i = 0;
		if (response.getPoints() != null) {

			while (response.getPoints().getSize() > i) {

				double nextPointLatitude = response.getPoints().getLatitude(i);
				double nextpointLongitude = response.getPoints().getLongitude(i);

				// Take the difference between current location and the
				// destination
				double distLat = currentLocation.getLatitude() - nextPointLatitude;
				double distLong = currentLocation.getLongitude() - nextpointLongitude;

				// While the difference is bigger than the threshold x
				//int j = 0;
				while ((Math.abs(distLat) > DIST_LAT_LONG || Math.abs(distLong) > DIST_LAT_LONG)) {
					// check if we have to move in lat direction
					if (Math.abs(distLat) > DIST_LAT_LONG) {
						// go xxx steps in direction
						if (distLat < 0) {
							currentLocation.setLatitude(currentLocation.getLatitude() + SPEED);
						} else {
							currentLocation.setLatitude(currentLocation.getLatitude() - SPEED);
						}
					}
					// check if we have to move in long direction
					if (Math.abs(distLong) > DIST_LAT_LONG) {
						if (distLong < 0) {
							currentLocation.setLongitude(currentLocation.getLongitude() + SPEED);
						} else {
							currentLocation.setLongitude(currentLocation.getLongitude() - SPEED);
						}
					}

					try {
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//if (j % 2 == 0)
						this.publishLocation();

						if (interruption.interrupt()) {
							System.out.println("Driving interrupted");
							return;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					distLat = currentLocation.getLatitude() - nextPointLatitude;
					distLong = currentLocation.getLongitude() - nextpointLongitude;
					
					//j++;

				}
				currentLocation.setLatitude(nextPointLatitude);
				currentLocation.setLongitude(nextpointLongitude);

				i = i + 1;
			}
		}
	}

	protected static interface Interruption {
		public boolean interrupt();
	}

	private static class FalseInterruption implements Interruption {

		@Override
		public boolean interrupt() {
			return false;
		}

	}

	public static double getSpeed() {
		String speed = System.getenv("SPEED");
		if (speed != null) {
			return Double.valueOf(speed);
		} else {
			return DEFAULT_SPEED;
		}
	}

}
