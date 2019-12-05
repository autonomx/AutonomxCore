package core.apiCore.helpers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ReadContext;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import io.restassured.response.Response;

public class JsonHelper {

	/**
	 * replaces output parameter with response values eg. $token with id form values
	 * are in form of list separated by ";"
	 * 
	 * @param response
	 * @param outputParam
	 */
	public static void saveOutboundJsonParameters(Response response, String outputParam) {
		if (response == null || outputParam.isEmpty())
			return;
		configMapJsonKeyValues(response, outputParam);
	}

	/**
	 * map key value to config eg.features.features.id:1:<$id>
	 * 
	 * @param response
	 * @param keyValue
	 */
	public static void configMapJsonKeyValues(Response response, String keyValue) {

		if (keyValue.isEmpty())
			return;

		// replace parameters for outputParam
		keyValue = DataHelper.replaceParameters(keyValue);

		List<KeyValue> keywords = DataHelper.getValidationMap(keyValue);
		for (KeyValue keyword : keywords) {
			
			// fail if value is wrong format
			if(!keyword.value.toString().startsWith("<") || !keyword.value.toString().contains("$")|| !keyword.value.toString().endsWith(">"))
				Helper.assertFalse("variable placement must of format <$variable>: " + keyword.value.toString());
			
			String key = (String) keyword.value;
			key = key.replace("$", "").replace("<", "").replace(">", "").trim();
			// gets json value. if list, returns string separated by comma
			String value = getJsonValue(response, keyword.key);

			if (!keyword.position.isEmpty()) {
				value = value.split(",")[Integer.valueOf(keyword.position) - 1];
			}
			Config.putValue(key, value);
			TestLog.logPass("replacing value " + key + " with: " + value);
		}
	}

	/**
	 * gets json value as list if applicable, or string if single item converts to
	 * string separated by ","
	 * 
	 * @param response
	 * @param path
	 * @return
	 */
	public static String getJsonValue(Response response, String path) {
		String jsonResponse = response.getBody().asString();
		String value = getJsonValue(jsonResponse, path);
		return value;
	}

	/**
	 * gets json value as list if applicable, or string if single item converts to
	 * string separated by "," https://github.com/json-path/JsonPath
	 * 
	 * @param path
	 *            https://github.com/json-path/JsonPath
	 *           
	 *            for testing json path values:
	 *            	http://jsonpath.herokuapp.com/
	 * @return value string list separated by ","
	 */
	public static String getJsonValue(String json, String path) {
		String prefix = "$.";
		
		Object values = null;
		
		// in case user forgets to remove prefix
		if(path.startsWith(prefix)) 
			path = path.replace(prefix, "");
		
		Configuration config = Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST);

		ReadContext ctx = JsonPath.using(config).parse(json);		
		
		try {
			values = ctx.read(prefix + path);
		}catch(Exception e) {
			Helper.assertFalse("invalid path: '" + path + "' for json string: " + json + "\n. see http://jsonpath.herokuapp.com to validate your path against json string. see https://github.com/json-path/JsonPath for more info. \n" + e.getMessage());
		}
		

		if(values == null )
			Helper.assertFalse("no results returned: '" + path + "'. see http://jsonpath.herokuapp.com to validate your path against json string. see https://github.com/json-path/JsonPath for more info.");

		// return json response without normalizing
		if(isValidJsonkeyValue(values)) {
			return values.toString();
		}
		
		return DataHelper.ObjectToString(values);
	}

	/**
	 * get json path value from xml string
	 * 
	 * @param xml
	 * @param path
	 * @return
	 */
	public static String getJsonValueFromXml(String xml, String path) {

		// convert xml stirng to json string
		String json = XMLToJson(xml);
		
		return getJsonValue(json, path);
	}

	/**
	 * convert xml string to json string
	 * 
	 * @param xml
	 *            string
	 * @return json string
	 */
	public static String XMLToJson(String xml) {
		int printIndentFactor = 4;
		String jsonString = StringUtils.EMPTY;
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(xml);
			jsonString = xmlJSONObj.toString(printIndentFactor);
		} catch (JSONException je) {
			je.toString();
		}
		return jsonString;
	}

	public static String getResponseValue(Response response) {
		return response.getBody().asString();
	}

	/**
	 * validates the json maps against the keyword requirements examples:
	 * "person.roles.name": hasItems("admin"), "person.lastName":
	 * equalTo("Administrator"), "person.lastName": isNotEmpty, "person.roles.name":
	 * contains("admin"), "person.roles.name": containsInAnyOrder(admin),
	 * "person.roles": nodeSizeGreaterThan(0), "person.sites.": nodeSizeExact(0)
	 * "person.roles.name": sequence("admin"),
	 * 
	 * 
	 * @param jsonMap
	 * @param response
	 */
	public static List<String> validateJsonKeywords(List<KeyValue> keywords, String responseString) {
		List<String> errorMessages = new ArrayList<String>();
		for (KeyValue keyword : keywords) {
			String jsonPath = Helper.removeSurroundingQuotes(keyword.key);
			String expectedValue = Helper.stringRemoveLines((String) keyword.value);
			String command = "";

			String[] expected = expectedValue.split("[\\(\\)]");
			// get value in between parenthesis
			if (expected.length > 1) {
				command = expected[0];
				expectedValue = expected[1];
			} else if (expected.length == 1) {
				command = expectedValue;
				expectedValue = "";
			}

			TestLog.ConsoleLog("command: " + command  + "json path: " + jsonPath);
			// get response string from json path (eg. data.user.id) would return "2"
			String jsonResponse = getJsonValue(responseString, jsonPath);

			// validate response
			String errorMessage =  DataHelper.validateCommand(command, jsonResponse, expectedValue, keyword.position);
			errorMessages.add(errorMessage);
		}
		
		// remove all empty response strings
		errorMessages.removeAll(Collections.singleton(""));
		return errorMessages;
	}

	/**
	 * validates json string
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isJSONValid(String value, boolean printError) {		
		String error = StringUtils.EMPTY;
		
		// if contains keyword indicators, then return false
		String expectedJson = Helper.stringNormalize(value);
		if (expectedJson.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR) || expectedJson.startsWith(DataHelper.VERIFY_RESPONSE_NO_EMPTY)
				|| expectedJson.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)) {
			return false;
		}
		
		try {
			new JSONObject(value);
		} catch (JSONException ex) {
			try {
				error = ex.getMessage();
				new JSONArray(value);
			} catch (JSONException ex1) {
				if(error.isEmpty()) error = ex1.getMessage();
				if(printError) TestLog.ConsoleLog("Invalid Json error: " + error);
				return false;
			}
		}
		return true;
	}

	/**
	 * validates expected json string against json body from response
	 * 
	 * @param expectedJson
	 * @param actualJson
	 * @return 
	 */
	public static String validateByJsonBody(String expectedJson, String response) {
		expectedJson = Helper.stringRemoveLines(expectedJson);
		if (JsonHelper.isJSONValid(expectedJson, true)) {
			if(StringUtils.isBlank(response)) 
				Helper.assertFalse("response is empty, please re-evaluate your json path");
			
			TestLog.logPass("expected: " + Helper.stringRemoveLines(expectedJson));
			try {
				JSONCompareResult result = JSONCompare.compareJSON(expectedJson, response, JSONCompareMode.LENIENT);
				 if (result.failed()) {
					 return result.getMessage() + "\n" + "see http://jsonpath.herokuapp.com to validate your path against json string. see https://github.com/json-path/JsonPath for more info. \n\n expectedJson: " +  expectedJson + "\n\n response: " + response + "\n\n";
				 }
				//JSONAssert.assertEquals(expectedJson, response, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return StringUtils.EMPTY;
	}

	public static boolean isValidExpectation(String expectedJson) {
		if (JsonHelper.isJSONValid(expectedJson, false)) {
			return true;
		}
		expectedJson = Helper.stringNormalize(expectedJson);
		if (expectedJson.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR) || expectedJson.startsWith("_NOT_EMPTY_")
				|| expectedJson.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)) {
			return true;
		}
		return false;
	}
	
	/**
	 * validates json response against keywords
	 * 
	 * @param expectedJson
	 * @param response
	 */
	public static List<String> validateByKeywords(String expectedJson, Response response) {
		String responseString = JsonHelper.getResponseValue(response);
		return validateByKeywords(expectedJson, responseString);
	}

	/**
	 * validates json response against keywords
	 * 
	 * @param expectedJson
	 * @param response
	 */
	public static List<String> validateByKeywords(String expectedJson, String responseString) {
		List<String> errorMessages = new ArrayList<String>();
		
		expectedJson = Helper.stringRemoveLines(expectedJson);
		expectedJson = Helper.removeSurroundingQuotes(expectedJson);
		
		if (!JsonHelper.isJSONValid(expectedJson, false)) {
			if (expectedJson.startsWith(DataHelper.VERIFY_JSON_PART_INDICATOR)) {
				// get hashmap of json path And verification
				List<KeyValue> keywords = DataHelper.getValidationMap(expectedJson);
				// validate based on keywords
				errorMessages = JsonHelper.validateJsonKeywords(keywords, responseString);
				
				// response is not empty
			} else if (expectedJson.startsWith("_NOT_EMPTY_")) {
				if(responseString.isEmpty())
					errorMessages.add("response is empty");
			}
		}
		return errorMessages;
	}
	
	/**
	 * validates response body this is validating the response body as text
	 * 
	 * @param expected
	 * @param response
	 * @return 
	 */
	public static String validateResponseBody(String expected, Response response) {
		String responseString = JsonHelper.getResponseValue(response);
		return validateResponseBody(expected, responseString);
		
	}

	/**
	 * validates response body this is validating the response body as text
	 * 
	 * @param expected
	 * @param response
	 * @return 
	 */
	public static String validateResponseBody(String expected, String responseString) {
		expected = Helper.stringRemoveLines(expected);
		expected = Helper.removeSurroundingQuotes(expected);
		
		if (!expected.startsWith(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR)) {
			return StringUtils.EMPTY;
		}
		// remove the indicator _VERIFY.RESPONSE.BODY_
		expected = removeResponseIndicator(expected);

		String[] expectedArr = expected.split("[\\(\\)]");
		String expectedValue = StringUtils.EMPTY;
		String command = StringUtils.EMPTY;
		
		// if the expected does not contain parameters. eg. isEmpty, isNotEmpty
		if(expectedArr.length == 1)
			command = expected.trim();
		else {
			// get value in between parenthesis
			command = expectedArr[0].trim();
			expectedValue = expectedArr[1].trim();
		}

		return DataHelper.validateCommand(command, responseString, expectedValue, "0");

	}

	/**
	 * remove response indicators
	 * 
	 * @param expected
	 * @return
	 */
	public static String removeResponseIndicator(String expected) {
		List<String> indicator = new ArrayList<String>();
		indicator.add(DataHelper.VERIFY_RESPONSE_BODY_INDICATOR);
		indicator.add(DataHelper.VERIFY_JSON_PART_INDICATOR);

		for (String value : indicator) {
			expected = expected.replace(value, "");
		}

		return expected;
	}
	
	/**
	 * if request body is empty, return json template string
	 * if request body contains @ variable tag, replace tag with value
	 * format for request body: json path:position:value or json path:vlaue
	 * eg. "features.feature.name:1:value_<@_TIME_19>"
	 * @param serviceObject
	 * @return
	 */
	public static String getRequestBodyFromJsonTemplate(ServiceObject serviceObject) {
		
		// return empty string if not json template
		if(!isJsonFile(serviceObject.getTemplateFile())) return StringUtils.EMPTY;
		
		Path templatePath = DataHelper.getTemplateFilePath(serviceObject.getTemplateFile());
		String jsonFileValue = DataHelper.convertFileToString(templatePath);
        jsonFileValue = DataHelper.replaceParameters(jsonFileValue);
        
		if(serviceObject.getRequestBody().isEmpty()) {
			return jsonFileValue;
		}else {
			return updateJsonFromRequestBody(serviceObject);
		}	
	}
	
	/**
	 * return true if file is json file
	 * @param filename
	 * @return
	 */
	public static boolean isJsonFile(String filename) {
		if(filename.toLowerCase().endsWith("json"))
				return true;
		return false;
	}
	
	public static String convertJsonFileToString(Path templatePath) {
		return Helper.readFileContent(templatePath.toString());		
	}
	
	public static String updateJsonFromRequestBody(ServiceObject serviceObject) {
		String jsonString = DataHelper.getServiceObjectTemplateString(serviceObject);
		Helper.assertTrue("json string is empty", !jsonString.isEmpty());
		
		// replace parameters
		jsonString = DataHelper.replaceParameters(jsonString);
		serviceObject.withRequestBody(DataHelper.replaceParameters(serviceObject.getRequestBody()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestBody());
		for(KeyValue keyword : keywords) {
			jsonString = replaceJsonPathValue(jsonString, keyword.key, keyword.value.toString());
		}
		return jsonString;
	}
	
	
	/**
	 * replace json string value based on json path
	 * eg. path: cars.name:toyota  or cars.name:2:toyota
	 * @param jsonString
	 * @param path
	 * @param value
	 * @return
	 */
	public static String replaceJsonPathValue(String jsonString, String path, String value) {
		
		
		DocumentContext doc = JsonPath.parse(jsonString);
		
		try {
			doc.set("$."+ path, value);
		}catch(Exception e) {
			e.printStackTrace();	
		}
		
		JsonObject jsonObj = new GsonBuilder().create().toJsonTree(doc.json()).getAsJsonObject();
		return jsonObj.toString();
	}
	
	/**
	 * return true if json string is a json array
	 * @param jsonString
	 * @return
	 */
	public static JSONArray getJsonArray(String jsonString) {
		try {
			return new JSONArray(jsonString);
		} catch (JSONException ex) {
			ex.getMessage();		
		}
		return null;
	}
	
	/**
	 * return true if json string is a json o
	 * @param jsonString
	 * @return
	 */
	public static JSONObject getJsonObject(String jsonString) {
		try {
			return new JSONObject(jsonString);
		} catch (JSONException ex) {
			ex.getMessage();		
		}
		return null;
	}
	
	/**
	 * checks if json string is a structured json body with key value pairs, or just array list. eg:
	 *  [{"key":"value"}] vs ["value1","value2"]
	 * @param jsonString
	 * @return
	 */
	public static boolean isValidJsonkeyValue(Object jsonObject) {
		if(jsonObject instanceof Map)
			jsonObject = new Gson().toJson(jsonObject, Map.class);
		
		String jsonString = jsonObject.toString();
		
		if(getJsonArray(jsonString) == null && getJsonObject(jsonString) == null)
			return false;
		
		String jsonNormalized = DataHelper.ObjectToString(jsonString);
		if(jsonNormalized.contains(":"))
			return true;
		return false;
	}
	
	public static String updateJsonFromJasonPath(String jsonPath) {
		return jsonPath;
		
	}
	
	public static List<String> validateExpectedValues2(List<String> responseValues, ServiceObject serviceObject) {
		
		List<String> errorMessages = new ArrayList<String>();
		// get response body as string
		TestLog.logPass("received response messages: " + String.join(System.lineSeparator(), responseValues));

		// validate response body against expected json string
		if (!serviceObject.getExpectedResponse().isEmpty()) {
			serviceObject.withExpectedResponse(DataHelper.replaceParameters(serviceObject.getExpectedResponse()));

			// separate the expected response by &&
			String[] criteria = serviceObject.getExpectedResponse().split("&&");
			for (String criterion : criteria) {
				if(!JsonHelper.isValidExpectation(criterion))
					continue;
				
				errorMessages = validateExpectedResponse(criterion, responseValues);
			}
		}
		// remove all empty response strings
		errorMessages.removeAll(Collections.singleton(""));
		return errorMessages;
	}
	
	/**
	 * validates expected requirement against response strings
	 * @param criterion
	 * @param responseString
	 * @return
	 */
	public static List<String> validateExpectedResponse(String criterion, List<String> responseString) {
		List<String> errorMessages = new ArrayList<String>();
		for(int i = 0; i < responseString.size(); i++) {
			errorMessages = new ArrayList<String>();
			
			errorMessages.add(JsonHelper.validateByJsonBody(criterion, responseString.get(i)));
			errorMessages.addAll(JsonHelper.validateByKeywords(criterion, responseString.get(i)));
			errorMessages.add(JsonHelper.validateResponseBody(criterion, responseString.get(i)));
			
			// if no errors, then validation passed, no need to validate against other responses
			if(errorMessages.isEmpty()) break;
			
			if(i > 0 &&i == responseString.size() && !errorMessages.isEmpty()) {
				errorMessages = new ArrayList<String>();
				errorMessages.add("expected requirement: " + criterion + " not met by the responses: " + String.join(System.lineSeparator(), responseString));
				
			}
		}
		return errorMessages;
	}
	
	public static List<String> validateExpectedValues(String responseString, ServiceObject serviceObject) {
		List<String> errorMessages = new ArrayList<String>();

		// get response body as string
		TestLog.logPass("response: " + responseString);

		// validate response body against expected json string
		if (!serviceObject.getExpectedResponse().isEmpty()) {
			serviceObject.withExpectedResponse(DataHelper.replaceParameters(serviceObject.getExpectedResponse()));

			// separate the expected response by &&
			String[] criteria = serviceObject.getExpectedResponse().split("&&");
			for (String criterion : criteria) {
				Helper.assertTrue("expected is not valid format: " + criterion,
						JsonHelper.isValidExpectation(criterion));
				errorMessages.add(JsonHelper.validateByJsonBody(criterion, responseString));
				errorMessages.addAll(JsonHelper.validateByKeywords(criterion, responseString));
				errorMessages.add(JsonHelper.validateResponseBody(criterion, responseString));
			}
		}
		// remove all empty response strings
		arrayRemoveAllEmptyString(errorMessages);
		
		return errorMessages;
	}
	
	public static List<String> arrayRemoveAllEmptyString(List<String> list){
		 list.removeAll(Collections.singleton(""));
		 return list;
	}
}
