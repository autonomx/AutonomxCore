package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.ServiceObject;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class Authentication {

	public static final String BASIC_AUTHORIZATION = "BASIC";


	/**
	 * interface for restful api calls
	 * 
	 * @param apiObject
	 * @return
	 */
	public static void tokenGenerator(ServiceObject apiObject) {

		if (apiObject == null)
			Helper.assertFalse("apiobject is null");

		// set timeout from api config
		RestApiInterface.setTimeout();

		// set proxy from config file
		RestApiInterface.setProxy();

		// replace parameters for request body
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));

		// set base uri
		setURI(apiObject);

		// send request And receive a response
		evaluateRequest(apiObject);
	}

	/**
	 * sets base uri for api call
	 */
	public static void setURI(ServiceObject serviceObject) {
		String url = StringUtils.EMPTY;
		
		// replace place holder values for uri
		serviceObject.withUriPath(DataHelper.replaceParameters(serviceObject.getUriPath()));
		serviceObject.withUriPath(Helper.stringRemoveLines(serviceObject.getUriPath()));
		
		// if uri is full path, Then set base uri as whats provided in csv file
		// else use baseURI from properties as base uri And extend it with csv file uri
		// path
		if (serviceObject.getUriPath().startsWith("http")) {
			url = serviceObject.getUriPath();
		} else {
			url = Helper.stringRemoveLines(Config.getValue("api.uriPath")) + serviceObject.getUriPath();
		}
		// keep track of full URL 
		serviceObject.withUriPath(url);
		
		URL aURL = Helper.convertToUrl(url);
		TestLog.logPass("request URL: " + aURL.toString());
		
		RestAssured.baseURI  = aURL.getProtocol() + "://" + aURL.getHost();
		RestAssured.port = aURL.getPort();
		RestAssured.basePath = aURL.getPath();
	}

	private static RequestSpecification evaluateRequest(ServiceObject apiObject) {
		// set content type
		RequestSpecification request = null;

		// evaluate options
		request = evaluateOption(apiObject, request);
		
		if (apiObject.getRequestBody().isEmpty()) {
			Helper.assertFalse("no request set");
		}

		Map<String, String> parameterMap = getParameters(apiObject);

		TestLog.logPass("authentication type: " + Helper.stringRemoveLines(apiObject.getOption()));

		switch (apiObject.getOption()) {
		case BASIC_AUTHORIZATION:
			String username = parameterMap.get("username");
			String password = parameterMap.get("password");
			List<String> credentials = new ArrayList<String>();
			credentials.add(username);
			credentials.add(password);
			
			// store basic request in config
			Config.putValue(BASIC_AUTHORIZATION, credentials);
			break;
		case "OAUTH2":
			username = parameterMap.get("username");
			password = parameterMap.get("password");
			String clientId = parameterMap.get("cliendId");
			String clientSecret = parameterMap.get("clientSecret");
			String grantType = parameterMap.get("grantType");
			String scope = parameterMap.get("scope");
			String redirectUri = parameterMap.get("redirectUri");

			request = given().auth().preemptive().basic(clientId, clientSecret).formParam("grant_type", grantType)
					.formParam("username", username).formParam("password", password)
					.formParam("redirect_uri", redirectUri).formParam("scope", scope);
			break;
		default:
			Helper.assertFalse("Correct authentication type not set. selected: <" + apiObject.getMethod()
					+ "> Available options: BASIC");
			break;
		}
		return request;
	}

	private static Map<String, String> getParameters(ServiceObject apiObject) {
		Map<String, String> parameterMap = new HashMap<String, String>();

		String[] formData = apiObject.getRequestBody().split(",");
		for (String data : formData) {
			String[] keyValue = data.split(":");
			parameterMap.put(keyValue[0].trim(), keyValue[1].trim());
		}

		return parameterMap;
	}

	/**
	 * sets the header, content type And body based on specifications
	 * 
	 * @param apiObject
	 * @return
	 */
	private static RequestSpecification evaluateOption(ServiceObject apiObject, RequestSpecification request) {

		// if no option specified
		if (apiObject.getOption().isEmpty()) {
			return request;
		}

		// replace parameters for request body
		apiObject.withOption(DataHelper.replaceParameters(apiObject.getOption()));

		// if additional options
		switch (apiObject.getOption()) {
		default:
			break;
		}

		return request;
	}
}
