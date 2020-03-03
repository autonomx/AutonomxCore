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
import java.util.ArrayList;
import java.util.List;
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
			ZoneId systemZone = ZoneId.of(zone);
			ZoneOffset offset = systemZone.getRules().getOffset(timeInstant);
			String dateString = timelocal.toInstant(offset).toString();
			return dateString;
		}

		formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of(zone));
		return formatter.format(timeInstant);
	}

	/**
	 * get day of week
	 * 
	 * @param day
	 * @return
	 */
	public int getDayOfWeekIndex(String day) {
		DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH);

		Locale loc = Locale.US;
		WeekFields wf = WeekFields.of(loc);

		DayOfWeek dayOfWeek = DayOfWeek.from(dayOfWeekFormatter.parse(day));
		int dayNumber = dayOfWeek.get(wf.dayOfWeek());
		return dayNumber;
	}

	/**
	 * get day of week
	 * 
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
	 * 
	 * @param time
	 * @return
	 */
	public int getMonthOfYearIndex(String month) {
		int monthNum = Month.valueOf(month.toUpperCase()).getValue();
		return monthNum;
	}

	/**
	 * get month of year
	 * 
	 * @param time
	 * @return
	 */
	public int getMonthOfYearIndex(LocalDateTime time) {
		return time.getMonth().getValue();
	}
	
	/**
	 * is source date between date1 and date2
	 * @param source
	 * @param date1
	 * @param date2
	 * @return
	 */
	public boolean isBetweenDates(String source, String date1, String date2) {
		LocalDateTime sourceDate = getLocalDateTime(source);
		LocalDateTime date1Date = getLocalDateTime(date1);
		LocalDateTime date2Date = getLocalDateTime(date2);

		if(sourceDate.isAfter(date1Date) && sourceDate.isBefore(date2Date))
			return true;
		return false;
	}
	
	/**
	 * are source dates between date1 and date2
	 * @param sources
	 * @param date1
	 * @param date2
	 * @return
	 */
	public boolean isBetweenDates(List<String> sources, String date1, String date2) {
		
		LocalDateTime date1Date = getLocalDateTime(date1);
		LocalDateTime date2Date = getLocalDateTime(date2);
		
		for(String source : sources ) {
			LocalDateTime sourceDate = getLocalDateTime(source);
			if(sourceDate.isBefore(date1Date) || sourceDate.isAfter(date2Date))
				return false;
				
		}
		return true;
	}

	
	/**
	 * get local date time from date string
	 * @param timeString
	 * @return
	 */
	public LocalDateTime getLocalDateTime(String timeString) {
		
		List<String> formats = new ArrayList<String>();
		formats.add("yyyy-MM-dd HH:mm");
		formats.add("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSSSSSSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSSSSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSSSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SSS");
		formats.add("yyyy-MM-dd HH:mm:ss.SS");
		formats.add("yyyy-MM-dd HH:mm:ss.S");
		formats.add("yyyy-MM-dd");
		formats.add("yyyy-MM-dd'T'HH:mm:ssZ");
		formats.add("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		formats.add("yyyy-MM-dd'T'HH:mm:ssX");
		formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		formats.add("yyMMddHHmmssZ");
		formats.add("yyyyy.MMMMM.dd GGG hh:mm aaa");
		formats.add("yyMMddHHmmssZ");
		formats.add("yyMMddHHmm");
		formats.add("yyyy.MM.dd G 'at' HH:mm:ss z");
		formats.add("h:mm a");
		formats.add("yyyyy.MMMMM.dd GGG hh:mm aaa");
		formats.add("ccyy-mm-dd");

		for(String format : formats) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
				LocalDateTime dateTime = LocalDateTime.parse(timeString, formatter);
				return dateTime;
			}catch(Exception e) {
				e.getMessage();
			}
		}
		Helper.assertFalse("no matching date format for string: " + timeString);
		return null;
	}

}
