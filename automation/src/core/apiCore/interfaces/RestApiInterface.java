package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

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
import io.restassured.authentication.AuthenticationScheme;
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
	 * @param apiObject
	 * @return
	 */
	public static Response RestfullApiInterface(ServiceObject apiObject) {

		if (apiObject == null)
			Helper.assertFalse("apiobject is null");

		// set timeout from api config
		setTimeout();

		// set proxy from api config
		setProxy();

		// replace parameters for request body
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));

		// set base uri
		setURI(apiObject);

		// send request and evaluate response
		Response response = evaluateRequestAndValidateResponse(apiObject);

		return response;
	}

	/**
	 * evaluate request and validate response retry until validation timeout period
	 * in seconds
	 * 
	 * @param apiObject
	 * @return
	 */
	public static Response evaluateRequestAndValidateResponse(ServiceObject apiObject) {
		List<String> errorMessages = new ArrayList<String>();
		Response response = null;

		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;

		boolean isValidationTimeout = Config.getBooleanValue(API_TIMEOUT_VALIDATION_ENABLED);
		int maxRetrySeconds = Config.getIntValue(API_TIMEOUT_VALIDATION_SECONDS);
		int currentRetryCount = 0;

		do {
			// send request And receive a response
			response = evaluateRequest(apiObject);

			// validate the response
			errorMessages = validateResponse(response, apiObject);

			// if validation timeout is not enabled, break out of the loop
			if (!isValidationTimeout)
				break;

			if (currentRetryCount > 0) {
				Helper.waitForSeconds(1);
				String errors = StringUtils.join(errorMessages, "\n error: ");
				TestLog.ConsoleLog("attempt failed with message: " + errors);
				TestLog.ConsoleLog("attempt #" + (currentRetryCount + 1));

			}
			currentRetryCount++;

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

		} while (!errorMessages.isEmpty() && passedTimeInSeconds < maxRetrySeconds);

		if (!errorMessages.isEmpty()) {
			String errorString = StringUtils.join(errorMessages, "\n error: ");
			TestLog.ConsoleLog(errorString);
			Helper.assertFalse(StringUtils.join(errorMessages, "\n error: "));
		}

		return response;
	}

	/**
	 * sets base uri for api call
	 */
	public static void setURI(ServiceObject apiObject) {

		// replace place holder values for uri
		apiObject.withUriPath(DataHelper.replaceParameters(apiObject.getUriPath()));
		apiObject.withUriPath(Helper.stringRemoveLines(apiObject.getUriPath()));
		// if uri is full path, Then set base uri as whats provided in csv file
		// else use baseURI from properties as base uri And extend it with csv file uri
		// path
		if (apiObject.getUriPath().startsWith("http")) {
			RestAssured.baseURI = apiObject.getUriPath();
			apiObject.withUriPath("");

		} else {
			RestAssured.baseURI = Helper.stringRemoveLines(Config.getValue("api.uriPath"));
			TestLog.logPass("request URI: " + RestAssured.baseURI + apiObject.getUriPath());
		}
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

	public static List<String> validateResponse(Response response, ServiceObject apiObject) {

		List<String> errorMessages = new ArrayList<String>();

		// fail test if no response is returned
		if (response == null) {
			errorMessages.add("no response returned");
			return errorMessages;
		}

		// saves response values to config object
		JsonHelper.saveOutboundJsonParameters(response, apiObject.getOutputParams());

		// validate status code
		if (!apiObject.getRespCodeExp().isEmpty()) {
			String message = "expected status code: " + apiObject.getRespCodeExp() + " response status code: "
					+ response.getStatusCode();
			TestLog.logPass(message);
			if (response.getStatusCode() != Integer.valueOf(apiObject.getRespCodeExp())) {
				errorMessages.add(message);
				return errorMessages;
			}

		}
		errorMessages = validateExpectedValues(response, apiObject);

		// remove all empty response strings
		errorMessages.removeAll(Collections.singleton(""));
		return errorMessages;
	}

	public static List<String> validateExpectedValues(Response response, ServiceObject apiObject) {
		List<String> errorMessages = new ArrayList<String>();

		// get response body as string
		String body = response.getBody().asString();
		TestLog.logPass("response: " + body);

		// validate response body against expected json string
		if (!apiObject.getExpectedResponse().isEmpty()) {
			apiObject.withExpectedResponse(DataHelper.replaceParameters(apiObject.getExpectedResponse()));

			// separate the expected response by &&
			String[] criteria = apiObject.getExpectedResponse().split("&&");
			for (String criterion : criteria) {
				Helper.assertTrue("expected is not valid format: " + criterion,
						JsonHelper.isValidExpectation(criterion));
				errorMessages.add(JsonHelper.validateByJsonBody(criterion, body));
				errorMessages.addAll(JsonHelper.validateByKeywords(criterion, response));
				errorMessages.add(JsonHelper.validateResponseBody(criterion, response));
			}
		}
		// remove all empty response strings
		errorMessages.removeAll(Collections.singleton(""));
		return errorMessages;
	}

	/**
	 * sets the header, content type And body based on specifications Headers are
	 * based on key value, separated by ";" Invalid token: if authorization token
	 * exists, replace last values with "invalid", else set to "invalid"
	 * 
	 * @param apiObject
	 * @return
	 */
	public static RequestSpecification evaluateRequestHeaders(ServiceObject apiObject) {
		// set request
		RequestSpecification request = given();

		// if no RequestHeaders specified
		if (apiObject.getRequestHeaders().isEmpty()) {
			return request;
		}

		// replace parameters for request body
		apiObject.withRequestHeaders(DataHelper.replaceParameters(apiObject.getRequestHeaders()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(apiObject.getRequestHeaders());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional request headers
			switch (keyword.key) {
			case Authentication.AUTHENTICATION_SCHEME:
				String value = (String) keyword.value;
				value = value.replace("$", "").replace("<", "").replace(">", "").trim();
				RestAssured.authentication = (AuthenticationScheme) Config.getObjectValue(value.replace("@", ""));
				break;

			case INVALID_TOKEN:
				String authValue = Config.getValue(AUTHORIZATION_HEADER);

				// replace authorization token with random string of equal value to the token
				if (!authValue.isEmpty()) {
					authValue = Helper.generateRandomString(authValue.length());
					request = given().header(AUTHORIZATION_HEADER, authValue);
				} else
					request = given().header(AUTHORIZATION_HEADER, "invalid");
				break;

			case NO_TOKEN:
				request = given().header(AUTHORIZATION_HEADER, "");
				break;

			case AUTHORIZATION_HEADER:
				// keep track of the token
				Config.putValue(AUTHORIZATION_HEADER, (String) keyword.value);
				request = given().header(keyword.key, keyword.value);
				break;

			default:
				request = given().header(keyword.key, keyword.value);
				break;
			}
		}
		return request;
	}

	public static RequestSpecification evaluateRequestBody(ServiceObject apiObject, RequestSpecification request) {
		if (apiObject.getRequestBody().isEmpty())
			return request;

		// set content type
		request = request.contentType(apiObject.getContentType());

		// set form data
		if (apiObject.getContentType().contains("form")) {
			request = request.config(RestAssured.config().encoderConfig(io.restassured.config.EncoderConfig
					.encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT)));

			String[] formData = apiObject.getRequestBody().split(",");
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
		return request.body(apiObject.getRequestBody());
	}

	/**
	 * sets the header, content type And body based on specifications
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static RequestSpecification evaluateOption(ServiceObject serviceObject, RequestSpecification request) {

		// reset validation timeout. will be overwritten by option value if set
		resetValidationTimeout();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return request;
		}

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
	 * reset validation timeout
	 */
	private static void resetValidationTimeout() {
		// reset validation timeout option
		String defaultValidationTimeoutIsEnabled = TestObject.getDefaultTestInfo().config
				.get(API_TIMEOUT_VALIDATION_ENABLED).toString();
		Config.putValue(API_TIMEOUT_VALIDATION_ENABLED, defaultValidationTimeoutIsEnabled);

	}

	public static Response evaluateRequest(ServiceObject serviceObject) {
		Response response = null;

		// set request header
		RequestSpecification request = evaluateRequestHeaders(serviceObject);

		// set request body
		request = evaluateRequestBody(serviceObject, request);

		// set options
		request = evaluateOption(serviceObject, request);

		TestLog.logPass("request body: " + Helper.stringRemoveLines(serviceObject.getRequestBody()));
		TestLog.logPass("request type: " + serviceObject.getMethod());

		switch (serviceObject.getMethod()) {
		case "POST":
			response = request.when().post(serviceObject.getUriPath());
			break;
		case "PUT":
			response = request.when().put(serviceObject.getUriPath());
			break;
		case "PATCH":
			response = request.when().patch(serviceObject.getUriPath());
			break;
		case "DELETE":
			response = request.when().delete(serviceObject.getUriPath());
			break;
		case "GET":
			response = request.when().get(serviceObject.getUriPath());
			break;
		case "OPTIONS":
			response = request.when().options(serviceObject.getUriPath());
			break;
		case "HEAD":
			response = request.when().head(serviceObject.getUriPath());
			break;
		default:
			Helper.assertTrue("request type not found", false);
			break;
		}
		TestLog.logPass("response: " + response.getBody().asString());

		return response.then().extract().response();
	}
}
