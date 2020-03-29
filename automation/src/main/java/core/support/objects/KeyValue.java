package core.support.objects;

import java.util.List;

import core.support.logger.TestLog;

public class KeyValue {

	public String key;
	public String position;
	public Object value;

	public KeyValue() {
	}

	public KeyValue(String key, String position, Object value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public KeyValue(String key, String position, String value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public KeyValue(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public KeyValue(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public void add(String key, String position, Object value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public void add(String key, String position, String value) {
		this.key = key;
		this.position = position;
		this.value = value;
	}

	public void add(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public void add(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	/**
	 * print values for key values with prefix 
	 * prefix: eg. header, option...
	 * @param values
	 * @param prefix
	 */
	public static void printKeyValue(List<KeyValue> values, String prefix) {
		
		for(KeyValue value :  values) {
			if(value.value.toString().isEmpty())
					TestLog.logPass("setting " + prefix + ": " + value.key);
			else
				TestLog.logPass("setting " + prefix + ": " + value.key + ": " + value.value);
		}
	}
	
	/**
	 * update key value
	 * @param values
	 * @param updateValue
	 * @return
	 */
	public static List<KeyValue> updateKey(List<KeyValue> values, KeyValue updateValue){
		
		for(int i = 0; i < values.size(); i++) {
			if(values.get(i).key.equals(updateValue.key)) {
				values.add(i, updateValue);
				break;
			}
		}
		return values;
	}
}