package core.support.objects;

import core.apiCore.helpers.CsvReader;
import core.apiCore.interfaces.RestApiInterface;
import core.support.configReader.Config;

public class ApiKeyword {
	
	public static ApiObject getApiDef(String key) {
		CsvReader.getAllKeywords();
		return TestObject.getTestInfo().apiMap.get(key);
	}
	
	public void login() {
		Config.putValue("username", "name");
		Config.putValue("password", "pasword");

		ApiObject login = TestObject.getApiDef("getToken");
		/*
		ApiObject login = Backend.getToken()
		.withUserName(user.username().get())
		.withPassword(user.password().get());
		*/
		RestApiInterface.RestfullApiInterface(login);
	}
}
