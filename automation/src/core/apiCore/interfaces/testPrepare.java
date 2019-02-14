package core.apiCore.interfaces;

import core.helpers.Helper;
import core.support.objects.ApiObject;

public class testPrepare {
	private static final String WAIT_FOR_SECONDS = "waitForSeconds";

	/**
	 * /* (String TestSuite, String TestCaseID, String RunFlag, String Description,
	 * String InterfaceType, String UriPath, String ContentType, String Method,
	 * String Option, String RequestHeaders, String TemplateFile, String
	 * RequestBody, String OutputParams, String RespCodeExp, String
	 * ExpectedResponse, String ExpectedResponse, String NotExpectedResponse,
	 * String TcComments, String tcName, String tcIndex)
	 *
	 * interface for azure storage api calls
	 * 
	 * @param apiObject
	 * @return
	 * @throws Exception
	 */
	public static void TestPrepareInterface(ApiObject apiObject) throws Exception {
		switch (apiObject.Method) {
		case WAIT_FOR_SECONDS:
			waitForSeconds(apiObject);
			break;
		default:
			Helper.assertTrue("method not selected", false);
			break;
		}
	}

	public static void waitForSeconds(ApiObject apiObject) throws Exception {
		int seconds = Integer.valueOf(apiObject.Option);
		Helper.wait.waitForSeconds(seconds);
	}

}
