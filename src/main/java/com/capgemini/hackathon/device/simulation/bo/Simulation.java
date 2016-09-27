package com.capgemini.hackathon.device.simulation.bo;

import java.util.concurrent.Callable;

import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.ibm.iotf.client.device.DeviceClient;

public abstract class Simulation implements Callable<String> {

	private DeviceClient deviceClient;
	private DeviceClientConfig deviceConfig;
	private Object id;

	public Simulation(DeviceClientConfig deviceClientConfig, Object id) {
		this.deviceConfig = deviceClientConfig;
		this.id = id;
	}

	public void connect() {
		try {
			deviceClient = new DeviceClient(deviceConfig.asProperties());
			// Connect to Internet of Things Foundation
			deviceClient.connect();
			configureDeviceClient(deviceClient);
			BORegistry.getInstance().register(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		deviceClient.disconnect();

		BORegistry.getInstance().unregister(this);
	}

	public DeviceClient getDeviceClient() {
		return deviceClient;
	}

	public Object getId() {
		return id;
	}

	public String call() {
		System.out.println("Started Thread" + Thread.currentThread().getName());
		this.connect();
		this.process();
		this.disconnect();
		System.out.println("Ending Thread" + Thread.currentThread().getName());
		return deviceClient.getDeviceId();
	}

	protected abstract void process();

	protected abstract void configureDeviceClient(DeviceClient deviceClient);

}
