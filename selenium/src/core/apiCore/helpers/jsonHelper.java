package core.apiCore.helpers;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;

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

public class jsonHelper {

	/**
	 * replaces output parameter with response values eg. $token with id form values
	 * are in form of list separated by ","
	 * 
	 * @param response
	 * @param outputParam
	 */
	public static void saveOutboundJsonParameters(Response response, String outputParam) {
		if (response == null || outputParam.isEmpty())
			return;

		String[] keyVals = outputParam.split(",");
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 2);
			if (parts.length < 2)
				Helper.assertFalse("key value pair incorrect: " + keyVal);
			String key = parts[1].replace("$", "").replace("<", "").replace(">", "").trim();

			// gets json value. if list, returns string separated by comma
			String value = getJsonValue(response, parts[0]);
			Config.putValue(key, value);
			TestLog.logPass("replacing value " + parts[1] + " with: " + value);

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
		List<String> values = new ArrayList<String>();
		try {
			values = response.jsonPath().getList(path);
		} catch (Exception e) {
			value = response.path(path);
		}
		if (values != null && !values.isEmpty())
			value = String.join(",", values);
		return value;
	}

	/**
	 * validates the json maps agains the keyword requirements using hamcrest
	 * matcher. examples: "person.roles.name": hasItems("admin"), "person.lastName":
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
			String value = Helper.stringNormalize(keyword.value);
			String command = "";

			String[] expected = value.split("[\\(\\)]");
			// get value inbetween parenthesis
			if (expected.length > 1) {
				command = expected[0];
				value = expected[1];
			} else if (expected.length == 1) {
				command = value;
				value = "";
			}

			Object responseVal = response.path(jsonPath);

			switch (command) {
			case "hasItems":
				String[] values = value.split(",");
				response.then().body(jsonPath, hasItems(values));
				break;
			case "equalTo":
				response.then().body(jsonPath, equalTo(value));
				break;
			case "contains":
				values = value.split(",");
				response.then().body(jsonPath, contains(values));
				break;
			case "containsInAnyOrder":
				values = value.split(",");
				response.then().body(jsonPath, containsInAnyOrder(values));
				break;
			case "nodeSizeGreaterThan":
				int intValue = Integer.valueOf(value);
				response.then().body(jsonPath, hasSize(greaterThan(intValue)));
				break;
			case "nodeSizeExact":
				intValue = Integer.valueOf(value);
				response.then().body(jsonPath, hasSize(equalTo(intValue)));
				break;
			case "sequence":
				values = value.split(",");
				response.then().body(jsonPath, contains(values));
				break;
			case "isNotEmpty":
				Helper.assertTrue("value: " + jsonPath + " is empty", responseVal != null);
				break;
			case "isEmpty":
				Helper.assertTrue("value: " + jsonPath + " is not empty", responseVal == null);
				break;
			default:
				break;
			}
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
		if (jsonHelper.isJSONValid(expectedJson)) {
			TestLog.logPass("expected: " + Helper.stringRemoveLines(expectedJson));
			String body = response.getBody().asString();
			try {
				JSONAssert.assertEquals(expectedJson, body, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * validates json response against hamcrest keywords
	 * 
	 * @param expectedJson
	 * @param response
	 */
	public static void validateByKeywords(String expectedJson, Response response) {
		expectedJson = Helper.stringNormalize(expectedJson);
		if (!jsonHelper.isJSONValid(expectedJson)) {
			if (expectedJson.startsWith("_VERIFY_JSON_PART_")) {
				// get hashmap of json path and verification
				List<KeyValue> keywords = dataHelper.getValidationMap(expectedJson);
				// validate based on keywords
				jsonHelper.validateJsonKeywords(keywords, response);

				// response is not empty
			} else if (expectedJson.startsWith("_NOT_EMPTY_")) {
				Helper.assertTrue("response is empty", response != null);
				response.then().body("isEmpty()", Matchers.is(false));
			}
		}
	}

}
