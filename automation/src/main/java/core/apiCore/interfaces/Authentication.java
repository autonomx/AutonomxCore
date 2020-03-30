package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import core.apiCore.helpers.DataHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;

public class Authentication {

	public static final String BASIC_AUTHORIZATION = "BASIC";
	public static final String NTLM_AUTHORIZATION = "NTLM";
	public static final String AUTHENTICATION_DISABLE = "authentication.disabled";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	public static final String AUTHENTICATION = "auth";

	/**
	 * interface for restful api calls
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static void authenticator(ServiceObject serviceObject) {

		if (serviceObject == null)
			Helper.assertFalse("apiobject is null");

		// if authentication is disabled, return
		if (Config.getBooleanValue(AUTHENTICATION_DISABLE))
			return;

		// set timeout from api config
		RestApiInterface.setTimeout();

		// set proxy from config file
		RestApiInterface.setProxy();

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// set base uri
		setURI(serviceObject);

		// send request And receive a response
		evaluateRequest(serviceObject);
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

		RestAssured.baseURI = aURL.getProtocol() + "://" + aURL.getHost();
		RestAssured.port = aURL.getPort();
		RestAssured.basePath = aURL.getPath();
	}

	private static RequestSpecification evaluateRequest(ServiceObject serviceObject) {
		// set content type
		RequestSpecification request = null;

		// evaluate options
		evaluateOption(serviceObject);

		Map<String, String> parameterMap = getParameters(serviceObject);

		TestLog.logPass("authentication type: " + Helper.stringRemoveLines(serviceObject.getOption()));

		switch (serviceObject.getOption()) {
		case BASIC_AUTHORIZATION:
			String username = parameterMap.get("username");
			String password = parameterMap.get("password");
			List<String> credentials = new ArrayList<String>();
			credentials.add(username);
			credentials.add(password);

			// store basic request in config
			saveOutboundParameter(serviceObject, credentials);
			break;

		case NTLM_AUTHORIZATION:
			username = parameterMap.get("username");
			password = parameterMap.get("password");
			String workstation = parameterMap.get("workstation");
			String domain = parameterMap.get("domain");

			credentials = new ArrayList<String>();
			credentials.add(username);
			credentials.add(password);
			credentials.add(workstation);
			credentials.add(domain);

			// store basic request in config
			saveOutboundParameter(serviceObject, credentials);
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
			Helper.assertFalse("Correct authentication type not set. selected: <" + serviceObject.getMethod()
					+ "> Available options: BASIC");
			break;
		}
		return request;
	}

	private static Map<String, String> getParameters(ServiceObject serviceObject) {

		Map<String, String> parameterMap = new HashMap<String, String>();

		if (serviceObject.getRequestBody().isEmpty())
			return parameterMap;

		String[] formData = serviceObject.getRequestBody().split(",");
		for (String data : formData) {
			String[] keyValue = data.split(":");
			parameterMap.put(keyValue[0].trim(), keyValue[1].trim());
		}

		return parameterMap;
	}

	/**
	 * sets the header, content type And body based on specifications
	 * 
	 * @param serviceObject
	 * @return
	 */
	private static void evaluateOption(ServiceObject serviceObject) {

		// reset validation timeout. will be overwritten by option value if set
		resetOptions();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}
		
		// store value to config directly using format: value:<$key> separated by colon ';'
		DataHelper.saveDataToConfig(serviceObject.getOption());

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {
			// if additional options
			switch (keyword.key) {
				default:
					break;
				}
			}
		KeyValue.printKeyValue(keywords, "option");
	}
	
	private static void resetOptions() {
		// reset authorization token tracker
		Config.putValue(AUTHORIZATION_HEADER, "", false);
	}

	/**
	 * save authorization object in user defined variables with format:
	 * AUTH:<$variable> Authorization is stored in variable
	 * 
	 * @param serviceObject
	 * @param authorization
	 */
	@SuppressWarnings("unchecked")
	private static void saveOutboundParameter(ServiceObject serviceObject, Object authorization) {
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOutputParams());

		// fail if incorrect format for AUTH:<$variable>
		if (keywords.isEmpty())
			Helper.assertFalse(
					"Autehntication value must be stored in a variable. eg. " + AUTHENTICATION + ":<$variable>");
		if (!keywords.get(0).key.equals(AUTHENTICATION))
			Helper.assertFalse("Authentication storing format: " + AUTHENTICATION + ":<$variable>");

		KeyValue keyword = keywords.get(0);
		// fail if value is wrong format
		if (!keyword.value.toString().startsWith("<") || !keyword.value.toString().contains("$")
				|| !keyword.value.toString().endsWith(">"))
			Helper.assertFalse(
					"variable placement must of format path: <$variable>. invalid value: " + keyword.value.toString());

		String key = (String) keyword.value;
		key = key.replace("$", "").replace("<", "").replace(">", "").trim();
		Config.putValue(key, authorization, false);
		
		ArrayList<String> request = (ArrayList<String>) authorization;
		TestLog.logPass("output parameter: " + key + " value: " + Arrays.toString(request.toArray()));
	}
}