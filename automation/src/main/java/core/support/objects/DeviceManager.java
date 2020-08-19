package core.support.objects;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import core.helpers.Helper;
import core.support.objects.DeviceObject.DeviceType;

/**
 * @author CAEHMAT
 *
 * 
 */
public class DeviceManager {

	public static Map<String, DeviceObject> devices = new ConcurrentHashMap<String, DeviceObject>();

	/**
	 * get first available device for test ios or android simulator or real device
	 * sets device availability to false
	 * 
	 * @return
	 */
	public synchronized static String getFirstAvailableDevice(DeviceType deviceType) {
		for (Entry<String, DeviceObject> entry : devices.entrySet()) {
			if (entry.getValue().deviceType == deviceType && entry.getValue().isAvailable.equals(true)) {
				entry.getValue().isAvailable = false;
				devices.put(entry.getKey(), entry.getValue());

				// set device name
				TestObject.getTestInfo().deviceName = entry.getValue().deviceName;

				return entry.getValue().deviceName;
			}
		}
		return "";
	}

	/**
	 * load all devices And set availablility to true
	 * 
	 * @param deviceList
	 */
	public static void loadDevices(List<String> deviceList, DeviceType type) {

		for (String deviceName : deviceList) {
			if (devices.get(deviceName) == null) {
				DeviceObject device = new DeviceObject().withDeviceName(deviceName).withIsAvailable(true)
						.withDeviceType(type);
				devices.put(deviceName, device);
			}
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
		
		// multiple winapp devices not supported
		if(Helper.mobile.isWinApp())
			return;

		String deviceName = TestObject.getTestInfo().deviceName;

		if (devices.get(deviceName) != null)
			devices.put(deviceName, devices.get(deviceName).withIsAvailable(true));
		else
			Helper.assertFalse("device not found: " + deviceName);
	}
}