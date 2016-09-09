package com.capgemini.hackathon.device.service;

public class DeviceConfig {

	public static final String ORG_ID = "orgId";
	public static final String CLIENT_ID = "clientId";
	public static final String TYPE_ID = "typeId";
	public static final String DEVICE_ID = "deviceId";
	public static final String API_TOKEN = "apiToken";
	public static final String METADATA = "metadata";

	private String orgId;
	private String clientId;
	private String typeId;
	private String deviceId;
	private String apiToken;
	private String metadata;

	public DeviceConfig(String orgId, String clientId, String typeId, String deviceId, String apiToken) {
		this(orgId, clientId, typeId, deviceId, apiToken, null);
	}

	public DeviceConfig(String orgId, String clientId, String typeId, String deviceId, String apiToken,
			String metadata) {
		super();
		this.orgId = orgId;
		this.clientId = clientId;
		this.typeId = typeId;
		this.deviceId = deviceId;
		this.apiToken = apiToken;
		this.metadata = metadata;
	}

	public DeviceConfig(DeviceConfig config) {
		this(config.orgId, config.clientId, config.typeId, config.deviceId, config.apiToken, config.metadata);
	}

	public String getOrgId() {
		return orgId;
	}

	public String getClientId() {
		return clientId;
	}

	public String getTypeId() {
		return typeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getApiToken() {
		return apiToken;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

}