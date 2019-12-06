package core.apiCore.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

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
	public static final String VERIFY_RESPONSE_BODY_INDICATOR = "_VERIFY.RESPONSE.BODY_";
	public static final String VERIFY_RESPONSE_NO_EMPTY = "_NOT_EMPTY_";
	public static final String VERIFY_HEADER_PART_INDICATOR = "_VERIFY.HEADER.PART_";
	public static final String VERIFY_TOPIC_PART_INDICATOR = "_VERIFY.TOPIC.PART_";

	enum JSON_COMMAND {
		hasItems, notHaveItems, notEqualTo, equalTo, notContain, contains, containsInAnyOrder, integerGreaterThan, integerLessThan, integerEqual, integerNotEqual, nodeSizeGreaterThan, nodeSizeExact, sequence, jsonbody, isNotEmpty, isEmpty, nodeSizeLessThan
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
				length = Helper.getIntFromString(parameter.split("[+-]")[0]);
				if (length > 17) length = 17;
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING_MS));			
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
						.withZone(ZoneId.of("UTC"));
				value = formatter.format(newTime).substring(0, length);	
			}else if (parameter.contains("_TIME_ISO_")) {
				length = Helper.getIntFromString(parameter.split("[+-]")[0]);
				if (length > 24) length = 24;
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING_ISO));
				value = newTime.toString().substring(0, length);
			}else if (parameter.contains("_TIME")) {
				length = Helper.getIntFromString(parameter.split("[+-]")[0]);
				if (length > 17) length = 17;
				newTime = getTime(parameter, Config.getValue(TestObject.START_TIME_STRING_MS));
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
						.withZone(ZoneId.of("UTC"));
				value = formatter.format(newTime).substring(0, length);	
			}else if (parameter.contains("_RAND")) {
				length = Helper.getIntFromString(parameter);
				value = Config.getValue(TestObject.RANDOM_STRING).substring(0, length);
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
				TestLog.ConsoleLog("replacing value " + parameter + "  with: " +  value);
				source = source.replace("<@" + parameter + ">", Matcher.quoteReplacement(value.toString()));
			}
		}

		return source;
	}
	
	public static Instant getTime(String parameter, String timeString) {
		Instant time = Instant.parse(timeString);
		Instant newTime = time;

		return newTime;
	}

	/**
	 * gets the map of the validation requirements
	 * split by ";"
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
			List<String> parts = splitRight(keyVal,":", 3);
			if (parts.size() == 1) {
				key = Helper.stringRemoveLines(parts.get(0));
			}
			if (parts.size()  == 2) { // without position
				key = Helper.stringRemoveLines(parts.get(0));
				position = StringUtil.EMPTY_STRING;
				value = Helper.stringRemoveLines(parts.get(1));
			} else if (parts.size()  == 3) { // with position
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
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;

		return templateTestPath + file;
	}
	
	public static Path getTemplateFilePath(String file) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;
		String fullLocation = templateTestPath + file;
		return new File(fullLocation).toPath();
	}

	public static File getFile(String filename) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;
		File file = new File(templateTestPath + filename);
		return file;
	}

    /**
     * returns service object template file as string
     * Template file name is from Template column in csv
     * @param apiObject
     * @return
     * @throws IOException 
     */
    public static String getServiceObjectTemplateString(ServiceObject serviceObject) {
    	
        Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
		return  DataHelper.convertFileToString(templatePath);
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
	public static String validateCommand(String command, String responseString, String expectedString, String position) {

		// remove surrounding quotes
		expectedString = Helper.removeSurroundingQuotes(expectedString);
		command = Helper.removeSurroundingQuotes(command);
		responseString = Helper.removeSurroundingQuotes(responseString);
		
		String[] expectedArray = expectedString.split(",");
		String[] actualArray = responseString.split(",");
		String actualString = "";

		int positionInt = 0;
		
		// if position has value, Then get response at position
		if (!position.isEmpty() && Helper.getIntFromString(position) > 0) {
			positionInt = Helper.getIntFromString(position);
			expectedString = expectedArray[0]; // single item
			boolean inBounds = (positionInt > 0) && (positionInt <= actualArray.length);
			if (!inBounds) {
				Helper.assertFalse("items returned are less than specified. returned: " + actualArray.length
						+ " specified: " + positionInt);
			}

			actualString = actualArray[positionInt - 1];
		}
		
	    if(getCommandFromExpectedString(command).isEmpty()) {
	    	Helper.assertFalse("Command not set. Options: " + Arrays.asList(JSON_COMMAND.values()) + ". See examples for usage.");
	    }
		
		JSON_COMMAND jsonCommand = JSON_COMMAND.valueOf(command);

		switch (jsonCommand) {
		case hasItems:
			boolean val = false;
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " has item " + expectedString);
				val = actualString.contains(expectedString);
				if(!val) return actualString + " does not have item " + expectedString;
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: " + responseString + " has item " + expectedString);
				val = responseString.contains(expectedString);
				if(!val) return responseString + " does not have item " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " has items " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does not have items " + Arrays.toString(expectedArray);
			}
			break;
		case notHaveItems:
			val = false;
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " does not have item " + expectedString);
				val = !actualString.contains(expectedString);
				if(!val) return actualString + " does have item " + expectedString;
				
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: " + responseString + " does not have item " + expectedString);
				val = !responseString.contains(expectedString);
				if(!val) return responseString + " does not have item " + expectedString;
				
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " does not have items " + Arrays.toString(expectedArray));
				val = !Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does have items " + Arrays.toString(expectedArray);
			}
			break;
		case notEqualTo:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " not equals " + expectedString);
				val = !actualString.equals(expectedString);
				if(!val) return actualString + " does equal " + expectedString;
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: " + responseString + " not equals " + expectedString);
				val = !responseString.equals(expectedString);
				if(!val) return responseString + " does equal " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " not equals " + Arrays.toString(expectedArray));
				val = !Arrays.equals(expectedArray, actualArray);
				if(!val) return Arrays.toString(actualArray) + " does equal " + Arrays.toString(expectedArray);
			}
			break;
		case equalTo:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " equals " + expectedString);
				val = actualString.equals(expectedString);
				if(!val) return actualString + " does not equal " + expectedString;
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: " + responseString + " equals " + expectedString);
				val = responseString.equals(expectedString);
				if(!val) return responseString + " does not equal " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " equals " + Arrays.toString(expectedArray));
				val = Arrays.equals(expectedArray, actualArray);
				if(!val) return Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray);
			}
			break;
		case notContain:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " does not contain " + expectedString);
				val = !actualString.contains(expectedString);
				if(!val) return actualString + " does contain " + expectedString;
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: " + expectedString + " does not contain " + expectedString);
				val = !responseString.contains(expectedString);
				if(!val) return responseString + " does contain " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " does not contain " + Arrays.toString(expectedArray));
				val = !Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does not contain " + Arrays.toString(expectedArray);
			}
			break;
		case contains:
			if (!position.isEmpty() && positionInt > 0) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " contains " + expectedString);
				val = actualString.contains(expectedString);
				if(!val) return actualString + " does not contain " + expectedString;
			} else if(!position.isEmpty() && positionInt == 0) { 
				TestLog.logPass("verifying: '" + responseString + "' contains " + expectedString);
				val = responseString.contains(expectedString);
				if(!val) return responseString + " does not contain " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " contains " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does not contain " + Arrays.toString(expectedArray);
			}
			break;
		case containsInAnyOrder:
			TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " contains any order "
					+ Arrays.toString(expectedArray));
			val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
			if(!val) return Arrays.toString(actualArray) + " does not contain in any order " + Arrays.toString(expectedArray);
			break;
		case integerGreaterThan:
			val = compareNumbers(responseString, expectedString, "greaterThan");
			if(!val) return "actual: " +  responseString + " is is less than expected: " + expectedString;
			break;
		case integerLessThan:
			val = compareNumbers(responseString, expectedString, "lessThan");
			if(!val) return "actual: " +  responseString + " is is greater than expected: " + expectedString;
			break;
		case integerEqual:
			val = compareNumbers(responseString, expectedString, "equalTo");
			if(!val) return "actual: " +  responseString + " is not equal to expected: " + expectedString;
			break;
		case integerNotEqual:
			val = !compareNumbers(responseString, expectedString, "equalTo");
			if(!val) return "actual: " +  responseString + " is not equal to expected: " + expectedString;
			break;
		case nodeSizeGreaterThan:
			int intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " greater than " + intValue);
			if(intValue >= actualArray.length) return "response node size is: " + actualArray.length + " expected it to be greated than: " + intValue;
			break;	
		case nodeSizeLessThan:
			intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " less than " + intValue);
			if(intValue < actualArray.length) return "response node size is: " + actualArray.length + " expected it to be greated than: " + intValue;
			break;
		case nodeSizeExact:
			intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " equals " + intValue);
			if(actualArray.length != intValue) return "response node size is: " + actualArray.length + " expected: " + intValue;
			break;
		case sequence:
			TestLog.logPass(
					"verifying: " + Arrays.toString(actualArray) + " with sequence " + Arrays.toString(expectedArray));
			val = Arrays.equals(expectedArray, actualArray);
			if(!val) return Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray);
			break;
		case jsonbody:
			TestLog.logPass(
					"verifying response: \n" + responseString + "\n against expected: \n" + expectedString);
			String error = JsonHelper.validateByJsonBody(expectedString,  responseString);
			if(!error.isEmpty()) return error;
			break;
		case isNotEmpty:
			TestLog.logPass("verifying response for path is not empty");
			if(isEmpty(responseString)) return "value is empty";
			break;
		case isEmpty:
			TestLog.logPass("verifying response for path is empty ");
			if(!isEmpty(responseString)) return "value is not empty";
			break;
		default:
	    	Helper.assertFalse("Command not set. Options: " + Arrays.asList(JSON_COMMAND.values()) + ". See examples for usage.");
			break;
		}
		return StringUtil.EMPTY_STRING;
	}
	
	public static boolean isGreaterThan(String value1, String value2) {
		if(Helper.isNumeric(value1) && Helper.isNumeric(value2)) {
			if(Integer.valueOf(value1) > Integer.valueOf(value2))
					return true;
		}
		return false;
	}
	
	public static boolean isLessThan(String value1, String value2) {
		if(Helper.isNumeric(value1) && Helper.isNumeric(value2)) {
			if(Integer.valueOf(value1) < Integer.valueOf(value2))
					return true;
		}
		return false;
	}
	
	/**
	 * compare string integer values based on comparator value
	 * @param value1
	 * @param value2
	 * @param comparator
	 * @return
	 */
	public static boolean compareNumbers(String value1, String value2, String comparator) {
		if(!Helper.isStringContainNumber(value1) || !Helper.isStringContainNumber(value2))
			return false;
		
		double val1Double = Helper.getDoubleFromString(value1); 
		double val2Double = Helper.getDoubleFromString(value2); 

		return compareNumbers(val1Double, val2Double, comparator );
	}
	
	/**
	 * compare integer values
	 * @param value1
	 * @param value2
	 * @param comparator
	 * @return
	 */
	public static boolean compareNumbers(double value1, double value2, String comparator) {
					
		switch (comparator) {
		case "greaterThan":
			if(value1 > value2)
				return true;
		break;
		case "lessThan":
			if(value1 < value2)
				return true;
			break;
		case "equalTo":
			if(value1 == value2)
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
	 * convert object to string
	 * object can be array
	 * @param values
	 * @return
	 */
	public static String ObjectToString(Object values) {
		String stringVal = values.toString();
		stringVal = stringVal.replaceAll("[\\[\\](){}]","");
		stringVal = stringVal.replace("\"", "");
		return stringVal;
	}
	
	public static List<String> splitRight(String value, String regex, int limit) {
		
		 // if json validation command, return format path:position:command or path:command
		String commandValue = getCommandFromExpectedString(value);
	    if(!commandValue.isEmpty()) {
	    	return getJsonKeyValue(value, commandValue);
	    }
		
		String string = value;
	    List<String> result = new ArrayList<String>();
	    String[] temp = new String[0];
	    for(int i = 1; i < limit; i++) {
	        if(string.matches(".*"+regex+".*")) {
	            temp = string.split(modifyRegex(regex));
	            result.add(temp[1]);
	            string = temp[0];
	        }
	    }
	    if(temp.length>0) { 
	        result.add(temp[0]);
	    }
	    
	    // handle single value
	    if(value.split(":").length == 1) result.add(string);  
	  
	    Collections.reverse(result);
	    return result;
	}
	
	/**
	 * get the json response validation command from string
	 * eg. soi:EquipmentID:1:notEqualTo(2019110423T11:00:00.000Z)  -> command: notEqual
	 * 	eg. value:isEmpty  -> command: isEmpty
	 * @param value
	 * @return 
	 */
	private static String getCommandFromExpectedString(String value) {
		String commandValue = StringUtils.EMPTY;
		
		for (JSON_COMMAND command : JSON_COMMAND.values()) {
			List<String> parameters = Helper.getValuesFromPattern(value, command + "\\(([^)]+)\\)");
			if(!parameters.isEmpty()) {  // command(value)
				commandValue =  command + "(" + parameters.get(0) + ")";
				if(value.endsWith(commandValue))
				  return commandValue;
			}
			else if(value.endsWith(command.name())) //isEmpty, isNotEmpty
				return command.name();
		}
		return StringUtils.EMPTY;
	}
	
	/**
	 * get json key value
	 * eg. store.book[?(@.price < 10)]:jsonbody(["key":"value"])
	 * becomes arraylist of size 2. 
	 * eg. store.book[?(@.price < 10)]:1:jsonbody(["key":"value"])
	 * becomes arraylist of size 3. 
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
		if(keyPositionList.size() == 2 && Helper.isStringContainOnlyNumber(keyPositionList.get(1))) {
			result.add(keyPositionList.get(0));
			result.add(keyPositionList.get(1));
		}else {
			result.add(keyPosition);
		}
		
		// add command + value. eg. equal(value) or isEmpty
		result.add(commandValue);
		
		return result;
	}

	private static String modifyRegex(String regex){
	    return regex + "(?!.*" + regex + ".*$)";
	}
	
	public static boolean isEmpty(String value) {
		if(StringUtils.isBlank(value))
			return true;
		if(value.equals("null"))
			return true;
		return false;		
	}
	
	/**
	 * get request body from template file: json, xml, other
	 * json template:
	 * 		- if request body is set, replace value in template with format:
	 * 		- request body: json path:position:value or json path:vlaue
	 * 		- eg. "features.feature.name:1:value_<@_TIME_19>" 
	 * xml template:
	 * 		- if request body is set, replace value in template with format:
	 * 		- request body: tag:position:value   or tag:value
	 * 		- eg. "soi:EquipmentID:1:equip_<@_TIME_17>"
	 * other file type:
	 * 		- return as string
	 * if no template set:
	 * 		- return request body
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static String getRequestBodyIncludingTemplate(ServiceObject serviceObject) {
		
		// replace request body parameters
		serviceObject.withRequestBody(replaceParameters(serviceObject.getRequestBody()));
		
		// if json template file
		if(JsonHelper.isJsonFile(serviceObject.getTemplateFile()))
			return JsonHelper.getRequestBodyFromJsonTemplate(serviceObject);
		
		// if xml template file
		if(XmlHelper.isXmlFile(serviceObject.getTemplateFile()))
			return XmlHelper.getRequestBodyFromXmlTemplate(serviceObject);
		
		// if other type of file
		if(!serviceObject.getTemplateFile().isEmpty()) {
			Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
			return convertFileToString(templatePath);
		}
		
		// if no template, return request body
		return serviceObject.getRequestBody();
	}
	
	/**
	 * get file content as text
	 * replaces parameters using syntax <@variable> from value in config
	 * @param templatePath
	 * @return
	 */
	public static String convertFileToString(Path templatePath) {
		String content = Helper.readFileContent(templatePath.toString());
		
		// replace content paramters
		return replaceParameters(content);
	}

	/**
	 * remove section from expected response separated by &&
	 * the section will start with the identifier. eg. _VERIFY.RESPONSE.BODY_ 
	 * @param section
	 * @param expectedResponse
	 * @return
	 */
	public static String removeSectionFromExpectedResponse(String section, String expectedResponse) {
		String[] criteria = expectedResponse.split("&&");
		List<String> newResponse = new ArrayList<String>();
		for (String criterion : criteria) {
			criterion = Helper.removeSurroundingQuotes(criterion);
			if (!criterion.trim().startsWith(section)){
				newResponse.add(criterion);
			}
		}
		return String.join("&&", newResponse);
	}
	
	/**
	 * get section from expected response separated by &&
	 * the section will start with the identifier. eg. _VERIFY.RESPONSE.BODY_ 
	 * @param section
	 * @param expectedResponse
	 * @return
	 */
	public static String getSectionFromExpectedResponse(String section, String expectedResponse) {
		String[] criteria = expectedResponse.split("&&");
		List<String> newResponse = new ArrayList<String>();
		for (String criterion : criteria) {
			criterion = Helper.removeSurroundingQuotes(criterion);
			if (criterion.trim().startsWith(section)){
				newResponse.add(criterion);
			}
		}
		return String.join("&&", newResponse);
	}
}
