package com.capgemini.hackathon.device.simulation.model;

import java.util.ArrayList;
import java.util.List;


import com.google.gson.*;
import com.graphhopper.GHResponse;

public class Route {
	
	public static final String EVENT = "route";
	private static final String VIN = "vin";
	private static final String NODES = "nodes";	
	protected String vin;
	protected List<Location> nodes;
	
	public JsonObject asJson(){
		JsonObject json = new JsonObject();
		JsonArray Jarr=new JsonArray();
		
		json.addProperty(VIN, vin);
		for(int i = 0; i < nodes.size();i++)
		{
			Jarr.add(nodes.get(i).asJson());
		}
		json.add(NODES, Jarr);
		
		return json;
	}
	
	public Route(String vin) {
		// TODO Auto-generated constructor stub
		this.vin=vin;
		nodes=new ArrayList<Location>();
	}
	
	public static Route fromGHRes(String vin,GHResponse res)
	{
		Route route = new Route(vin);
		if(res.getPoints() != null){
			for(int i =0; i<res.getPoints().getSize();i++)
			{
				Location node = new Location(res.getPoints().getLat(i),res.getPoints().getLon(i));
				route.nodes.add(node);
			}
			
		}
		return route;
	}
	
}
