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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.reflections8.util.Joiner;

import core.apiCore.helpers.DataHelper;
import core.support.configReader.Config;
import core.support.objects.DateFormats;

public class DateHelper {
	
	public static final String CONFIG_DATE_FORMAT = "date.format";
	public static final String CONFIG_DATE_CURRENT_ZONE = "date.current.zone";
	public static final String CONFIG_DATE_OUTPUT_ZONE = "date.output.zone";
	public static final String CONFIG_DATE_LOCAL = "date.local";
	
	public static final String CONFIG_DATE_FORMAT_DEFAULT = "date.format.default";
	public static final String CONFIG_DATE_ZONE_INPUT_DEFAULT = "date.zone.input.default";
	public static final String CONFIG_DATE_ZONE_OUTPUT_DEFAULT = "date.zone.output.default";

	
	
	// get time in milliseconds
	public String getTimestampMiliseconds() {
		return getCurrentTime("yyyyMMddHHmmssSSSSS");
	}
	
	public String getCurrentTimeEpochSeconds() {
		String value = String.valueOf( Instant.now().getEpochSecond());
		return value;
	}
	
	public String getCurrentTimeEpochMS() {
		String value = String.valueOf( Instant.now().toEpochMilli());
		return value;
	}
	
	public String getTimeEpochMS(ZonedDateTime time) {
		String value = String.valueOf(time.toInstant().toEpochMilli());
		return value;
	}

	public String getTimeInstance() {
		return Instant.now().toString();
	}

	public String getTimestampSeconds() {
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}

	public String getTimeISOInstant() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
	}

	/**
	 * get current time based on format and time zone
	 * output zone will be UTC
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getCurrentTime(String format, String currentZone) {
		Instant time = Instant.now();
		return getTime(time, format, currentZone);
	}

	/**
	 * get current time based on format
	 * 
	 * @param format
	 * @return
	 */
	public String getCurrentTime(String format) {
		Instant time = Instant.now();
		return getTime(time, format);
	}
	
	/**
	 * get current time based on locale
	 * 
	 * @param format
	 * @return
	 */
	public String getCurrentTime(Locale locale) {
		Instant time = Instant.now();
		return getTime(time, locale);
	}
	
	/**
	 * get current time based on locale
	 * 
	 * @param format
	 * @return
	 */
	public String getCurrentTime(String format, String currentZone, String outputZone, Locale locale) {
		Instant time = Instant.now();
		return getTime(time, format, currentZone, outputZone, locale);
	}
	
	/**
	 * get time string
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time) {
		
		return getTime(time, StringUtils.EMPTY, "UTC", "UTC", null);
	}
	
	/**
	 * get time based on locale
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, Locale locale) {
		
		return getTime(time, StringUtils.EMPTY, "UTC", "UTC", locale);
	}
	
	/**
	 * get time based on format
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format) {
		
		return getTime(time, format, "UTC", "UTC", null);
	}
	
	/**
	 * get time based on format
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format, Locale locale) {
		
		return getTime(time, format, "UTC", "UTC", locale);
	}
	
	/**
	 * get time based on format
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format, String zone) {
		
		return getTime(time, format, zone, "UTC", null);
	}
	
	/**
	 * get time based on format
	 * output time zone will be UTC by default
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format, String zone, Locale locale) {
		
		return getTime(time, format, zone, "UTC", locale);
	}
	

	/**
	 * get time based on format
	 * @param time
	 * @param format
	 * @return
	 */
	public String getTime(Instant time, String format, String currentZone, String outputZone, Locale locale) {
		
		ZonedDateTime zdt = null;
		
		String formatDefault = Config.getValue(CONFIG_DATE_FORMAT_DEFAULT);
		String zoneInputDefault = Config.getValue(CONFIG_DATE_ZONE_INPUT_DEFAULT);
		String zoneOoutputDefault = Config.getValue(CONFIG_DATE_ZONE_OUTPUT_DEFAULT);
		
		// if default values left blank, set to these values
		if(StringUtils.isBlank(formatDefault) && StringUtils.isBlank(format)) format = "yyyy-MM-dd'T'HH:mm:ss.SSS";
		if(StringUtils.isBlank(zoneInputDefault) && StringUtils.isBlank(currentZone)) currentZone = "UTC";
		if(StringUtils.isBlank(zoneOoutputDefault) && StringUtils.isBlank(outputZone)) outputZone = "UTC";
		
		// set default time zone
		if(StringUtils.isBlank(currentZone)) currentZone = zoneInputDefault;
		if(StringUtils.isBlank(outputZone)) outputZone = zoneOoutputDefault;
		
		// set time zone to "zone" value
		if(!StringUtils.isBlank(currentZone)) {
			
			// set time to zone specified
			LocalDateTime timelocal = LocalDateTime.ofInstant(time, ZoneId.of("UTC"));
			zdt = ZonedDateTime.of(timelocal, ZoneId.of(currentZone)); 
		}
		
		// convert zone to utc
		zdt = zdt.withZoneSameInstant(ZoneId.of(outputZone));
		
		// default format. required for zone and locale
		if(StringUtils.isBlank(format))
			format = formatDefault;

		
		// format year
		if(format.startsWith("cc") || format.startsWith("CC")) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY").withZone(ZoneId.of("UTC"));
			String year = formatter.format(time).substring(0, 2);
			int cc = Integer.valueOf(year) + 1;
			
			format = format.replace("cc", "").replace("CC", "");
			formatter = DateTimeFormatter.ofPattern(format);
			return String.valueOf(cc) + formatter.format(zdt);
		}
		
		DateTimeFormatter formatter = null;
		if(locale != null)
			formatter = DateTimeFormatter.ofPattern(format).withLocale(locale);
		else
			formatter = DateTimeFormatter.ofPattern(format);
		return formatter.format(zdt);
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
		return getTime(timeString, format, zone, StringUtils.EMPTY, null);
	}
	
	/**
	 * get time based on format and time zone
	 * 
	 * @param time
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getTime(String timeString, String format, String outputZone, String zone) {
		return getTime(timeString, format, zone, outputZone, null);
	}
	
	public String getTimeString(String timeString, String format, String zone, String outputZone, String localeStr) {
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
		return getTime(timeString, format, zone, outputZone, locale);
		
	}

	/**
	 * get time based on format and time zone
	 * 
	 * @param time
	 * @param format
	 * @param zone
	 * @return
	 */
	public String getTime(String timeString, String format, String zone, String outputZone, Locale locale) {
		LocalDateTime date1Date = getLocalDateTime(timeString);
		Instant timeInstant = date1Date.atZone(ZoneId.of("UTC")).toInstant();
		
		return getTime(timeInstant, format, zone, outputZone, locale);
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
	 * reset format, zone, and local values
	 */
	private static void resetDateConfig() {
		Config.putValue(DateHelper.CONFIG_DATE_FORMAT, "", false);
		Config.putValue(DateHelper.CONFIG_DATE_CURRENT_ZONE, "", false);
		Config.putValue(DateHelper.CONFIG_DATE_OUTPUT_ZONE, "", false);
		Config.putValue(DateHelper.CONFIG_DATE_LOCAL, "", false);
	}
	
	/**
	 * get time based time modification, format or fixed time eg.
	 * <@_TIME_ISO_17+30h;setTime:14h23m33s> or
	 * <@_TIME_ISO_17+30h;FORMAT:yyyyMMddHHmmssSSS>
	 * 
	 * order of calculation: setTime,setDay,setMonth,time modification (+-hmdwm), format,timezone,locale
	 * 
	 * @param parameter
	 * @param timeString
	 * @return
	 */
	public String getTime(String parameter, String timeString) {

		parameter = DataHelper.replaceParameters(parameter, "\\{@(.+?)\\}","{@", "}");

		// ensure setTime, setDay, setMonth is added at beginning
		parameter = Helper.date.setTimeParameterFormat(parameter);
		
		String[] values = parameter.split(";");
		
		for (String value : values) {

			if (value.contains("FORMAT")) {
				String format = value.split("FORMAT")[1];
				format = removeFirstAndLastChars(format, ":", "<", ">");
				Config.putValue(DateHelper.CONFIG_DATE_FORMAT, format, false);
			} else if (value.contains("OUTPUT_ZONE")) {
				String zone = value.split("OUTPUT_ZONE")[1];
				zone = removeFirstAndLastChars(zone, ":", "<", ">");
				Config.putValue(DateHelper.CONFIG_DATE_OUTPUT_ZONE, zone, false);
			} else if (value.contains("ZONE")) {
				String zone = value.split("ZONE")[1];
				zone = removeFirstAndLastChars(zone, ":", "<", ">");
				Config.putValue(DateHelper.CONFIG_DATE_CURRENT_ZONE, zone, false);
			} else if (value.contains("LOCALE")) {
				String locale = value.split("LOCALE")[1];
				locale = removeFirstAndLastChars(locale, ":", "<", ">");
				Config.putValue(DateHelper.CONFIG_DATE_LOCAL, locale, false);
			} else if (value.contains("setInitialDate")) {
				String setInitialDate = value.split("setInitialDate")[1];
				setInitialDate = removeFirstAndLastChars(setInitialDate, ":", "<", ">");
				timeString = setInitialDate(setInitialDate);
			} else if (value.contains("setTime")) {
				String setTime = value.split("setTime")[1];
				setTime = removeFirstAndLastChars(setTime, ":", "<", ">");
				timeString = setTime(setTime, timeString);
			} else if (value.contains("setDay")) {
				String setDay = value.split("setDay")[1];
				setDay = removeFirstAndLastChars(setDay, ":", "<", ">");
				timeString = setDay(setDay, timeString);
			} else if (value.contains("setMonth")) {
				String setDay = value.split("setMonth")[1];
				setDay = removeFirstAndLastChars(setDay, ":", "<", ">");
				timeString = setMonth(setDay, timeString);
			} else {
				value = removeFirstAndLastChars(value, ":", "<", ">");
				timeString = getTimeWithModification(value, timeString);
			}
		}
		
	
		timeString = Helper.date.getTimeString(timeString, Config.getValue(DateHelper.CONFIG_DATE_FORMAT), Config.getValue(DateHelper.CONFIG_DATE_CURRENT_ZONE), Config.getValue(DateHelper.CONFIG_DATE_OUTPUT_ZONE), Config.getValue(DateHelper.CONFIG_DATE_LOCAL));
		
		// reset format, zone, and local values
		DateHelper.resetDateConfig();
		return timeString;
	}
	
	/**
	 * setTime, setDay, setMonth parameters at beginning of date modification list
	 * @param parameter
	 * @return
	 */
	public String setTimeParameterFormat(String parameter) {
		String setTimeString = "setTime";
		String setDayString = "setDay";
		String setMonthString = "setMonth";

		if(!parameter.contains(setTimeString) && !parameter.contains(setDayString) && !parameter.contains(setMonthString))
			return parameter;
		
		String[] values = parameter.split(";");
		List<String> updatedParameters = new ArrayList<String>();
		List<String> existingParameters = new ArrayList<String>();

		// set updatedParameters list to store all values other than zone and format parameters
		for(String value : values) {
			if(!value.contains(setTimeString) && !value.contains(setDayString) && !value.contains(setMonthString))
				existingParameters.add(value);
			else if(value.contains(setDayString)) {
				updatedParameters.add(value);
			}
			else if(value.contains(setTimeString)) {
				updatedParameters.add(value);
			}
			else if(value.contains(setMonthString)) {
				updatedParameters.add(value);
			}
		}
		
		// add setTime at beginning
		updatedParameters.addAll(existingParameters);

		parameter = Joiner.on(";").join(updatedParameters); 
		return parameter;
	}
	
	/**
	 * set time to time 
	 * overwrites the TestObject.START_TIME_STRING value
	 * @param parameter
	 * @return
	 */
	public String setInitialDate(String parameter) {
		LocalDateTime date = Helper.date.getLocalDateTime(parameter);
		Instant time = date.atZone(ZoneId.of("UTC")).toInstant();
		return time.toString();
	}


	/**
	 * sets time based on format: setTime:hh:mm:ss eg: 14:42:33 any combination will
	 * work uses utc zone to set time
	 * 
	 * @param parameter
	 * @param timeString
	 * @return
	 */
	public String setTime(String parameter, String timeString) {
		LocalDateTime date = Helper.date.getLocalDateTime(timeString);
		Instant time = date.atZone(ZoneId.of("UTC")).toInstant();

		String[] parameters = parameter.split(":");
		if (parameters.length != 3)
			Helper.assertFalse("format must be hh:mm:ss. value: " + parameter);
		int hour = Helper.getIntFromString(parameters[0]);
		int minute = Helper.getIntFromString(parameters[1]);
		int second = Helper.getIntFromString(parameters[2]);

		time = time.atZone(ZoneOffset.UTC).withHour(hour).withMinute(minute).withSecond(second).withNano(0).toInstant();
		return time.toString();
	}

	/**
	 * set day based on format setDay:Day
	 * 
	 * @param parameter
	 * @param timeString
	 * @return
	 */
	public String setDay(String dayName, String timeString) {
		LocalDateTime time = Helper.date.getLocalDateTime(timeString);

		int currentDay = Helper.date.getDayOfWeekIndex(time);
		int targetDay = Helper.date.getDayOfWeekIndex(dayName);
		int timeDifference = targetDay - currentDay;

		time = time.plusDays(timeDifference);
		return time.toString();
	}

	/**
	 * set month based on format setMonth:Month
	 * 
	 * @param monthName
	 * @param timeString
	 * @return
	 */
	public String setMonth(String monthName, String timeString) {
		LocalDateTime time = Helper.date.getLocalDateTime(timeString);

		int currentMonth = Helper.date.getMonthOfYearIndex(time);
		int targetMonth = Helper.date.getMonthOfYearIndex(monthName);
		int timeDifference = targetMonth - currentMonth;

		time = time.plusMonths(timeDifference);
		return time.toString();
	}

	/**
	 * removes surrounding character from string
	 * 
	 * @param value
	 * @param toRemove
	 * @return
	 */
	public String removeFirstAndLastChars(String value, String... toRemove) {
		if (StringUtils.isBlank(value))
			return value;
		if (toRemove.length == 0)
			return value;

		for (String remove : toRemove) {
			if (value.startsWith(remove))
				value = StringUtils.removeStart(value, remove);
			if (value.endsWith(remove))
				value = StringUtils.removeEnd(value, remove);
		}
		return value;
	}

	/**
	 * time: _TIME_STRING_17-72h or _TIME_STRING_17+72h
	 * 
	 * @param parameter: time parameter with modification. eg. _TIME_STRING_17-72h
	 * @param timeString
	 * @return
	 */
	public String getTimeWithModification(String parameter, String timeString) {
		LocalDateTime localTime = Helper.date.getLocalDateTime(timeString);
		Instant newTime = localTime.atZone(ZoneId.of("UTC")).toInstant();

		String[] parameterArray = parameter.split("(?=[+-])");
		List<String> parameterList = new LinkedList<String>(Arrays.asList(parameterArray));
		
		if(!parameterList.get(0).contains("_TIME"))
			Helper.assertFalse("Date does not have correct format. must start with: _TIME");
		
		parameterList.remove(0);
		
		for(String value: parameterList) {

			// return non modified time if modifier not set
			if (parameterArray.length == 1)
				return newTime.toString();
	
			String modifier = value.split("[+-]")[1];
	
			String modiferSign = value.replaceAll("[^+-]", "");
			int modifierDuration = Helper.getIntFromString(modifier);
			String modifierUnit = modifier.replaceAll("[^A-Za-z]+", "");
	
			if (modiferSign.isEmpty() || modifierDuration == -1 || modifierUnit.isEmpty())
				Helper.assertFalse("invalid time modifier. format: eg. _TIME_STRING_17+72h or _TIME_STRING_17-72m");
	
			switch (modifierUnit) {
			case "y":
				if (modiferSign.equals("+"))
					localTime = localTime.plusYears(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusYears(modifierDuration);
				break;
			case "mo":
				if (modiferSign.equals("+"))
					localTime = localTime.plusMonths(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusMonths(modifierDuration);
				break;
			case "w":
				if (modiferSign.equals("+"))
					localTime = localTime.plusWeeks(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusWeeks(modifierDuration);
				break;
			case "d":
				if (modiferSign.equals("+"))
					localTime = localTime.plusDays(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusDays(modifierDuration);
				break;
			case "h":
				if (modiferSign.equals("+"))
					localTime = localTime.plusHours(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusHours(modifierDuration);
				break;
			case "m":
				if (modiferSign.equals("+"))
					localTime = localTime.plusMinutes(modifierDuration);
				else if (modiferSign.equals("-"))
					localTime = localTime.minusMinutes(modifierDuration);
				break;
			default:
				Helper.assertFalse("invalid time modifier. format: eg. +2d or +72h or -72m or +1mo or +2y");
	
			}
		}
		String dateString = localTime.toInstant(ZoneOffset.UTC).toString();
		return dateString;
	}
	
}