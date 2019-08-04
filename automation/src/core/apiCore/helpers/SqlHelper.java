package core.apiCore.helpers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class SqlHelper {

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

		JsonHelper.configMapJsonKeyValues(response, outputParam);
	}

	/**
	 * replaces output parameter with response values eg. $token with id form
	 * response eg. ASSET:1:<$asset_id_selected> -> column:row:variable
	 * 
	 * @param response
	 * @param outputParam
	 * @throws Exception
	 */
	public static void saveOutboundSQLParameters(ResultSet resSet, String outputParam) throws Exception {
		configMapSqlKeyValues(resSet, outputParam);
	}

	/**
	 * map key value to config eg.features.features.id:1:<$id>
	 * 
	 * @param response
	 * @param keyValue
	 * @throws SQLException
	 * @throws NumberFormatException
	 */
	public static void configMapSqlKeyValues(ResultSet resSet, String keyValue) throws Exception {

		if (keyValue.isEmpty())
			return;

		// set random value based on database max number of rows. 0...max-row-count
		keyValue = setRandomRowValue(resSet, keyValue);

		// replace parameters for outputParam
		keyValue = DataHelper.replaceParameters(keyValue);

		List<KeyValue> keywords = DataHelper.getValidationMap(keyValue);
		for (KeyValue keyword : keywords) {
			String key = (String) keyword.value;
			key = key.replace("$", "").replace("<", "").replace(">", "").trim();
			String value = "";

			// eg. NAME:1:<$name> : if row available, get value of column at row
			// eg. NAME:<$name> : if row not available, gets all values of rows from column
			if (keyword.position.isEmpty()) {
				value = getAllValuesInColumn(resSet, keyword.key);
			} else {
				resSet.absolute(Integer.valueOf(keyword.position));
				value = resSet.getString(keyword.key);
			}

			if (!keyword.position.isEmpty()) {
				value = value.split(",")[Integer.valueOf(keyword.position) - 1];
			}
			Config.putValue(key, value);
			TestLog.logPass("replacing value " + key + " with: " + value);
		}
	}

	private static String getAllValuesInColumn(ResultSet resSet, String column) throws SQLException {
		resSet.beforeFirst();
		List<String> results = new ArrayList<String>();
		while (resSet.next()) {
			results.add(resSet.getString(column));
		}
		return String.join(",", results);
	}

	/**
	 * replaces RAND_DatabaseMaxRows variable with random number
	 * 
	 * @param resSet
	 * @param outputParam
	 * @return
	 * @throws SQLException
	 */
	public static String setRandomRowValue(ResultSet resSet, String outputParam) throws SQLException {
		if (outputParam.isEmpty())
			return outputParam;

		// set random value based on database max number of rows. 0...max-row-count
		if (outputParam.contains("<@RAND_DatabaseMaxRows>")) {
			int maxRowCount = getMaxResultRowCount(resSet);
			int row = Helper.generateRandomNumber(1, maxRowCount);
			outputParam = outputParam.replace("<@RAND_DatabaseMaxRows>", String.valueOf(row));
		}
		return outputParam;
	}

	/**
	 * gets the number of results from ResultSet
	 * 
	 * @param resSet
	 * @return
	 * @throws SQLException
	 */
	private static int getMaxResultRowCount(ResultSet resSet) throws SQLException {
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
			String expectedValue = Helper.stringNormalize((String) keyword.value);
			String responseString = "";
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

			// if no position specified, Then set row to 1, else row = position
			if (position.isEmpty()) {
				responseString = getAllValuesInColumn(resSet, keyword.key);
			} else {
				resSet.absolute(Integer.valueOf(position));
				responseString = Helper.stringNormalize(resSet.getString(key));
			}

			// validate response
			DataHelper.validateCommand(command, responseString, expectedValue);
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
		if (SqlHelper.isValidJson(expectedJson)) {
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
	 * is valid json based on key:value string splits the string by ":" Then
	 * validates if "value" is valid json
	 * 
	 * @param expected
	 * @return
	 */
	public static boolean isValidJson(String expected) {
		String value = StringUtils.substringAfter(expected, ":");
		return JsonHelper.isJSONValid(value);
	}
}
