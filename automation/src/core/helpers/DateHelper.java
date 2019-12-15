package core.helpers;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateHelper {
	
	// get time in miliseconds
	public String getTimestampMiliseconds() {
		return new SimpleDateFormat("yyyyMMddHHmmssSSSSS").format(new Date());
	}
	
	public String getTimeInstance() {
		return Instant.now().toString();
	}
	
	public String getTimestampSeconds() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	public String getTimeISOInstant() {
		return ZonedDateTime.now( ZoneOffset.UTC ).format( DateTimeFormatter.ISO_INSTANT );
	}
}
