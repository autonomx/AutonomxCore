package core.apiCore.interfaces;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import core.apiCore.helpers.connectionHelper;
import core.apiCore.helpers.dataHelper;
import core.apiCore.helpers.sqlHelper;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ApiObject;
import core.support.objects.KeyValue;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

public class sqlInterface {

	private static final String SQL_JDBC_DRIVER = "DBDRIVER";
	private static final String SQL_DB_URL = "DBUrl";
	private static final String SQL_DB_NAME = "DBName";
	private static final String SQL_DB_USERNAME = "DBUsername";
	private static final String SQL_DB_PASSWORD = "DBPassword";

	public static Connection conn = null;

	/**
	 * /* (String TestSuite, String TestCaseID, String RunFlag, String Description,
	 * String InterfaceType, String UriPath, String ContentType, String Method,
	 * String Option, String RequestHeaders, String TemplateFile, String
	 * RequestBody, String OutputParams, String RespCodeExp, String
	 * ExpectedResponse, String PartialExpectedResponse, String NotExpectedResponse,
	 * String TcComments, String tcName, String tcIndex)
	 *
	 * interface for database api calls
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void DataBaseInterface(ApiObject apiObject) throws Exception {
		// connect to db
		connectDb();
		// evaluate the sql query
		ResultSet resSet = evaluateDbQuery(apiObject);

		// evaluate the response
		evaluateReponse(apiObject, resSet);
	}

	/**
	 * 
	 * @throws Exception
	 */
	public synchronized static void connectDb() {
		if (conn == null) {
			try {
				
				// connect through ssh if set in api config
				connectionHelper.sshConnect();
				
				// Register JDBC driver
				String SQLDriver = Config.getValue(SQL_JDBC_DRIVER);
				Class.forName(SQLDriver);

				// connect to db
				String dbURL = Config.getValue(SQL_DB_URL);
				String dbName = Config.getValue(SQL_DB_NAME);

				// set database connection info
				String connectionString = dbURL + "/" + dbName;
				String dbUserName = Config.getValue(SQL_DB_USERNAME);
				String dbPassword = Config.getValue(SQL_DB_PASSWORD);
				TestLog.logPass("db connection: " + connectionString);
				TestLog.logPass("db username: " + dbUserName);
				TestLog.logPass("db password: " + dbPassword);

				// conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb",
				// "postgres", "123");
				conn = DriverManager.getConnection(connectionString, dbUserName, dbPassword);
				Helper.wait.waitForSeconds(1);
			} catch (Exception e) {
				TestLog.logPass("sql connection failed: " + e.getMessage());
				Helper.assertTrue("sql connection failed", false);
			}
		}
	}

	/**
	 * evaluaes the sql statement
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static ResultSet evaluateDbQuery(ApiObject apiObject) throws Exception {

		// replace parameters for request body
		apiObject.RequestBody = dataHelper.replaceParameters(apiObject.RequestBody);

		// execute query
		String sql = apiObject.RequestBody;
		TestLog.logPass("sql statement: " + sql);

		PreparedStatement sqlStmt = conn.prepareStatement(sql);
		sqlStmt = conn.prepareStatement(sql);

		// execute and wait for response if expected values are set
		ResultSet resSet = executeAndWaitForDbResponse(sqlStmt, apiObject);

		return resSet;
	}

	/**
	 * evaluate the response
	 * 
	 * @param apiObject
	 * @param resSet
	 * @throws SQLException
	 */
	public static void evaluateReponse(ApiObject apiObject, ResultSet resSet) throws SQLException {

		// return if expected response is empty
		if (apiObject.PartialExpectedResponse.isEmpty() && apiObject.OutputParams.isEmpty()
				&& apiObject.ExpectedResponse.isEmpty())
			return;

		// fail test if no results returned
		if (!resSet.isBeforeFirst()) {
			Helper.assertTrue("no results returned from db query", false);
		}

		// get the first result row
		resSet.next();

		// saves response values to config object
		dataHelper.saveOutboundSQLParameters(resSet, apiObject.OutputParams);

		// validate expected response if exists
		validateExpectedResponse(apiObject.ExpectedResponse, resSet);

		// validate partial expected response if exists
		validateExpectedResponse(apiObject.PartialExpectedResponse, resSet);

		// Clean-up environment
		resSet.close();
	}

	/**
	 * executes and waits for response calls the query in each loop does not wait if
	 * expected or partial expected response are empty
	 * 
	 * @param sqlStmt
	 * @param apiObject
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeAndWaitForDbResponse(PreparedStatement sqlStmt, ApiObject apiObject)
			throws SQLException {
		int timeout = CrossPlatformProperties.getGlobalTimeout();
		ResultSet resSet;
		StopWatchHelper watch = StopWatchHelper.start();
		boolean messageReceived = false;
		long passedTimeInSeconds = 0;
		do {
			sqlStmt.execute();
			resSet = sqlStmt.getResultSet();

			// if no response expected, do not wait for response
			if (apiObject.ExpectedResponse.isEmpty() && apiObject.PartialExpectedResponse.isEmpty())
				return resSet;

			if (resSet.isBeforeFirst()) {
				messageReceived = true;
				return resSet;
			}
			Helper.wait.waitForSeconds(1);
			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);
		} while (!messageReceived && passedTimeInSeconds < timeout);

		return resSet;
	}

	public static void validateExpectedResponse(String expected, ResultSet resSet) throws SQLException {
		if (expected.isEmpty())
			return;
		// validate response body against expected string
		expected = dataHelper.replaceParameters(expected);
		TestLog.logPass("expected result: " + Helper.stringRemoveLines(expected));

		// separate the expected response by &&
		String[] criteria = expected.split("&&");
		for (String criterion : criteria) {
			if (sqlHelper.isValidJson(criterion)) {
				sqlHelper.validateByJsonBody(criterion, resSet);
			} else {
				List<KeyValue> keywords = dataHelper.getValidationMap(expected);
				sqlHelper.validateSqlKeywords(keywords, resSet);
			}
		}
	}
}
