package core.support.logger;

import org.apache.logging.log4j.Level;

public class LogObject {

	public String value;
	public Level priority;

	public LogObject() {
	}

	public LogObject(String value, Level priority) {
		this.value = value;
		this.priority = priority;
	}
}