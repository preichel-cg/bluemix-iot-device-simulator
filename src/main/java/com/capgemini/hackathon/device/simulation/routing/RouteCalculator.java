package com.capgemini.hackathon.device.simulation.routing;

import java.util.Locale;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;

/**
 * 
 * @author belmahjo
 *
 */
public class RouteCalculator {

	private static final RouteCalculator INSTANCE = new RouteCalculator();
	
	private GraphHopper engine = new GraphHopper();
	
	public static RouteCalculator getInstance(){
		return INSTANCE;
	}
	
	public RouteCalculator(){
		
		// Set the location of graphhopper files
		engine.setGraphHopperLocation("maps");		
		engine.setEncodingManager(new EncodingManager("car"));

		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		engine.importOrLoad();
	}



	/**
	 * This method calculates the route between start and end position
	 * 
	 * @param latFrom
	 *            latitude of starting position
	 * @param lonFrom
	 *            longitude of starting position
	 * @param latTo
	 *            latitude of destination
	 * @param lonTo
	 *            longitude of destination
	 */
	public GHResponse calculateRoute(double latFrom, double lonFrom, double latTo, double lonTo) {
		
		// create a request object
		GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).setWeighting("fastest").setVehicle("car")
				.setLocale(Locale.UK);
		GHResponse rsp = engine.route(req);
	
		return rsp;

	}

}
