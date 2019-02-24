package core.support.objects;

import core.apiCore.helpers.csvReader;
import core.apiCore.interfaces.restApiInterface;
import core.support.configReader.Config;

public class apiKeyword {
	
	public static ApiObject getApiDef(String key) {
		csvReader.getAllKeywords();
		return TestObject.getTestInfo().apiMap.get(key);
	}
	
	public void login() {
		Config.putValue("username", "name");
		Config.putValue("password", "pasword");

		ApiObject login = TestObject.getApiDef("getToken");
		/*
		ApiObject login = TestObject.backend.api.getToken()
		.withUserName(user.username().get())
		.withPassword(user.password().get());
		*/
		restApiInterface.RestfullApiInterface(login);
	}
}
