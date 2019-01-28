package core.support.logger;

import org.apache.log4j.Priority;

public class logObject {

	public String value;
	public Priority priority;

	public logObject() {
	}

	public logObject(String value, Priority priority) {
		this.value = value;
		this.priority = priority;
	}
}