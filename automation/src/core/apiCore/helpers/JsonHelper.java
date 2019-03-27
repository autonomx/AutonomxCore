package core.apiCore.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
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
	 * map key value to config
	 * eg.features.features.id:1:<$id>
	 * @param response
	 * @param keyValue
	 */
	public static void configMapJsonKeyValues(Response response, String keyValue) {
		
		if (keyValue.isEmpty()) return;
		
		// replace parameters for outputParam
		keyValue = DataHelper.replaceParameters(keyValue);
		
		List<KeyValue> keywords = DataHelper.getValidationMap(keyValue);
		for (KeyValue keyword : keywords) {
			String key = keyword.value.replace("$", "").replace("<", "").replace(">", "").trim();
			// gets json value. if list, returns string separated by comma
			String value = getJsonValue(response, keyword.key);
			
			if(!keyword.position.isEmpty()) {
				value = value.split(",")[Integer.valueOf(keyword.position) -1 ];
			}
			Config.putValue(key, value);
			TestLog.logPass("replacing value " + key + " with: " + value);
		}
	}

	/**
	 * gets json value as list if applicable, or string if single item converts to
	 * string separted by ","
	 * 
	 * @param response
	 * @param path
	 * @return
	 */
	public static String getJsonValue(Response response, String path) {
		String value = "";
		List<String> values = getJsonListValueResponse(response, path);
		
		if(values == null || values.isEmpty()) {
			 value = getJsonStringResponse(response, path);
		}
			
		if (values != null && !values.isEmpty())
			value = listToString(values);
		return value;
	}
	
	public static String listToString(List<String> values) {
		String result = "";
		for(Object val : values) {
			String value = Objects.toString(val, "");
			result = result + value;
			if(values.size()>1) result+=",";
		}
		return result;		
	}
	
	private static List<String> getJsonListValueResponse(Response response, String path){
		List<String> values = new ArrayList<String>();
		try {
			values = response.jsonPath().getList(path);
		} catch (Exception e) {
			e.getMessage();
		}	
		return values;
	}
	
	private static String getJsonStringResponse(Response response, String path) {
		String value = "";
		
		if(response.path(path) instanceof String) {
			value = response.path(path);
		}
		
		if(response.path(path) instanceof Integer) {
			value = Integer.toString(response.path(path));
		
		}
		
		if(response.path(path) instanceof Boolean) {		 	
			value = Boolean.toString(response.path(path));
		}
		if(value == null) value = "";
		return value;
	}
	
	

	/**
	 * validates the json maps against the keyword requirements 
	 * examples: "person.roles.name": hasItems("admin"), "person.lastName":
	 * equalTo("Administrator"), "person.lastName": isNotEmpty, "person.roles.name":
	 * contains("admin"), "person.roles.name": containsInAnyOrder(admin),
	 * "person.roles": nodeSizeGreaterThan(0), "person.sites.": nodeSizeExact(0)
	 * "person.roles.name": sequence("admin"),
	 * 
	 * 
	 * @param jsonMap
	 * @param response
	 */
	public static void validateJsonKeywords(List<KeyValue> keywords, Response response) {
		for (KeyValue keyword : keywords) {
			String jsonPath = Helper.stringNormalize(keyword.key);
			String expectedValue = Helper.stringNormalize(keyword.value);
			String command = "";
			
			String[] expected = expectedValue.split("[\\(\\)]");
			// get value inbetween parenthesis
			if (expected.length > 1) {
				command = expected[0];
				expectedValue = expected[1];
			} else if (expected.length == 1) {
				command = expectedValue;
				expectedValue = "";
			}

			// get response string from json path (eg. data.user.id) would return "2"
			String responseString = getJsonValue(response, jsonPath);
			
			// validate response
			DataHelper.validateCommand(command, responseString, expectedValue, keyword.position);
		}
	}
	
	
	/**
	 * validates json string
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isJSONValid(String test) {
		try {
			new JSONObject(test);
		} catch (JSONException ex) {
			try {
				new JSONArray(test);
			} catch (JSONException ex1) {
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
	 */
	public static void validateByJsonBody(String expectedJson, Response response) {
		expectedJson = Helper.stringNormalize(expectedJson);
		if (JsonHelper.isJSONValid(expectedJson)) {
			TestLog.logPass("expected: " + Helper.stringRemoveLines(expectedJson));
			String body = response.getBody().asString();
			try {
				JSONAssert.assertEquals(expectedJson, body, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	public static boolean isValidExpectation(String expectedJson) {
		if (JsonHelper.isJSONValid(expectedJson)) {
			return true;
		}
		expectedJson = Helper.stringNormalize(expectedJson);
		if (expectedJson.startsWith("_VERIFY_JSON_PART_") || expectedJson.startsWith("_NOT_EMPTY_")) {
			return true;
		}
		return false;
	}

	/**
	 * validates json response against hamcrest keywords
	 * 
	 * @param expectedJson
	 * @param response
	 */
	public static void validateByKeywords(String expectedJson, Response response) {
		expectedJson = Helper.stringNormalize(expectedJson);
		if (!JsonHelper.isJSONValid(expectedJson)) {
			if (expectedJson.startsWith("_VERIFY_JSON_PART_")) {
				// get hashmap of json path And verification
				List<KeyValue> keywords = DataHelper.getValidationMap(expectedJson);
				// validate based on keywords
				JsonHelper.validateJsonKeywords(keywords, response);

				// response is not empty
			} else if (expectedJson.startsWith("_NOT_EMPTY_")) {
				Helper.assertTrue("response is empty", response != null);
				response.then().body("isEmpty()", Matchers.is(false));
			}
		}
	}

}
