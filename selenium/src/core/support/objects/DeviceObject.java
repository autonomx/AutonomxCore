package core.support.objects;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.openqa.selenium.Capabilities;

import core.helpers.Helper;
import io.appium.java_client.AppiumDriver;

/**
 * @author CAEHMAT
 *
 * 
 */
public class DeviceObject {

	public String deviceName;
	public Boolean isAvailable = true;

	public static Map<String, Boolean> devices = new ConcurrentHashMap<String, Boolean>();

	/**
	 * get first avaialble device for test ios or android simulator or real device
	 * 
	 * @return
	 */
	public synchronized static String getFirstAvailableDevice() {
		for (Entry<String, Boolean> entry : devices.entrySet()) {
			if (entry.getValue().equals(true)) {
				devices.put(entry.getKey(), false);
				return entry.getKey();
			}
		}
		return "";
	}

	/**
	 * load all devices and set availablility to true
	 * 
	 * @param deviceList
	 */
	public static void loadDevices(List<String> deviceList) {
		if (!devices.isEmpty())
			return;

		for (String device : deviceList) {
			devices.put(device, true);
		}
	}

	/**
	 * set device availability used for device management
	 * 
	 * @param isAvailable
	 */
	public static void setDeviceAvailability(Boolean isAvailable) {
		// if its not a mobile test, return
		if (!Helper.mobile.isMobile())
			return;

		Capabilities cap = ((AppiumDriver<?>) TestObject.getTestInfo().webDriverList).getCapabilities();
		String deviceName = cap.getCapability("deviceName").toString();
		String deviceId = cap.getCapability("udid").toString();

		if (devices.get(deviceName) != null)
			devices.put(deviceName, isAvailable);
		if (devices.get(deviceId) != null)
			devices.put(deviceId, isAvailable);
		Helper.assertFalse("device not found: " + deviceName + " or deviceId: " + deviceId);
	}
}