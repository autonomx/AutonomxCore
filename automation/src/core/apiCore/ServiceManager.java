package core.apiCore;

import core.apiCore.interfaces.Authentication;
import core.apiCore.interfaces.AzureInterface;
import core.apiCore.interfaces.RabbitMqInterface;
import core.apiCore.interfaces.RestApiInterface;
import core.apiCore.interfaces.SqlInterface;
import core.apiCore.interfaces.TestPrepare;
import core.helpers.Helper;
import core.support.objects.ServiceObject;
import core.uiCore.drivers.AbstractDriverTestNG;

public class ServiceManager {
	private static final String TOKEN_GENERATOR = "Authentication";
	public static final String SERVICE_TEST_RUNNER_ID = "ServiceTestRunner"; // matches the name of the service test runner class
	private static final String RESTFULL_API_INTERFACE = "RESTfulAPI";
	private static final String SQL_DB_INTERFACE = "SQLDB";

	private static final String AZURE_INTERFACE = "AZURE";
	private static final String RABBIT_MQ_INTERFACE = "RABBITMQ";

	private static final String TEST_PREPARE_INTERFACE = "TestPrepare";

	public static void TestRunner(ServiceObject apiObject) throws Exception {

		// setup api driver
		new AbstractDriverTestNG().setupApiDriver(apiObject);
		runInterface(apiObject);
	}
	
	public static void runInterface(ServiceObject serviceObject) throws Exception {
		switch (serviceObject.getInterfaceType()) {
		case TOKEN_GENERATOR:
			Authentication.tokenGenerator(serviceObject);
			break;
		case RESTFULL_API_INTERFACE:
			RestApiInterface.RestfullApiInterface(serviceObject);
			break;
		case SQL_DB_INTERFACE:
			SqlInterface.DataBaseInterface(serviceObject);
			break;
		case AZURE_INTERFACE:
			AzureInterface.AzureClientInterface(serviceObject);
			break;
		case RABBIT_MQ_INTERFACE:
			RabbitMqInterface.testRabbitMqInterface(serviceObject);
			break;
		case TEST_PREPARE_INTERFACE:
			TestPrepare.TestPrepareInterface(serviceObject);
			break;
		default:
			Helper.assertFalse("no interface found: " + serviceObject.getInterfaceType() + ". Options:"
					+ "Authentication, RESTfulAPI, SQLDB, RABBITMQ");
			break;
		}
	}
}