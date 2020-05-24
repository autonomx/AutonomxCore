package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import core.apiCore.ServiceManager;
import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.DataHelper.JSON_COMMAND;
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

	public static final String API_TIMEOUT_PAGINATION_VALIDATION_ENABLED = "api.timeout.pagination.validation.isEnabled";

	private static final String INVALID_TOKEN = "INVALID_TOKEN";
	private static final String NO_TOKEN = "NO_TOKEN";

	private static final String OPTION_PAGINATION_STOP_CRITERIA = "PAGINATION_STOP_CRITERIA";
	private static final String OPTION_PAGINATION_MAX_PAGES = "PAGINATION_MAX_PAGES";
	private static final String OPTION_PAGINATION_FROM = "PAGINATION_FROM";
	private static final String OPTION_PAGINATION_INCREMENET = "PAGINATION_INCREMENT";

	public static final String API_PAGINATION_STOP_CRITERIA = "api.pagination.stop.criteria";
	public static final String API_PAGINATION_MAX_PAGES = "api.pagination.max.pages";
	public static final String API_PAGINATION_PAGES_FROM = "api.pagination.pages.from";
	public static final String API_PAGINATION_INCREMENT = "api.pagination.incremenet";

	public static final String API_PAGINATION_COUNTER = "PAGINATION";

	public static final String API_PARAMETER_ENCODING = "api.encoding.parameter";
	public static final String API_URL_ENCODING = "api.encoding.url";

	public static final String API_BASE_URL = "api.uriPath";

	public static boolean API_AUTO_PROXY_SET = false;

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

		// send request and evaluate response
		Response response = evaluate(serviceObject);

		return response;
	}

	public static Response evaluate(ServiceObject serviceObject) {
		Response response = null;

		// if pagination
		if (serviceObject.getUriPath().contains(API_PAGINATION_COUNTER))
			response = evaluatePagination(serviceObject);
		else
			response = evaluateRequestAndValidateResponse(serviceObject);
		return response;
	}

	/**
	 * evaluate request and validate response retry until validation timeout period
	 * in seconds RetryAfterSecond is based on waiting after the validation round is
	 * complete, including wait for response wait period
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static Response evaluateRequestAndValidateResponse(ServiceObject serviceObject) {

		// set options
		evaluateOption(serviceObject);
		
		int getRetryCount = Config.getIntValue(ServiceManager.SERVICE_RETRY_COUNT);
		int getRetryAfterSecond = Config.getIntValue(ServiceManager.SERVICE_RETRY_AFTER_SERCONDS);
		
		// retry test if value set
		for (int i = 1; i <= getRetryCount + 1; i++) {
			// evaluate request and receive response
			evaluateRequestAndReceiveResponse(serviceObject);

			// do not evaluate errors if no expectations set
			if (serviceObject.getExpectedResponse().isEmpty() && serviceObject.getRespCodeExp().isEmpty())
				return serviceObject.getResponse();

			// if no errors, then break
			if (serviceObject.getErrorMessages().isEmpty())
				break;

			// wait after the test is complete before next retry. default is 0
			if (i <= getRetryCount) {
				TestLog.logPass("Run: " + i + " Failed, attempting another retry... " + (getRetryCount + 1 - i)
						+ " retry(s) remaining");
				if (getRetryAfterSecond > 0)
					Helper.waitForSeconds(getRetryAfterSecond);
			}
		}

		if (!serviceObject.getErrorMessages().isEmpty()) {
			String errorString = StringUtils.join(serviceObject.getErrorMessages(), "\n error: ");
			TestLog.ConsoleLog(ServiceObject.normalize(errorString));
			Helper.assertFalse(StringUtils.join(serviceObject.getErrorMessages(), "\n error: "));
		}

		return serviceObject.getResponse();
	}

	/**
	 * evaluate pagination format: http://url?page=<@PAGINATION_FROM_1> counter will
	 * start from page 1 will iterate through the pages until either: - the expected
	 * response criteria is met - max pages are reached. specified by
	 * PAGINATION_MAX_PAGES:100 in options - response criteria for max pages is
	 * reached. OPTION_PAGINATION_STOP_CRITERIA:.results.id - if the list of
	 * responses on a selected page is 0, that means the page has no results, hence,
	 * it is the last page
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static Response evaluatePagination(ServiceObject serviceObject) {

		// set options
		evaluateOption(serviceObject);
		boolean isValidationTimeout = Config.getBooleanValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED);

		// set pagination response validation
		Config.putValue(API_TIMEOUT_PAGINATION_VALIDATION_ENABLED, isValidationTimeout, false);

		boolean isCriteriaSuccess = false;

		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;
		int maxRetrySeconds = -1;

		int currentRunCount = 1;
		// if validation timeout set, will retry for duration of validation timeout
		// validation timeout per page is disabled
		do {
			currentRunCount++;

			if (currentRunCount > 1)
				TestLog.ConsoleLog("attempt #" + (currentRunCount));

			// evaluate request and validate pagination
			isCriteriaSuccess = evaluateRequestAndValidatePagination(serviceObject);

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

			// if validation timeout is not enabled, break out of the loop
			maxRetrySeconds = Config.getIntValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS);
			if (!isValidationTimeout)
				break;

		} while (!isCriteriaSuccess && passedTimeInSeconds < maxRetrySeconds);

		// reset pagination timeout
		Config.putValue(API_TIMEOUT_PAGINATION_VALIDATION_ENABLED, false);

		Helper.assertTrue("expected validation not found in pages.", isCriteriaSuccess);

		return serviceObject.getResponse();
	}

	public static boolean evaluateRequestAndValidatePagination(ServiceObject serviceObject) {

		String criteria = Config.getValue(API_PAGINATION_STOP_CRITERIA);
		int maxPages = Config.getIntValue(API_PAGINATION_MAX_PAGES);
		int startingPage = Helper.getIntFromString(Config.getValue(API_PAGINATION_PAGES_FROM));
		int incrementBy = Helper.getIntFromString(Config.getValue(API_PAGINATION_INCREMENT));

		String uri = serviceObject.getUriPath();
		boolean isCriteriaSuccess = false;

		for (int index = startingPage; index <= maxPages; index += incrementBy) {

			TestLog.logPass("Validating page: " + index);

			// update uri to include the incrementally increasing page numbers
			// resets page number to <@PAGINATION> keyword which will get overwritten
			serviceObject = serviceObject.withUriPath(uri);
			Config.putValue(API_PAGINATION_COUNTER, index, false);

			// evaluate the request and receive response. errors are stored at
			// serviceObject.errorMessages
			serviceObject = evaluateRequestAndReceiveResponse(serviceObject);

			// error indicates that there are no more results on the page
			List<String> criteriaErrors = validatePaginationStopCriteria(serviceObject, criteria);
			if (!criteriaErrors.isEmpty()) {
				TestLog.logPass("no more results returned at page: " + index + " with criteria: " + criteria);
				break;
			}

			// if errors (requirements not met), reset errors for next page
			if (!serviceObject.getErrorMessages().isEmpty()) {
				TestLog.logPass(Arrays.toString(serviceObject.getErrorMessages().toArray()));
			} else if (serviceObject.getErrorMessages().isEmpty()) {
				isCriteriaSuccess = true;
				break;
			}
		}

		return isCriteriaSuccess;
	}

	/**
	 * stoppage criteria for api pagination will iterate through pages in api call,
	 * untill the criteria node size reaches 0 - this indicates that there are no
	 * more results on the page
	 * 
	 * @param serviceObject
	 * @param criteria
	 * @return
	 */
	private static List<String> validatePaginationStopCriteria(ServiceObject serviceObject, String criteria) {
		// add pagination page item count criteria to expected response
		if (!criteria.isEmpty()) {
			criteria = DataHelper.VERIFY_JSON_PART_INDICATOR + criteria + ":" + JSON_COMMAND.nodeSizeGreaterThan.name()
					+ "(0)";
		}
		ServiceObject criteriaService = new ServiceObject().withResponse(serviceObject.getResponse())
				.withExpectedResponse(criteria);

		// validate the response
		List<String> criteriaErrors = validateResponse(criteriaService);
		return criteriaErrors;
	}

	/**
	 * evaluates the request and stores the response in service object
	 * 
	 * @param serviceObject
	 * @return
	 */
	public static ServiceObject evaluateRequestAndReceiveResponse(ServiceObject serviceObject) {
		StopWatchHelper watch = StopWatchHelper.start();
		long passedTimeInSeconds = 0;

		int maxRetrySeconds = -1;
		int currentRetryCount = 0;

		do {
			currentRetryCount++;
			
			// reset error list
			List<String> errors = new ArrayList<String>();
			serviceObject.withErrorMessages(errors);

			if (currentRetryCount > 1) {
				TestLog.ConsoleLog("attempt #" + (currentRetryCount));
			
				// set options. options initially set before evaluateRequestAndReceiveResponse
				evaluateOption(serviceObject);
			}

			// set base uri
			RequestSpecification request = setURI(serviceObject);

			// replace parameters for request body, including template file (json, xml, or
			// other)
			serviceObject.withRequestBody(DataHelper.getRequestBodyIncludingTemplate(serviceObject));

			// send request And receive a response. adds errors if exception
			serviceObject = evaluateRequest(serviceObject, request);

			// validate the response
			serviceObject.withErrorMessages(validateResponse(serviceObject));

			passedTimeInSeconds = watch.time(TimeUnit.SECONDS);

			// if validation timeout is not enabled, break out of the loop
			boolean isValidationTimeout = Config.getBooleanValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED);
			maxRetrySeconds = Config.getIntValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS);
			if (!isValidationTimeout)
				break;

			// log errors if exist
			logTestRunError(currentRetryCount, serviceObject.getErrorMessages());

		} while (!serviceObject.getErrorMessages().isEmpty() && passedTimeInSeconds < maxRetrySeconds);

		if (!serviceObject.getErrorMessages().isEmpty()) {
			TestLog.ConsoleLog("Validation failed after: " + passedTimeInSeconds + " seconds");
		}

		return serviceObject;
	}

	private static void logTestRunError(int currentRetryCount, List<String> errorMessages) {
		int waitTime = Config.getIntValue(ServiceManager.SERVICE_RESPONSE_DELAY_BETWEEN_ATTEMPTS_SECONDS);

		String errors = StringUtils.join(errorMessages, "\n error: ");
		if(!errors.isEmpty())
			TestLog.ConsoleLog("attempt failed with message: " + ServiceObject.normalize(errors));
		
		if (currentRetryCount > 1)
			Helper.waitForSeconds(waitTime);
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
			url = Helper.stringRemoveLines(Config.getValue(API_BASE_URL)) + serviceObject.getUriPath();
		}
		// keep track of full URL
		serviceObject.withUriPath(url);

		URL aURL = Helper.convertToUrl(url);
		TestLog.logPass("request URL: " + aURL.toString());

		request.baseUri(aURL.getProtocol() + "://" + aURL.getHost());
		request.port(aURL.getPort());
		request.basePath(aURL.getPath());

		// set url encoding
		boolean urlEncoding = Config.getBooleanValue(API_URL_ENCODING);
		request = request.urlEncodingEnabled(urlEncoding);

		return request;
	}

	/**
	 * set connection timeout in milliseconds
	 */
	public static void setTimeout() {
		int connectTimeout = Config.getIntValue(ServiceManager.SERVICE_RESPONSE_TIMEOUT_SECONDS);
		if (connectTimeout == -1)
			connectTimeout = 60; // connect timeout defaults to 60 seconds

		RestAssured.config = RestAssuredConfig.config().httpClient(
				HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", connectTimeout * 1000)
						.setParam("http.socket.timeout", connectTimeout * 1000)
						.setParam("http.connection-manager.timeout", connectTimeout * 1000));

	}

	/**
	 * set proxy from config file value to use proxy is set at API_AUTO_PROXY_SET We
	 * evaluate if we need to use proxy once in test run
	 * 
	 * @throws Exception
	 */
	public static void setProxy() {

		String host = Config.getValue(TestObject.PROXY_HOST);
		int port = Config.getIntValue(TestObject.PROXY_PORT);
		String proxyProtocal = Config.getValue(TestObject.PROXY_PROTOCOL);
		boolean isProxyAutoDetect = Config.getBooleanValue(TestObject.PROXY_AUTO_DETECT);
		boolean isProxyEnabled = false;

		// set proxy enabled value based on proxy auto detection. if auto detect
		// enabled,
		// attempt to connect to url with proxy info. if able to connect, enable proxy
		if (isProxyAutoDetect && !API_AUTO_PROXY_SET) {
			isProxyEnabled = Helper.setProxyAutoDetection(getBaseUrl());
			API_AUTO_PROXY_SET = true;
		} else if (!isProxyAutoDetect)
			isProxyEnabled = Config.getBooleanValue(TestObject.PROXY_ENABLED);

		if (!isProxyEnabled)
			return;

		if (host.isEmpty() || port == -1)
			return;

		if (proxyProtocal.equals("http") || proxyProtocal.equals("https"))
			RestAssured.proxy(host, port, proxyProtocal);
		else
			RestAssured.proxy(host, port);
	}

	public static List<String> validateResponse(ServiceObject serviceObject) {

		List<String> errorMessages = serviceObject.getErrorMessages();

		// fail test if no response is returned
		if (serviceObject.getResponse() == null) {
			errorMessages.add("no response returned");
			return errorMessages;
		}

		// saves response values to config object
		JsonHelper.saveOutboundJsonParameters(serviceObject.getResponse(), serviceObject.getOutputParams());

		// validate status code
		errorMessages.addAll(validateStatusCode(serviceObject.getResponse(), serviceObject));

		// get response values and validate
		String responseString = JsonHelper.getResponseValue(serviceObject.getResponse());
		List<String> responses = new ArrayList<String>();
		responses.add(responseString);
		errorMessages.addAll(DataHelper.validateExpectedValues(responses, serviceObject.getExpectedResponse()));

		// remove all empty response strings
		errorMessages = DataHelper.removeEmptyElements(errorMessages);
		return errorMessages;
	}

	/**
	 * validate status code
	 * 
	 * @param response
	 * @param serviceObject
	 * @return
	 */
	public static List<String> validateStatusCode(Response response, ServiceObject serviceObject) {
		List<String> errorMessages = new ArrayList<String>();

		// validate status code
		if (!serviceObject.getRespCodeExp().isEmpty()) {
			String message = "expected status code: " + serviceObject.getRespCodeExp() + " response status code: "
					+ response.getStatusCode();
			TestLog.logPass(message);
			if (response.getStatusCode() != Integer.valueOf(serviceObject.getRespCodeExp())) {
				errorMessages.add(message);
			}
		}
		return errorMessages;
	}

	/**
	 * sets the header, content type And body based on specifications Headers are
	 * based on key value, separated by ";" Invalid token: if authorization token
	 * exists, replace last values with "invalid", else set to "invalid"
	 * 
	 * we replace parameters per authentication type
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

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getRequestHeaders());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional request headers
			switch (keyword.key) {
			case Authentication.BASIC_AUTHORIZATION:
				List<String> keys = Helper.getValuesFromPattern(keyword.value.toString(), "<@(.+?)>");
				if (keys.size() == 0)
					Helper.assertFalse("value not set with format identifier:<@variable>");
				String key = keys.get(0);

				ArrayList<String> basicRequest = (ArrayList<String>) Config.getObjectValue(key);
				if (basicRequest == null || basicRequest.size() != 2)
					Helper.assertFalse("basicRequest request info not correct. Should include username, password: "
							+ Arrays.toString(basicRequest.toArray()));
				request = request.auth().basic(basicRequest.get(0), basicRequest.get(1));
				keyword.value = Arrays.toString(basicRequest.toArray());
				break;
				
			case Authentication.NTLM_AUTHORIZATION:
				keys = Helper.getValuesFromPattern(keyword.value.toString(), "<@(.+?)>");
				if (keys.size() == 0)
					Helper.assertFalse("value not set with format identifier:<@variable>");
				key = keys.get(0);

				ArrayList<String> ntlmRequest = (ArrayList<String>) Config.getObjectValue(key);
				if (ntlmRequest == null || ntlmRequest.size() != 4)
					Helper.assertFalse(
							"ntlmRequest request info not correct. Should include username, password, workstation, domain: "
									+ Arrays.toString(ntlmRequest.toArray()));
				request = request.auth().ntlm(ntlmRequest.get(0), ntlmRequest.get(1), ntlmRequest.get(2),
						ntlmRequest.get(3));
				keyword.value = Arrays.toString(ntlmRequest.toArray());
				break;
				
			case INVALID_TOKEN:
				String authValue = Config.getValue(Authentication.AUTHORIZATION_HEADER);

				// replace authorization token with random string of equal value to the token
				if (!authValue.isEmpty()) {
					authValue = Helper.generateRandomString(authValue.length());
					request = request.given().header(Authentication.AUTHORIZATION_HEADER, authValue);
				} else
					request = request.given().header(Authentication.AUTHORIZATION_HEADER, "invalid");
				break;

			case NO_TOKEN:
				request = request.given().header(Authentication.AUTHORIZATION_HEADER, "");
				break;

			case Authentication.AUTHORIZATION_HEADER:
				keyword.value = DataHelper.replaceParameters(keyword.value.toString());
				// keep track of the token
				Config.putValue(Authentication.AUTHORIZATION_HEADER, keyword.value, false);
				request = request.given().header(keyword.key, keyword.value);
				break;
				
			default:
				keyword.value = DataHelper.replaceParameters(keyword.value.toString());
				request = request.given().header(keyword.key, keyword.value);
				break;
			}
		}
		KeyValue.printKeyValue(keywords, "header");
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
			if (query.length == 1)
				request = request.given().queryParam(query[0], "");
			else
				request = request.given().queryParam(query[0], query[1]);
		}

		// set parameter encoding
		boolean paramterEncoding = Config.getBooleanValue(API_PARAMETER_ENCODING);
		request = request.urlEncodingEnabled(paramterEncoding);

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
				String[] keyValue = data.split(":", 2);
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
	public static void evaluateOption(ServiceObject serviceObject) {

		// reset validation timeout. will be overwritten by option value if set
		resetValidationTimeout();

		// if no option specified
		if (serviceObject.getOption().isEmpty()) {
			return;
		}

		// replace parameters for request body
		serviceObject.withOption(DataHelper.replaceParameters(serviceObject.getOption()));

		// get key value mapping of header parameters
		List<KeyValue> keywords = DataHelper.getValidationMap(serviceObject.getOption());

		// iterate through key value pairs for headers, separated by ";"
		for (KeyValue keyword : keywords) {

			// if additional options
			switch (keyword.key) {
			case ServiceManager.OPTION_NO_VALIDATION_TIMEOUT:
				Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, false, false);
				break;
			case ServiceManager.OPTION_WAIT_FOR_RESPONSE:
				// disable per page wait for response if pagination validation is enabled
				if (Config.getBooleanValue(API_TIMEOUT_PAGINATION_VALIDATION_ENABLED))
					Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, false, false);
				else
					Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, true, false);
				Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS, keyword.value, false);
				break;
			case ServiceManager.OPTION_WAIT_FOR_RESPONSE_DELAY:
				Config.putValue(ServiceManager.SERVICE_RESPONSE_DELAY_BETWEEN_ATTEMPTS_SECONDS, keyword.value, false);
				break;
			case ServiceManager.OPTION_RETRY_COUNT:
				Config.putValue(ServiceManager.SERVICE_RETRY_COUNT, keyword.value, false);
				break;
			case ServiceManager.OPTION_RETRY_AFTER_SECONDS:
				Config.putValue(ServiceManager.SERVICE_RETRY_AFTER_SERCONDS, keyword.value, false);
				break;
			case OPTION_PAGINATION_STOP_CRITERIA:
				Config.putValue(API_PAGINATION_STOP_CRITERIA, keyword.value, false);
				break;
			case OPTION_PAGINATION_MAX_PAGES:
				Config.putValue(API_PAGINATION_MAX_PAGES, keyword.value, false);
				break;
			case OPTION_PAGINATION_FROM:
				Config.putValue(API_PAGINATION_PAGES_FROM, keyword.value, false);
				break;
			case OPTION_PAGINATION_INCREMENET:
				Config.putValue(API_PAGINATION_INCREMENT, keyword.value, false);
				break;
			default:
				break;
			}
		}
		KeyValue.printKeyValue(keywords, "option");
	}

	/**
	 * reset validation timeout
	 */
	private static void resetValidationTimeout() {
		// reset validation timeout option
		String defaultValidationTimeoutIsEnabled = Config
				.getGlobalValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED);

		String defaultValidationTimeoutIsSeconds = Config
				.getGlobalValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS);

		int defaultValidationTimeoutDelay = Config
				.getGlobalIntValue(ServiceManager.SERVICE_RESPONSE_DELAY_BETWEEN_ATTEMPTS_SECONDS);
		if (defaultValidationTimeoutDelay == -1)
			defaultValidationTimeoutDelay = 3;

		Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_ENABLED, defaultValidationTimeoutIsEnabled, false);
		Config.putValue(ServiceManager.SERVICE_TIMEOUT_VALIDATION_SECONDS, defaultValidationTimeoutIsSeconds, false);
		Config.putValue(ServiceManager.SERVICE_RESPONSE_DELAY_BETWEEN_ATTEMPTS_SECONDS, defaultValidationTimeoutDelay, false);

		// reset retry count
		int defaultRetryCount = Config.getGlobalIntValue(ServiceManager.SERVICE_RETRY_COUNT);
		if (defaultRetryCount == -1)
			defaultRetryCount = 0;

		int defaultRetryAfterSeconds = Config.getGlobalIntValue(ServiceManager.SERVICE_RETRY_AFTER_SERCONDS);
		if (defaultRetryAfterSeconds == -1)
			defaultRetryAfterSeconds = 1;

		Config.putValue(ServiceManager.SERVICE_RETRY_COUNT, defaultRetryCount, false);
		Config.putValue(ServiceManager.SERVICE_RETRY_AFTER_SERCONDS, defaultRetryAfterSeconds, false);

		Config.putValue(API_PAGINATION_STOP_CRITERIA, "", false);
		Config.putValue(API_PAGINATION_MAX_PAGES, 100, false);
		Config.putValue(API_PAGINATION_PAGES_FROM, 1, false);
		Config.putValue(API_PAGINATION_INCREMENT, 1, false);
	}

	public static ServiceObject evaluateRequest(ServiceObject serviceObject, RequestSpecification request) {
		Response response = null;

		List<String> errors = new ArrayList<String>();

		// set request header
		request = evaluateRequestHeaders(serviceObject, request);

		request = evaluateQueryParameters(serviceObject, request);

		// set request body
		request = evaluateRequestBody(serviceObject, request);

		serviceObject.withRequest(request);
		if(!serviceObject.getRequestBody().isEmpty())
			TestLog.logPass("request body: " + Helper.stringRemoveLines(serviceObject.getRequestBody()));
		TestLog.logPass("request type: " + serviceObject.getMethod());

		try {
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
		} catch (Exception e) {
			errors.add(e.getMessage());
			serviceObject.withErrorMessages(errors);
		}

		if (response != null) {
			TestLog.logPass("response: " + ServiceObject.normalize(response.getBody().asString()));
			serviceObject.withResponse(response.then().extract().response());
		} else
			serviceObject.withResponse(response);

		return serviceObject;
	}

	/**
	 * get base url from the config
	 * 
	 * @return
	 */
	public static URL getBaseUrl() {
		URL baseUrl = null;
		String baseUrlValue = Config.getValue(API_BASE_URL);
		try {
			baseUrl = new URL(baseUrlValue);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return baseUrl;
	}

}
