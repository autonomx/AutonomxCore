package core.support.objects;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

public class DatabaseObject {

	public enum driverOptions {
		driver, url, name, username, password
	}

	public Connection connection = null;
	public String driver = StringUtils.EMPTY;
	public String url = StringUtils.EMPTY;
	public String databaseName = StringUtils.EMPTY;
	public String username = StringUtils.EMPTY;
	public String password = StringUtils.EMPTY;

	public static Map<Integer, DatabaseObject> DATABASES = new ConcurrentHashMap<Integer, DatabaseObject>();

	public DatabaseObject withConnection(Connection connection) {
		this.connection = connection;
		return this;
	}

	public DatabaseObject withDriver(String driver) {
		this.driver = driver;
		return this;
	}

	public DatabaseObject withUrl(String url) {
		this.url = url;
		return this;
	}

	public DatabaseObject withDatabaseName(String databaseName) {
		this.databaseName = databaseName;
		return this;
	}

	public DatabaseObject withUsername(String username) {
		this.username = username;
		return this;
	}

	public DatabaseObject withPassword(String password) {
		this.password = password;
		return this;
	}

	public Connection getConnection() {
		return this.connection;
	}

	public String getDriver() {
		return this.driver;
	}

	public String getUrl() {
		return this.url;
	}

	public String getDatabaseName() {
		return this.databaseName;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}
}