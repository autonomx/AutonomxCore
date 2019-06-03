package core.apiCore.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import core.support.objects.TestObject;
import io.netty.util.internal.StringUtil;

public class DataHelper {
	
	public static final String VERIFY_JSON_PART_INDICATOR = "_VERIFY.JSON.PART_";
	public static final String VERIFY_RESPONSE_BODY_INDICATOR = "_VERIFY.RESPONSE.BODY_";

	/**
	 * replaces placeholder values with values from config properties
	 * 
	 * @param source
	 * @return
	 */
	public static String replaceParameters(String source) {
		
		if (source.isEmpty()) return source;
		
		List<String> parameters = Helper.getValuesFromPattern(source, "<(.+?)>");
		String valueStr = null;
		Object val = null;
		int length = 0;
		for (String parameter : parameters) {

			if(parameter.contains("$"))
				continue;
			if (parameter.contains("@_TIME")) {
				length = getIntFromString(parameter);
				valueStr = TestObject.getTestInfo().startTime.substring(0, length);
			} else if (parameter.contains("@_RAND")) {
				length = getIntFromString(parameter);
				valueStr = TestObject.getTestInfo().randStringIdentifier.substring(0, length);
			} else {
				val = Config.getObjectValue(parameter.replace("@", ""));
				if(val instanceof String)
					valueStr = (String) val;
			}
			if (val == null)
				Helper.assertTrue("parameter value not found: " + parameter, false);

			// disabled due to this running before anything and causing null point exception
			//TestLog.logPass("replacing value '" + parameter + "' with: " + value + "");
			if(val instanceof String)
				source = source.replaceAll("<" + parameter + ">", Matcher.quoteReplacement(valueStr));
		}

		return source;
	}

	public static int getIntFromString(String value) {
		return Integer.parseInt(value.replaceAll("[\\D]", ""));
	}

	/**
	 * gets the map of the validation requirements
	 * 
	 * @param expected
	 * @return
	 */
	public static List<KeyValue> getValidationMap(String expected) {
		// get hashmap of json path And verification
		List<KeyValue> keywords = new ArrayList<KeyValue>();
		expected = expected.replaceAll("_[^_]*_", "");
		//expected = expected.replace("_VERIFY_JSON_PART_", "");
		String[] keyVals = expected.split(";");
		String key = "";
		String position = "";
		String value = "";
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 3);
			if(parts.length == 1) { 
				 key = Helper.stringRemoveLines(parts[0]);
			}
			if(parts.length == 2) { // without position
				 key = Helper.stringRemoveLines(parts[0]);
				 position = StringUtil.EMPTY_STRING;
				 value = Helper.stringRemoveLines(parts[1]);
			}else if(parts.length == 3) { // with position
				 key = Helper.stringRemoveLines(parts[0]);
				 position = Helper.stringRemoveLines(parts[1]);
				 value = Helper.stringRemoveLines(parts[2]);
			}
			
			// if there is a value
			if(!key.isEmpty()) {
				KeyValue keyword = new KeyValue(key, position, value);
				keywords.add(keyword);
			}
		}
		return keywords;
	}
	
	/**
	 * get value in between tags >value<
	 * @param requestBody
	 * @param tag
	 * @return
	 */
	public static String getTagValue(String requestBody, String tag) {
		String value = "";
		try {
			String patternString = ":" + tag + ">(.+?)</";
			final Pattern pattern = Pattern.compile(patternString);
			final Matcher matcher = pattern.matcher(requestBody);
			matcher.find();
			value = matcher.group(1);
		} catch (Exception e) {
			e.getMessage();
		}
		return value;
	}
	
	public static String getTemplateFile(String file) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;

		return templateTestPath + file;	
	}
	
	public static File getFile(String filename) {
		String templatePath = Config.getValue(TestDataProvider.TEST_DATA_TEMPLATE_PATH);
		String templateTestPath = PropertiesReader.getLocalRootPath() + templatePath;
		File file = new File(templateTestPath + filename);
		return file;	
	}
	
	public static String convertTemplateToString(String templateFilePath) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(templateFilePath)));
		String line;
		StringBuilder sb = new StringBuilder();

		while((line=br.readLine())!= null){
		    sb.append(line.trim());
		}
		br.close();
		
		return sb.toString();
	}
	
	   /**
     * In outputParams get the params enclosed by <$> look up their values
     * in outboundMsg And Then add them to ConfigurationParams
     *
     * @param outputParams
     * @param outboundMsg
     */
    public static void addOutputMessageToConfigParams(String outputParams, String outboundMsg) {
        
        //Copy responseBody into the variable
         String key = StringUtils.substringBetween(outputParams, "<$", ">");
         Config.putValue(key, outboundMsg);
        
        TestLog.logPass("Get Service Bus Outbound Message:{0}", Config.getValue(key));
    }
    
    /**
     * validates response against expected values
     * @param command
     * @param responseString
     * @param expectedString
     */
    public static void validateCommand(String command, String responseString, String expectedString) {
    	validateCommand(command,responseString,expectedString, StringUtils.EMPTY);
    }
    
    /**
	 * validates response against expected values
	 * @param command
	 * @param responseString
	 * @param expectedString
	 * @param position
	 */
	public static void validateCommand(String command, String responseString, String expectedString, String position) {
	
		String[] expectedArray = expectedString.split(",");
		String[] actualArray = responseString.split(",");
		String actualString = "";
		
		// if position has value, Then get response at position
		if(!position.isEmpty()) {
			expectedString = expectedArray[0]; //single item
			actualString = actualArray[Integer.valueOf(position)-1];
		}
		
		switch (command) {
		case "hasItems":
			boolean val = false;
			if(!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " has item " + expectedString);
				val = actualString.contains(expectedString);
				Helper.assertTrue(actualString + " does not have item " + expectedString, val);
			}else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " has items " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				Helper.assertTrue(Arrays.toString(actualArray) + " does not have items " + Arrays.toString(expectedArray), val);
			}
			break;
		case "equalTo":
			if(!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " equals " + expectedString);
				val = actualString.equals(expectedString);
				Helper.assertTrue(actualString + " does not equal " + expectedString, val);
			}
			else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " equals " + Arrays.toString(expectedArray));
				val = Arrays.equals(expectedArray, actualArray);
				Helper.assertTrue(Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray), val);
			}
			break;
		case "contains":				
			if(!position.isEmpty()) { // if position is provided
				TestLog.logPass("verifying: " + actualString + " contains " + expectedString);
				val = actualString.contains(expectedString);
				Helper.assertTrue(actualString + " does not contain " + expectedString, val);
			}else {
				TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " contains " + Arrays.toString(expectedArray));
				val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
				Helper.assertTrue(Arrays.toString(actualArray) + " does not contain " + Arrays.toString(expectedArray), val);
			}
			break;
		case "containsInAnyOrder":
			TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " contains any order " + Arrays.toString(expectedArray));
			val = Arrays.asList(actualArray).containsAll(Arrays.asList(expectedArray));
			Helper.assertTrue(Arrays.toString(actualArray) + " does not contain in any order " + Arrays.toString(expectedArray), val);
			break;
		case "nodeSizeGreaterThan":
			int intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " greater than " + intValue);
			Helper.assertTrue("response node size is: " + actualArray.length + " expected it to be greated than: " + intValue, actualArray.length > intValue);
			break;
		case "nodeSizeExact":
			intValue = Integer.valueOf(expectedString);
			TestLog.logPass("verifying node with size " + actualArray.length + " equals " + intValue);
			Helper.assertTrue("response node size is: " + actualArray.length + " expected: " + intValue, actualArray.length == intValue);
			break;
		case "sequence":
			TestLog.logPass("verifying: " + Arrays.toString(actualArray) + " with sequence " + Arrays.toString(expectedArray));
			val = Arrays.equals(expectedArray, actualArray);
			Helper.assertTrue(Arrays.toString(actualArray) + " does not equal " + Arrays.toString(expectedArray), val);
			break;
		case "isNotEmpty":
			TestLog.logPass("verifying response for path is not empty");
			Helper.assertTrue("value is empty", !responseString.isEmpty());
			break;
		case "isEmpty":
			TestLog.logPass("verifying response for path is empty ");
			Helper.assertTrue("value is not empty", responseString.isEmpty());
			break;
		default:
			break;
		}
	}
}
