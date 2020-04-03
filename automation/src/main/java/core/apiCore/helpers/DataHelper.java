package core.apiCore.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import com.opencsv.CSVReader;

import core.apiCore.TestDataProvider;
import core.apiCore.interfaces.ExternalInterface;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.configReader.PropertiesReader;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import io.netty.util.internal.StringUtil;

public class DataHelper {

	public static final String VERIFY_JSON_PART_INDICATOR = "_VERIFY.JSON.PART_";
	public static final String VERIFY_JSON_PART_INDICATOR_UNDERSCORE = "_VERIFY_JSON_PART_";
	public static final String VERIFY_RESPONSE_BODY_INDICATOR = "_VERIFY_RESPONSE_BODY_";
	public static final String VERIFY_RESPONSE_NO_EMPTY = "_NOT_EMPTY_";
	public static final String VERIFY_HEADER_PART_INDICATOR = "_VERIFY_HEADER_PART_";
	public static final String VERIFY_TOPIC_PART_INDICATOR = "_VERIFY_TOPIC_PART_";
	public static final String EXPECTED_MESSAGE_COUNT = "EXPECTED_MESSAGE_COUNT";

	public static final String TEST_DATA_TEMPLATE_DATA_PATH = "api.templateDataFile";

	public enum JSON_COMMAND {
		hasItems, notHaveItems, notEqualTo, equalTo, notContain, contains, containsInAnyOrder, integerGreaterThan,
		integerLessThan, integerEqual, integerNotEqual, nodeSizeGreaterThan, nodeSizeExact, sequence, jsonbody,
		isNotEmpty, isEmpty, nodeSizeLessThan, isBetweenDate, allValuesEqualTo, countGreaterThan, countLessThan, countExact, command, notContains, contain
	}

	/**
	 * replaces placeholder values with values from config properties replaces only
	 * string values
	 * 
	 * @param source
	 * @return
	 */
	public static String replaceParameters(String source) {

		if (source.isEmpty())
			return source; 

		List<String> parameters = Helper.getValuesFromPattern(source, "<@(.+?)>");
		Object value = null;
		int length = 0;
		String newTime = StringUtils.EMPTY;
		for (String parameter : parameters) {
			if (parameter.contains("_TIME_MS_")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				Instant time = Instant.parse(newTime);
				value = getTimeSubstring(parameter, String.valueOf(time.toEpochMilli()));
			} else if (parameter.contains("_TIME_STRING_")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, Helper.date.getTime(newTime, "yyyyMMddHHmmssSSS", null));
			} else if (parameter.contains("_TIME_ISO_")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, newTime);
			} else if (parameter.contains("_TIME")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, newTime);
			} else if (parameter.contains("_RAND")) {
				length = Helper.getIntFromString(parameter);
				value = Config.getValue(TestObject.RANDOM_STRING).substring(0, length);
			} else if (parameter.contains("_INCREMENT_FROM_")) {
				value = getIncrementalValue(parameter);
			} else if (parameter.contains("_XML")) {
				// syntax:e.g. <@_XML:ID:1> will be replaced by 2
				String[] valueArray = parameter.split(":");
				int index = 0;
				String tag = valueArray[1];
				if (valueArray.length == 3) // if has index value
					index = Integer.valueOf(parameter.split(":")[2]);
				value = XmlHelper.getXmlTagValue(source, tag, index + 1);
			} else {
				value = Config.getObjectValue(parameter.replace("@", ""));
			}
			if (value == null)
				TestLog.logWarning("parameter value not found: " + parameter);
			else {
					// don't log if value already replaced
					if(TestObject.getTestInfo().replacedValues.get(parameter) == null || !TestObject.getTestInfo().replacedValues.get(parameter).toString().equals(value.toString()))
					{
						//TestLog.ConsoleLog("replacing value " + parameter + "  with: " + value);
						TestObject.getTestInfo().replacedValues.put(parameter, value);
					}
					source = source.replace("<@" + parameter + ">", Matcher.quoteReplacement(value.toString()));
				}
		}

		return source;
	}

	/**
	 * incremental keyword value setter starting value + current run count by
	 * default: starting value = 1 eg. <@_INCREMENT_FROM_3> current test count is
	 * appended to test case id. eg. validateId_run_1
	 * 
	 * @param parameter
	 */
	public static int getIncrementalValue(String parameter) {
		int startingValue = Helper.getIntFromString(parameter);

		int testCurrentRunCount = 1;
		String testId = TestObject.getTestInfo().serviceObject.getTestCaseID();
		if (testId.matches(".*" + CsvReader.SERVICE_RUN_PREFIX + "(\\d)?$")) {
			testId = testId.substring(testId.lastIndexOf(CsvReader.SERVICE_RUN_PREFIX) + 1);
			testCurrentRunCount = Helper.getIntFromString(testId);
		}

		int incrementalValue = startingValue + testCurrentRunCount - 1;
		return incrementalValue;
	}

	/**
	 * get substring of time based on time string length format _TIME_STRING_17-72h.
	 * 17 is the length
	 * 
	 * @param parameter
	 * @param maxLength
	 * @param finalTime
	 * @return
	 */
	public static String getTimeSubstring(String parameter, String finalTime) {
		// values after the last "_", then after the last :

		Matcher matcher = Pattern.compile("\\d+").matcher(parameter);
		matcher.find();
		int length = Integer.valueOf(matcher.group());

		int maxLength = finalTime.length();
		if (length > maxLength || length == -1)
			length = maxLength;

		return finalTime.substring(0, length);
	}

	/**
	 * get time based time modification, format or fixed time eg.
	 * <@_TIME_ISO_17+30h;setTime:14h23m33s> or
	 * <@_TIME_ISO_17+30h;FORMAT:yyyyMMddHHmmssSSS>
	 * 
	 * @param parameter
	 * @param timeString
	 * @return
	 */
	public static String getTime(String parameter, String timeString) {

		// ensure ZONE and FORMAT are ordered at the end
		parameter = Helper.date.setTimeParameterFormat(parameter);
		
		String[] values = parameter.split(";");

		for (String value : values) {

			if (value.contains("FORMAT")) {
				String format = value.split("FORMAT")[1];
				format = removeFirstAndLastChars(format, ":", "<", ">");
				timeString = Helper.date.getTime(timeString, format, null);
			} else if (value.contains("ZONE")) {
				String zone = value.split("ZONE")[1];
				zone = removeFirstAndLastChars(zone, ":", "<", ">");
				timeString = Helper.date.getTime(timeString, null, zone);
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
		return timeString;
	}

	/**
	 * sets time based on format: setTime:hh:mm:ss eg: 14:42:33 any combination will
	 * work uses utc zone to set time
	 * 
	 * @param parameter
	 * @param timeString
	 * @return
	 */
	public static String setTime(String parameter, String timeString) {
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
	public static String setDay(String dayName, String timeString) {
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
	public static String setMonth(String monthName, String timeString) {
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
	public static String removeFirstAndLastChars(String value, String... toRemove) {
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
	public static String getTimeWithModification(String parameter, String timeString) {
		Instant time = Instant.parse(timeString);
		LocalDateTime localTime = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
		Instant newTime = time;

		String[] parameterArray = parameter.split("[+-]");

		// return non modified time if modifier not set
		if (parameterArray.length == 1)
			return newTime.toString();

		String modifier = parameter.split("[+-]")[1];

		String modiferSign = parameter.replaceAll("[^+-]", "");
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
		String dateString = localTime.toInstant(ZoneOffset.UTC).toString();
		return dateString;
	}
	
	/**
	 * gets the map of the validation requirements split by ";"
	 * 
	 * @param expected
	 * @return
	 */
	public static List<KeyValue> getValidationMap(String expected) {
		return getValidationMap(expected, ";");
	}

	/**
	 * gets the map of the validation requirements split by ";"
	 * 
	 * @param expected
	 * @return
	 */
	public static List<KeyValue> getValidationMap(String expected, String separator) {
		// get hashmap of json path And verification
		List<KeyValue> keywords = new ArrayList<KeyValue>();

		// remove json indicator _VERIFY.JSON.PART_
		expected = JsonHelper.removeResponseIndicator(expected);

		String[] keyVals = expected.split(separator);
		String key = "";
		String position = "";
		String value = "";
		for (String keyVal : keyVals) {

			// if empty, skip
			if (keyVal.isEmpty())
				continue;

			List<String> parts = splitToKeyPositionValue(keyVal, ":", 3);
			if (parts.size() == 1) {
				key = Helper.stringRemoveLines(parts.get(0));
			}
			if (parts.size() == 2) { // without position
				key = Helper.stringRemoveLines(parts.get(0));
				position = StringUtil.EMPTY_STRING;
				value = Helper.stringRemoveLines(parts.get(1));
			} else if (parts.size() == 3) { // with position
				key = Helper.stringRemoveLines(parts.get(0));
				position = Helper.stringRemoveLines(parts.get(1));
				value = Helper.stringRemoveLines(parts.get(2));
			}

			// if there is a value
			if (!key.isEmpty()) {
				KeyValue keyword = new KeyValue(key, position, value);
				keywords.add(keyword);
			}
		}
		return keywords;
	}
	
	public static String getTemplateFileLocation(String file) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH).trim();
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;

		return templateTestPath + file;
	}

	public static Path getTemplateFilePath(String file) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH).trim();
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;
		String fullLocation = templateTestPath + file;
		return new File(fullLocation).toPath();
	}

	public static File getFile(String filename) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH).trim();
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;
		File file = new File(templateTestPath + filename);
		return file;
	}

	/**
	 * returns service object template file as string Template file name is from
	 * Template column in csv
	 * 
	 * @param apiObject
	 * @return
	 * @throws IOException
	 */
	public static String getServiceObjectTemplateString(ServiceObject serviceObject) {

		Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
		return DataHelper.convertFileToString(templatePath);
	}

	/**
	 * validates response against expected values
	 * 
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @return
	 */
	public static String validateCommand(String command, String responseString, String expectedString) {
		String error = validateExpectedCommand(command, responseString, expectedString, StringUtils.EMPTY);
		if (error.isEmpty())
			TestLog.ConsoleLog(
					"validation passed for command: " + responseString + " " + command + " " + expectedString);
		else
			TestLog.ConsoleLog("validation failed for command: " + command + " with error: " + error);

		return error;
	}

	/**
	 * validates response against expected values
	 * 
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @param position
	 * @return
	 */
	public static String validateCommand(String command, String responseString, String expectedString,
			String position) {
		String error = validateExpectedCommand(command, responseString, expectedString, position);
		if (error.isEmpty())
			TestLog.ConsoleLog("validation passed for command: response " + command + " " + expectedString);
		else
			TestLog.ConsoleLog("validation failed for command: " + command + " with error: " + error);

		return error;
	}

	/**
	 * validates response against expected values
	 * 
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @param position
	 */
	public static String validateExpectedCommand(String command, String responseString, String expectedString,
			String position) {

		// remove surrounding quotes
		expectedString = Helper.removeSurroundingQuotes(expectedString);
		command = Helper.removeSurroundingQuotes(command);
		responseString = Helper.removeSurroundingQuotes(responseString);

		List<String> expectedArray = getResponseArray(expectedString);
		List<String> actualArray = getResponseArray(responseString);
		String actualString = "";

		int positionInt = 0;

		// if position has value, Then get response at position
		if (!position.isEmpty() && Helper.getIntFromString(position) > 0) {
			positionInt = Helper.getIntFromString(position);
			boolean inBounds = (positionInt > 0) && (positionInt <= actualArray.size());
			if (!inBounds) {
				Helper.assertFalse("items returned are less than specified. returned: " + actualArray.size()
						+ " specified: " + positionInt);
			}

			actualString = actualArray.get(positionInt - 1);
		}

		if (getCommandFromExpectedString(command).isEmpty()) {
			Helper.assertFalse(
					"Command not set. Options: " + Arrays.asList(JSON_COMMAND.values()) + ". See examples for usage.");
		}

		JSON_COMMAND jsonCommand = JSON_COMMAND.valueOf(command);

		switch (jsonCommand) {
		
		//Jsonpath:command(External.testMethod, value)
		// Jsonpath:command(Command.hasMethod, value1, value2)
		case command:
			if(expectedArray.size() < 1)
				Helper.assertFalse("invalid command format. must be: Jsonpath:command(class.methodname, value) eg. Jsonpath:command(Command.hasMethod, value");
			String method = expectedArray.get(0);
			expectedArray.remove(0);
			
			expectedArray = removeEmptyElements(expectedArray);
			String values = StringUtils.EMPTY;
			if(!expectedArray.isEmpty())
				values = "values:" + Arrays.toString(expectedArray.toArray());
			
			ServiceObject serviceObject = new ServiceObject()
			.withMethod("METHOD:" + method)
			.withRequestBody("response:"+ responseString +";" + values);
			
			return ExternalInterface.ExternalInterfaceRunner(serviceObject).toString();
		case notEqualTo:
			boolean val = false;
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " not equals " + expectedString);
				val = !actualString.equals(expectedString);
				if (!val)
					return actualString + " does equal " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: " + responseString + " not equals " + expectedString);
				val = !responseString.equals(expectedString);
				if (!val)
					return responseString + " does equal " + expectedString;
			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " not equals "
						+ Arrays.toString(expectedArray.toArray()));
				val = !actualArray.equals(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does equal "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
		case equalTo:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " equals " + expectedString);
				val = actualString.equals(expectedString);
				if (!val)
					return actualString + " does not equal " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: " + responseString + " equals " + expectedString);
				val = responseString.equals(expectedString);
				if (!val)
					return responseString + " does not equal " + expectedString;
			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " equals "
						+ Arrays.toString(expectedArray.toArray()));
				val = actualArray.equals(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does not equal "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
		case allValuesEqualTo:
			val = actualArray.get(0).equals(expectedString) && actualArray.stream().distinct().limit(2).count() == 1;
			if (!val)
				return Arrays.toString(actualArray.toArray()) + " are not all equal to: "+ expectedString;
			break;
		case notHaveItems:	
		case notContains:
		case notContain:
			// if response is single item, it is same as command with position 1 and treated as string
				actualArray = DataHelper.removeEmptyElements(actualArray);
				if (actualArray.size() == 1) {
					position = "1";
				}
						
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " does not contain " + expectedString);
				Set<String> missing = isListContain(actualString, expectedArray);
				if (missing.isEmpty())
					return actualString + " does contain " + missing.toString();
			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " does not contain "
						+ Arrays.toString(expectedArray.toArray()));
				Set<String> missing = isListContain(responseString, expectedArray);
				if (missing.isEmpty())
					return Arrays.toString(actualArray.toArray()) + " does contain "
							+ expectedArray.removeAll(new ArrayList<String>(missing));
			}
			break;
		case hasItems:
		case contains:
		case contain:
			// if response is single item, it is same as command with position 1 and treated as string
			actualArray = DataHelper.removeEmptyElements(actualArray);
			if (actualArray.size() == 1) {
				position = "1";
			}
			
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " contains " + expectedString);
				Set<String> missing = isListContain(actualString, expectedArray);
				if (!missing.isEmpty())
					return actualString + " does not contain " + missing.toString();
			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " contains "
						+ Arrays.toString(expectedArray.toArray()));
				Set<String> missing = isListContain(responseString, expectedArray);
				if (!missing.isEmpty())
					return Arrays.toString(actualArray.toArray()) + " does not contain "
							+ missing.toString();
			}
			break;
		case containsInAnyOrder:
			TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " contains any order "
					+ Arrays.toString(expectedArray.toArray()));
			Set<String> missing = isListContain(responseString, expectedArray);
			if (!missing.isEmpty())
				return Arrays.toString(actualArray.toArray()) + " does not contain "
						+ missing.toString();
			break;
		case integerGreaterThan:
			TestLog.logPass("verifying: " + responseString + " is greater than " + expectedString);
			val = compareNumbers(responseString, expectedString, "greaterThan");
			if (!val)
				return "actual: " + responseString + " is less than expected: " + expectedString;
			break;
		case integerLessThan:
			TestLog.logPass("verifying: " + responseString + " is less than " + expectedString);
			val = compareNumbers(responseString, expectedString, "lessThan");
			if (!val)
				return "actual: " + responseString + " is greater than expected: " + expectedString;
			break;
		case integerEqual:
			TestLog.logPass("verifying: " + responseString + " is equal to " + expectedString);
			val = compareNumbers(responseString, expectedString, "equalTo");
			if (!val)
				return "actual: " + responseString + " is not equal to expected: " + expectedString;
			break;
		case integerNotEqual:

			val = !compareNumbers(responseString, expectedString, "equalTo");
			if (!val)
				return "actual: " + responseString + " is not equal to expected: " + expectedString;
			break;
		case countGreaterThan:
		case nodeSizeGreaterThan:
			int intValue = Integer.valueOf(expectedString);
			int actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " greater than " + intValue);
			if (!(actualLength > intValue))
				return "response node size is: " + actualLength + " expected it to be greater than: " + intValue;
			break;
		case countLessThan:
		case nodeSizeLessThan:
			intValue = Integer.valueOf(expectedString);
			actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " less than " + intValue);
			if (!(actualLength < intValue))
				return "response node size is: " + actualLength + " expected it to be less than: " + intValue;
			break;
		case countExact:
		case nodeSizeExact:
			intValue = Integer.valueOf(expectedString);
			actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " equals " + intValue);
			if (actualLength != intValue)
				return "response node size is: " + actualLength + " expected: " + intValue;
			break;
		case sequence:
			TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " with sequence "
					+ Arrays.toString(expectedArray.toArray()));
			val = actualArray.equals(expectedArray);
			if (!val)
				return Arrays.toString(actualArray.toArray()) + " does not equal "
						+ Arrays.toString(expectedArray.toArray());
			break;
		case jsonbody:
			TestLog.logPass("verifying response: \n" + ServiceObject.normalize(responseString)
					+ "\n against expected: \n" +  ServiceObject.normalize(expectedString));
			String error = JsonHelper.validateByJsonBody(expectedString, responseString, true);
			if (!error.isEmpty())
				return error;
			break;
		case isNotEmpty:
			TestLog.logPass("verifying response for path is not empty");
			if (isEmpty(responseString))
				return "value is empty";
			break;
		case isEmpty:
			TestLog.logPass("verifying response for path is empty ");
			if (!isEmpty(responseString))
				return "value is not empty";
			break;
		case isBetweenDate:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying date: " + actualString + " is in between dates: " + expectedString);
				String[] expectedDates = expectedString.split(",");
				if(expectedDates.length != 2) Helper.assertFalse("require 2 dates to validate inbewteen date");
				val = Helper.date.isBetweenDates(actualString, expectedDates[0], expectedDates[1]);
				if (!val)
					return actualString + " is not in between dates: " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying date: " + responseString + " is in between dates: " + expectedString);
				String[] expectedDates = expectedString.split(",");
				if(expectedDates.length != 2) Helper.assertFalse("require 2 dates to validate inbewteen date");
				val = Helper.date.isBetweenDates(responseString, expectedDates[0], expectedDates[1]);
				if (!val)
					return responseString + " is not in between dates: " + expectedString;
			} else {
				TestLog.logPass("verifying dates: " + Arrays.toString(actualArray.toArray()) + " is in between dates: "
						+ Arrays.toString(expectedArray.toArray()));
				String[] expectedDates = expectedString.split(",");
				if(expectedDates.length != 2) Helper.assertFalse("require 2 dates to validate inbewteen date");
				val = Helper.date.isBetweenDates(actualArray, expectedDates[0], expectedDates[1]);

				if (!val)
					return Arrays.toString(actualArray.toArray()) + " is not in between dates: "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
		default:
			Helper.assertFalse(
					"Command not set. Options: " + Arrays.asList(JSON_COMMAND.values()) + ". See examples for usage.");
			break;
		}
		return StringUtil.EMPTY_STRING;
	}

	/**
	 * converts string separated by "," to array[] trims each value and removes
	 * quotes or array brackets
	 * 
	 * @param array
	 * @return
	 */
	public static List<String> getResponseArray(String array) {
		List<String> list = new ArrayList<String>();	
				
		String[] responses = array.split(",");
		for (String response : responses) {
			response = response.trim().replace("\"", "");
			response = response.replace("[", "").replace("]", "");
			list.add(response);
		}
		return list;
	}

	public static int getResponseArrayLength(List<String> actualArray, String responseString) {
		int responseLength = -1;
		actualArray = removeEmptyElements(actualArray);
		JSONArray jsonArray = JsonHelper.getJsonArray(responseString);
		if (jsonArray != null)
			responseLength = jsonArray.length();
		else
			responseLength = actualArray.size();
		return responseLength;
	}

	public static boolean isGreaterThan(String value1, String value2) {
		if (Helper.isNumeric(value1) && Helper.isNumeric(value2)) {
			if (Integer.valueOf(value1) > Integer.valueOf(value2))
				return true;
		}
		return false;
	}

	public static boolean isLessThan(String value1, String value2) {
		if (Helper.isNumeric(value1) && Helper.isNumeric(value2)) {
			if (Integer.valueOf(value1) < Integer.valueOf(value2))
				return true;
		}
		return false;
	}

	/**
	 * compare string integer values based on comparator value
	 * 
	 * @param value1
	 * @param value2
	 * @param comparator
	 * @return
	 */
	public static boolean compareNumbers(String value1, String value2, String comparator) {
		if (!Helper.isStringContainNumber(value1) || !Helper.isStringContainNumber(value2))
			return false;

		double val1Double = Helper.getDoubleFromString(value1);
		double val2Double = Helper.getDoubleFromString(value2);

		return compareNumbers(val1Double, val2Double, comparator);
	}

	/**
	 * compare integer values
	 * 
	 * @param value1
	 * @param value2
	 * @param comparator
	 * @return
	 */
	public static boolean compareNumbers(double value1, double value2, String comparator) {

		switch (comparator) {
		case "greaterThan":
			if (value1 > value2)
				return true;
			break;
		case "lessThan":
			if (value1 < value2)
				return true;
			break;
		case "equalTo":
			if (value1 == value2)
				return true;
			break;
		}
		return false;
	}

	/**
	 * converts list to string separated by ","
	 * 
	 * @param values
	 * @return
	 */
	public static String listToString(List<String> values) {
		return StringUtils.join(values, ",");
	}

	/**
	 * convert object to string object can be array
	 * 
	 * @param values
	 * @return
	 */
	public static String objectToString(Object values) {
		String stringVal = values.toString();
		stringVal = stringVal.replaceAll("[\\[\\](){}]", "");
		stringVal = stringVal.replace("\"", "");

		// replace \/ with /. limitation in json parsing
		stringVal = stringVal.replace("\\/", "/");

		return stringVal;
	}

	/**
	 * split based on key position value
	 * 
	 * @param keyvalue
	 * @param regex
	 * @param limit
	 * @return
	 */
	public static List<String> splitToKeyPositionValue(String keyvalue, String regex, int limit) {
		List<String> result = new ArrayList<String>();

		// if no key value pair
		if (!keyvalue.contains(":")) {
			result.add(keyvalue);
			return result;
		}

		// if json validation command, return format path:position:command or
		// path:command
		String commandValue = getCommandFromExpectedString(keyvalue);
		if (!commandValue.isEmpty()) {
			return getJsonKeyValue(keyvalue, commandValue);
		}

		String position = StringUtils.EMPTY;

		// position has format :position:
		boolean hasPosition = Pattern.compile(":\\d{1}:").matcher(keyvalue).find();

		// split based on position, and add to result list
		if (hasPosition) {
			String[] resultArray = keyvalue.split(":\\d{1}:");
			Pattern pattern = Pattern.compile(":\\d{1}:");
			Matcher matcher = pattern.matcher(keyvalue);
			if (matcher.find())
				position = matcher.group(0);
			result.add(resultArray[0]);
			result.add(removeFirstAndLastChars(position, ":"));
			result.add(resultArray[1]);
		} else { // split left to right
			String[] resultArray = keyvalue.split(":", 2);
			result.add(resultArray[0]);
			result.add(resultArray[1]);
		}
		return result;
	}

	public static List<String> splitRight(String value, String regex, int limit) {

		String string = value;
		List<String> result = new ArrayList<String>();
		String[] temp = new String[0];
		for (int i = 1; i < limit; i++) {
			if (string.matches(".*" + regex + ".*")) {
				temp = string.split(modifyRegex(regex));
				Helper.assertTrue("value not set for: " + string, temp.length > 1);
				result.add(temp[1]);
				string = temp[0];
			}
		}
		if (temp.length > 0) {
			result.add(temp[0]);
		}

		// handle single value
		if (value.split(":").length == 1)
			result.add(string);

		Collections.reverse(result);
		return result;
	}

	/**
	 * get the json response validation command from string eg.
	 * soi:EquipmentID:1:notEqualTo(2019110423T11:00:00.000Z) -> command: notEqual
	 * eg. value:isEmpty -> command: isEmpty
	 * 
	 * @param value
	 * @return
	 */
	private static String getCommandFromExpectedString(String value) {
		String commandValue = StringUtils.EMPTY;

		for (JSON_COMMAND command : JSON_COMMAND.values()) {
			List<String> parameters = Helper.getValuesFromPattern(value, command + "\\(([^)]+)\\)");
			if (!parameters.isEmpty()) { // command(value)
				commandValue = command + "(" + parameters.get(0) + ")";
				if (value.endsWith(commandValue))
					return commandValue;
			} else if (value.endsWith(command.name())) // isEmpty, isNotEmpty
				return command.name();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * get json key value eg. store.book[?(@.price < 10)]:jsonbody(["key":"value"])
	 * becomes arraylist of size 2. eg. store.book[?(@.price <
	 * 10)]:1:jsonbody(["key":"value"]) becomes arraylist of size 3.
	 * 
	 * @param value
	 * @return
	 */
	private static List<String> getJsonKeyValue(String value, String commandValue) {
		List<String> result = new ArrayList<String>();

		// remove commandValue
		value = value.replace(commandValue, "");

		// remove last colon. eg. soi:EquipmentID:1: becomes: soi:EquipmentID:1
		String keyPosition = value.trim().replaceAll(":$", "");
		List<String> keyPositionList = splitRight(keyPosition, ":", 2);

		// if key value has position. eg: store.book[?(@.price < 10)]:1
		if (keyPositionList.size() == 2 && Helper.isStringContainOnlyNumber(keyPositionList.get(1))) {
			result.add(keyPositionList.get(0));
			result.add(keyPositionList.get(1));
		} else {
			result.add(keyPosition);
		}

		// add command + value. eg. equal(value) or isEmpty
		result.add(commandValue);

		return result;
	}

	private static String modifyRegex(String regex) {
		return regex + "(?!.*" + regex + ".*$)";
	}

	public static boolean isEmpty(String value) {
		if (StringUtils.isBlank(value))
			return true;
		if (value.equals("null"))
			return true;
		return false;
	}

	/**
	 * get request body from template file: json, xml, other json template: - if
	 * request body is set, replace value in template with format: - request body:
	 * json path:position:value or json path:vlaue - eg.
	 * "features.feature.name:1:value_<@_TIME_19>" xml template: - if request body
	 * is set, replace value in template with format: - request body:
	 * tag:position:value or tag:value - eg. "soi:EquipmentID:1:equip_<@_TIME_17>"
	 * other file type: - return as string if no template set: - return request body
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static String getRequestBodyIncludingTemplate(ServiceObject serviceObject) {

		String requestbody = StringUtils.EMPTY;

		// load data file to config
		loadDataFile(serviceObject);

		// if json template file
		if (JsonHelper.isJsonFile(serviceObject.getTemplateFile())) {
			requestbody = JsonHelper.getRequestBodyFromJsonTemplate(serviceObject);

			// if xml template file
		} else if (XmlHelper.isXmlFile(serviceObject.getTemplateFile())) {
			requestbody = XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);

			// if other type of file
		} else if (!serviceObject.getTemplateFile().isEmpty()) {
			Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
			requestbody = convertFileToString(templatePath);

			// if no template, return request body
		} else if (requestbody.isEmpty())
			requestbody = serviceObject.getRequestBody();

		// replace request body parameters
		requestbody = replaceParameters(requestbody);

		return requestbody;
	}

	/**
	 * loads template data info based on value set on request body format:
	 * DataFile:file:dataId
	 * 
	 * @param serviceObject
	 */
	public static void loadDataFile(ServiceObject serviceObject) {
		if (serviceObject.getRequestBody().isEmpty())
			return;

		final String DataFile = "DataFile";

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestBody());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case DataFile:

				// remove DataFile value from request body
				String updateRequest = serviceObject.getRequestBody().replace(keyword.value.toString(), "")
						.replace(keyword.value.toString() + ";", "").replace(DataFile + ":", "");

				String[] dataInfo = keyword.value.toString().split(":");
				if (dataInfo.length != 2)
					Helper.assertFalse("format must be file:dataId. actual value: " + keyword.value.toString());

				String dataFilename = dataInfo[0];
				String expectedDataId = dataInfo[1];

				// get data file in csv format
				String templateDataFilePath = PropertiesReader.getLocalRootPath()
						+ Config.getValue(TEST_DATA_TEMPLATE_DATA_PATH);
				File dataFile = new File(templateDataFilePath + dataFilename + ".csv");
				try {

					CSVReader reader = CsvReader.readCsvFile(dataFile);

					// read header separately
					String[] header = reader.readNext();
					int dataId = CsvReader.getColumnIndexByName("dataId", header);

					// add semicolon to separate from the rest of the data
					if (header.length > 1 && !updateRequest.isEmpty())
						updateRequest = updateRequest + ";";

					// if dataId matches expected dataId, add all row data
					String[] line;
					while ((line = reader.readNext()) != null) {
						if (!expectedDataId.equals(line[dataId]))
							continue;

						for (int i = 1; i < header.length; i++) {
							updateRequest = updateRequest + header[i] + ":" + line[i] + ";";
						}
					}
					reader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				serviceObject.withRequestBody(updateRequest);
				break;
			default:
				break;
			}

		}
	}

	/**
	 * stores value in config format: value:<$key> separated by colon ';'
	 * 
	 * @param source
	 */
	public static void saveDataToConfig(String source) {

		if (source.isEmpty())
			return;

		List<KeyValue> keywords = DataHelper.getValidationMap(source);
		for (KeyValue keyword : keywords) {

			// return if value is wrong format
			if (!keyword.value.toString().startsWith("<") || !keyword.value.toString().contains("$")
					|| !keyword.value.toString().endsWith(">"))
				return;

			String value = (String) keyword.value;
			value = value.replace("$", "").replace("<", "").replace(">", "").trim();
			// gets json value. if list, returns string separated by comma
			String key = keyword.key;

			Config.putValue(value, key);
		}
	}

	/**
	 * get file content as text replaces parameters using syntax <@variable> from
	 * value in config
	 * 
	 * @param templatePath
	 * @return
	 */
	public static String convertFileToString(Path templatePath) {
		String content = Helper.readFileContent(templatePath.toString());

		// replace content parameters
		return replaceParameters(content);
	}

	/**
	 * remove section from expected response separated by && the section will start
	 * with the identifier. eg. _VERIFY_RESPONSE_BODY_
	 * 
	 * @param section
	 * @param expectedResponse
	 * @return
	 */
	public static String removeSectionFromExpectedResponse(String section, String expectedResponse) {
		String[] criteria = expectedResponse.split("&&");
		List<String> newResponse = new ArrayList<String>();
		for (String criterion : criteria) {
			criterion = Helper.removeSurroundingQuotes(criterion);
			if (!criterion.trim().startsWith(section)) {
				newResponse.add(criterion);
			}
		}
		return String.join("&&", newResponse);
	}

	/**
	 * get section from expected response separated by && the section will start
	 * with the identifier. eg. _VERIFY_RESPONSE_BODY_
	 * 
	 * @param section
	 * @param expectedResponse
	 * @return
	 */
	public static String getSectionFromExpectedResponse(String section, String expectedResponse) {
		String[] criteria = expectedResponse.split("&&");
		List<String> newResponse = new ArrayList<String>();
		for (String criterion : criteria) {
			criterion = Helper.removeSurroundingQuotes(criterion);
			if (criterion.trim().startsWith(section)) {
				newResponse.add(criterion);
			}
		}
		return String.join("&&", newResponse);
	}

	/**
	 * validates expected values (xml, json, text, or jsonpath keywords
	 * 
	 * @param responseValues
	 * @param expectedResponse
	 * @return
	 */
	public static List<String> validateExpectedValues(List<String> responseValues, String expectedResponse) {

		List<String> errorMessages = new ArrayList<String>();

		if (expectedResponse.trim().isEmpty())
			return errorMessages;

		// return error message if response is empty
		errorMessages = validateEmptyResponse(responseValues, expectedResponse);
		if (!errorMessages.isEmpty())
			return errorMessages;

		// validate response body against expected json string
		expectedResponse = DataHelper.replaceParameters(expectedResponse);

		// separate the expected response by &&
		String[] criteria = expectedResponse.split("&&");

		// get response body as string
		logJsonResponse(responseValues);

		for (String criterion : criteria) {
			Helper.assertTrue("expected response is not valid xml or json, or section identifier, are you missing the section identifier? eg. _VERIFY_JSON_PART_:  " + criterion,
					isValidExpectation(criterion));

			// convert xml string to json for validation
			if (XmlHelper.isValidXmlString(criterion)) {
				TestLog.ConsoleLog("expected xml: " + ServiceObject.normalize(criterion));
				criterion = JsonHelper.XMLToJson(criterion);
				TestLog.ConsoleLog(
						"expected value converted to json for validation: " + ServiceObject.normalize(criterion));
			}

			errorMessages.addAll(validateExpectedResponse(criterion, responseValues));
		}
		// remove all empty response strings
		errorMessages = removeEmptyElements(errorMessages);
		return errorMessages;
	}

	public static void logJsonResponse(List<String> responseValues) {
		List<String> updatedList = new ArrayList<String>();
		for (String response : responseValues) {
			updatedList.add(response.replace(System.lineSeparator(), ""));
		}
		String responseString = String.join(System.lineSeparator(), updatedList);
		TestLog.logPass("response to be validated: " + ServiceObject.normalize(responseString));
	}

	/**
	 * validates if empty response is expected and received
	 * 
	 * @param responseValues
	 * @param expected
	 * @return
	 */
	public static List<String> validateEmptyResponse(List<String> responseValues, String expected) {
		List<String> errorMessage = new ArrayList<String>();
		boolean isEmptyExpected = isEmptyResponseExpected(expected);

		for (String resonse : responseValues) {
			if (resonse.isEmpty() && !isEmptyExpected) {
				errorMessage.add("response value is empty");
				return errorMessage;
			}
		}
		return errorMessage;
	}

	/**
	 * returns true if empty response is expected. denoted by isEmpty
	 * 
	 * @param expected
	 * @return
	 */
	public static boolean isEmptyResponseExpected(String expected) {
		expected = JsonHelper.removeResponseIndicator(expected);
		if (expected.equals(JSON_COMMAND.isEmpty.name()))
			return true;
		return false;
	}

	/**
	 * validates expected requirement against response strings
	 * 
	 * @param criterion
	 * @param responseString
	 * @return
	 */
	public static List<String> validateExpectedResponse(String criterion, List<String> responseString) {
		List<String> errorMessages = new ArrayList<String>();
		for (int i = 0; i < responseString.size(); i++) {
			errorMessages = new ArrayList<String>();

			// if response is xml, convert to json for validation
			if (XmlHelper.isValidXmlString(responseString.get(i)))
				responseString.set(i, JsonHelper.XMLToJson(responseString.get(i)));

			errorMessages.add(JsonHelper.validateByJsonBody(criterion, responseString.get(i)));
			errorMessages.addAll(JsonHelper.validateByKeywords(criterion, responseString.get(i)));
			errorMessages.add(JsonHelper.validateResponseBody(criterion, responseString.get(i)));

			// if no errors, then validation passed, no need to validate against other
			// responses
			errorMessages = removeEmptyElements(errorMessages);
			if (errorMessages.isEmpty())
				break;

			if (i > 0 && i == responseString.size() && !errorMessages.isEmpty()) {
				errorMessages = new ArrayList<String>();
				errorMessages.add("expected requirement: " + criterion + " not met by the responses: "
						+ String.join(System.lineSeparator(), responseString));

			}
		}
		return errorMessages;
	}

	/**
	 * validates if expected value is valid: json, xml or starts with valid
	 * indicator
	 * 
	 * @param expectedValue
	 * @return
	 */
	public static boolean isValidExpectation(String expectedValue) {
		if (JsonHelper.isJSONValid(expectedValue, false)) {
			return true;
		}

		if (XmlHelper.isValidXmlString(expectedValue))
			return true;

		expectedValue = Helper.stringNormalize(expectedValue);
		if (expectedValue.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR)
				|| expectedValue.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR_UNDERSCORE)
				|| expectedValue.startsWith(VERIFY_RESPONSE_NO_EMPTY)
				|| expectedValue.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)
				|| expectedValue.startsWith(DataHelper.VERIFY_HEADER_PART_INDICATOR)
				|| expectedValue.startsWith(DataHelper.EXPECTED_MESSAGE_COUNT)
				|| expectedValue.startsWith(DataHelper.VERIFY_TOPIC_PART_INDICATOR)) {
			return true;
		}
		return false;
	}

	/**
	 * removes empty elements from list
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> removeEmptyElements(List<String> list) {

		Iterator<String> i = list.iterator();
		while (i.hasNext()) {
			String s = i.next();
			if (s == null || s.trim().isEmpty()) {
				i.remove();
			}
		}
		return list;
	}

	public static String[] removeEmptyElements(String[] array) {

		List<String> list = new ArrayList<String>();
		for (String text : array) {
			if (text != null && !text.trim().isEmpty()) {
				list.add(text);
			}
		}
		array = list.toArray(new String[0]);
		return array;
	}
	
	/**
	 * verifies actual string contains list of expected values
	 * @param actual
	 * @param expectedValues
	 * @return
	 */
	public static Set<String> isListContain(String actual, List<String> expectedValues) {
		actual = actual.trim().replace("\"", "");
		Set<String> missing = new HashSet<String>();

		for(String expected : expectedValues) {
			if(!actual.contains(expected)) {
				missing.add(expected);
				continue;
			}
		}
		return missing;
	}
}
