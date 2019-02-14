package core.apiCore.helpers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import io.restassured.response.Response;

public class sqlHelper {

	/**
	 * replaces output parameter with response values eg. $token with id form
	 * response
	 * 
	 * @param response
	 * @param outputParam
	 */
	public static void saveOutboundJsonParameters(Response response, String outputParam) {
		if (response == null || outputParam.isEmpty())
			return;

		jsonHelper.configMapJsonKeyValues(response, outputParam);
	}
	
	/** replaces output parameter with response values eg. $token with id form
	 * response
	 * eg. ASSET:1:<$asset_id_selected> -> column:row:variable
	 * @param response
	 * @param outputParam
	 * @throws SQLException
	 */
	public static void saveOutboundSQLParameters(ResultSet resSet, String outputParam) throws SQLException {
		if (outputParam.isEmpty())
			return;

		// set random value based on database max number of rows. 0...max-row-count
		outputParam = setRandomRowValue(resSet, outputParam);

		// replace parameters for outputParam
		outputParam = dataHelper.replaceParameters(outputParam);

		String[] keyVals = outputParam.split(";");
		for (String keyVal : keyVals) {
			String[] parts = keyVal.split(":", 3);
			// eg. ASSET:1:<$asset_id_selected> -> column:row:variable
			String key = parts[2].replace("$", "").replace("<", "").replace(">", "");
			int row = Integer.valueOf(parts[1]);
			resSet.absolute(row);
			String value = resSet.getString(parts[0]);
			Config.putValue(key, value);
			TestLog.logPass("replacing value: " + key + " with: " + value);
		}
	}
	
	/**
	 * replaces RAND_DatabaseMaxRows variable with random number
	 * @param resSet
	 * @param outputParam
	 * @return
	 * @throws SQLException
	 */
	public static String setRandomRowValue(ResultSet resSet, String outputParam) throws SQLException{
		if (outputParam.isEmpty()) return outputParam;

		// set random value based on database max number of rows. 0...max-row-count
		if(outputParam.contains("<@RAND_DatabaseMaxRows>"))	{
			int maxRowCount = getMaxResultRowCount(resSet);
			int row = Helper.generateRandomNumber(1, maxRowCount);
			outputParam = outputParam.replace("<@RAND_DatabaseMaxRows>", String.valueOf(row));
		}
		return outputParam;
	}
	
	/**
	 * gets the number of results from ResultSet
	 * @param resSet
	 * @return
	 * @throws SQLException 
	 */
	private static int getMaxResultRowCount(ResultSet resSet) throws SQLException{
		resSet.last();
		int size = resSet.getRow();
		resSet.beforeFirst();
		return size;
	}

	/**
	 * * validates the maps agains the keyword requirements matcher. examples:
	 * "title": equalTo("Administrator"), "name": isNotEmpty, "name":
	 * contains("admin")
	 * 
	 * @param keywords
	 * @param response
	 * @throws SQLException
	 */
	public static void validateSqlKeywords(List<KeyValue> keywords, ResultSet resSet) throws SQLException {
		for (KeyValue keyword : keywords) {
			String key = Helper.stringNormalize(keyword.key);
			String position = Helper.stringNormalize(keyword.position);
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
			
			// if no position specified, then set row to 1, else row = position
			if(position.isEmpty()) 
				resSet.absolute(Integer.valueOf(1));
			else
				resSet.absolute(Integer.valueOf(position));
			
			String response = Helper.stringNormalize(resSet.getString(key));

			switch (command) {
			case "equalTo":
				Helper.assertEquals(value, response);
				break;
			case "contains":
				Helper.assertContains(response, value);
				break;
			case "isNotEmpty":
				TestLog.logPass("validating if response for key: " + key + " is not empty");
				Helper.assertTrue("key: " + key + "is empty", !response.isEmpty());
				break;
			case "isEmpty":
				TestLog.logPass("validating if response for key:" + key + " is empty");
				Helper.assertTrue("key: " + key + "is not empty", response.isEmpty());
				break;
			default:
				break;
			}
		}
	}

	/**
	 * validates expected json string against json body from response
	 * 
	 * @param expectedJson
	 * @param actualJson
	 * @throws SQLException
	 */
	public static void validateByJsonBody(String expectedJson, ResultSet resSet) throws SQLException {
		if (sqlHelper.isValidJson(expectedJson)) {
			TestLog.logPass("expected: " + Helper.stringRemoveLines(expectedJson));
			String key = StringUtils.substringBefore(expectedJson, ":");
			String json = StringUtils.substringAfter(expectedJson, ":");
			try {
				String responseBody = resSet.getString(key);
				JSONAssert.assertEquals(json, responseBody, JSONCompareMode.LENIENT);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * is valid json based on key:value string splits the string by ":" then
	 * validates if "value" is valid json
	 * 
	 * @param expected
	 * @return
	 */
	public static boolean isValidJson(String expected) {
		String value = StringUtils.substringAfter(expected, ":");
		return jsonHelper.isJSONValid(value);
	}
}
