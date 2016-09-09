package com.capgemini.hackathon.device.simulation;

import java.util.Properties;

public class ApplicationClientConfig {

	private String id;
	private String apikey;
	private String apiToken;

	public ApplicationClientConfig(String id, String apikey, String apiToken) {
		super();
		this.id = id;
		this.apikey = apikey;
		this.apiToken = apiToken;
	}

	public Properties asProperties() {
		Properties properties = new Properties();
		properties.put("id", id);
		properties.put("auth-method", "apikey");
		properties.put("auth-key", apikey);
		properties.put("auth-token", apiToken);
		return properties;
	}
}
