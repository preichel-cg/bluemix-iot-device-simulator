package com.capgemini.hackathon.device.simulation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.apache.http.util.Asserts;

import com.capgemini.hackathon.device.simulation.bo.Simulation;
import com.capgemini.hackathon.device.simulation.routing.RouteCalculator;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeviceSimulator {

	public static void main(String... args) throws Exception {

		ExecutorCompletionService<String> executor = new ExecutorCompletionService<String>(
				Executors.newCachedThreadPool());
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties");

		Properties properties = new Properties();
		properties.load(in);
		in.close();

		String ioTApiKey = properties.getProperty("api-key", null);
		String ioTApiToken = properties.getProperty("api-token", null);
		String deviceConfigPath = properties.getProperty("devices", null);

		Asserts.notNull(ioTApiKey, "API Key for IoT not available");
		Asserts.notNull(ioTApiToken, "API Token for IoT not available");
		Asserts.notNull(deviceConfigPath, "No device configuration found");

		RouteCalculator.getInstance().init();
		
		System.out.println("Loading device config: " + deviceConfigPath);

		in = Thread.currentThread().getContextClassLoader().getResourceAsStream(deviceConfigPath);
		JsonArray configArray = new JsonParser().parse(new InputStreamReader(in)).getAsJsonArray();

		for (JsonElement element : configArray) {
			JsonObject config = element.getAsJsonObject();
			String orgaId = config.get("orgId").getAsString();
			String typeId = config.get("typeId").getAsString();
			String deviceId = config.get("deviceId").getAsString();
			String apiToken = config.get("apiToken").getAsString();
			String clazz = config.get("simulatorClazz").getAsString();
			String id = config.get("id").getAsString();

			DeviceClientConfig deviceClientConfig = new DeviceClientConfig(orgaId, typeId, deviceId, apiToken);

			Constructor<?> constructor = Class.forName(clazz).getConstructor(DeviceClientConfig.class, Object.class);
			Simulation simulator = (Simulation) constructor.newInstance(deviceClientConfig, id);

			executor.submit(simulator);
		}
	}

}
