package core.apiCore.interfaces;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import core.apiCore.helpers.ConnectionHelper;
import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.SqlHelper;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.DatabaseObject;
import core.support.objects.DatabaseObject.driverOptions;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import core.uiCore.driverProperties.globalProperties.CrossPlatformProperties;

/**
 * @author ehsan.matean
 *
 */
public class SqlInterface {

	private static final String SQL_PREFIX = "db.";

	public static final String SQL_CURRENT_DATABASE = "db.current.database";

	private static final String OPTION_DATABASE = "database";

	/**
	 *
	 * interface for database calls
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static void DataBaseInterface(ServiceObject serviceObject) throws Exception {

		// get and keep track of all the databases from config
		setDatabaseMap();
		
		// set default database at position 0(no position) or 1(position 1)
		setDefaultDatabase();
		
		// evluate options
		evaluateOption(serviceObject);
		
		// connect to db
		connectDB();

		// evaluate the sql query
		ResultSet resSet = evaluateDbQuery(serviceObject);

		// evaluate the response
		evaluateReponse(serviceObject, resSet);
	}

	/**
	 * connect to database based on the current database
	 */
	public synchronized static void connectDB() {

		DatabaseObject currentDb = (DatabaseObject) Config.getObjectValue(SQL_CURRENT_DATABASE);

		if (currentDb.getConnection() == null) {
			try {

				// connect through ssh if set in api config
				ConnectionHelper.sshConnect();

				// Register JDBC driver
				String SQLDriver = currentDb.getDriver();
				Class.forName(SQLDriver);

				// connect to db
				String dbURL = currentDb.getUrl();
				String dbName = currentDb.getDatabaseName();

				// set database connection info
				String connectionString = dbURL + "/" + dbName;
				String dbUserName = currentDb.getUsername();
				String dbPassword = currentDb.getPassword();
				TestLog.logPass("db connection: " + connectionString);
				TestLog.logPass("db username: " + dbUserName);
				TestLog.logPass("db password: " + dbPassword);

				// conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb",
				// "postgres", "123");
				currentDb.withConnection(DriverManager.getConnection(connectionString, dbUserName, dbPassword));
				Helper.wait.waitForSeconds(1);
			} catch (Exception e) {
				TestLog.logPass("sql connection failed: " + e.getMessage());
				e.printStackTrace();
				Helper.assertTrue("sql connection failed", false);
			}
		}
	}

	public static void evaluateOption(ServiceObject serviceObject) {
		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key.toLowerCase()) {
			case OPTION_DATABASE:
				int position = Helper.getIntFromString(keyword.value.toString(), true);
				if (DatabaseObject.DATABASES.get(position) == null)
					Helper.assertFalse("database number: " + position + " not found");
				DatabaseObject database = DatabaseObject.DATABASES.get(position);
				Config.putValue(SQL_CURRENT_DATABASE, database);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * set database values from config into database object hashmap
	 * format: db.1.driver = org.postgresql.Driver
	 * 1 is position
	 * driver is key (command)
	 * org.postgresql.Driver is value
	 */
	public static void setDatabaseMap() {
		
		// if already set, 
		if(DatabaseObject.DATABASES.size() > 0) return;
		
		// get all keys from config
		Map<String, Object> propertiesMap = TestObject.getTestInfo().config;

		// load config/properties values from entries with "db." prefix
		for (Entry<String, Object> entry : propertiesMap.entrySet()) {

			// if starts with chrome.options or firefox.options prefix
			boolean isDatabase = entry.getKey().toString().startsWith(SQL_PREFIX);

			// format db.position.command = value. eg. db.1.driver = org.postgresql.Driver
			if (isDatabase) {
				String fullKey = entry.getKey().toString();
				int position = Helper.getIntFromString(fullKey);
				
				// if position is not set, set to 0 (default)
				String splitter = SQL_PREFIX + position + ".";
				if(position == -1) {
					position = 0;
					splitter = SQL_PREFIX;
				}
				String[] split = fullKey.split(splitter);
				String command = split[1].trim();
				String value = entry.getValue().toString().trim();

				// set database object in global hashmap
				setDatabaseObject(command, position, value);
			}
		}
	}
	
	/**
	 * set default database to be from position 0 or 1
	 * if db 0 is set, set as default else set db 1
	 * fail if no position 0 or 1 is not set
	 */
	public static void setDefaultDatabase() {
		
		if(DatabaseObject.DATABASES.get(0) != null) {
			Config.putValue(SQL_CURRENT_DATABASE, DatabaseObject.DATABASES.get(0));
		}else if(DatabaseObject.DATABASES.get(1) != null) {
			Config.putValue(SQL_CURRENT_DATABASE, DatabaseObject.DATABASES.get(1));
		}else {
			Helper.assertFalse("database position must be set. eg. db.1.driver = value where 1 is position of database " );
		}
	}

	/**
	 * set database object in hashmap key: position value: database object
	 * 
	 * @param command
	 * @param position
	 * @param value
	 */
	public static void setDatabaseObject(String command, int position, String value) {
		DatabaseObject database = null;
		if (DatabaseObject.DATABASES.get(position) == null)
			database = new DatabaseObject();
		else
			database = DatabaseObject.DATABASES.get(position);

		switch (command) {
		case "driver":
			database.withDriver(value);
			break;
		case "url":
			database.withUrl(value);
			break;
		case "name":
			database.withDatabaseName(value);
			break;
		case "username":
			database.withUsername(value);
			break;
		case "password":
			database.withPassword(value);
			break;
		default:
			Helper.assertFalse("command not set correctly. options: " + Arrays.asList(driverOptions.values()));
		}

		DatabaseObject.DATABASES.put(position, database);
	}

	/**
	 * evaluaes the sql statement
	 * 
	 * @param serviceObject
	 * @return
	 * @throws Exception
	 */
	public static ResultSet evaluateDbQuery(ServiceObject serviceObject) throws Exception {

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// execute query
		String sql = serviceObject.getRequestBody();
		TestLog.logPass("sql statement: " + sql);

		DatabaseObject currentDb = (DatabaseObject) Config.getObjectValue(SQL_CURRENT_DATABASE);
		PreparedStatement sqlStmt = currentDb.getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_UPDATABLE);

		// execute And wait for response if expected values are set
		ResultSet resSet = executeAndWaitForDbResponse(sqlStmt, serviceObject);

		return resSet;
	}

	/**
	 * evaluate the response
	 * 
	 * @param serviceObject
	 * @param resSet
	 * @throws Exception
	 */
	public static void evaluateReponse(ServiceObject serviceObject, ResultSet resSet) throws Exception {

		// return if expected response is empty
		if (serviceObject.getExpectedResponse().isEmpty() && serviceObject.getOutputParams().isEmpty())
			return;

		// fail test if no results returned
		if (!resSet.isBeforeFirst()) {
			Helper.assertTrue("no results returned from db query", false);
		}

		// get the first result row
		resSet.next();

		// saves response values to config object
		SqlHelper.saveOutboundSQLParameters(resSet, serviceObject.getOutputParams());

		// validate partial expected response if exists
		validateExpectedResponse(serviceObject.getExpectedResponse(), resSet);

		// Clean-up environment
		resSet.close();
	}

	/**
	 * executes And waits for response calls the query in each loop does not wait if
	 * expected or partial expected response are empty
	 * 
	 * @param sqlStmt
	 * @param serviceObject
	 * @return
	 * @throws SQLException
	 */
	public static ResultSet executeAndWaitForDbResponse(PreparedStatement sqlStmt, ServiceObject serviceObject)
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
			if (serviceObject.getExpectedResponse().isEmpty())
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
