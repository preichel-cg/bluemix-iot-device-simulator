package com.capgemini.hackathon.device.simulation.bo;

import org.joda.time.DateTime;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.capgemini.hackathon.device.simulation.model.Location;
import com.capgemini.hackathon.device.simulation.model.Route;
import com.capgemini.hackathon.device.simulation.model.VehicleLocation;
import com.capgemini.hackathon.device.simulation.routing.MapCoordinatePoint;
import com.capgemini.hackathon.device.simulation.routing.RouteCalculator;
import com.google.gson.JsonObject;
import com.graphhopper.GHResponse;

public abstract class Vehicle extends Simulation {

	private static final Interruption FALSE_INTERRUPTION = new Vehicle.FalseInterruption();

	// How close the vehicles reach their destination
	private static final double DIST_LAT_LONG = 0.00005;
	private static final double DEFAULT_SPEED = 0.0001;
	// The steps driving the vehicles per iteration
	private final static double SPEED = Vehicle.getSPEED();

	// current Location
	private Location currentLocation;
	// destination Location
	private Location destination;
	// route Information
	private Route route;

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
			getDeviceClient().publishEvent(VehicleLocation.EVENT, event);
			//System.out.println(event);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void publishRoute() {
		try {
			JsonObject JsonRoute = route.asJson();
			getDeviceClient().publishEvent(Route.EVENT, JsonRoute);
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

		// System.out.println("Entering");
		
		GHResponse response = RouteCalculator.getInstance().calculateRoute(currentLocation.getLatitude(),
				currentLocation.getLongitude(), destination.getLatitude(), destination.getLongitude());

		// in case of error, the vehicle seems to be stuck somewhere
		if (response.hasErrors()) {
			
			System.out.println("Unable to find route to: " + destination.getLatitude() + ":" + destination.getLongitude() + 
			" , current location: " + currentLocation.getLatitude() + ":" + currentLocation.getLongitude());
			
			// set a new starting location to make it run again
			currentLocation.setLatitude(MapCoordinatePoint.pointHospitalLatitude);
			currentLocation.setLongitude(MapCoordinatePoint.pointHospitalLongitude);
			
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}
		
		route = Route.fromGHRes(this.getId().toString(), response);
		try {
			this.publishRoute();
		} catch (Exception e) {
			e.printStackTrace();
		}

		int i = 0;
		long millis = DateTime.now().getMillis();
		long previousMillis = millis;
		
		if (response.getPoints() != null) {
		
			while (response.getPoints().getSize() > i) {
				
				double nextPointLatitude = response.getPoints().getLatitude(i);
				double nextpointLongitude = response.getPoints().getLongitude(i);

				// Take the difference between current location and the destination
				double distLat = nextPointLatitude - currentLocation.getLatitude();
				double distLong = nextpointLongitude - currentLocation.getLongitude();
				
/*				System.out.println("Going to location: " + destination.getLatitude() + ":" + destination.getLongitude() + 
				" , point: " + i + " , distLat " + distLat + " , distLong " + distLong +
				" , location: " + currentLocation.getLatitude() + ":" + currentLocation.getLongitude());
*/
				// While the difference is bigger than the threshold x
				while ((Math.abs(distLat) > DIST_LAT_LONG || Math.abs(distLong) > DIST_LAT_LONG)) {
						
					double absVec = Math.sqrt((distLat * distLat) + (distLong * distLong));
					double factor = SPEED / absVec;

					if (factor > 1) {
						factor = 1;
					}

					currentLocation.setLatitude(currentLocation.getLatitude() + (distLat * factor));
					currentLocation.setLongitude(currentLocation.getLongitude() + (distLong * factor));

/*					System.out.println("Going to location: " + destination.getLatitude() + ":" + destination.getLongitude() + 
					" , point: " + i + " , absVector " + absVec + " , faktor " + factor +
					" , distLat " + distLat + " , distLong " + distLong +
					" , location: " + currentLocation.getLatitude() + ":" + currentLocation.getLongitude());
*/					
					try {
						this.publishLocation();

					} catch (Exception e) {
						e.printStackTrace();
					}
					
					if (interruption.interrupt()) {
						System.out.println("Driving interrupted");
						return;
					}

					distLat = nextPointLatitude - currentLocation.getLatitude();
					distLong = nextpointLongitude - currentLocation.getLongitude();
					
					try {
						// whatever time has passed so far, wait until 1s has passed
						millis = DateTime.now().getMillis();
						long diffMillies = millis - previousMillis;
						if (diffMillies < 1000) {
							// System.out.println("Sleeping :" + (1000 - diffMillies) + "ms");
							Thread.sleep(1000 - diffMillies);
						}
						
						previousMillis = DateTime.now().getMillis();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

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

	public static double getSPEED() {
		String SPEED = System.getenv("SPEED");
		if (SPEED != null) {
			return Double.valueOf(SPEED);
		} else {
			return DEFAULT_SPEED;
		}
	}

}
