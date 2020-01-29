package core.apiCore.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

import core.apiCore.TestDataProvider;
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

	public enum JSON_COMMAND {
		hasItems, notHaveItems, notEqualTo, equalTo, notContain, contains, containsInAnyOrder, integerGreaterThan,
		integerLessThan, integerEqual, integerNotEqual, nodeSizeGreaterThan, nodeSizeExact, sequence, jsonbody,
		isNotEmpty, isEmpty, nodeSizeLessThan
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
		Instant newTime = null;
		for (String parameter : parameters) {
			if (parameter.contains("_TIME_MS_")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));			
				value = getTimeSubstring(parameter, String.valueOf(newTime.toEpochMilli()));
			}else if (parameter.contains("_TIME_STRING_")) {
					newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
					value = getTimeSubstring(parameter, Helper.date.getTime(newTime, "yyyyMMddHHmmssSSS"));
			} else if (parameter.contains("_TIME_ISO_")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, newTime.toString());
			} else if (parameter.contains("_TIME")) {
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, getTimeWithFormatingZone(newTime, parameter));
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
				TestLog.ConsoleLog("replacing value " + parameter + "  with: " + value);
				source = source.replace("<@" + parameter + ">", Matcher.quoteReplacement(value.toString()));
			}
		}

		return source;
	}
	
	/**
	 * incremental keyword value setter
	 * starting value + current run count
	 * by default: starting value = 1
	 * eg. <@_INCREMENT_FROM_3>
	 * current test count is appended to test case id. eg. validateId_run_1
	 * 	
	 * @param parameter
	 */
	public static int getIncrementalValue(String parameter) {
		int startingValue = Helper.getIntFromString(parameter);
		
		int testCurrentRunCount = 1;
		String testId = TestObject.getTestInfo().serviceObject.getTestCaseID();
		if(testId.matches(".*" + CsvReader.SERVICE_RUN_PREFIX + "(\\d)?$")) {
			testId = testId.substring(testId.lastIndexOf(CsvReader.SERVICE_RUN_PREFIX) + 1);
			testCurrentRunCount = Helper.getIntFromString(testId);
		}

		int incrementalValue = startingValue + testCurrentRunCount - 1;
		return incrementalValue;
	}
	
	/**
	 * get substring of time based on time string length
	 * format _TIME_STRING_17-72h. 17 is the length
	 * @param parameter
	 * @param maxLength
	 * @param finalTime
	 * @return
	 */
	public static String getTimeSubstring(String parameter, String finalTime) {
		// values after the last "_", then after the last :
		parameter = parameter.substring(parameter.lastIndexOf("_") + 1);
		parameter = parameter.substring(parameter.lastIndexOf(":") + 1);
				
		int length = Helper.getIntFromString(parameter.split("[+-]")[0]);
		int maxLength = finalTime.length();
		if (length > maxLength)
			length = maxLength;
		else if(length == -1) length = 1;
		return finalTime.substring(0, length);
	}
	
	/**
	 * set format and time zone for time
	 * eg.
	    <@_TIME_FORMAT:yyyyMMddHHmmssSSS:13+72h>
		<@_TIME_FORMAT:yyyyMMddHHmmssSSS:Zone:UTC:13+72h>
	 * @param parameter
	 * @param time
	 * @return
	 */
	public static String getTimeWithFormatingZone(Instant time, String parameter) {
		String timeFormatted = StringUtils.EMPTY;
		String format = StringUtils.substringBetween(parameter, "FORMAT:", ":Zone");
		if (StringUtils.isBlank(format) && parameter.contains("FORMAT:")) {
			parameter = parameter.substring(0, parameter.lastIndexOf(":"));
			if(parameter.endsWith(":")) parameter = parameter.substring(0, parameter.length()-1);
			format = parameter.split("FORMAT:")[1];
		}
		String zone = StringUtils.substringBetween(parameter, "Zone:", ":");
		
		timeFormatted = Helper.date.getTime(time, format, zone);
		return timeFormatted;
	}

	/**
	 *  time: _TIME_STRING_17-72h or _TIME_STRING_17+72h
	 * @param parameter: time parameter with modification. eg. _TIME_STRING_17-72h
	 * @param timeString
	 * @return
	 */
	public static Instant getTime(String parameter, String timeString) {
		Instant time = Instant.parse(timeString);
		Instant newTime = time;
		
		// values after the last "_", then after the last :
		parameter = parameter.substring(parameter.lastIndexOf("_") + 1);
		parameter = parameter.substring(parameter.lastIndexOf(":") + 1);

		String[] parameterArray = parameter.split("[+-]");
		
		// return non modified time if modifier not set
		if( parameterArray.length == 1) return newTime;
		
		String modifier =  parameter.split("[+-]")[1];
		
		String modiferSign = parameter.replaceAll("[^+-]", "");
		int modifierDuration = Helper.getIntFromString(modifier);
		String modifierUnit =  modifier.replaceAll("[^A-Za-z]+", "");
		
		if(modiferSign.isEmpty() || modifierDuration == -1 || modifierUnit.isEmpty())
			  Helper.assertFalse("invalid time modifier. format: eg. _TIME_STRING_17+72h or _TIME_STRING_17-72m");

		switch(modifierUnit) {
		  case "h":
			  if(modiferSign.equals("+"))
				  newTime = newTime.plus(modifierDuration, ChronoUnit.HOURS);
			  else if(modiferSign.equals("-"))
				  newTime = newTime.minus(modifierDuration, ChronoUnit.HOURS);
		    break;
		  case "m":
			  if(modiferSign.equals("+"))
				  newTime = newTime.plus(modifierDuration, ChronoUnit.MINUTES);
			  else if(modiferSign.equals("-"))
				  newTime = newTime.minus(modifierDuration, ChronoUnit.MINUTES);
		    break;
		  default:
			  Helper.assertFalse("invalid time modifier. format: eg. +72h or -72m");
		    
		}
		return newTime;
	}

	/**
	 * gets the map of the validation requirements split by ";"
	 * 
	 * @param expected
	 * @return
	 */
	public static List<KeyValue> getValidationMap(String expected) {
		// get hashmap of json path And verification
		List<KeyValue> keywords = new ArrayList<KeyValue>();

		// remove json indicator _VERIFY.JSON.PART_
		expected = JsonHelper.removeResponseIndicator(expected);

		String[] keyVals = expected.split(";");
		String key = "";
		String position = "";
		String value = "";
		for (String keyVal : keyVals) {
			List<String> parts = splitRight(keyVal, ":", 3);
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
		return validateCommand(command, responseString, expectedString, StringUtils.EMPTY);
	}

	/**
	 * validates response against expected values
	 * 
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @param position
	 */
	public static String validateCommand(String command, String responseString, String expectedString,
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
			expectedString = expectedArray.get(0); // single item
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
		case hasItems:
			boolean val = false;
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " has item " + expectedString);
				val = actualString.contains(expectedString);
				if (!val)
					return actualString + " does not have item " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: " + responseString + " has item " + expectedString);
				val = responseString.contains(expectedString);
				if (!val)
					return responseString + " does not have item " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray.toArray()) + " has items " + Arrays.toString(expectedArray.toArray()));
				val = actualArray.containsAll(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does not have items " + Arrays.toString(expectedArray.toArray());
			}
			break;
		case notHaveItems:
			val = false;
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " does not have item " + expectedString);
				val = !actualString.contains(expectedString);
				if (!val)
					return actualString + " does have item " + expectedString;

			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: " + responseString + " does not have item " + expectedString);
				val = !responseString.contains(expectedString);
				if (!val)
					return responseString + " does not have item " + expectedString;

			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " does not have items "
						+ Arrays.toString(expectedArray.toArray()));
				val = !actualArray.containsAll(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does have items " + Arrays.toString(expectedArray.toArray());
			}
			break;
		case notEqualTo:
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
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray.toArray()) + " not equals " + Arrays.toString(expectedArray.toArray()));
				val = !actualArray.equals(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does equal " + Arrays.toString(expectedArray.toArray());
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
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray.toArray()) + " equals " + Arrays.toString(expectedArray.toArray()));
				val = actualArray.equals(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does not equal " + Arrays.toString(expectedArray.toArray());
			}
			break;
		case notContain:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " does not contain " + expectedString);
				val = !actualString.contains(expectedString);
				if (!val)
					return actualString + " does contain " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: " + expectedString + " does not contain " + expectedString);
				val = !responseString.contains(expectedString);
				if (!val)
					return responseString + " does contain " + expectedString;
			} else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " does not contain "
						+ Arrays.toString(expectedArray.toArray()));
				val = !actualArray.containsAll(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does not contain " + Arrays.toString(expectedArray.toArray());
			}
			break;
		case contains:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " contains " + expectedString);
				val = actualString.contains(expectedString);
				if (!val)
					return actualString + " does not contain " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying: '" + responseString + "' contains " + expectedString);
				val = responseString.contains(expectedString);
				if (!val)
					return responseString + " does not contain " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray.toArray()) + " contains " + Arrays.toString(expectedArray.toArray()));
				val = actualArray.containsAll(expectedArray);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " does not contain " + Arrays.toString(expectedArray.toArray());
			}
			break;
		case containsInAnyOrder:
			TestLog.logPass("verifying: " + Arrays.toString(actualArray.toArray()) + " contains any order "
					+ Arrays.toString(expectedArray.toArray()));
			val = actualArray.containsAll(expectedArray);
			if (!val)
				return Arrays.toString(actualArray.toArray()) + " does not contain in any order "
						+ Arrays.toString(expectedArray.toArray());
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
		case nodeSizeGreaterThan:
			int intValue = Integer.valueOf(expectedString);
			int actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " greater than " + intValue);
			if (!(actualLength > intValue))
				return "response node size is: " + actualLength + " expected it to be greater than: " + intValue;
			break;
		case nodeSizeLessThan:
			intValue = Integer.valueOf(expectedString);
			actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " less than " + intValue);
			if (!(actualLength < intValue))
				return "response node size is: " + actualLength + " expected it to be less than: " + intValue;
			break;
		case nodeSizeExact:
			intValue = Integer.valueOf(expectedString);
			actualLength = getResponseArrayLength(actualArray, responseString);
			TestLog.logPass("verifying node with size " + actualLength + " equals " + intValue);
			if (actualLength != intValue)
				return "response node size is: " + actualLength + " expected: " + intValue;
			break;
		case sequence:
			TestLog.logPass(
					"verifying: " + Arrays.toString(actualArray.toArray()) + " with sequence " + Arrays.toString(expectedArray.toArray()));
			val = Arrays.asList(actualArray).equals(Arrays.asList(expectedArray));
			if (!val)
				return Arrays.toString(actualArray.toArray()) + " does not equal " + Arrays.toString(expectedArray.toArray());
			break;
		case jsonbody:
			TestLog.logPass("verifying response: \n" + responseString + "\n against expected: \n" + expectedString);
			String error = JsonHelper.validateByJsonBody(expectedString, responseString);
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
		default:
			Helper.assertFalse(
					"Command not set. Options: " + Arrays.asList(JSON_COMMAND.values()) + ". See examples for usage.");
			break;
		}
		return StringUtil.EMPTY_STRING;
	}
	
	
	/**
	 * converts string separated by "," to array[]
	 * trims each value and removes quotes
	 * @param array
	 * @return
	 */
	public static List<String> getResponseArray(String array) {
		List<String> list = new ArrayList<String>();
		String[] responses = array.split(",");
		for(String response : responses) {
			response = response.trim().replace("\"", "");
			list.add(response);
		}
		return list;
	}
	
	public static int getResponseArrayLength(List<String> actualArray, String responseString) {
		int responseLength = -1;
		actualArray = removeEmptyElements(actualArray);
		JSONArray jsonArray = JsonHelper.getJsonArray(responseString);
		if(jsonArray != null)
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

	public static List<String> splitRight(String value, String regex, int limit) {

		// if json validation command, return format path:position:command or
		// path:command
		String commandValue = getCommandFromExpectedString(value);
		if (!commandValue.isEmpty()) {
			return getJsonKeyValue(value, commandValue);
		}

		String string = value;
		List<String> result = new ArrayList<String>();
		String[] temp = new String[0];
		for (int i = 1; i < limit; i++) {
			if (string.matches(".*" + regex + ".*")) {
				temp = string.split(modifyRegex(regex));
				Helper.assertTrue("value not set for: " + string,temp.length > 1);
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

		// if json template file
		if (JsonHelper.isJsonFile(serviceObject.getTemplateFile())) {
			requestbody = JsonHelper.getRequestBodyFromJsonTemplate(serviceObject);

		// if xml template file
		}else if (XmlHelper.isXmlFile(serviceObject.getTemplateFile())) {
			requestbody = XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);

		// if other type of file
		}else if (!serviceObject.getTemplateFile().isEmpty()) {
			Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
			requestbody = convertFileToString(templatePath);
		

		// if no template, return request body
		}else if(requestbody.isEmpty())
			requestbody = serviceObject.getRequestBody();
		
		// replace request body parameters
		requestbody = replaceParameters(requestbody);
		
		TestLog.ConsoleLog("request: " + requestbody);
		return requestbody;
	}
	
	/**
	 * stores value in config
	 * format: value:<$key> separated by colon ';'
	 * @param source
	 */
	public static void saveDataToConfig(String source) {

		if (source.isEmpty())
			return;

		List<KeyValue> keywords = DataHelper.getValidationMap(source);
		for (KeyValue keyword : keywords) {
			
			// return if value is wrong format
			if(!keyword.value.toString().startsWith("<") || !keyword.value.toString().contains("$")|| !keyword.value.toString().endsWith(">"))
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
		if(!errorMessages.isEmpty())
			return errorMessages;

		// validate response body against expected json string
		expectedResponse = DataHelper.replaceParameters(expectedResponse);

		// separate the expected response by &&
		String[] criteria = expectedResponse.split("&&");
		
		// get response body as string
		logJsonResponse(responseValues);
		
		for (String criterion : criteria) {
			Helper.assertTrue("expected response is not a valid json, xml or keyword:  " + criterion, isValidExpectation(criterion));

			// convert xml string to json for validation
			if (XmlHelper.isValidXmlString(criterion)) {
				TestLog.ConsoleLog("expected xml: " + ServiceObject.normalize(criterion));
				criterion = JsonHelper.XMLToJson(criterion);
				TestLog.ConsoleLog("expected value converted to json for validation: " + ServiceObject.normalize(criterion));
			}

			errorMessages.addAll(validateExpectedResponse(criterion, responseValues));
		}
		// remove all empty response strings
		errorMessages = removeEmptyElements(errorMessages);
		return errorMessages;
	}
	
	public static void logJsonResponse(List<String> responseValues) {
		List<String> updatedList = new ArrayList<String>();
		for(String response: responseValues) {
			updatedList.add(response.replace(System.lineSeparator(), ""));
		}
		String responseString = String.join(System.lineSeparator(), updatedList);
		TestLog.logPass("received response: " + responseString);
	}
	
	/**
	 * validates if empty response is expected and received
	 * @param responseValues
	 * @param expected
	 * @return
	 */
	public static List<String> validateEmptyResponse(List<String> responseValues, String expected) {
		List<String> errorMessage = new ArrayList<String>();
		boolean isEmptyExpected = isEmptyResponseExpected(expected);
		
		for(String resonse : responseValues) {
			if(resonse.isEmpty() && !isEmptyExpected){
				errorMessage.add("response value is empty");
				return errorMessage;
			}
		}
		return errorMessage;
	}
	
	/**
	 * returns true if empty response is expected. denoted by isEmpty
	 * @param expected
	 * @return
	 */
	public static boolean isEmptyResponseExpected(String expected) {
		expected = JsonHelper.removeResponseIndicator(expected);
		if(expected.equals(JSON_COMMAND.isEmpty.name()))
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
	 * @param list
	 * @return
	 */
	public static List<String> removeEmptyElements(List<String> list) {
		
		Iterator<String> i = list.iterator();
		while (i.hasNext())
		{
		    String s = i.next();
		    if (s == null || s.trim().isEmpty())
		    {
		        i.remove();
		    }
		}
		return list;
	}
	
	public static String[] removeEmptyElements(String[] array) {
			
			List<String> list = new ArrayList<String>();
			for (String text : array)
			{
			    if (text != null && !text.trim().isEmpty())
			    {
			        list.add(text);
			    }
			}
			array = list.toArray(new String[0]);
			return array;
		}
	
	
}
