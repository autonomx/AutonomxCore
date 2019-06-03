package core.apiCore.interfaces;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.apiCore.helpers.DataHelper;
import core.apiCore.helpers.JsonHelper;
import core.helpers.Helper;
import core.support.configReader.Config;
import core.support.logger.TestLog;
import core.support.objects.KeyValue;
import core.support.objects.ServiceObject;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Authentication {
	
	public static final String AUTHENTICATION_SCHEME = "auth";

	
	/**
	 * interface for restful api calls
	 * 
	 * @param apiObject
	 * @return
	 */
	public static Response tokenGenerator(ServiceObject apiObject) {
		
		if(apiObject == null) Helper.assertFalse("apiobject is null");
		
		// replace parameters for request body
		apiObject.withRequestBody(DataHelper.replaceParameters(apiObject.getRequestBody()));

		// set base uri
		setURI(apiObject);

		// send request And receive a response
		Response response = evaluateRequest(apiObject);

		// validate the response
		validateResponse(response, apiObject);

		return response;
	}

	/**
	 * sets base uri for api call
	 */
	private static void setURI(ServiceObject apiObject) {

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

	private static void validateResponse(Response response, ServiceObject apiObject) {

		// store authentication scheme value 
		List<KeyValue> keyword = DataHelper.getValidationMap(apiObject.getOutputParams());
		if(keyword.get(0).key.equals(AUTHENTICATION_SCHEME)) {
			String key = (String) keyword.get(0).value;
		    key = key.replace("$", "").replace("<", "").replace(">", "").trim();
			Config.putValue(key, RestAssured.authentication, "<authentication scheme>");
			return;
		}
		
		// fail test if no response is returned
		if (response == null)
			Helper.assertTrue("no response returned", false);
		
		// saves response values to config object
		saveOutboundTokens(response, apiObject.getOutputParams());

		// validate status code
		if (!apiObject.getRespCodeExp().isEmpty()) {
			TestLog.logPass("expected status code: " + apiObject.getRespCodeExp() + " response status code: "
					+ response.getStatusCode());
			response.then().statusCode(Integer.valueOf(apiObject.getRespCodeExp()));
		}

		validateExpectedValues(response, apiObject);
	}
	
	private static void saveOutboundTokens(Response response, String outputParam) {
		if (response == null || outputParam.isEmpty())
			return;
		JsonHelper.configMapJsonKeyValues(response, outputParam);
	}

	private static void validateExpectedValues(Response response, ServiceObject apiObject) {
		// get response body as string
		String body = response.getBody().asString();
		TestLog.logPass("response: " + body);

		// validate response body against expected json string
		if (!apiObject.getExpectedResponse().isEmpty()) {
			apiObject.withExpectedResponse(DataHelper.replaceParameters(apiObject.getExpectedResponse()));

			// separate the expected response by &&
			String[] criteria = apiObject.getExpectedResponse().split("&&");
			for (String criterion : criteria) {
				Helper.assertTrue("expected is not valid format: " + criterion, JsonHelper.isValidExpectation(criterion));
				JsonHelper.validateByJsonBody(criterion, response);
				JsonHelper.validateByKeywords(criterion, response);
			}
		}
	}
	
	private static RequestSpecification evaluateRequestBody(ServiceObject apiObject) {		
		// set content type
		RequestSpecification request = null;
		
		if(apiObject.getRequestBody().isEmpty()) {
			Helper.assertFalse("no request set");
		}
		
		Map<String, String> parameterMap = getParameters(apiObject);
		
		TestLog.logPass("authentication type: " + Helper.stringRemoveLines(apiObject.getOption()));

		switch (apiObject.getOption()) {
		case "BASIC":
			String username = parameterMap.get("username");
			String password = parameterMap.get("password");
	        RestAssured.authentication =  RestAssured.basic(username, password);
			break;
		case "OAUTH2":			
			username = parameterMap.get("username");
			password = parameterMap.get("password");
			String clientId = parameterMap.get("cliendId");
			String clientSecret = parameterMap.get("clientSecret");
			String grantType = parameterMap.get("grantType");
			String scope = parameterMap.get("scope");
			String redirectUri = parameterMap.get("redirectUri");
			
			request =  given().auth().preemptive().basic(clientId, clientSecret)   
                    .formParam("grant_type", grantType)
                    .formParam("username", username)
                    .formParam("password", password)
                    .formParam("redirect_uri", redirectUri)
                    .formParam("scope", scope);
		default:
			Helper.assertFalse("Correct authentication type not set. selected: <" + apiObject.getMethod() + "> Available options: BASIC");
			break;
		}
		return request;
	}
	
	
	private static Map<String, String> getParameters(ServiceObject apiObject){
		Map<String, String> parameterMap = new HashMap<String, String>();
		
		String[] formData = apiObject.getRequestBody().split(",");
		for(String data : formData) {
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

	private static Response evaluateRequest(ServiceObject apiObject) {
		Response response = null;
		
		// set request body
		RequestSpecification request = evaluateRequestBody(apiObject);

		// set options
	    request = evaluateOption(apiObject, request);

		TestLog.logPass("request body: " + Helper.stringRemoveLines(apiObject.getRequestBody()));
		TestLog.logPass("request type: " + apiObject.getMethod());
		
		// in case of basic authentication where AuthenticationScheme is returned and token is not generated
        if(request == null) return response;
        
		switch (apiObject.getMethod()) {
		case "POST":
			response = request.when().post(apiObject.getUriPath());
			break;
		case "PUT":
			response = request.when().put(apiObject.getUriPath());
			break;
		case "PATCH":
			response = request.when().patch(apiObject.getUriPath());
			break;
		case "DELETE":
			response = request.when().delete(apiObject.getUriPath());
			break;
		case "GET":
			response = request.when().get(apiObject.getUriPath());
			break;
		case "OPTIONS":
			response = request.when().options(apiObject.getUriPath());
			break;
		case "HEAD":
			response = request.when().head(apiObject.getUriPath());
			break;
		default:
			Helper.assertTrue("request type not found", false);
			break;
		}
		TestLog.logPass("response: " + response.getBody().asString());
		return response.then().extract().response();
	}
}
