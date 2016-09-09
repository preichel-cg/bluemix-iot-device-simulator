package com.capgemini.hackathon.device.service;

import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeviceManagementFacade {
	public String host;
	public Executor executor;
	public String orgaId;

	public DeviceManagementFacade(String orgaId, String apiKey, String apiToken) {
		this.host = "https://" + orgaId + ".internetofthings.ibmcloud.com";
		this.orgaId = orgaId;
		CredentialsProvider authProvider = new BasicCredentialsProvider();
		authProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(apiKey, apiToken));

		this.executor = Executor.newInstance();
		this.executor.authPreemptive(this.host);
		this.executor.use(authProvider);
	}

	public DeviceConfig createDevice(String deviceType, String deviceId, boolean deleteWhenExist) throws Exception {
		String url = host + "/api/v0002/device/types/" + deviceType + "/devices";

		JsonObject device = new JsonObject();
		JsonObject deviceInfo = createEmptyDeviceInfo();
		JsonObject location = createEmtpyLocation();
		JsonObject metadata = new JsonObject();

		device.addProperty("deviceId", deviceId);
		device.addProperty("authToken", UUID.randomUUID().toString().toUpperCase());
		device.add("deviceInfo", deviceInfo);
		device.add("location", location);
		device.add("metadata", metadata);

		Response response = executor
				.execute(Request.Post(url).body(new StringEntity(device.toString(), ContentType.APPLICATION_JSON)));
		HttpResponse httpresponse = response.returnResponse();

		if (httpresponse.getStatusLine().getStatusCode() == 409 && deleteWhenExist) {
			deleteDevice(deviceType, deviceId);
			return createDevice(deviceType, deviceId, false);
		}

		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(EntityUtils.toString(httpresponse.getEntity())).getAsJsonObject();
		return new DeviceConfig(orgaId, json.get("clientId").getAsString(), json.get("typeId").getAsString(),
				json.get("deviceId").getAsString(), json.get("authToken").getAsString());

	}

	public void deleteDevice(String deviceType, String deviceId) throws Exception {
		String url = host + "/api/v0002/device/types/" + deviceType + "/devices/" + deviceId;
		Response response = executor.execute(Request.Delete(url));
		System.out.println("Device " + url + "deleted: " + response.returnResponse().getStatusLine().getStatusCode());
	}

	public void createDeviceType(String id, String description) throws Exception {
		String url = host + "/api/v0002/device/types";

		JsonObject deviceType = new JsonObject();
		JsonObject deviceInfo = createEmptyDeviceInfo();
		JsonObject metadata = new JsonObject();

		deviceType.addProperty("id", id);
		deviceType.addProperty("description", description);
		deviceType.addProperty("classId", "Device");
		deviceType.add("deviceInfo", deviceInfo);
		deviceType.add("metadata", metadata);

		Response response = executor
				.execute(Request.Post(url).body(new StringEntity(deviceType.toString(), ContentType.APPLICATION_JSON)));
		HttpResponse httpresponse = response.returnResponse();

		System.out.println(EntityUtils.toString(httpresponse.getEntity()));
	}

	public void getServerStatus() throws Exception {

		String url = host + "/api/v0002/service-status";
		System.out.println(url);
		Response response = executor.execute(Request.Get(url));
		System.out.println(response.returnContent().asString());
	}

	private JsonObject createEmptyDeviceInfo() {
		JsonObject deviceInfo = new JsonObject();
		deviceInfo.addProperty("serialNumber", "");
		deviceInfo.addProperty("manufacturer", "");
		deviceInfo.addProperty("model", "");
		deviceInfo.addProperty("deviceClass", "");
		deviceInfo.addProperty("description", "");
		deviceInfo.addProperty("fwVersion", "");
		deviceInfo.addProperty("hwVersion", "");
		deviceInfo.addProperty("descriptiveLocation", "");
		return deviceInfo;
	}

	private JsonObject createEmtpyLocation() {
		JsonObject location = new JsonObject();
		location.addProperty("longitude", 0);
		location.addProperty("latitude", 0);
		location.addProperty("elevation", 0);
		location.addProperty("accuracy", 0);
		location.addProperty("measuredDateTime", "2016-08-30T018:55:28.705Z");
		return location;
	}
}