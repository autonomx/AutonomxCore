package core.support.objects;

public class KeyValue {

	public String key;
	public String position;
	public String value;

	public KeyValue() {
	}
	
	public KeyValue(String key, String position, String value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public void add(String key, String position, String value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public void add(String key, String value) {
		this.key = key;
		this.value = value;
	}
}