package core.helpers.databaseHelper;

public class MongoObject {

	public String username;
	public String password;
	public String database;

	public MongoObject withUsername(String username) {
		this.username = username;
		return this;
	}

	public MongoObject withPassword(String password) {
		this.password = password;
		return this;
	}

	public MongoObject withDatabase(String database) {
		this.database = database;
		return this;
	}
}