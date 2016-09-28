package com.capgemini.hackathon.device.simulation.bo;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BORegistry {

	private static final BORegistry INSTANCE = new BORegistry();

	public final Map<Class<?>, Map<Object, Simulation>> bos = new HashMap<Class<?>, Map<Object, Simulation>>();

	public static BORegistry getInstance() {
		return INSTANCE;
	}

	public void register(Simulation bo) {
		if (!bos.containsKey(bo.getClass())) {
			bos.put(bo.getClass(), new HashMap<Object, Simulation>());
		}

		if (!bos.get(bo.getClass()).containsKey(bo.getId())) {
			bos.get(bo.getClass()).put(bo.getId(), bo);
		}
	}

	public void unregister(Simulation bo) {
		if (bos.containsKey(bo.getClass()) && bos.get(bo.getClass()).containsKey(bo.getId())) {
			bos.get(bo.getClass()).remove(bo.getId());
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Simulation> T getBoById(Object id, Class<T> clazz) {
		Map<Object, Simulation> simulations = bos.get(clazz);

		if (simulations == null) {
			return null;
		}

		return (T) simulations.get(id);
	}

	public Car getCar(Object id) {
		return (Car) getBoById(id, Car.class);
	}

	public List<Car> getCars() {
		Collection<Simulation> cars = bos.get(Car.class).values();
		return Arrays.asList(cars.toArray(new Car[cars.size()]));
	}

	public Ambulance getAmbulance(Object id) {
		return (Ambulance) getBoById(id, Ambulance.class);
	}

	public List<Ambulance> getAmbulances() {
		Collection<Simulation> ambulances = bos.get(Ambulance.class).values();
		return Arrays.asList(ambulances.toArray(new Ambulance[ambulances.size()]));
	}

}
