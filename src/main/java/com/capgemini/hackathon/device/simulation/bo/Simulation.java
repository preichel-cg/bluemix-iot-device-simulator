package com.capgemini.hackathon.device.simulation.bo;

import java.util.concurrent.Callable;

import com.capgemini.hackathon.device.simulation.ApplicationClientConfig;
import com.capgemini.hackathon.device.simulation.DeviceClientConfig;
import com.ibm.iotf.client.app.ApplicationClient;
import com.ibm.iotf.client.device.DeviceClient;

public abstract class Simulation implements Callable<String> {

	private DeviceClient deviceClient;
	private ApplicationClient applicationClient;

	private DeviceClientConfig deviceConfig;
	private ApplicationClientConfig appConfig;

	public Simulation(DeviceClientConfig deviceClientConfig, ApplicationClientConfig appClientConfig) {
		this.deviceConfig = deviceClientConfig;
		this.appConfig = appClientConfig;
	}

	public Simulation(DeviceClientConfig deviceClientConfig) {
		this(deviceClientConfig, null);
	}

	public void connect() {
		try {
			deviceClient = new DeviceClient(deviceConfig.asProperties());
			// Connect to Internet of Things Foundation
			deviceClient.connect();
			configureDeviceClient(deviceClient);

			applicationClient = new ApplicationClient(appConfig.asProperties());
			applicationClient.connect();
			configureAppplicationClient(applicationClient);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		deviceClient.disconnect();
		applicationClient.disconnect();
	}

	public DeviceClient getDeviceClient() {
		return deviceClient;
	}

	public ApplicationClient getApplicationClient() {
		return applicationClient;
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

	protected abstract void configureAppplicationClient(ApplicationClient applicationClient);

}
