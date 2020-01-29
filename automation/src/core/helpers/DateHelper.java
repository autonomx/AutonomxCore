package core.helpers;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;

public class DateHelper {

	// get time in milliseconds
	public String getTimestampMiliseconds() {
		return getTime("yyyyMMddHHmmssSSSSS");
	}

	public String getTimeInstance() {
		return Instant.now().toString();
	}

	public String getTimestampSeconds() {
		return getTime("yyyy-MM-dd HH:mm:ss");
	}

	public String getTimeISOInstant() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
	}

	/**
	 * get current time based on format and time zone
	 * 
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getTime(String format, String zone) {
		Instant time = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(zone));
		return formatter.format(time);
	}

	/**
	 * get current time based on format
	 * 
	 * @param format
	 * @return
	 */
	public String getTime(String format) {
		Instant time = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
		return formatter.format(time);
	}

	/**
	 * get time based on format
	 * 
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
		return formatter.format(time);
	}

	/**
	 * get time based on format and time zone
	 * 
	 * @param time
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getTime(Instant time, String format, String zone) {
		DateTimeFormatter formatter = null;

		if (StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return time.toString();

		if (!StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return getTime(time, format);

		if (StringUtils.isBlank(format) && !StringUtils.isBlank(zone)) {
			return time.atZone(ZoneId.of(zone)).toString();
		}

		formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(zone));
		return formatter.format(time);
	}
}
