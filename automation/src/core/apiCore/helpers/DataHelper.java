package core.apiCore.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		String valueStr = null;
		Object val = null;
		int length = 0;
		for (String parameter : parameters) {

			if (parameter.contains("_TIME")) {
				length = getIntFromString(parameter);
				if(length > 19) length = 19;
				valueStr = TestObject.getTestInfo().startTime.substring(0, length);
			} else if (parameter.contains("_RAND")) {
				length = getIntFromString(parameter);
				valueStr = TestObject.getTestInfo().randStringIdentifier.substring(0, length);
			} else {
				val = Config.getObjectValue(parameter);
				if (val instanceof String)
					valueStr = (String) val;
			}
			if (StringUtil.isNullOrEmpty(valueStr))
				TestLog.logWarning("parameter value not found: " + parameter);

			if (valueStr instanceof String) {
				source = source.replace("<@" + parameter + ">", Matcher.quoteReplacement(valueStr));
				// TestLog.logPass("replacing value '" + parameter + "' with: " + valueStr +
				// "");
			}
		}
		return source;
	}

	public static int getIntFromString(String value) {
		return Integer.parseInt(value.replaceAll("[\\D]", ""));
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
	
	/**
	 * get value in between tags >value<
	 * 
	 * @param requestBody
	 * @param tag
	 * @return
	 */
	public static String getXmlTagValue(String value, String tag) {
		
		return getXmlTagValue(value, tag, ":" + tag + ">(.+?)</");
	}


	/**
	 * get value in between tags >value<
	 * 
	 * @param requestBody
	 * @param tag
	 * @return
	 */
	public static String getXmlTagValue(String requestBody, String tag, String patternString) {
		String value = StringUtil.EMPTY_STRING;
		try {
			final Pattern pattern = Pattern.compile(patternString);
			final Matcher matcher = pattern.matcher(requestBody);
			matcher.find();
			value = matcher.group(1);
		} catch (Exception e) {
			e.getMessage();
		}
		return value;
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

	public static String convertTemplateToString(String templateFilePath) {
		String xml = StringUtils.EMPTY;
		try {
			 xml = new String(
				    Files.readAllBytes(new File(templateFilePath).toPath()), StandardCharsets.UTF_8);
				System.out.println(xml.length());

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return xml; 
	}
	
    /**
     * returns service object template file as string
     * Template file name is from Template column in csv
     * @param apiObject
     * @return
     * @throws IOException 
     */
    public static String getServiceObjectTemplateString(ServiceObject serviceObject) {
    	
        String path = DataHelper.getTemplateFileLocation(serviceObject.getTemplateFile());
        File file = new File(path);

        return DataHelper.convertTemplateToString(file.getAbsolutePath());
    }

	/**
	 * validates response against expected values
	 * 
	 * @param command
	 * @param responseString
	 * @param expectedString
	 */
	public static void validateCommand(String command, String responseString, String expectedString) {
		validateCommand(command, responseString, expectedString, StringUtils.EMPTY);
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

		String[] expectedArray = expectedString.split(",");
		String[] actualArray = responseString.split(",");
		String actualString = "";

		// if position has value, Then get response at position
		if (!position.isEmpty()) {
			int positionInt = Integer.valueOf(position);
			expectedString = expectedArray[0]; // single item
			boolean inBounds = (positionInt > 0) && (positionInt <= actualArray.length);
			if (!inBounds) {
				Helper.assertFalse("items returned are less than specified. returned: " + actualArray.length
						+ " specified: " + positionInt);
			}

			actualString = actualArray[positionInt - 1];
		}

		switch (command) {
		case "hasItems":
			boolean val = false;
			if (!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " has item " + expectedString);
				val = actualString.contains(expectedString);
				if(!val) return actualString + " does not have item " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " has items " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does not have items " + Arrays.toString(expectedArray);
			}
			break;
		case "equalTo":
			if (!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " equals " + expectedString);
				val = actualString.equals(expectedString);
				if(!val) return actualString + " does not equal " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " equals " + Arrays.toString(expectedArray));
				val = Arrays.equals(expectedArray, actualArray);
				if(!val) return Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray);
			}
			break;
		case "contains":
			if (!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " contains " + expectedString);
				val = actualString.contains(expectedString);
				if(!val) return actualString + " does not contain " + expectedString;
			} else {
				TestLog.logPass(
						"verifying: " + Arrays.toString(actualArray) + " contains " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				if(!val) return Arrays.toString(actualArray) + " does not contain " + Arrays.toString(expectedArray);
			}
			break;
		case "containsInAnyOrder":
			TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " contains any order "
					+ Arrays.toString(expectedArray));
			val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
			if(!val) return Arrays.toString(actualArray) + " does not contain in any order " + Arrays.toString(expectedArray);
			break;
		case "nodeSizeGreaterThan":
			int intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " greater than " + intValue);
			if(intValue >= actualArray.length) return "response node size is: " + actualArray.length + " expected it to be greated than: " + intValue;
			break;
		case "nodeSizeExact":
			intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " equals " + intValue);
			if(actualArray.length != intValue) return "response node size is: " + actualArray.length + " expected: " + intValue;
			break;
		case "sequence":
			TestLog.logPass(
					"verifying: " + Arrays.toString(actualArray) + " with sequence " + Arrays.toString(expectedArray));
			val = Arrays.equals(expectedArray, actualArray);
			if(!val) return Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray);
			break;
		case "jsonbody":
			TestLog.logPass(
					"verifying response: " + responseString + " against expected: " + expectedString);
			String error = JsonHelper.validateByJsonBody(expectedString,  responseString);
			if(!error.isEmpty()) return error;
			break;
		case "isNotEmpty":
			TestLog.logPass("verifying response for path is not empty");
			if(isEmpty(responseString)) return "value is empty";
			break;
		case "isEmpty":
			TestLog.logPass("verifying response for path is empty ");
			if(!isEmpty(responseString)) return "value is not empty";
			break;
		default:
			Helper.assertFalse("Command not set. Options: hasItems, equalTo,"
					+ " contains, containsInAnyOrder, nodeSizeGreaterThan, nodeSizeExact, sequence, isNotEmpty, isEmpty. See examples for usage.");
			break;
		}
		return StringUtil.EMPTY_STRING;
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
	
	/**
	 * convert object to string
	 * object can be array
	 * @param values
	 * @return
	 */
	public static String ObjectRemoveBrackets(Object values) {
		String stringVal = values.toString();
		stringVal = stringVal.replaceAll("[\\[\\]]","");
		return stringVal;
	}
	
	public static List<String> splitRight(String value, String regex, int limit) {
		
		 // get jason key value
	    if(value.contains("jsonbody")) {
	    	return getJsonKeyValue(value);
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
	 * get json key value
	 * eg. store.book[?(@.price < 10)]:jsonbody(["key":"value"])
	 * becomes arraylist of size 2. 
	 * @param value
	 * @return
	 */
	private static List<String> getJsonKeyValue(String value) {
	    List<String> result = new ArrayList<String>();

		String[] valueArray = value.split(":jsonbody");
		result.add(valueArray[0]);
		result.add("jsonbody" + valueArray[1]);
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
}
