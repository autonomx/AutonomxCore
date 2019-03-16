package core.apiCore;

import core.apiCore.interfaces.AzureInterface;
import core.apiCore.interfaces.RestApiInterface;
import core.apiCore.interfaces.SqlInterface;
import core.apiCore.interfaces.TestPrepare;
import core.helpers.Helper;
import core.support.objects.ApiObject;
import core.uiCore.drivers.AbstractDriverTestNG;

public class ServiceRunner {
	public static final String SERVICE_TEST_RUNNER_ID = "ServiceTestRunner"; // matches the name of the service test runner class
	private static final String RESTFULL_API_INTERFACE = "RESTfulAPI";
	private static final String SQL_DB_INTERFACE = "SQLDB";

	private static final String AZURE_INTERFACE = "AZURE";
	private static final String TEST_PREPARE_INTERFACE = "TestPrepare";

	public static void TestRunner(ApiObject apiObject) throws Exception {

		// setup api driver
		new AbstractDriverTestNG().setupApiDriver(apiObject);

		switch (apiObject.InterfaceType) {
		case RESTFULL_API_INTERFACE:
			RestApiInterface.RestfullApiInterface(apiObject);
			break;
		case SQL_DB_INTERFACE:
			SqlInterface.DataBaseInterface(apiObject);
			break;
		case AZURE_INTERFACE:
			AzureInterface.AzureClientInterface(apiObject);
			break;
		case TEST_PREPARE_INTERFACE:
			TestPrepare.TestPrepareInterface(apiObject);
			break;
		default:
			Helper.assertFalse("no interface found: " + apiObject.InterfaceType);
			break;
		}
	}
}