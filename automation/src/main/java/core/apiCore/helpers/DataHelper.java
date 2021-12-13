package core.apiCore.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.json.JSONArray;

import com.opencsv.CSVReader;

import core.apiCore.TestDataProvider;
import core.apiCore.interfaces.ExternalInterface;
import core.helpers.Helper;
import core.support.configReader.Config;
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
	public static final String IS_IGNORE_XML_NAMESPACE = "service.xml.ignore.namespace";

	public static final String TEST_DATA_TEMPLATE_DATA_PATH = "api.templateDataFile";

	public static final String VALIDATION_OR_CONDITION_ECODE = "\\|\\|";
	public static final String VALIDATION_OR_CONDITION = "||";
	public static final String VALIDATION_AND_CONDITION = "&&";
	
	public static final String REQUEST_BODY_UPDATE_INDICATOR = "_UPDATE_REQUEST_";


	
	public enum JSON_COMMAND {
		hasItems, notHaveItems, notEqualTo, equalTo, notContain, contains, containsInAnyOrder, integerGreaterThan,
		integerLessThan, integerEqual, integerNotEqual, nodeSizeGreaterThan, nodeSizeExact, sequence, jsonbody,
		isNotEmpty, isEmpty, nodeSizeLessThan, isBetweenDate, allValuesEqualTo, countGreaterThan, countLessThan, countExact, command, notContains, contain, isDateAfter, isDateBefore, isDateEqual, isDateNotEqual
	}

	public static String replaceParameters(String source) {
		return replaceParameters(source, "<@(.+?)>", "<@", ">");
	}
	/**
	 * replaces placeholder values with values from config properties replaces only
	 * string values
	 * 
	 * @param source
	 * @return
	 */
	public static String replaceParameters(String source, String tagPattern, String openingTag, String closingTag) {

		if (source.isEmpty())
			return source; 

		List<String> parameters = Helper.getValuesFromPattern(source,tagPattern);
		Object value = null;
		int length = 0;
		String newTime = StringUtils.EMPTY;
		for (String parameter : parameters) {
			if (parameter.contains("_TIME_MS_")) {
				newTime = Helper.date.getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				Instant time = getTimeInstance(newTime);
				value = getTimeSubstring(parameter, String.valueOf(time.toEpochMilli()));
			}else if (parameter.contains("_TIME_S_")) {
					newTime = Helper.date.getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
					Instant time = getTimeInstance(newTime);
					value = getTimeSubstring(parameter, String.valueOf(time.getEpochSecond()));
			} else if (parameter.contains("_TIME_STRING_")) {
				newTime = Helper.date.getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, Helper.date.getTime(newTime, "yyyyMMddHHmmssSSS", null));
			} else if (parameter.contains("_TIME_ISO_")) {
				newTime = Helper.date.getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, newTime);
			} else if (parameter.contains("_TIME")) {
				newTime = Helper.date.getTime(parameter, Config.getValue(TestObject.START_TIME_STRING));
				value = getTimeSubstring(parameter, newTime);
			} else if (parameter.contains("_RANDUUID")) {	
				value = Helper.generateUUID();
			} else if (parameter.contains("_UUID_STATIC")) {	
				value = Config.getValue(TestObject.UUID_STATIC_STRING);
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
			if (isObjectEmpty(value))
				TestLog.logWarning("parameter value not found: " + parameter);
			else {
					source = source.replace(openingTag + parameter + closingTag, Matcher.quoteReplacement(value.toString()));
			}			
		}

		return source;
	}
	
	public static boolean isObjectEmpty(Object value) {
		if (value == null || StringUtils.isBlank(value.toString()) || value.equals("null"))
			return true;
		return false;
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
		String[] values = parameter.split(";");
		
		String modifier = values[0].split("[+-]")[0];
		int length = Helper.getFirstNumber(modifier);

		int maxLength = finalTime.length();
		if (length > maxLength || length == -1 || length == 0)
			length = maxLength;

		return finalTime.substring(0, length);
	}
	
	public static Instant getTimeInstance(String timeString) {
		LocalDateTime localDateTime = Helper.date.getLocalDateTime(timeString);
		Instant timeInstant =  localDateTime.toInstant(ZoneOffset.UTC);
		return timeInstant;
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
		
		for (String keyVal : keyVals) {
			String key = "";
			String position = "";
			String value = "";
			
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
		String templateTestPath = Helper.getFullPath(templatePath);

		return templateTestPath + file;
	}

	public static Path getTemplateFilePath(String file) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH).trim();
		String templateTestPath = Helper.getFullPath(templatePath);
		String fullLocation = templateTestPath + file;
		return new File(fullLocation).toPath();
	}

	public static File getFile(String filename) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH).trim();
		String templateTestPath = Helper.getFullPath(templatePath);
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
	 * if skipValidation = false -> response variable contains error
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @param position
	 * @return
	 */
	public static String validateCommand(String command, String responseString, String expectedString,
			String position, boolean skipValidation) {
		String response = validateExpectedCommand(command, responseString, expectedString, position);
		
		if(skipValidation)
			return response;
		
		if (response.isEmpty())
			TestLog.ConsoleLog("validation passed for command: response " + command + " " + expectedString);
		else
			TestLog.ConsoleLog("validation failed for command: " + command + " with error: " + response);

		return response;
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
				return "actual: " + responseString + " is not greater than expected: " + expectedString;
			break;
		case integerLessThan:
			TestLog.logPass("verifying: " + responseString + " is less than " + expectedString);
			val = compareNumbers(responseString, expectedString, "lessThan");
			if (!val)
				return "actual: " + responseString + " is not less than expected: " + expectedString;
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
			TestLog.logPass("verifying response: \n" + ServiceObject.normalizeLog(responseString)
					+ "\n against expected: \n" +  ServiceObject.normalizeLog(expectedString));
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
		case isDateEqual:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying date: " + actualString + " is equal date: " + expectedString);
				val = Helper.date.isDateEqual(actualString, expectedString);
				if (!val)
					return actualString + " is equal date: " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying date: " + responseString + " is equal date: " + expectedString);
				val = Helper.date.isDateEqual(responseString, expectedString);
				if (!val)
					return responseString + " is not equal date: " + expectedString;
			} else {
				TestLog.logPass("verifying date: " + Arrays.toString(actualArray.toArray()) + " is equal date: "
						+ Arrays.toString(expectedArray.toArray()));
				val = Helper.date.isDateEqual(actualArray, expectedString);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " is equal date: "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
			
		case isDateNotEqual:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying date: " + actualString + " is not equal date: " + expectedString);
				val = Helper.date.isDateNotEqual(actualString, expectedString);
				if (!val)
					return actualString + " is not equal date: " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying date: " + responseString + " is not equal date: " + expectedString);
				val = Helper.date.isDateNotEqual(responseString, expectedString);
				if (!val)
					return responseString + " is not equal date: " + expectedString;
			} else {
				TestLog.logPass("verifying date: " + Arrays.toString(actualArray.toArray()) + " is not equal date: "
						+ Arrays.toString(expectedArray.toArray()));
				val = Helper.date.isDateNotEqual(actualArray, expectedString);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " is not equal date: "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
		case isDateAfter:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying date: " + actualString + " is after date: " + expectedString);
				val = Helper.date.isDateAfter(actualString, expectedString);
				if (!val)
					return actualString + " is after date: " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying date: " + responseString + " is after date: " + expectedString);
				val = Helper.date.isDateAfter(responseString, expectedString);
				if (!val)
					return responseString + " is not after date: " + expectedString;
			} else {
				TestLog.logPass("verifying date: " + Arrays.toString(actualArray.toArray()) + " is after date: "
						+ Arrays.toString(expectedArray.toArray()));
				val = Helper.date.isDateAfter(actualArray, expectedString);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " is after date: "
							+ Arrays.toString(expectedArray.toArray());
			}
			break;
			
		case isDateBefore:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying date: " + actualString + " is before date: " + expectedString);
				val = Helper.date.isDateBefore(actualString, expectedString);
				if (!val)
					return actualString + " is before date: " + expectedString;
			} else if (!position.isEmpty() && positionInt == 0) {
				TestLog.logPass("verifying date: " + responseString + " is before date: " + expectedString);
				val = Helper.date.isDateBefore(responseString, expectedString);
				if (!val)
					return responseString + " is not before date: " + expectedString;
			} else {
				TestLog.logPass("verifying date: " + Arrays.toString(actualArray.toArray()) + " is before date: "
						+ Arrays.toString(expectedArray.toArray()));
				val = Helper.date.isDateBefore(actualArray, expectedString);
				if (!val)
					return Arrays.toString(actualArray.toArray()) + " is before date: "
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
		
		if(array == null)
			return list;
				
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
			result.add(Helper.date.removeFirstAndLastChars(position, ":"));
			if(resultArray.length == 2)
				result.add(resultArray[1]);
			else
				result.add(StringUtils.EMPTY);
		} else { // split left to right
			String[] resultArray = keyvalue.split(":", 2);
			result.add(resultArray[0]);
			if(resultArray.length == 2)
				result.add(resultArray[1]);
			else
				result.add(StringUtils.EMPTY);
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
	@SuppressWarnings("deprecation")
	public static String getRequestBodyIncludingTemplate(ServiceObject serviceObject) {

		String requestbody = StringUtils.EMPTY;

		// load data file to config
		loadDataFile(serviceObject);
		
		String[] criterion =  serviceObject.getRequestBody().split(VALIDATION_AND_CONDITION);
		
		// if request only contains request update keyword, set request first index to empty
		if(criterion.length == 1 && criterion[0].startsWith(REQUEST_BODY_UPDATE_INDICATOR)) {
			criterion = ArrayUtils.add(criterion, 0, "");
		}
		
		for(String criteria : criterion) {
			criteria = Helper.stringRemoveLines(criteria);
			if(!criteria.startsWith(REQUEST_BODY_UPDATE_INDICATOR)) {
				serviceObject.withRequestBody(criteria);
				
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
			}else {
				// remove update indicator _UPDATE_REQUEST_
				criteria = JsonHelper.removeResponseIndicator(criteria);
				
				if(JsonHelper.isJSONValid(requestbody, false))
					requestbody = JsonHelper.updateJsonFromRequestBody(criteria, requestbody);
				else if(XmlHelper.isValidXmlString(requestbody))
					requestbody = XmlHelper.replaceRequestTagValues(criteria, requestbody);
			
				// replace request body parameters
				requestbody = replaceParameters(requestbody);
			}
		
		}

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
				String templateDataFilePath = Helper.getFullPath(Config.getValue(TEST_DATA_TEMPLATE_DATA_PATH));
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
		
		// replace parameters for the source
		source = DataHelper.replaceParameters(source);

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
		String[] criteria = getCriteria(expectedResponse);
		

		// get response body as string
		logJsonResponse(responseValues);

		String booleanLogicPattern = StringUtils.EMPTY;
		for (String criterion : criteria) {
			criterion = criterion.trim();
			
			booleanLogicPattern += getValidationPattern(criterion);
			
			 // remove logic values if criteria starts with "(", &&, "||" or end with ")"
			criterion = removeLogicIdentifiers(criterion);
			
			Helper.assertTrue("expected response is not valid xml or json, or section identifier, are you missing the section identifier? eg. _VERIFY_JSON_PART_:  " + criterion,
					isValidExpectation(criterion));

			//convert xml string to json for validation
			criterion = convertXmlResponseToJson(criterion);

			List<String> errors = validateExpectedResponse(criterion, responseValues);
			
			errors = removeEmptyElements(errors);
			
			// set boolean logic response for the logic pattern. eg "Value && Value" becomes "true && false" 
			if(errors.isEmpty())
				booleanLogicPattern = booleanLogicPattern.replace("value", "true");
			else
				booleanLogicPattern = booleanLogicPattern.replace("value", "false");
			
			errorMessages.addAll(errors);

		}
		
		boolean isPass = evaluateLogic(booleanLogicPattern);
		
		// if logic passes, remove all errors strings
		if(isPass)
			errorMessages = new ArrayList<String>();
		
		// remove all empty response strings
		errorMessages = removeEmptyElements(errorMessages);
		return errorMessages;
	}
	
	/**
	 * split expected validation based on && or ||
	 * we ignore json path && or ||
	 * @param expectedResponse
	 * @return
	 */
	private static String[] getCriteria(String expectedResponse) {
		
		// separate the expected response by && or ||
		String[] criteria =   expectedResponse.split("(?="+ VALIDATION_AND_CONDITION +")|(?="+ VALIDATION_OR_CONDITION_ECODE +")");
		List<String> updatedCriteria = new ArrayList<String>();
		
		String criteriaVal = StringUtils.EMPTY;
		for(int i = 0 ; i<criteria.length; i++) {
			criteriaVal = criteria[i];
			criteriaVal = removeLogicIdentifiers(criteriaVal);
			
			// !@ or @ character indicate json path 
			if(criteriaVal.trim().startsWith("!@") || criteriaVal.trim().startsWith("@")) {
				int lastItem = updatedCriteria.size() - 1;
				updatedCriteria.set(lastItem, updatedCriteria.get(lastItem) + criteria[i]);		
			}else
				updatedCriteria.add(criteria[i]); 	
		}
			
		return updatedCriteria.toArray(new String[updatedCriteria.size()]);
	}
	
	/**
	 * removes && or || identifiers at the beginning of logic string
	 * used for getting the actual expression to evaluate
	 * format: eg. && expression
	 * @param value
	 * @return
	 */
	public static String removeAndOrIndicator(String value) {
		if(value.trim().startsWith(VALIDATION_OR_CONDITION)) {
			value = value.replaceFirst(VALIDATION_OR_CONDITION_ECODE, StringUtils.EMPTY);
		} else if(value.trim().startsWith(VALIDATION_AND_CONDITION)) {
			value = value.replaceFirst(VALIDATION_AND_CONDITION, StringUtils.EMPTY);
		}
		return value;
	}
	
	/**
	 * evaluates string logic. eg ((true) && (false || false || true)) returns true
	 * @param logicString
	 * @return
	 */
	public static boolean evaluateLogic(String logicString) {
		
		logicString = removeAndOrIndicator(logicString);
		
	    boolean result = false;
		try {
			 Context polyglot = Context.newBuilder("js")
					    .option("engine.WarnInterpreterOnly", "false")
					    .build();
		     Value array = polyglot.eval("js", logicString.trim());
		     result = array.asBoolean();
		} catch (Exception e) {
			e.printStackTrace();
			Helper.assertFalse(e.getMessage());	
		}
	    
	    return result;
	}
	
	/**
	 * remove logic values if criteria starts with "(", &&, "||" or end with ")"
	 * @param criterion
	 * @return
	 */
	public static String removeLogicIdentifiers(String criterion) {
		// remove && or || prefix
		criterion = removeAndOrIndicator(criterion);
		
		// add opening "(" if exist
		while(criterion.trim().startsWith("(")) {
			criterion = criterion.replaceFirst("\\(", "");
		}
		
		while(criterion.trim().endsWith("))")) {
			criterion = criterion.substring(0,criterion.length() - 1);
		}
		
		while(criterion.trim().endsWith(";)")) {
			criterion = criterion.substring(0,criterion.length() - 1);
		}
		
		return criterion;
	}
	
	/**
	 * converts response validation to proper logic pattern
	 * @param criterion
	 * @return
	 */
	public static String getValidationPattern(String criterion) {
		String pattern = StringUtils.EMPTY;
		
		boolean isORCondition = criterion.startsWith(VALIDATION_OR_CONDITION);
		if(isORCondition)
			pattern += " ||";
		else
			pattern += " &&";
		
		// remove && or || prefix
		criterion = removeAndOrIndicator(criterion);
		
		// add opening "(" if exist
		while(criterion.trim().startsWith("(")) {
			pattern += " (";
			criterion = criterion.replaceFirst("\\(", "");
		}
		
		pattern += " value";
		
		while(criterion.trim().endsWith("))")) {
			pattern += " )";
			criterion = criterion.substring(0,criterion.length() - 1);
		}
		
		while(criterion.trim().endsWith(";)")) {
			pattern += " )";
			criterion = criterion.substring(0,criterion.length() - 1);
		}
	
		return pattern;
		
	}
	
	/**
	 * convert xml string to json for validation
	 * @param xmlString
	 * @return
	 */
	private static String convertXmlResponseToJson(String xmlString){
		if (!XmlHelper.isValidXmlString(xmlString)) return xmlString;
		
		TestLog.ConsoleLog("expected xml: " + ServiceObject.normalizeLog(xmlString));
		boolean isIgnoreNamespace = Config.getBooleanValue(IS_IGNORE_XML_NAMESPACE);
		if(isIgnoreNamespace)
			xmlString = XmlHelper.removeXmlNameSpace(xmlString);

		xmlString = JsonHelper.XMLToJson(xmlString);
		TestLog.ConsoleLog(
				"expected value converted to json for validation: " + ServiceObject.normalizeLog(xmlString));

		return xmlString;
	}
	public static void logJsonResponse(List<String> responseValues) {
		List<String> updatedList = new ArrayList<String>();
		for (String response : responseValues) {
			updatedList.add(response.replace(System.lineSeparator(), ""));
		}
		String responseString = String.join(System.lineSeparator(), updatedList);
		TestLog.logPass("response to be validated: " + ServiceObject.normalizeLog(responseString));
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
			if (XmlHelper.isValidXmlString(responseString.get(i))) {
				boolean isIgnoreNamespace = Config.getBooleanValue(IS_IGNORE_XML_NAMESPACE);
				// if ignore name space, criteria and response are stripped of namespace
				if(isIgnoreNamespace) {
					String xmlIgnoreNameSpace = XmlHelper.removeXmlNameSpace(responseString.get(i));
					responseString.set(i, JsonHelper.XMLToJson(xmlIgnoreNameSpace));
				}else
					responseString.set(i, JsonHelper.XMLToJson(responseString.get(i)));
			}

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
				list.add(text.trim());
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
