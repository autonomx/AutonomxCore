package core.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {
	
	// get time in miliseconds
	public String getTimestampMiliseconds() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
	}
	
	public String getTimestampSeconds() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
}
