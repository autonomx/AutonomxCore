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

import core.support.configReader.Config;
import core.support.objects.DateFormats;

public class DateHelper {
	
	public static final String CONFIG_DATE_FORMAT = "date.format";
	public static final String CONFIG_DATE_ZONE = "date.zone";
	public static final String CONFIG_DATE_local = "date.local";
	
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
	public String getTime(Instant time, String format, String zone, Locale locale) {
		
		// set time zone
		if(!StringUtils.isBlank(zone)) {
			LocalDateTime timelocal = LocalDateTime.ofInstant(time, ZoneId.of("UTC"));
			ZoneId zoneId = ZoneId.of( zone );
			ZonedDateTime zdt = timelocal.atZone( zoneId );
			time = zdt.toInstant();
		}
		
		
		
		if(format.startsWith("cc") || format.startsWith("CC")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY").withZone(ZoneId.of("UTC"));
			String year = formatter.format(time).substring(0, 2);
			int cc = Integer.valueOf(year) + 1;
			
			format = format.replace("cc", "").replace("CC", "");
			formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
			return String.valueOf(cc) + formatter.format(time);
		}
		
		
		if(StringUtils.isBlank(format) && StringUtils.isBlank(zone) && locale == null)
			return time.toString();
		
		// default format. required for zone and locale
		if(StringUtils.isBlank(format))
			format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

		DateTimeFormatter formatter = null;
		if(locale != null)
			formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC")).withLocale(locale);
		else
			formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
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
		return getTime(timeString, format, zone, null);
	}
	
	public String getTimeString(String timeString, String format, String zone, String localeStr) {
		Locale locale = null;
				
		switch(localeStr) {
		  case "english":
			  locale = Locale.ENGLISH;
		    break;
		  case "france":
			  locale = Locale.FRANCE;
		    break;
		  case "germany":
			  locale = Locale.GERMAN;
		    break;
		  case "canada":
			  locale = Locale.CANADA;
		    break;
		  case "china":
			  locale = Locale.CHINA;
		    break;
		  case "japan":
			  locale = Locale.JAPAN;
		    break;
		  case "italy":
			  locale = Locale.ITALY;
		    break;
		  case "uk":
			  locale = Locale.UK;
		    break;
		  case "taiwan":
			  locale = Locale.TAIWAN;
		    break;
		  case "korea":
			  locale = Locale.KOREA;
		    break;
		  case "us":
			  locale = Locale.US;
		    break;
		    
		  default:
			 if(StringUtils.isBlank(localeStr))
				 locale = null;
			 else
				 Helper.assertFalse("correct local not selected. options: english, france, germany, canada, china, jpan, italy, uk, taiwan, korea, us");
		}
		return getTime(timeString, format, zone, locale) ;
		
	}

	/**
	 * get time based on format and time zone
	 * 
	 * @param time
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getTime(String timeString, String format, String zone, Locale locale) {
		LocalDateTime date1Date = getLocalDateTime(timeString);
		Instant timeInstant = date1Date.atZone(ZoneId.of("UTC")).toInstant();
		
		return getTime(timeInstant, format, zone, locale);
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
	 * is source string equal target date string
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean isDateEqual(String source, String target) {
		LocalDateTime date1Date = getLocalDateTime(target);
		
		LocalDateTime sourceDate = getLocalDateTime(source);
		if(sourceDate.isEqual(date1Date))
				return true;		
		return false;
	}
	
	/**
	 * is source string eqia; target date string
	 * @param sources
	 * @param date1
	 * @return
	 */
	public boolean isDateEqual(List<String> sources, String target) {
		LocalDateTime date1Date = getLocalDateTime(target);
		
		for(String source : sources ) {
			LocalDateTime sourceDate = getLocalDateTime(source);
			if(!(sourceDate.equals(date1Date)))
				return false;			
		}
		return true;
	}
	
	/**
	 * is source string not equal target date string
	 * @param source
	 * @param target
	 * @return
	 */
	public boolean isDateNotEqual(String source, String target) {
		LocalDateTime date1Date = getLocalDateTime(target);
		
		LocalDateTime sourceDate = getLocalDateTime(source);
		if(!sourceDate.isEqual(date1Date))
				return true;		
		return false;
	}
	
	/**
	 * is source string not equal target date string
	 * @param sources
	 * @param date1
	 * @return
	 */
	public boolean isDateNotEqual(List<String> sources, String target) {
		LocalDateTime date1Date = getLocalDateTime(target);
		
		for(String source : sources ) {
			LocalDateTime sourceDate = getLocalDateTime(source);
			if(sourceDate.equals(date1Date))
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
		String LocaleString = "LOCAL";
		
		if(!parameter.contains(ZoneString) && !parameter.contains(FormatString))
			return parameter;
		
		String[] values = parameter.split(";");
		List<String> updatedParameters = new ArrayList<String>();
		String zoneParameter = StringUtils.EMPTY;
		String FormatParameter = StringUtils.EMPTY;
		String LocaleParameter = StringUtils.EMPTY;

		// set updatedParameters list to store all values other than zone and format parameters
		for(String value : values) {
			if(!value.contains(ZoneString) && !value.contains(FormatString) && !value.contains(LocaleString))
				updatedParameters.add(value);
			if(value.contains(ZoneString)) {
				zoneParameter = value;
			}
			if(value.contains(FormatString))
				FormatParameter = value;
			if(value.contains(LocaleString))
				LocaleParameter = value;
		}
		
		// add zone, format, and locale parameters at the end
		updatedParameters.add(zoneParameter);
		updatedParameters.add(LocaleParameter);
		updatedParameters.add(FormatParameter);

		parameter = Joiner.on(";").join(updatedParameters); 
		return parameter;
	}
	
	/**
	 * reset format, zone, and local values
	 */
	public static void resetDateConfig() {
		Config.putValue(DateHelper.CONFIG_DATE_FORMAT, "");
		Config.putValue(DateHelper.CONFIG_DATE_ZONE, "");
		Config.putValue(DateHelper.CONFIG_DATE_local, "");
	}
}