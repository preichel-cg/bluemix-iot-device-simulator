package com.capgemini.hackathon.device.simulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.apache.http.util.Asserts;

import com.capgemini.hackathon.device.simulation.bo.Simulation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

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

			DeviceClientConfig deviceClientConfig = new DeviceClientConfig(orgaId, typeId, deviceId, apiToken);
			ApplicationClientConfig appClientConfig = new ApplicationClientConfig(deviceId, ioTApiKey, ioTApiToken);

			Simulation simulator = null;
			if (config.has("metadata")) {
				String metadata = config.get("metadata").getAsString();
				Constructor<?> constructor = Class.forName(clazz).getConstructor(DeviceClientConfig.class,
						ApplicationClientConfig.class, String.class);
				simulator = (Simulation) constructor.newInstance(deviceClientConfig, appClientConfig, metadata);
			} else {
				Constructor<?> constructor = Class.forName(clazz).getConstructor(DeviceClientConfig.class,
						ApplicationClientConfig.class);
				simulator = (Simulation) constructor.newInstance(deviceClientConfig, appClientConfig);
			}

			executor.submit(simulator);
		}
	}

}
