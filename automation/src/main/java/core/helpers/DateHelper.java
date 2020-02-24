package core.helpers;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

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
	public String getTime(String timeString, String format, String zone) {
		Instant timeInstant = Instant.parse(timeString);
		DateTimeFormatter formatter = null;

		if (StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return timeInstant.toString();

		if (!StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return getTime(timeInstant, format);

		if (StringUtils.isBlank(format) && !StringUtils.isBlank(zone)) {
			LocalDateTime timelocal = LocalDateTime.ofInstant(timeInstant, ZoneId.of(zone));	
			ZoneId systemZone =  ZoneId.of(zone);
			ZoneOffset offset = systemZone.getRules().getOffset(timeInstant);
			String dateString = timelocal.toInstant(offset).toString();
			return dateString;
		}

		formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(zone));
		return formatter.format(timeInstant);
	}
	
	/**
	 * get day of week 
	 * @param day
	 * @return
	 */
	public int getDayOfWeekIndex(String day) {
		DateTimeFormatter dayOfWeekFormatter= DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);
		
		Locale loc = Locale.US;
		WeekFields wf = WeekFields.of(loc);
		
		DayOfWeek dayOfWeek = DayOfWeek.from(dayOfWeekFormatter.parse(day));
		int dayNumber = dayOfWeek.get(wf.dayOfWeek());
		return dayNumber;
	}
	
	/**
	 * get day of week
	 * @param time
	 * @return
	 */
	public int getDayOfWeekIndex(LocalDateTime time) {
		Locale loc = Locale.US;
		WeekFields wf = WeekFields.of(loc);
		
		DayOfWeek dayOfWeek = time.getDayOfWeek();
		int day = dayOfWeek.get(wf.dayOfWeek());
		return day;
	}
	
	/**
	 * get month of year
	 * @param time
	 * @return
	 */
	public int getMonthOfYearIndex(String month) {
		int monthNum = Month.valueOf(month.toUpperCase()).getValue();
		return monthNum;
	}
	
	/**
	 * get month of year
	 * @param time
	 * @return
	 */
	public int getMonthOfYearIndex(LocalDateTime time) {
		return time.getMonth().getValue();
	}
	
}
