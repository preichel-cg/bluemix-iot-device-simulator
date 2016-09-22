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

		int nrOfGroups = Integer.valueOf(properties.getProperty("nr.of.groups", "0"));
		int nrOfCars = Integer.valueOf(properties.getProperty("nr.of.cars", "0"));
		int nrOfAmbulances = Integer.valueOf(properties.getProperty("nr.of.ambulances", "0"));

		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>();
		List<TypedDeviceConfig> ambulances = new ArrayList<TypedDeviceConfig>();

		for (int i = 0; i < nrOfGroups; i++) {
			List<TypedDeviceConfig> carList = admin.createCarDevices("group" + i, (i + 1) * 100, nrOfCars);
			List<TypedDeviceConfig> ambulanceList = admin.createAmbulanceDevices("group" + i, (i + 1) + 100,
					nrOfAmbulances);
			configList.addAll(carList);
			configList.addAll(ambulanceList);
			ambulances.addAll(ambulanceList);

		}
		configList.add(admin.createHospital(ambulances));

		Gson gson = new Gson();
		String jsonConfig = gson.toJson(configList);
		System.out.println("############## START SIMULATOR CONFIGURATION #############");
		System.out.println(jsonConfig);
		System.out.println("############## END SIMULATOR CONFIGURATION #############");

	}

	public DeviceAdmin(String orgId, String apiKey, String apiToken) {
		service = new DeviceManagementFacade(orgId, apiKey, apiToken);
	}

	public List<TypedDeviceConfig> createCarDevices(String groupname, int vinoffset, int nr) throws Exception {
		String carTypeId = "car-" + groupname;
		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>(nr);

		service.createDeviceType(carTypeId, "car for group " + groupname);
		for (int i = 0; i < nr; i++) {
			DeviceConfig config = service.createDevice(carTypeId, "car" + vinoffset + i, true);
			configList.add(new TypedDeviceConfig(config, Car.class));
		}
		return configList;
	}

	public List<TypedDeviceConfig> createAmbulanceDevices(String groupname, int vinoffset, int nr) throws Exception {
		String ambulanceTypeId = "ambulance-" + groupname;
		List<TypedDeviceConfig> configList = new ArrayList<TypedDeviceConfig>(nr);

		service.createDeviceType(ambulanceTypeId, "ambulance for group " + groupname);
		for (int i = 0; i < nr; i++) {
			DeviceConfig config = service.createDevice(ambulanceTypeId, "ambulance" + vinoffset + i, true);
			configList.add(new TypedDeviceConfig(config, Ambulance.class));
		}

		return configList;
	}

	private TypedDeviceConfig createHospital(List<TypedDeviceConfig> ambulances) throws Exception {
		service.createDeviceType("hospital", "Hospital device which shares information to all groups");
		DeviceConfig config = service.createDevice("hospital", "hospital1", true);
		String hopsitalJson = (new Gson().toJson(config));
		config.setMetadata(new Gson().toJson(ambulances));

		for (TypedDeviceConfig ambulance : ambulances) {
			ambulance.setMetadata(hopsitalJson);
		}

		return new TypedDeviceConfig(config, Hospital.class);
	}

	private static final class TypedDeviceConfig extends DeviceConfig {

		private String simulatorClazz;

		public TypedDeviceConfig(DeviceConfig config, Class<?> simulatorClazz) {
			super(config);
			this.simulatorClazz = simulatorClazz.getName();
		}

		@SuppressWarnings("unused")
		public String getSimulatorClazz() {
			return simulatorClazz;
		}
	}
}
