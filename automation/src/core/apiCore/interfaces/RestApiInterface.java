package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import core.apiCore.ServiceManager;
import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.JsonHelper;
import core.helpers.Helper;
import core.helpers.StopWatchHelper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import core.support.objects.TestObject;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class RestApiInterface {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String API_TIMEOUT_VALIDATION_ENABLED = "api.timeout.validation.isEnabled";
	public static final String API_TIMEOUT_VALIDATION_SECONDS = "api.timeout.validation.seconds";

	private static final String INVALID_TOKEN = "INVALID_TOKEN";
	private static final String NO_TOKEN = "NO_TOKEN";

	private static final String OPTION_NO_VALIDATION_TIMEOUT = "NO_VALIDATION_TIMEOUT";
	private static final String OPTION_WAIT_FOR_RESPONSE = "WAIT_FOR_RESPONSE";

	/**
	 * interface for restful API calls
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static Response RestfullApiInterface(ServiceObject serviceObject) {

		if (serviceObject == null)
			Helper.assertFalse("service object is null");

		// set timeout from api config
		setTimeout();

		// set proxy from api config
		setProxy();

		// set base uri
		RequestSpecification request = setURI(serviceObject);

		// set options
		request = evaluateOption(serviceObject, request);

		// send request and evaluate response
		Response response = evaluate(serviceObject, request);

		return response;
	}

	/**
	 * run and evaluate the api request rerun based on service retry count. default
	 * 1 will not fail test until all iterations are complete
	 * 
	 * @param serviceObject
	 * @param request
	 * @return
	 */
	public static Response evaluate(ServiceObject serviceObject, RequestSpecification request) {

		Response response = null;

		// replace parameters for request body, including template file (json, xml, or
		// other)
		serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

		// send request and evaluate response
		response = evaluateRequestAndValidateResponse(serviceObject, request);

		return response;
	}

	/**
	 * evaluate request and validate response retry until validation timeout period
	 * in seconds
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static Response evaluateRequestAndValidateResponse(ServiceObject serviceObject,
			RequestSpecification request) {
		List<String> errorMessages = new ArrayList<String>();
		Response response = null;

		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;

		boolean isValidationTimeout = Config.getBooleanValue(API_TIMEOUT_VALIDATION_ENABLED);
		int maxRetrySeconds = Config.getIntValue(API_TIMEOUT_VALIDATION_SECONDS);
		int currentRetryCount = 0;

		do {
			// send request And receive a response
			response = evaluateRequest(serviceObject, request);

			// validate the response
			errorMessages = validateResponse(response, serviceObject);

			// if validation timeout is not enabled, break out of the loop
			if (!isValidationTimeout)
				break;

			if (currentRetryCount > 0) {
				Helper.waitForSeconds(3);
				String errors = StringUtils.join(errorMessages, "\n error: ");
				TestLog.ConsoleLog("attempt failed with message: " + errors);
				TestLog.ConsoleLog("attempt #" + (currentRetryCount + 1));

			}
			currentRetryCount++;

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

		} while (!errorMessages.isEmpty() && passedTimeInSeconds < maxRetrySeconds);

		// log results
		if (!errorMessages.isEmpty()) {
			ServiceManager.logResults(errorMessages, passedTimeInSeconds);
		}
		return response;
	}


	/**
	 * sets base uri for api call
	 * 
	 * @return
	 */
	public static RequestSpecification setURI(ServiceObject serviceObject) {
		String url = StringUtils.EMPTY;

		// set request
		RequestSpecification request = given();

		// replace place holder values for URI
		serviceObject.withUriPath(DataHelper.replaceParameters(serviceObject.getUriPath()));
		serviceObject.withUriPath(Helper.stringRemoveLines(serviceObject.getUriPath()));

		// if URI is full path, Then set base URI as what's provided in CSV file
		// else use baseURI from properties as base URI And extend it with CSV file URI
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

		request.baseUri(aURL.getProtocol() + "://" + aURL.getHost());
		request.port(aURL.getPort());
		request.basePath(aURL.getPath());

		return request;
	}

	/**
	 * set connection timeout in milliseconds
	 */
	public static void setTimeout() {
		int connectTimeout = Config.getIntValue("api.timeout.connect.seconds");
		if (connectTimeout == -1)
			return;

		RestAssured.config = RestAssuredConfig.config().httpClient(
				HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", connectTimeout * 1000)
						.setParam("http.socket.timeout", connectTimeout * 1000)
						.setParam("http.connection-manager.timeout", connectTimeout * 1000));

	}

	/**
	 * set proxy from config file
	 */
	public static void setProxy() {
		String host = Config.getValue("api.proxy.host");
		String port = Config.getValue("api.proxy.port");

		if (host.isEmpty())
			return;

		RestAssured.proxy(host);
		if (!port.isEmpty())
			RestAssured.proxy(port);
	}

	public static List<String> validateResponse(Response response, ServiceObject serviceObject) {

		List<String> errorMessages = new ArrayList<String>();

		// fail test if no response is returned
		if (response == null) {
			errorMessages.add("no response returned");
			return errorMessages;
		}

		// saves response values to config object
		JsonHelper.saveOutboundJsonParameters(response, serviceObject.getOutputParams());

		// validate status code
		if (!serviceObject.getRespCodeExp().isEmpty()) {
			String message = "expected status code: " + serviceObject.getRespCodeExp() + " response status code: "
					+ response.getStatusCode();
			TestLog.logPass(message);
			if (response.getStatusCode() != Integer.valueOf(serviceObject.getRespCodeExp())) {
				errorMessages.add(message);
				return errorMessages;
			}
		}

		// get response values and validate
		String responseString = JsonHelper.getResponseValue(response);
		List<String> responses = new ArrayList<String>();
		responses.add(responseString);
		errorMessages = DataHelper.validateExpectedValues(responses, serviceObject.getExpectedResponse());

		// remove all empty response strings
		errorMessages = DataHelper.removeEmptyElements(errorMessages);
		return errorMessages;
	}

	/**
	 * sets the header, content type And body based on specifications Headers are
	 * based on key value, separated by ";" Invalid token: if authorization token
	 * exists, replace last values with "invalid", else set to "invalid"
	 * 
	 * @param serviceObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static RequestSpecification evaluateRequestHeaders(ServiceObject serviceObject,
			RequestSpecification request) {

		// if no RequestHeaders specified
		if (serviceObject.getRequestHeaders().isEmpty()) {
			return request;
		}

		// replace parameters for request body
		serviceObject.withRequestHeaders(DataHelper.replaceParameters(serviceObject.getRequestHeaders()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestHeaders());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional request headers
			switch (keyword.key) {
			case Authentication.BASIC_AUTHORIZATION:
				ArrayList<String> basicRequest = (ArrayList<String>) Config
						.getObjectValue(Authentication.BASIC_AUTHORIZATION);
				if (basicRequest.size() == 0)
					Helper.assertFalse("basic request info not found: " + Arrays.toString(basicRequest.toArray()));
				request = request.auth().basic(basicRequest.get(0), basicRequest.get(1));
				break;
			case INVALID_TOKEN:
				String authValue = Config.getValue(AUTHORIZATION_HEADER);

				// replace authorization token with random string of equal value to the token
				if (!authValue.isEmpty()) {
					authValue = Helper.generateRandomString(authValue.length());
					request = request.given().header(AUTHORIZATION_HEADER, authValue);
				} else
					request = request.given().header(AUTHORIZATION_HEADER, "invalid");
				break;

			case NO_TOKEN:
				request = request.given().header(AUTHORIZATION_HEADER, "");
				break;

			case AUTHORIZATION_HEADER:
				// keep track of the token
				Config.putValue(AUTHORIZATION_HEADER, (String) keyword.value);
				request = request.given().header(keyword.key, keyword.value);
				break;
			default:
				request = request.given().header(keyword.key, keyword.value);
				break;
			}
		}
		return request;
	}

	/**
	 * evaluate query parameters format: "name=key=value&key2=value2"
	 * 
	 * @param serviceObject
	 * @param request
	 * @return
	 */
	public static RequestSpecification evaluateQueryParameters(ServiceObject serviceObject,
			RequestSpecification request) {
		URL aURL = Helper.convertToUrl(serviceObject.getUriPath());

		if (StringUtils.isBlank(aURL.getQuery()))
			return request;

		String[] queryParameters = aURL.getQuery().split("(&&)|(&)");

		if (queryParameters.length == 0)
			Helper.assertFalse(
					"query parameters are wrong format: " + aURL.getQuery() + ". should be \"key=value&key2=value2\"");

		for (String queryParameter : queryParameters) {
			String[] query = queryParameter.split("=");
			if (query.length == 0)
				Helper.assertFalse("query parameters are wrong format: " + aURL.getQuery()
						+ ". should be \"key=value&key2=value2\"");
			request = request.given().queryParam(query[0], query[1]);
		}
		return request;
	}

	public static RequestSpecification evaluateRequestBody(ServiceObject serviceObject, RequestSpecification request) {
		if (serviceObject.getRequestBody().isEmpty())
			return request;

		// set content type
		request = request.contentType(serviceObject.getContentType());

		// set form data
		if (serviceObject.getContentType().contains("form")) {
			request = request.config(RestAssured.config().encoderConfig(io.restassured.config.EncoderConfig
					.encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)));

			String[] formData = serviceObject.getRequestBody().split(",");
			for (String data : formData) {
				String[] keyValue = data.split(":");
				if (keyValue.length == 3) {
					switch (keyValue[1]) { // data type
					case "FILE":
						File file = DataHelper.getFile(keyValue[2]);
						request.multiPart(file);
						break;
					default:
						break;
					}
				} else
					request = request.formParam(keyValue[0].trim(), keyValue[1].trim());
			}
			return request;
		}

		// if json data type
		return request.body(serviceObject.getRequestBody());
	}

	/**
	 * sets the header, content type And body based on specifications
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static RequestSpecification evaluateOption(ServiceObject serviceObject, RequestSpecification request) {

		// reset validation timeout. will be overwritten by option value if set
		resetOptions();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return request;
		}

		// store value to config directly using format: value:<$key> separated by colon
		// ';'
		DataHelper.saveDataToConfig(serviceObject.getOption());

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case OPTION_NO_VALIDATION_TIMEOUT:
				Config.putValue(API_TIMEOUT_VALIDATION_ENABLED, false);
				break;

			case OPTION_WAIT_FOR_RESPONSE:
				Config.putValue(API_TIMEOUT_VALIDATION_ENABLED, true);
				Config.putValue(API_TIMEOUT_VALIDATION_SECONDS, keyword.value);
				break;
			default:
				break;
			}
		}

		return request;
	}

	/**
	 * reset option values to default from config
	 */
	private static void resetOptions() {
		// reset validation timeout option
		String defaultValidationTimeoutIsEnabled = TestObject.getDefaultTestInfo().config
				.get(API_TIMEOUT_VALIDATION_ENABLED).toString();

		String defaultValidationTimeoutIsSeconds = TestObject.getDefaultTestInfo().config
				.get(API_TIMEOUT_VALIDATION_SECONDS).toString();

		Config.putValue(API_TIMEOUT_VALIDATION_ENABLED, defaultValidationTimeoutIsEnabled);
		Config.putValue(API_TIMEOUT_VALIDATION_SECONDS, defaultValidationTimeoutIsSeconds);
	}

	public static Response evaluateRequest(ServiceObject serviceObject, RequestSpecification request) {
		Response response = null;

		// set request header
		request = evaluateRequestHeaders(serviceObject, request);

		request = evaluateQueryParameters(serviceObject, request);

		// set request body
		request = evaluateRequestBody(serviceObject, request);

		TestLog.logPass("request body: " + Helper.stringRemoveLines(serviceObject.getRequestBody()));
		TestLog.logPass("request type: " + serviceObject.getMethod());

		switch (serviceObject.getMethod()) {
		case "POST":
			response = request.when().post();
			break;
		case "PUT":
			response = request.when().put();
			break;
		case "PATCH":
			response = request.when().patch();
			break;
		case "DELETE":
			response = request.when().delete();
			break;
		case "GET":
			response = request.when().get();
			break;
		case "OPTIONS":
			response = request.when().options();
			break;
		case "HEAD":
			response = request.when().head();
			break;
		default:
			Helper.assertTrue("request type not found", false);
			break;
		}
		TestLog.logPass("response: " + response.getBody().asString());

		return response.then().extract().response();
	}
}
