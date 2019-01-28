package core.helpers.databaseHelper;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDbHelper {

	/**
	 * assert true
	 * 
	 * @param message
	 * @param value
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public static void deleteData(MongoObject mongo) throws Exception {
		/*
		 * MongoCredential credential = MongoCredential.createCredential(mongo.username,
		 * mongo.database, mongo.password); MongoClient mongoClient = new
		 * MongoClient(new ServerAddress("https://mongodev2.gaialandscape.com"),
		 * Arrays.asList(credential));
		 * 
		 * 
		 * URL url = new URL("ftp://" + URLEncoder.encode(mongo.username, "UTF-8") + ":"
		 * + URLEncoder.encode(mongo.password, "UTF-8") + "@" +
		 * "https://mongodev2.gaialandscape.com/?authSource=db1");
		 * 
		 * MongoClientURI uri = new MongoClientURI("mongodb://Fortify:" +
		 * URLEncoder.encode(mongo.password, "UTF-8") +
		 * "@https://mongodev2.gaialandscape.com/?authSource=db1"); MongoClient
		 * mongoClient = new MongoClient(uri);
		 */

		List<ServerAddress> seeds = new ArrayList<ServerAddress>();
		seeds.add(new ServerAddress("localhost"));
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		credentials.add(
				MongoCredential.createMongoCRCredential(mongo.username, mongo.database, mongo.password.toCharArray()));
		MongoClient mongoClient = new MongoClient(seeds, credentials);

		@SuppressWarnings("deprecation")
		DB db = mongoClient.getDB(mongo.database);

		DBCollection collection = db.getCollection("Person");

		BasicDBObject query = new BasicDBObject();
		query.put("name", Pattern.compile(Pattern.quote("zzz_")));

		collection.remove(query);

		mongoClient.close();
	}
}