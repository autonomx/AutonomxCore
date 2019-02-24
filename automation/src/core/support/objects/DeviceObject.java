package core.support.objects;

/**
 * @author CAEHMAT
 *
 * 
 */
public class DeviceObject {
	public String deviceName;
	public Boolean isAvailable;
	public DeviceType deviceType;
	public int devicePort = -1;
	
	public enum DeviceType {
	    iOS, Android, Win
	}
	
	public DeviceObject withDeviceName(String deviceName) {
		this.deviceName = deviceName;
		return this;
	}
	
	public DeviceObject withIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
		return this;
	}
	
	public DeviceObject withDeviceType(DeviceType type) {
		this.deviceType = type;
		return this;
	}
	
	public DeviceObject withDevicePort(int devicePort) {
		this.devicePort = devicePort;
		return this;
	}
	
}