package com.capgemini.hackathon.device.simulation;

import java.util.Properties;

public class DeviceClientConfig {

	private String orgId;
	private String deviceType;
	private String deviceId;
	private String authToken;

	public DeviceClientConfig(String orgId, String deviceType, String deviceId, String authToken) {
		super();
		this.orgId = orgId;
		this.deviceType = deviceType;
		this.deviceId = deviceId;
		this.authToken = authToken;
	}

	public Properties asProperties() {
		Properties properties = new Properties();
		properties.put("Organization-ID", orgId);
		properties.put("Device-Type", deviceType);
		properties.put("Device-ID", deviceId);
		properties.put("Authentication-Method", "token");
		properties.put("Authentication-Token", authToken);

		return properties;
	}

	public String getOrgId() {
		return orgId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getAuthToken() {
		return authToken;
	}

}
