package com.capgemini.hackathon.device.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.util.Asserts;

import com.capgemini.hackathon.device.simulation.bo.Ambulance;
import com.capgemini.hackathon.device.simulation.bo.Car;
import com.capgemini.hackathon.device.simulation.bo.Hospital;
import com.google.gson.Gson;

public class DeviceAdmin {

	private DeviceManagementFacade service;

	public static void main(String... args) throws Exception {

		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");

		Properties properties = new Properties();
		properties.load(in);
		in.close();

		String ioTApiKey = properties.getProperty("api-key", null);
		String ioTApiToken = properties.getProperty("api-token", null);
		String orgaid = properties.getProperty("orga-id", null);

		Asserts.notNull(ioTApiKey, "API Key for IoT not available");
		Asserts.notNull(ioTApiToken, "API Token for IoT not available");
		Asserts.notNull(orgaid, "Organization Id for IoT not availble");

		DeviceAdmin admin = new DeviceAdmin(orgaid, ioTApiKey, ioTApiToken);

		in = Thread.currentThread().getContextClassLoader().getResourceAsStream("admin.properties");
		properties = new Properties();
		properties.load(in);
		in.close();

		String groupname = properties.getProperty("group.name", "nogroup");
		int nrOfCars = Integer.valueOf(properties.getProperty("nr.of.cars", "0"));
		int nrOfAmbulances = Integer.valueOf(properties.getProperty("nr.of.ambulances", "0"));

		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>();
		List<TypedDeviceConfig> carList = admin.createCarDevices(groupname, nrOfCars);
		List<TypedDeviceConfig> ambulanceList = admin.createAmbulanceDevices(groupname, nrOfAmbulances);
		configList.addAll(carList);
		configList.addAll(ambulanceList);
		configList.add(admin.createHospital());

		Gson gson = new Gson();
		String jsonConfig = gson.toJson(configList);
		System.out.println("############## START SIMULATOR CONFIGURATION #############");
		System.out.println(jsonConfig);
		System.out.println("############## END SIMULATOR CONFIGURATION #############");

	}

	public DeviceAdmin(String orgId, String apiKey, String apiToken) {
		service = new DeviceManagementFacade(orgId, apiKey, apiToken);
	}

	public List<TypedDeviceConfig> createCarDevices(String groupname,  int nr) throws Exception {
		String carTypeId = "car-" + groupname;
		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>(nr);

		service.createDeviceType(carTypeId, "car for group " + groupname);
		for (int i = 0; i < nr; i++) {
			DeviceConfig config = service.createDevice(carTypeId, "car" + i, true);
			configList.add(new TypedDeviceConfig(config, Car.class,"car" + i));
		}
		return configList;
	}

	public List<TypedDeviceConfig> createAmbulanceDevices(String groupname, int nr) throws Exception {
		String ambulanceTypeId = "ambulance-" + groupname;
		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>(nr);

		service.createDeviceType(ambulanceTypeId, "ambulance for group " + groupname);
		for (int i = 0; i < nr; i++) {
			DeviceConfig config = service.createDevice(ambulanceTypeId, "ambulance" + i, true);
			configList.add(new TypedDeviceConfig(config, Ambulance.class, "ambulance" + i));
		}

		return configList;
	}

	private TypedDeviceConfig createHospital() throws Exception {
		service.createDeviceType("hospital", "Hospital device which shares information to all groups");
		DeviceConfig config = service.createDevice("hospital", "hospital1", true);
		return new TypedDeviceConfig(config, Hospital.class, "hospital");
	}

	private static final class TypedDeviceConfig extends DeviceConfig {

		private String simulatorClazz;
		private String id;

		public TypedDeviceConfig(DeviceConfig config, Class<?> simulatorClazz, String id) {
			super(config);
			this.simulatorClazz = simulatorClazz.getName();
			this.id = id;
		}

		@SuppressWarnings("unused")
		public String getSimulatorClazz() {
			return simulatorClazz;
		}

		@SuppressWarnings("unused")
		public String getId() {
			return id;
		}
	}
}
