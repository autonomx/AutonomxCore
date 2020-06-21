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
import org.reflections8.util.Joiner;

import core.support.objects.DateFormats;

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
		if(format.startsWith("cc") || format.startsWith("CC")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY").withZone(ZoneId.of("UTC"));
			String year = formatter.format(time).substring(0, 2);
			int cc = Integer.valueOf(year) + 1;
			
			format = format.replace("cc", "").replace("CC", "");
			formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
			return String.valueOf(cc) + formatter.format(time);
		}
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
		LocalDateTime date1Date = getLocalDateTime(timeString);
		Instant timeInstant = date1Date.atZone(ZoneId.of("UTC")).toInstant();
		
		DateTimeFormatter formatter = null;

		if (StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return timeInstant.toString();

		if (!StringUtils.isBlank(format) && StringUtils.isBlank(zone))
			return getTime(timeInstant, format);

		if (StringUtils.isBlank(format) && !StringUtils.isBlank(zone)) {
			LocalDateTime timelocal = LocalDateTime.ofInstant(timeInstant, ZoneId.of("UTC"));
			ZoneId zoneId = ZoneId.of( zone );
			ZonedDateTime zdt = timelocal.atZone( zoneId );

			String dateString = zdt.toInstant().toString();
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
			if(!(sourceDate.isAfter(date1Date) && sourceDate.isBefore(date2Date)))
				return false;
				
		}
		return true;
	}
	
	/**
	 * if date is after target date1
	 * @param source
	 * @param date1
	 * @return
	 */
	public boolean isDateAfter(String source, String date1) {
		LocalDateTime date1Date = getLocalDateTime(date1);
		
		LocalDateTime sourceDate = getLocalDateTime(source);
		if(sourceDate.isAfter(date1Date))
				return true;
				
		return false;
	}
	
	/**
	 * if date list is after target date1
	 * @param source
	 * @param date1
	 * @return
	 */
	public boolean isDateAfter(List<String> sources, String date1) {
		LocalDateTime date1Date = getLocalDateTime(date1);
		
		for(String source : sources ) {
			LocalDateTime sourceDate = getLocalDateTime(source);
			if(!(sourceDate.isAfter(date1Date)))
				return false;
				
		}
		return true;
	}
	
	/**
	 * if date list is before date1
	 * @param source
	 * @param date1
	 * @return
	 */
	public boolean isDateBefore(String source, String date1) {
		LocalDateTime date1Date = getLocalDateTime(date1);
		
		LocalDateTime sourceDate = getLocalDateTime(source);
		if(sourceDate.isBefore(date1Date))
				return true;
				
		return false;
	}
	
	/**
	 * if date list is before date1
	 * @param source
	 * @param date1
	 * @return
	 */
	public boolean isDateBefore(List<String> sources, String date1) {
		LocalDateTime date1Date = getLocalDateTime(date1);
		
		for(String source : sources ) {
			LocalDateTime sourceDate = getLocalDateTime(source);
			if(!(sourceDate.isBefore(date1Date)))
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

		LocalDateTime dateTime = null;
		timeString = timeString.trim();
		
		// for date formats
		for(String format : DateFormats.dateFormats) {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
				dateTime = LocalDateTime.parse(timeString.trim(), formatter);
				return dateTime;
			}catch(Exception e) {
				e.getMessage();
			}
		}
		
		// for epoch time for seconds and milliseconds
		if(Helper.isNumeric(timeString)) {
			try {
				Long epochTime = Long.valueOf(timeString);
				if(timeString.length() <= 10)
					dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochTime), ZoneId.of("UTC"));
				else 
					dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneId.of("UTC"));
				return dateTime;
			}catch(Exception e) {
				e.getMessage();
			}
		}
		
		Helper.assertFalse("no matching date format for string: " + timeString);
		return null;
	}
	
	
	/**
	 * reorders time zone and format 
	 * zone and format will come at the end
	 * eg. _TIME_MS_23+1w;FORMAT:YYYY-MM-DD;ZONE:Canada/Pacific;setDay:Monday;setTime:09:00:00
	 * becomes _TIME_MS_23+1w;setDay:Monday;setTime:09:00:00;ZONE:Canada/Pacific;FORMAT:YYYY-MM-DD
	 * @param value
	 * @return
	 */
	public String setTimeParameterFormat(String parameter) {
		String FormatString = "FORMAT";
		String ZoneString = "ZONE";
		
		if(!parameter.contains(ZoneString) && !parameter.contains(FormatString))
			return parameter;
		
		String[] values = parameter.split(";");
		List<String> updatedParameters = new ArrayList<String>();
		String zoneParameter = StringUtils.EMPTY;
		String FormatParameter = StringUtils.EMPTY;

		// set updatedParameters list to store all values other than zone and format parameters
		for(String value : values) {
			if(!value.contains(ZoneString) && !value.contains(FormatString))
				updatedParameters.add(value);
			if(value.contains(ZoneString)) {
				zoneParameter = value;
			}
			if(value.contains(FormatString))
				FormatParameter = value;
		}
		
		// add zone and format parameters at the end
		updatedParameters.add(zoneParameter);
		updatedParameters.add(FormatParameter);
		
		parameter = Joiner.on(";").join(updatedParameters); 
		return parameter;
	}
}