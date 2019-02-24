package core.apiCore;

import core.apiCore.interfaces.azureInterface;
import core.apiCore.interfaces.restApiInterface;
import core.apiCore.interfaces.sqlInterface;
import core.apiCore.interfaces.testPrepare;
import core.helpers.Helper;
import core.support.objects.ApiObject;
import core.uiCore.drivers.AbstractDriverTestNG;

public class apiRunner {
	private static final String RESTFULL_API_INTERFACE = "RESTfulAPI";
	private static final String SQL_DB_INTERFACE = "SQLDB";

	private static final String AZURE_INTERFACE = "AZURE";
	private static final String TEST_PREPARE_INTERFACE = "TestPrepare";

	public static void TestRunner(ApiObject apiObject) throws Exception {

		// setup api driver
		new AbstractDriverTestNG().setupApiDriver(apiObject);

		switch (apiObject.InterfaceType) {
		case RESTFULL_API_INTERFACE:
			restApiInterface.RestfullApiInterface(apiObject);
			break;
		case SQL_DB_INTERFACE:
			sqlInterface.DataBaseInterface(apiObject);
			break;
		case AZURE_INTERFACE:
			azureInterface.AzureInterface(apiObject);
			break;
		case TEST_PREPARE_INTERFACE:
			testPrepare.TestPrepareInterface(apiObject);
			break;
		default:
			Helper.assertFalse("no interface found: " + apiObject.InterfaceType);
			break;
		}
	}
}