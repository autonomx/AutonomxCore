package core.apiCore.interfaces;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import core.apiCore.helpers.ConnectionHelper;
import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.SqlHelper;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;


/**
 * @author ehsan.matean
 *
 */
public class SqlInterface {

	private static final String SQL_JDBC_DRIVER = "db.url";
	private static final String SQL_DB_URL = "db.name";
	private static final String SQL_DB_NAME = "db.username";
	private static final String SQL_DB_USERNAME = "DBUsername";
	private static final String SQL_DB_PASSWORD = "db.password";

	public static Connection conn = null;

	/**
	 * /* (String TestSuite, String TestCaseID, String RunFlag, String Description,
	 * String InterfaceType, String UriPath, String ContentType, String Method,
	 * String Option, String RequestHeaders, String TemplateFile, String
	 * RequestBody, String OutputParams, String RespCodeExp, String
	 * ExpectedResponse, String ExpectedResponse, String NotExpectedResponse,
	 * String TcComments, String tcName, String tcIndex)
	 *
	 * interface for database api calls
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void DataBaseInterface(ServiceObject apiObject) throws Exception {
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
				ConnectionHelper.sshConnect();
				
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
	public static ResultSet evaluateDbQuery(ServiceObject apiObject) throws Exception {

		// replace parameters for request body
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));

		// execute query
		String sql = apiObject.getRequestBody();
		TestLog.logPass("sql statement: " + sql);

		PreparedStatement sqlStmt = conn.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_UPDATABLE);

		// execute And wait for response if expected values are set
		ResultSet resSet = executeAndWaitForDbResponse(sqlStmt, apiObject);

		return resSet;
	}

	/**
	 * evaluate the response
	 * 
	 * @param apiObject
	 * @param resSet
	 * @throws Exception 
	 */
	public static void evaluateReponse(ServiceObject apiObject, ResultSet resSet) throws Exception {

		// return if expected response is empty
		if (apiObject.getExpectedResponse().isEmpty() && apiObject.getOutputParams().isEmpty())
			return;

		// fail test if no results returned
		if (!resSet.isBeforeFirst()) {
			Helper.assertTrue("no results returned from db query", false);
		}

		// get the first result row
		resSet.next();

		// saves response values to config object
		SqlHelper.saveOutboundSQLParameters(resSet, apiObject.getOutputParams());

		// validate partial expected response if exists
		validateExpectedResponse(apiObject.getExpectedResponse(), resSet);

		// Clean-up environment
		resSet.close();
	}

	/**
	 * executes And waits for response calls the query in each loop does not wait if
	 * expected or partial expected response are empty
	 * 
	 * @param sqlStmt
	 * @param apiObject
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeAndWaitForDbResponse(PreparedStatement sqlStmt, ServiceObject apiObject)
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
			if (apiObject.getExpectedResponse().isEmpty())
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
		expected = DataHelper.replaceParameters(expected);
		TestLog.logPass("expected result: " + Helper.stringRemoveLines(expected));

		// separate the expected response by &&
		String[] criteria = expected.split("&&");
		for (String criterion : criteria) {
			if (SqlHelper.isValidJson(criterion)) {
				SqlHelper.validateByJsonBody(criterion, resSet);
			} else {
				List<KeyValue> keywords = DataHelper.getValidationMap(expected);
				SqlHelper.validateSqlKeywords(keywords, resSet);
			}
		}
	}
}